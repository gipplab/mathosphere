package com.formulasearchengine.mathosphere.restd.rest;

import com.formulasearchengine.mathosphere.basex.Client;
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

/**
 * This class handles all REST requests to the BaseX server.
 */
@Component
@RestxResource
public class BaseXResource {
	@GET("/texquery")
	@PermitAll
	public MathRequest texquery( String query ) {
		final MathRequest request = new MathRequest( query ).setType( "tex" );
		Cache.addQuery( request );
		return request.run();
	}
	@POST("/texquery")
	@PermitAll
	public MathRequest texquery( MathRequest request ) {
		if( request.getType() == null ||  "".equals( request.getType() ) ){
			request.setType( "tex" );
		}
		Cache.addQuery( request );
		return request.run();
	}
	@GET("/xquery")
	@PermitAll
	public MathRequest xquery( String query ) {
		final MathRequest request = new MathRequest( query ).setType( "xquery" );
		Cache.addQuery( request );
		return request.run();
	}
	@POST("/xquery")
	@PermitAll
	public MathRequest xquery( MathRequest request ) {
		if( request.getType() == null ||  "".equals( request.getType() ) ){
			request.setType( "xquery" );
		}
		Cache.addQuery( request );
		return request.run();
	}
	@GET("/mwsquery")
	@PermitAll
	public MathRequest mwsquery( String q ) {
		final MathRequest request = new MathRequest( q ).setType( "mws" );
		Cache.addQuery( request );
		return request.run();
	}
	@POST("/mwsquery")
	@PermitAll
	public MathRequest mwsquery( MathRequest request ) {
		if( request.getType() == null ||  "".equals( request.getType() ) ) {
			request.setType( "mws" );
		}
		Cache.addQuery( request );
		return request.run();
	}
	@POST("/")
	@PermitAll
	public MathRequest query( MathRequest request ) {
		Cache.addQuery( request );
		return request.run();
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
}
