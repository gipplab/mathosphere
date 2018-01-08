package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.http.NameValuePair;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moritz on 28.09.2015.
 */
public class TexInfo {

  private static CloseableHttpClient cachingClient = null;
  private static HttpCacheContext context;
  private static CacheResponseStatus cacheResponseStatus;
  private static boolean USE_POST = true;
  private static String makeRequest(String tex, String url)  {
    HttpRequestBase post;
    if (cachingClient == null || context == null) {
      CacheConfig cacheConfig = CacheConfig.custom()
              .setMaxCacheEntries(1000)
              .setMaxObjectSize(8192)
              .build();
      RequestConfig requestConfig = RequestConfig.custom()
              .setConnectTimeout(30000)
              .setSocketTimeout(30000)
              .build();
      cachingClient = CachingHttpClients.custom()
              .setCacheConfig(cacheConfig)
              .setDefaultRequestConfig(requestConfig)
              .build();
      context = HttpCacheContext.create();
    }
    if (USE_POST){
       post = new HttpPost(url);
      List<NameValuePair> nameValuePairs = new ArrayList<>(1);
      nameValuePairs.add(new BasicNameValuePair("q", tex));
      try {
        ((HttpPost) post).setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    } else {
      String q = null;
      try {
        q = URLEncoder.encode(tex,"UTF-8").replace("+", "%20");
      } catch (UnsupportedEncodingException e) {
        q = tex;
      }
      url = url.replace("texvcinfo", "get/texvcinfo");
      post = new HttpGet(url +"/tex/" + q);
    }

    try {

      CloseableHttpResponse response = cachingClient.execute(post, context);
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String line;
      String result = "";
      while ((line = rd.readLine()) != null) {
        result += line;
      }

        cacheResponseStatus = context.getCacheResponseStatus();

        post.releaseConnection();


      //HttpResponse response = client.execute(post);

      return result;
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      post.releaseConnection();
    }
    return "";
  }

  public static Multiset<String> getIdentifiers(String tex, String url) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException {
    final Multiset<String> strings = HashMultiset.create();
    //long t0 = System.nanoTime();
    String json = makeRequest(tex, url);
    if (tex.length() == 0) {
      return strings;
    }
    //System.out.println((System.nanoTime()-t0)/1000000+"ms for "+tex);
    try {
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(json);
      JSONArray identifiers = jsonObject.getJSONArray("identifiers");
      strings.addAll(identifiers);
    } catch (Exception e) {
      System.out.println(tex + " Parsing problem");
      System.out.println("Retrieved: " + json);
      //e.printStackTrace();
    }
    return strings;
  }

  public static CacheResponseStatus getCacheResponseStatus() {
    return cacheResponseStatus;
  }
}
