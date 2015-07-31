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
import com.formulasearchengine.mathosphere.basex.types.Hit;
import com.formulasearchengine.mathosphere.basex.types.Result;
import com.formulasearchengine.mathosphere.basex.types.Results;
import com.formulasearchengine.mathosphere.basex.types.Run;
import org.w3c.dom.Document;

import javax.xml.xquery.XQException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	private String errorMessage = "";

	//By default return the non typed run
	private String runType = "";

	//By default return the results for non numbered query
	private String queryID = "NTCIR11-Math-";

	//Setting offset to 0 or lower disables offset
	private int offset = 0;

	//Setting limit to 0 or lower disables limit
	private int limit = 0;

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

	/**
	 * This clone constructor clones everything except for results, error message, and success.
	 * @param request
	 */
	public MathRequest( MathRequest request ) {
		this.type = request.getType();
		this.query = request.getQuery();
		this.runType = request.getRunType();
		this.queryID = request.getQueryID();
		this.offset = request.getOffset();
		this.limit = request.getLimit();
	}

	public void processRequestParams( Optional<String> runtype, Optional<String> queryID,
								  Optional<String> offset, Optional<String> limit ) {
		if ( runtype.isPresent() ) {
			this.setRunType( runtype.get() );
		}
		if ( queryID.isPresent() ) {
			this.setQueryID( queryID.get() );
		}
		if ( offset.isPresent() ) {
			try {
				this.setOffset( Integer.parseInt( offset.get() ) );
			} catch ( final NumberFormatException e ) {
				errorMessage += "Unable to parse int from offset.\n";
			}
		}
		if ( limit.isPresent() ) {
			try {
				this.setLimit( Integer.parseInt( limit.get() ) );
			} catch ( final NumberFormatException e ) {
				errorMessage += "Unable to parse int from limit.\n";
			}
		}
	}

	public MathRequest setRunType( String runType ) {
		this.runType = runType;
		return this;
	}
	public MathRequest setQueryID( String queryID ) {
		this.queryID = queryID;
		return this;
	}
	public MathRequest setLimit( int limit ) {
		this.limit = limit;
		return this;
	}
	public MathRequest setOffset( int offset ) {
		this.offset = offset;
		return this;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public String getRunType() {
		return this.runType;
	}
	public String getQueryID() {
		return this.queryID;
	}
	public int getOffset() {
		return this.offset;
	}
	public int getLimit() {
		return this.limit;
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
	public MathRequest setSuccess( final boolean success ) {
		this.success = success;
		return this;
	}

	@JsonIgnore
	public MathRequest run () {
		if ( type == null || type == "" ){
			type = "mws";
		}

		Cache.logQuery( new MathRequest( this ) );

		Results cachedResults = Cache.getCachedResults( query );

		if ( cachedResults != null ) {
			System.out.println("Cached result found!");
			errorMessage = null;
			success = true;
		} else {
			cachedResults = runQuery();
			Cache.cacheResults( query, cachedResults );
		}
		results = setResultsToCache( runType, queryID, cachedResults );

		return this;
	}

	@JsonIgnore
	private static Results setResultsToCache( String runType, String queryID, Results cachedResults ) {
		Results results = new Results();
		if ( cachedResults != null ) {
			results.setShowTime( cachedResults.getShowTime() );
			final List<Run> cachedRuns = cachedResults.getRuns();
			for ( final Run run : cachedRuns ) {
				if ( run.getRunType().equals( runType ) ) {
					results.addRun( new Run( run ) );
					break;
				}
			}
			if ( results.getRuns().isEmpty() ) {
				return new Results();
			} else {
				for ( final Result result : results.getRuns().get( 0 ).getResults() ) {
					if ( result.getQueryID().equals( queryID ) ) {
						List<Result> addResults = new ArrayList<>();
						addResults.add( result );
						results.getRuns().get( 0 ).setResults( addResults );
						break;
					}
				}

				if ( results.getRuns().get( 0 ).getResults().isEmpty() ) {
					return new Results();
				}
			}
		}
		return results;
	}

	@JsonIgnore
	private Results runQuery() {
		//TODO implement setting run type and query ID
		Client client = new Client();
		client.setShowTime( showTime );

		Results out;
		switch ( type ) {
			case "tex":
				try {
					out = client.runTexQuery( query );
					errorMessage = null;
					success = true;
				} catch ( final IOException | XQException e ) {
					out = new Results();
					errorMessage += "Tex Query failed, due to the following exception:\n" + e.getMessage();
					success = false;
				}
				break;
			case "xquery":
				try {
					out = client.runQueryNtcirWrap( query );
					errorMessage = null;
					success = true;
				} catch ( final XQException e ) {
					out = new Results();
					errorMessage += "XQuery Query failed, due to the following exception:\n" + e.getMessage();
					success = false;
				}
				break;
			default:
				Document doc = XMLHelper.String2Doc( query, true );
				if ( doc != null ) {
					try {
						out = client.runMWSQuery( doc );
						errorMessage = null;
						success = true;
					} catch ( final XQException e ) {
						out = new Results();
						errorMessage += "XQuery Query failed, due to the following exception:\n" + e.getMessage();
						success = false;
					}
				} else {
					out = new Results();
					errorMessage += "XQuery Query failed. No valid XML Document could be retrieved.";
					success = false;
				}
		}

		return out;
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
