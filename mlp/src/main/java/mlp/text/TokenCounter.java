package mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.apache.flink.api.java.tuple.Tuple2;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import java.io.InputStream;

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
      throw new IOException("Expected a JSON array Unexpected token " + jParser.getText());
    }
    while (jParser.nextToken() != JsonToken.END_ARRAY) {
      if (jParser.getCurrentToken() == JsonToken.START_OBJECT) {
        jParser.nextToken();
        if (jParser.getCurrentName().equals("inputhash")) {
          jParser.nextToken();
          hash = jParser.getText();
          jParser.nextToken();
        } else {
          throw new IOException("Missing inputhash " + jParser.getText());
        }
      } else {
        throw new IOException("Unexpected token " + jParser.getText());
      }
      switch (jParser.getCurrentName()) {
        case "tokens":
        case "texvcinfo":
          if (jParser.nextToken() != JsonToken.START_ARRAY) {
            throw new IOException("[ after texvcinfo expected");
          } else {
            while (jParser.nextToken() != JsonToken.END_ARRAY) {
              tokens.addFromJson(jParser);
            }
          }
          break;
        default:
          throw new IOException("Unexpected token" + jParser.getText());
      }
      if (jParser.nextToken() != JsonToken.END_OBJECT) {
        throw new IOException("Missing object end");
      }
    }
    jParser.close();
  }

}
