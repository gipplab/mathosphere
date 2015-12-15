package com.formulasearchengine.mathosphere.mlp.text;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Moritz on 28.09.2015.
 */
public class TexInfo {
  private static String makeRequest(String tex){
    HttpClient client = new DefaultHttpClient();
    //HttpPost post = new HttpPost("http://localhost/convert");
    HttpPost post = new HttpPost("http://api.formulasearchengine.com/v1/media/math/check/tex");
    try {
      List<NameValuePair> nameValuePairs = new ArrayList<>(1);
      nameValuePairs.add( new BasicNameValuePair("q", tex ) );
      post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "utf-8"));
      HttpResponse response = client.execute(post);
      BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
      String line;
      String result="";
      while ((line = rd.readLine()) != null) {
        result+=line;
      }
      return result;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return "";
  }

  public static Multiset<String> getIdentifiers(String tex) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException {
	  final Multiset<String> strings = HashMultiset.create();
	  //long t0 = System.nanoTime();
	  String json = makeRequest(tex);
	  //System.out.println((System.nanoTime()-t0)/1000000+"ms for "+tex);
	  try {
		  JSONObject Ojson = (JSONObject) JSONSerializer.toJSON(json);
		  JSONArray identifiers = Ojson.getJSONArray("identifiers");
		 strings.addAll(identifiers);
	  } catch (Exception e) {
		  e.printStackTrace();
	  }
	  return strings;
  }
}
