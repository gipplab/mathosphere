package com.formulasearchengine.mlp.evaluation;

import com.google.common.base.Throwables;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Leo on 25.10.2016.
 */
public class CliTest {
  @Test
  public void testCli() throws IOException {
    Main.main(new String[]{
      "eval",
      "-in", resourcePath("formulasearchengine/mlp/gold/extraction.csv"),
      "-gold", resourcePath("formulasearchengine/mlp/gold/gold.json")
    });
  }

  @Test
  public void testHelp() throws IOException {
    Main.main(new String[]{});
    Main.main(new String[]{
      "help"
    });
  }

  private String resourcePath(String resourceName) {
    ClassLoader classLoader = getClass().getClassLoader();
    URL resource = classLoader.getResource(resourceName);
    return decodePath(resource.getFile());
  }

  private static String decodePath(String urlEncodedPath) {
    try {
      return URLDecoder.decode(urlEncodedPath, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }
}
