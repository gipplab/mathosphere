package com.formulasearchengine.mathosphere.basex;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TexQueryGenerator {
	private boolean success = false;
	private String LaTeXMLURL = "http://gw125.iu.xsede.org:8888";
	private Map ob;
	private List<NameValuePair> params;

	public Exception getLastException () {
		return lastException;
	}

	private Exception lastException;

	public List<NameValuePair> getParams () {
		if ( params == null ){
			params = new ArrayList<>();
			params.add( new BasicNameValuePair( "format", "xhtml" ) );
			params.add( new BasicNameValuePair( "whatsin", "math" ) );
			params.add( new BasicNameValuePair( "whatsout", "math" ) );
			params.add( new BasicNameValuePair( "cmml", "" ) );
			params.add( new BasicNameValuePair( "nodefaultresources", "" ) );
			params.add( new BasicNameValuePair( "preload", "LaTeX.pool" ) );
			params.add( new BasicNameValuePair( "preload", "article.cls" ) );
			params.add( new BasicNameValuePair( "preload", "amsmath.sty" ) );
			params.add( new BasicNameValuePair( "preload", "amsthm.sty" ) );
			params.add( new BasicNameValuePair( "preload", "amstext.sty" ) );
			params.add( new BasicNameValuePair( "preload", "amssymb.sty" ) );
			params.add( new BasicNameValuePair( "preload", "eucal.sty" ) );
			params.add( new BasicNameValuePair( "preload", "[dvipsnames]xcolor.sty" ) );
			params.add( new BasicNameValuePair( "preload", "url.sty" ) );
			params.add( new BasicNameValuePair( "preload", "hyperref.sty" ) );
			params.add( new BasicNameValuePair( "preload", "mws.sty" ) );
			params.add( new BasicNameValuePair( "preload", "texvc" ) );
		}
		return params;
	}

	public void setParams (List<NameValuePair> params) {
		this.params = params;
	}

	public boolean isSuccess () {
		return success;
	}

	public String getLaTeXMLURL () {
		return LaTeXMLURL;
	}

	public Map getOb () {
		return ob;
	}

	public void setLaTeXMLURL (String laTeXMLURL) {
		LaTeXMLURL = laTeXMLURL;
	}



	public String request (String tex) {
		HttpPost httppost = new HttpPost( LaTeXMLURL );
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> p = getParams();
		p.add( new BasicNameValuePair( "tex", tex.trim() ) );
		HttpResponse response;
		try {
			httppost.setEntity( new UrlEncodedFormEntity( p, "UTF-8" ) );
			response = httpClient.execute( httppost );
		} catch ( IOException e ) {
			lastException = e;
			fail( e.getLocalizedMessage() );
			return null;
		}

		HttpEntity entity = response.getEntity();
		try{
			InputStream instream = entity.getContent();
			ob = new ObjectMapper().readValue( instream, Map.class );
			if ( Integer.parseInt(  ob.get( "status_code" ).toString() ) < 2 ) {
				success = true;
			}
			return ob.get( "result" ).toString();
		} catch (  IOException | NullPointerException e ){
			lastException = e;
		}
		fail( "LaTeXML crashed" );
		return null;
	}

	private void fail (String message) {
		success = false;
		ob = new HashMap(  );
		ob.put( "status_code", 4 );
		ob.put( "status", message );
	}

	public String getErrorMessage() throws JsonProcessingException {
		String out = "Problem during TeX to MathML conversion:\n";
		if (lastException != null){
			out += "Exception:" + lastException.getLocalizedMessage() +"\n";
		}
		ObjectMapper mapper = new ObjectMapper();
		out +=  mapper.writeValueAsString( ob );
		return out;
	}

}
