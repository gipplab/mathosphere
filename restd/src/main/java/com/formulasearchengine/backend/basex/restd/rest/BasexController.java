package com.formulasearchengine.backend.basex.restd.rest;

import com.formulasearchengine.backend.basex.Client;
import org.restexpress.Request;
import org.restexpress.Response;

public class BasexController {

	private String query (String query) {
		Client client = new Client();
		return client.execute( query );
	}

	public String read (Request req, Response res) {
		String query = req.getHeader( "query" );
		res.setContentType( "text/xml" );
		return query( query );
	}

	public String create (Request request, Response response) {
		response.setResponseCreated();
		String query = request.getBodyAs( String.class );
		return query(query);
	}

	public String delete (Request request, Response response) {
		return "not supported request type";
	}


	public String update (Request request, Response response) {
		return "not supported request type";
	}
}
