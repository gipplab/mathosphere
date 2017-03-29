package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.cli.MachineLearningDefinienExtractionConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.EvaluationResult;
import com.formulasearchengine.mathosphere.mlp.pojos.Relation;
import com.formulasearchengine.mathosphere.mlp.pojos.WikiDocumentOutput;
import com.formulasearchengine.mathosphere.utils.GoldUtil;
import com.formulasearchengine.mlp.evaluation.Evaluator;
import com.formulasearchengine.mlp.evaluation.pojo.GoldEntry;
import com.formulasearchengine.mlp.evaluation.pojo.ScoreSummary;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.util.Collector;

import java.io.File;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * All found candidates are equally good. Used to find a baseline how many definiens can be extracted at all.
 * Created by Leo on 30.01.2017.
 */
public class StupidRelationScorer implements GroupReduceFunction<WikiDocumentOutput, EvaluationResult> {

  private MachineLearningDefinienExtractionConfig config;

  public StupidRelationScorer(MachineLearningDefinienExtractionConfig config) {
    this.config = config;
  }

  @Override
  public void reduce(Iterable<WikiDocumentOutput> values, Collector<EvaluationResult> out) throws Exception {
    List<GoldEntry> gold = (new Evaluator()).readGoldEntries(new File(config.getGoldFile()));
    Collection<String> extractions = new ArrayList<>();
    //get all candidates
    for (WikiDocumentOutput value : values) {
      for (Relation relation : value.getRelations()) {
        //retain only correctly extracted identifiers
        if (GoldUtil.getGoldEntryByTitle(gold, value.getTitle()).getDefinitions()
          .stream().map(i -> i.getIdentifier()).collect(Collectors.toList())
          .contains(relation.getIdentifier())) {
          String extraction =
            value.getqId() + ","
              + "\"" + value.getTitle().replaceAll("\\s", "_") + "\","
              + "\"" + relation.getIdentifier() + "\","
              + "\"" + relation.getDefinition().toLowerCase() + "\"";
          extractions.add(extraction);
        }
      }
    }
    //remove duplicates from extraction
    StringBuilder e = new StringBuilder();
    Set<String> set = new HashSet();
    set.addAll(extractions);
    List<String> list = new ArrayList<>(set);
    list.sort(Comparator.naturalOrder());
    for (String extraction : list) {
      e.append(extraction).append("\n");
    }
    Evaluator evaluator = new Evaluator();
    StringReader reader = new StringReader(e.toString());
    ScoreSummary s = evaluator.evaluate(evaluator.readExtractions(reader, gold, true), gold, true);
    System.out.println(s.toString());
  }
}
