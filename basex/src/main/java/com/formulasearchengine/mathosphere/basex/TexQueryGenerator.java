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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TexQueryGenerator {
	private String LaTeXMLURL = "http://drmf-latexml.wmflabs.org";
	private Map ob = new LinkedHashMap();
	private List<NameValuePair> params;

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

	public String getLaTeXMLURL () {
		return LaTeXMLURL;
	}

	public Map getOb () {
		return ob;
	}

	public void setLaTeXMLURL (String laTeXMLURL) {
		LaTeXMLURL = laTeXMLURL;
	}

	public String request (String tex) throws IOException, IllegalStateException {
		HttpPost httppost = new HttpPost( LaTeXMLURL );
		CloseableHttpClient httpClient = HttpClients.createDefault();
		List<NameValuePair> p = getParams();
		p.add( new BasicNameValuePair( "tex", tex.trim() ) );
		HttpResponse response;

		httppost.setEntity( new UrlEncodedFormEntity( p, "UTF-8" ) );
		response = httpClient.execute( httppost );

		HttpEntity entity = response.getEntity();

		InputStream instream = entity.getContent();
		ob = new ObjectMapper().readValue( instream, Map.class );

		if ( ob.get( "status_code" ) == null || ob.get( "result" ) == null ) {
			throw new IOException( "Unable to process MathML conversion server response to Tex request.");
		}

		if ( Integer.parseInt(  ob.get( "status_code" ).toString() ) > 1 ) {
			throw new IOException( "Tex request to MathML conversion server produced failed response.",
					new IOException( ob.get( "result" ).toString()));
		}

		return ob.get( "result" ).toString();
	}
}
