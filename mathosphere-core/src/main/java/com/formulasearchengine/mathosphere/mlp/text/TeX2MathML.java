package com.formulasearchengine.mathosphere.mlp.text;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by Moritz on 28.09.2015.
 */
public class TeX2MathML {
  private static String tex2json(String tex) throws IOException {
    CachingHttpClientBuilder cachingHttpClientBuilder = CachingHttpClientBuilder.create();
    CacheConfig cacheCfg = new CacheConfig();
    cacheCfg.setMaxCacheEntries(100000);
    cacheCfg.setMaxObjectSize(8192);
    cachingHttpClientBuilder.setCacheConfig(cacheCfg);
    HttpClient client = cachingHttpClientBuilder.build();
    //HttpPost post = new HttpPost("http://localhost/convert");
    HttpPost post = new HttpPost("https://drmf-latexml.wmflabs.org");
    List<NameValuePair> nameValuePairs = new ArrayList<>(1);
    nameValuePairs.add(new BasicNameValuePair("tex",
        "$" + tex + "$"));
    //WARNING: This does not produce pmml, since there is a xstl trasformation that rewrites the output and removes pmml
//	      nameValuePairs.add(new BasicNameValuePair("profile",
//		          "mwsquery"));
    nameValuePairs.add(new BasicNameValuePair("preload",
        "mws.sty"));
    nameValuePairs.add(new BasicNameValuePair("profile",
        "math"));
    nameValuePairs.add(new BasicNameValuePair("noplane1",
        ""));
    nameValuePairs.add(new BasicNameValuePair("whatsout",
        "math"));
    post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
    HttpResponse response = client.execute(post);
    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
    String line = "";
    String result = "";
    while ((line = rd.readLine()) != null) {
      result += line;
    }
    return result;
  }

  private static String[] json2xml(String json) {
    String[] result = {"", "", ""};
    JSONObject Ojson = (JSONObject) JSONSerializer.toJSON(json);
    //result[0]=(Ojson.getString("status"));
    result[1] = (Ojson.getString("log"));
    result[2] = (Ojson.getString("result"));
    return result;
  }

  public static String TeX2MML(String tex) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException {
    final String XPath = "//math/semantics/mrow";
    String mml = "";
    return json2xml(tex2json(tex))[2];
  }
}
