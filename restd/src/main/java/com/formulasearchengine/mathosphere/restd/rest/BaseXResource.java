package com.formulasearchengine.mathosphere.restd.rest;

import com.formulasearchengine.mathosphere.basex.Client;
import com.formulasearchengine.mathosphere.basex.types.Results;
import com.formulasearchengine.mathosphere.restd.domain.Cache;
import com.formulasearchengine.mathosphere.restd.domain.MathRequest;
import com.formulasearchengine.mathosphere.restd.domain.MathUpdate;
import restx.Status;
import restx.annotations.DELETE;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

import java.util.List;
import java.util.Map;

/**
 * This class handles all REST requests to the BaseX server.
 */
@Component
@RestxResource
public class BaseXResource {
	private MathRequest logAndGetResponse( MathRequest request ) {
		Cache.logQuery( request );
		final Results cachedResults = Cache.getCachedResults( request );
		if ( cachedResults != null ) {
			request.setResults( cachedResults );
			request.setSuccess( true );
			return request;
		} else {
			request.run();
			Cache.cacheResults( request );
			return request;
		}
	}

	@GET("/texquery")
	@PermitAll
	public MathRequest texquery( String query ) {
		final MathRequest request = new MathRequest( query ).setType( "tex" );
		return logAndGetResponse( request );

	}
	@POST("/texquery")
	@PermitAll
	public MathRequest texquery( MathRequest request ) {
		if( request.getType() == null ||  "".equals( request.getType() ) ){
			request.setType( "tex" );
		}
		return logAndGetResponse( request );
	}
	@GET("/xquery")
	@PermitAll
	public MathRequest xquery( String query ) {
		final MathRequest request = new MathRequest( query ).setType( "xquery" );
		return logAndGetResponse( request );
	}
	@POST("/xquery")
	@PermitAll
	public MathRequest xquery( MathRequest request ) {
		if( request.getType() == null ||  "".equals( request.getType() ) ){
			request.setType( "xquery" );
		}
		return logAndGetResponse( request );
	}
	@GET("/mwsquery")
	@PermitAll
	public MathRequest mwsquery( String q ) {
		final MathRequest request = new MathRequest( q ).setType( "mws" );
		return logAndGetResponse( request );
	}
	@POST("/mwsquery")
	@PermitAll
	public MathRequest mwsquery( MathRequest request ) {
		if( request.getType() == null ||  "".equals( request.getType() ) ) {
			request.setType( "mws" );
		}
		return logAndGetResponse( request );
	}
	@POST("/")
	@PermitAll
	public MathRequest query( MathRequest request ) {
		return logAndGetResponse( request );
	}
	@POST("/update")
	@PermitAll
	public MathUpdate update( MathUpdate update ) {
		return update.run();
	}
	@GET("/cntRev")
	@PermitAll
	public Integer dbsize( Integer revision ) {
		Client client = new Client();
		return client.countRevisionFormula( revision );
	}
	@GET("/cntAll")
	@PermitAll
	public Integer dbsize(  ) {
		Client client = new Client();
		return client.countAllFormula();
	}

	@DELETE("/queryLog/")
	@PermitAll
	public Status flushQueryLog() {
		Cache.flushQueryLog();
		return Status.of( "Flushed query log" );
	}

	@GET("/queryLog/")
	@PermitAll
	public List<MathRequest> getQueryLog() {
		return Cache.getQueryLog();
	}

	@DELETE("/resultsCache/")
	@PermitAll
	public Status flushResultsCache() {
		Cache.flushCachedResults();
		return Status.of( "Flushed results cache" );
	}

	@GET("/resultsCache/")
	@PermitAll
	public Map<String, String> getResultsCache() {
		return Cache.getAllCachedResultsAsStrings();
	}
}
