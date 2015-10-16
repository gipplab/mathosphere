package mlp.text;


import java.util.regex.Matcher;

public interface StringReplacerCallback {
  String replace(Matcher match);
}
