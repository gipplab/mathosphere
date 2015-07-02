package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.List;

/**
 * Store Run in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
@XStreamAlias("run")
public class Run {
	@XStreamAlias("runtag")
	@XStreamAsAttribute
	private final String runtag;

	//This is a string so that "" strings are deserialized correctly
	@XStreamAlias("runtime")
	@XStreamAsAttribute
	private String ms;

	@XStreamAlias("run_type")
	@XStreamAsAttribute
	private final String type;

	@XStreamImplicit
	private List<Result> results;

	@XStreamOmitField
	private boolean showTime = true;

	public Run( String runtag, Long ms, String type ) {
		this.runtag = runtag;
		this.ms = ms == null ? "" : String.valueOf( ms );
		this.type = type;
		this.results = new ArrayList<>();
	}

	public Run( String runtag, String type ) {
		this.runtag = runtag;
		this.type = type;
		this.results = new ArrayList<>();
		this.ms = "";
	}

	public void setShowTime( boolean showTime ) {
		this.showTime = showTime;

		if ( results != null ) {
			for ( final Result result : results ) {
				result.setShowTime( showTime );
			}
		}
	}

	public boolean getShowTime() {
		return this.showTime;
	}

	public void setTime( Long ms ) {
		this.ms = ms == null ? "" : String.valueOf( ms );
	}

	public void addResult( Result result ) {
		result.setShowTime( showTime );
		results.add( result );
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults( List<Result> results ) {
		this.results = new ArrayList<>( results );

		for ( final Result result : results ) {
			result.setShowTime( showTime );
		}
	}

	public int getNumResults() {
		return results.size();
	}
}
