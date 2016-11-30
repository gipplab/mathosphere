package com.formulasearchengine.mathosphere.mlp;

import com.formulasearchengine.mathosphere.mlp.contracts.*;
import com.formulasearchengine.mathosphere.utils.Util;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formulasearchengine.mathosphere.mlp.cli.EvalCommandConfig;
import com.formulasearchengine.mathosphere.mlp.cli.FlinkMlpCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.ParsedWikiDocument;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextUtils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.core.fs.FileSystem.WriteMode;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlinkMlpRelationFinder {

  public static void main(String[] args) throws Exception {
    FlinkMlpCommandConfig config = FlinkMlpCommandConfig.from(args);
    run(config);
  }

  public static void run(FlinkMlpCommandConfig config) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    DataSource<String> source = readWikiDump(config, env);
    DataSet<ParsedWikiDocument> documents =
      source.flatMap(new TextExtractorMapper())
        .map(new TextAnnotatorMapper(config));

    DataSet<WikiDocumentOutput> result = documents.map(new CreateCandidatesMapper(config));

    result.map(new JsonSerializerMapper<>())
      .writeAsText(config.getOutputDir(), WriteMode.OVERWRITE);
    //int cores = Runtime.getRuntime().availableProcessors();
    //env.setParallelism(1); // rounds down
    final int parallelism = config.getParallelism();
    if (parallelism > 0) {
      env.setParallelism(parallelism);
    }
    env.execute("Relation Finder");
  }

  public String runFromText(FlinkMlpCommandConfig config, String input) throws Exception {
    final JsonSerializerMapper<Object> serializerMapper = new JsonSerializerMapper<>();
    return serializerMapper.map(outDocFromText(config, input));
  }

  public WikiDocumentOutput outDocFromText(FlinkMlpCommandConfig config, String input) throws Exception {
    final TextAnnotatorMapper textAnnotatorMapper = new TextAnnotatorMapper(config);
    textAnnotatorMapper.open(null);
    final CreateCandidatesMapper candidatesMapper = new CreateCandidatesMapper(config);

    final ParsedWikiDocument parsedWikiDocument = textAnnotatorMapper.parse(input);
    return candidatesMapper.map(parsedWikiDocument);
  }

  public static DataSource<String> readWikiDump(FlinkMlpCommandConfig config, ExecutionEnvironment env) {
    Path filePath = new Path(config.getDataset());
    TextInputFormat inp = new TextInputFormat(filePath);
    inp.setCharsetName("UTF-8");
    inp.setDelimiter("</page>");
    return env.readFile(inp, config.getDataset());
  }


  public static void evaluate(EvalCommandConfig config) throws Exception {
    ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
    DataSource<String> source = readWikiDump(config, env);
    final MapFunction<ParsedWikiDocument, WikiDocumentOutput> candidatesMapper;
    if (config.isPatternMatcher()) {
      candidatesMapper = new PatternMatcherMapper();
    } else {
      candidatesMapper = new CreateCandidatesMapper(config);
    }
    DataSet<ParsedWikiDocument> documents =
      source.flatMap(new TextExtractorMapper())
        .map(new TextAnnotatorMapper(config));
    final File file = new File(config.getQueries());
    ObjectMapper mapper = new ObjectMapper();
    List userData = mapper.readValue(file, List.class);
    Map<String, Object> gold = new HashMap<>();
    for (Object o : userData) {
      final Map entry = (Map) o;
      Map formula = (Map) entry.get("formula");
      gold.put((String) formula.get("title"), o);
    }
    final File ndFile = new File(config.getNdFile());
    List ndList = mapper.readValue(ndFile, List.class);
    Map<String, Object> ndData = new HashMap<>();
    for (Object o : ndList) {
      final Map entry = (Map) o;
      ndData.put(((String) entry.get("document_title")).replaceAll(" ", "_"), o);
    }
    GroupReduceFunction<ParsedWikiDocument, String> reduceFunction = new GroupReduceFunction<ParsedWikiDocument, String>() {
      @Override
      public void reduce(Iterable<ParsedWikiDocument> iterable, Collector<String> collector) throws Exception {
        Multiset<String> tpOverall = HashMultiset.create();
        Multiset<String> fnOverall = HashMultiset.create();
        Multiset<String> fpOverall = HashMultiset.create();
        Multiset<Relation> tpRelOverall = HashMultiset.create();
        Integer fnRelOverallCnt = 0;
        Multiset<Relation> fpRelOverall = HashMultiset.create();
        for (ParsedWikiDocument parsedWikiDocument : iterable) {
          String title = parsedWikiDocument.getTitle().replaceAll(" ", "_");
          try {
            Map goldElement = (Map) gold.get(title);
            Map formula = (Map) goldElement.get("formula");
            final Integer fid = Integer.parseInt((String) formula.get("fid"));
            final String tex = (String) formula.get("math_inputtex");
            final String qId = (String) formula.get("qID");
            int pos = WikiTextUtils.getFormulaPos(parsedWikiDocument, fid);
            final MathTag seed = parsedWikiDocument.getFormulas().get(pos);
            if (!seed.getContent().equals(tex)) {
              System.err.println("PROBLEM WITH" + title);
              System.err.println(seed.getContent());
              System.err.println(tex);
              throw new Exception("Invalid numbering.");
            }
            final WikiDocumentOutput wikiDocumentOutput = candidatesMapper.map(parsedWikiDocument);
            List<Relation> relations = wikiDocumentOutput.getRelations();
            Set<String> relIdents = new HashSet<>();
            for (Relation relation : relations) {
              relIdents.add(relation.getIdentifier());
            }
            final Set<String> real = seed.getIdentifiers(config).elementSet();
            //only keep identifiers that have a definition
            //real.retainAll(relIdents);
            final Map definitions = (Map) goldElement.get("definitions");
            final Set expected = definitions.keySet();
            Set<String> tp = new HashSet<>(expected);
            Set<String> fn = new HashSet<>(expected);
            Set<String> fp = new HashSet<>(real);
            tp.retainAll(real);
            fn.removeAll(real);
            fp.removeAll(expected);
            tpOverall.addAll(tp);
            fnOverall.addAll(fn);
            fpOverall.addAll(fp);
            System.err.println("https://en.formulasearchengine.com/wiki/" + title + "#math." + formula.get("oldId") + "." + fid);
            if (config.getNamespace()) {
              getNamespaceData(title, relations);
            }
            relations.removeIf(r -> !expected.contains(r.getIdentifier()));
            Collections.sort(relations, Relation::compareToName);
            removeDuplicates(definitions, relations);
            writeRelevanceTemplates(qId, relations);
            Util.writeExtractedDefinitionsAsCsv(config.getOutputDir() + "/extraction.csv", qId, wikiDocumentOutput.getTitle().replaceAll("\\s", "_"), relations);
            Map<Tuple2<String, String>, Integer> references = new HashMap<>();
            if (config.getRelevanceFolder() != null) {
              final FileReader relevance = new FileReader(config.getRelevanceFolder() + "/q" + qId + ".csv");
              Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(relevance);
              for (CSVRecord record : records) {
                String identifier = record.get(0);
                if (identifier.length() > 0) {
                  String definition = record.get(1);
                  Integer relevanceRanking = Integer.valueOf(record.get(2));
                  references.put(new Tuple2<>(identifier, definition), relevanceRanking);
                }
              }
              int tpcnt = 0;
              for (Relation relation : relations) {
                Integer score = references.get(relation.getTuple());
                if (score != null && score >= config.getLevel()) {
                  tpRelOverall.add(relation);
                  System.err.println("tp: " + relation.getIdentifier() + ", " + relation.getDefinition());
                  tpcnt++;
                } else {
                  fpRelOverall.add(relation);
                  System.err.println("fp: " + relation.getIdentifier() + ", " + relation.getDefinition());
                }
              }
              fnRelOverallCnt += (expected.size() - tpcnt);
            }
          } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Problem with " + title);
          }
        }
        System.err.println("Overall identifier evaluation");
        System.err.println("fp:" + fpOverall.size());
        System.err.println("fn:" + fnOverall.size());
        System.err.println("tp:" + tpOverall.size());

        System.err.println("Overall definition evaluation - by this method, better use evaluation in Evaluation package.");
        System.err.println("fp=" + fpRelOverall.size() + "; fn=" + fnRelOverallCnt
          + "; tp=" + tpRelOverall.size());
        System.err.println(fpRelOverall.toString());
      }

      public void removeDuplicates(Map definitions, List<Relation> relations) {
        String lastDef = "";
        String lastIdent = "";
        final Iterator<Relation> iterator = relations.iterator();
        while (iterator.hasNext()) {
          final Relation relation = iterator.next();
          final List<String> refList = getDefiniens(definitions, relation);
          final String definition = relation.getDefinition().replaceAll("(\\[\\[|\\]\\])", "").trim().toLowerCase();
          if (refList.contains(definition)) {
            relation.setRelevance(2);
          }
          if (lastIdent.compareTo(relation.getIdentifier())
            + relation.getDefinition().compareToIgnoreCase(lastDef) == 0) {
            iterator.remove();
          }
          lastDef = relation.getDefinition();
          lastIdent = relation.getIdentifier();
        }
      }

      public void writeRelevanceTemplates(String qId, List<Relation> relations) throws IOException {
        if (config.getOutputDir() != null) {
          final File output = new File(config.getOutputDir() + "/q" + qId + ".csv");
          output.createNewFile();
          OutputStreamWriter w = new FileWriter(output);
          CSVPrinter printer = CSVFormat.DEFAULT.withRecordSeparator("\n").print(w);
          for (Relation relation : relations) {
            String sScore;
            if (relation.getRelevance() == null) {
              sScore = "";
            } else {
              sScore = String.valueOf(relation.getRelevance());
            }
            String[] out = new String[]{relation.getIdentifier(), relation.getDefinition(), sScore};
            printer.printRecord(out);
          }
          w.flush();
          w.close();
        }
      }

      public void getNamespaceData(String title, List<Relation> relations) {
        final Map nd = (Map) ndData.get(title);
        if (nd != null) {
          List relNS = (List) nd.get("namespace_relations");
          for (Object o : relNS) {
            Relation rel = new Relation(o);
            relations.add(rel);
          }
        }
      }
    };
    env.setParallelism(1);
    documents.reduceGroup(reduceFunction).print();
  }

  public static List<String> getDefiniens(Map definitions, Relation relation) {
    List<String> result = new ArrayList<>();
    List definiens = (List) definitions.get(relation.getIdentifier());
    for (Object definien : definiens) {
      if (definien instanceof Map) {
        Map<String, String> var = (Map) definien;
        for (Map.Entry<String, String> stringStringEntry : var.entrySet()) {
          // there is only one entry
          final String def = stringStringEntry.getValue().trim().replaceAll("\\s*\\(.*?\\)$", "").toLowerCase();
          result.add(def);
        }
      } else {
        result.add(((String) definien).toLowerCase());
      }
    }
    return result;
  }
}
