package com.formulasearchengine.backend.basex.restd.rest;

import com.formulasearchengine.backend.basex.Client;
import com.formulasearchengine.backend.basex.XMLHelper;
import org.apache.commons.io.IOUtils;
import org.restexpress.Request;
import org.restexpress.Response;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class MWSController {

	private String query (String query) {
		Client client = new Client();
		try {
			Document doc = XMLHelper.String2Doc( query );
			return client.runMWSQuery( doc );
		} catch ( ParserConfigurationException e ) {
			e.printStackTrace();
		} catch ( IOException e ) {
			e.printStackTrace();
		}
		return "No valid XML Document retrieved.";
	}

	public String read (Request req, Response res) {
		String query = req.getHeader( "query" );
		res.setContentType( "text/xml" );
		return query( query );
	}

	public String create (Request request, Response response) {
		response.setResponseCreated();
		InputStream is = request.getBodyAsStream();
		String query = null;
		try {
			query = IOUtils.toString( is );
		} catch ( IOException e ) {
			query = "Input can not be understood"+e.getLocalizedMessage();
		}
		return query( query );
	}

	public String delete (Request request, Response response) {
		return "not supported request type";
	}


	public String update (Request request, Response response) {
		return "not supported request type";
	}
}
