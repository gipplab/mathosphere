package mlp.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import mlp.cli.CountCommandConfig;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import static mlp.RelationExtractor.createPrinter;

/**
 * Created by Moritz on 07.10.2015.
 *
 * @TODO: Investigate https://github.com/FasterXML/jackson-databind
 */
public class TokenCounter {
  public abstract class Extractor<T> {
    public final Multiset<T> tokens;

    public Extractor() {
      tokens = HashMultiset.create();
    }

    abstract public void addFromJson(JsonParser jsonParser) throws IOException;
  }

  public class TupleExtractor extends Extractor<Tuple2<String, String>> {
    public void addFromJson(JsonParser jParser) throws IOException {
      jParser.nextToken(); // [
      String type = jParser.getText();
      jParser.nextToken();
      String value = jParser.getText();
      jParser.nextToken(); // ]
      tokens.add(new Tuple2<>(type, value));
    }
  }

  public class IdentifierExtractor extends Extractor<String> {
    public void addFromJson(JsonParser jParser) throws IOException {
      String value = jParser.getText();
      tokens.add(value);
    }
  }


  public Multiset<Tuple2<String, String>> countTokens(InputStream in) throws IOException {
    TupleExtractor tokens = new TupleExtractor();
    processFile(in, tokens);
    return tokens.tokens;
  }

  public Multiset<String> countIdentifer(InputStream in) throws IOException {
    IdentifierExtractor tokens = new IdentifierExtractor();
    processFile(in, tokens);
    return tokens.tokens;
  }

  private void processFile(InputStream in, Extractor tokens) throws IOException {
    JsonFactory jfactory = new JsonFactory();
    JsonParser jParser = jfactory.createJsonParser(in);
    String hash = "";
    if (jParser.nextToken() != JsonToken.START_ARRAY) {
      emitError("Expected a JSON array Unexpected token " + jParser.getText());
    }
    while (jParser.nextToken() != JsonToken.END_ARRAY) {
      if (jParser.getCurrentToken() == JsonToken.START_OBJECT) {
        jParser.nextToken();
        if (jParser.getCurrentName().equals("inputhash")) {
          jParser.nextToken();
          hash = jParser.getText();
          jParser.nextToken();
        } else {
          emitError("Missing inputhash " + jParser.getText());
        }
      } else {
        emitError("Unexpected token " + jParser.getText());
      }
      switch (jParser.getCurrentName()) {
        case "tokens":
        case "texvcinfo":
          if (jParser.nextToken() == JsonToken.START_ARRAY) {
            while (jParser.nextToken() != JsonToken.END_ARRAY) {
              tokens.addFromJson(jParser);
            }
          } else if((jParser.nextToken() == JsonToken.START_OBJECT)) {
            if (jParser.nextToken() != JsonToken.END_OBJECT) {
              //noinspection StatementWithEmptyBody
              do {

              } while (jParser.nextToken() != JsonToken.END_OBJECT);
            }
          } else {
            emitError("[ after texvcinfo expected in " + hash + " but got " + jParser.getText());
          }
          break;
        default:
          emitError("Unexpected token" + jParser.getText());
      }
      if (jParser.nextToken() != JsonToken.END_OBJECT) {
        emitError("Missing object end");
      }
    }
    jParser.close();
  }

  private void emitError(String message) throws IOException {
    throw new IOException(message);
  }

  public static void run(CountCommandConfig config) {
    try {
      PrintWriter pw = createPrinter(config);
      InputStream in = new FileInputStream(config.getInput());
      TokenCounter tokenCounter = new TokenCounter();
      ObjectMapper mapper = new ObjectMapper().registerModule(new GuavaModule());
      if(config.isIdentifiers()){
        ImmutableSet<Multiset.Entry<String>> entries = Multisets.copyHighestCountFirst(tokenCounter.countIdentifer(in)).entrySet();
        if (config.isCsv()){
          CSVPrinter printer = CSVFormat.DEFAULT.withHeader("tex", "count").withRecordSeparator("\n").print(pw);

          for (Multiset.Entry<String> entry : entries) {
            String[] output  = {entry.getElement(), String.valueOf(entry.getCount())};
            printer.printRecord(output);
          }
        } else{
          mapper.writeValue(pw, entries);
        }
      } else {
        ImmutableSet<Multiset.Entry<Tuple2<String, String>>> entries = Multisets.copyHighestCountFirst(tokenCounter.countTokens(in)).entrySet();
        mapper.writeValue(pw, entries);
      }
      pw.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
