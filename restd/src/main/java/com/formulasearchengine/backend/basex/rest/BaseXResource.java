package com.formulasearchengine.backend.basex.rest;

import com.formulasearchengine.backend.basex.Client;
import com.formulasearchengine.backend.basex.XMLHelper;
import org.w3c.dom.Document;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import javax.validation.constraints.NotNull;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Component
@RestxResource
public class BaseXResource {
	@GET("/xquery")
	@PermitAll
	public String xquery( String query ) {
		String result = "";
		try {
			Client client = new Client();
			result = client.execute( query );
		} catch ( Exception e ) {

		}
		if ( result.length() == 0 ) {
			result = "no data retrieved";
		}
		return result;
	}

	@POST("/mwsquery")
	@PermitAll
	public MyPOJO mwsquery( MyPOJO q ) {
		q.setValue( mwsquery( q.getValue() ) );
		return q;
	}

	@GET("/mwsquery")
	@PermitAll
	public String mwsquery( String q ) {
		Client client = new Client();
		try {
			Document doc = XMLHelper.String2Doc( q );
			return client.runMWSQuery( doc );
		} catch ( ParserConfigurationException | NullPointerException | IOException e ) {
			e.printStackTrace();
		}
		return "No valid XML Document retrieved.";
	}

	public static class MyPOJO {
		@NotNull
		String value;

		public String getValue() {
			return value;
		}

		public void setValue( String value ) {
			this.value = value;
		}
	}

}
