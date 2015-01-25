package com.formulasearchengine.backend.basex.restd.rest;

import com.formulasearchengine.backend.basex.Client;
import org.restexpress.Request;
import org.restexpress.Response;

public class BasexController {

	private String query (String query) {
		String result = "";
		try {
			Client client = new Client();
			result = client.execute( query );
		} catch ( Exception e ){

		}
		if ( result.length() == 0 ){
			result = "no data retrieved";
		}
		return result+result.length();
	}

	public String read (Request req, Response res) {
		String query = req.getHeader( "query" );
		res.setContentType( "text/xml" );
		return query( query );
	}

	public String create (Request request, Response response) {
		response.setResponseCreated();
		try{
			String query = request.getBodyAs( String.class );
			return query(query);
		} catch ( Exception e ){
			e.printStackTrace();
			return "No valid post data received.";
		}

	}

	public String delete (Request request, Response response) {
		return "not supported request type";
	}


	public String update (Request request, Response response) {
		return "not supported request type";
	}
}
