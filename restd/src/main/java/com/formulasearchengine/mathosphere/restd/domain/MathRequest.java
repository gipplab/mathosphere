package com.formulasearchengine.mathosphere.restd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.formulasearchengine.mathosphere.basex.Client;
import com.formulasearchengine.mathmlquerygenerator.xmlhelper.XMLHelper;
import com.formulasearchengine.mathosphere.basex.types.Results;
import org.w3c.dom.Document;

import javax.xml.xquery.XQException;
import java.io.IOException;

/**
 * Created by Moritz on 14.03.2015.
 */
public class MathRequest {
	private String type = "";
	private String query = "";

	@JsonSerialize(using = ResultsSerializer.class)
	@JsonProperty("response")
	private Results results = null;

	private boolean success = false;

	private String errorMessage = null;

	@JsonIgnore
	private boolean showTime = false;

	public String getType () {
		return type;
	}

	public String getQuery () {
		return query;
	}

	public Results getResults() {
		return results;
	}

	public MathRequest(String query){
		this.query = query;
	}

	public MathRequest (String type, String query, Results results) {
		this.type = type;
		this.query = query;
		this.results = results;
	}

	public MathRequest () {
	}

	public MathRequest setType (final String type) {
		this.type = type;
		return this;
	}

	public MathRequest setQuery (final String query) {
		this.query = query;
		return this;
	}

	public MathRequest setShowTime(final boolean showTime) {
		this.showTime = showTime;
		return this;
	}

	public MathRequest setResults( final Results results ) {
		this.results = results;
		return this;
	}

	@JsonIgnore
	public MathRequest run () {
		if (type == null || type == "" ){
			type = "mws";
		}
		Client client = new Client();
		client.setShowTime( showTime );
		switch ( type ) {
			case "tex":
				try {
					results = client.runTexQuery( query );
					errorMessage = null;
					success = true;
				} catch ( final IOException | XQException e ) {
					results = null;
					errorMessage = "Tex Query failed, due to the following exception:\n" + e.getMessage();
					success = false;
				}
				break;
			case "xquery":
				try {
					results = client.runQueryNtcirWrap( query );
					errorMessage = null;
					success = true;
				} catch ( final XQException e ) {
					results = null;
					errorMessage = "XQuery Query failed, due to the following exception:\n" + e.getMessage();
					success = false;
				}
				break;
			default:
				Document doc = XMLHelper.String2Doc( query, true );
				if ( doc != null ) {
					try {
						results = client.runMWSQuery( doc );
						errorMessage = null;
						success = true;
					} catch ( final XQException e ) {
						results = null;
						errorMessage = "XQuery Query failed, due to the following exception:\n" + e.getMessage();
						success = false;
					}
				} else {
					results = null;
					errorMessage = "XQuery Query failed. No valid XML Document could be retrieved.";
					success = false;
				}
		}
		return this;
	}

	public boolean isSuccess () {
		return success;
	}

	public static class ResultsSerializer extends JsonSerializer<Results> {
		@Override
		public void serialize( Results results, JsonGenerator jGen, SerializerProvider provider ) throws IOException, JsonProcessingException {
			if ( results != null ) {
				jGen.writeString( Client.resultsToXML( results ) );
			}
		}
	}
}

/* 		String result = "";
		try {
			Client client = new Client();
			result = client.runQueryNtcirWrap( query );
		} catch ( Exception e ) {

		}
		if ( result.length() == 0 ) {
			result = "no data retrieved";
		}
		return result; */
