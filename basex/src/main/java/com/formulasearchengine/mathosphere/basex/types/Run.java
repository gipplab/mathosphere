package com.formulasearchengine.mathosphere.basex.types;

import java.util.ArrayList;
import java.util.List;

/**
 * Store Run in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
public class Run {
	private final String runtag;
	private Long ms;
	private final String type;

	private boolean showTime = true;

	private final List<Result> results = new ArrayList<Result>();

	public Run( String runtag, Long ms, String type ) {
		this.runtag = runtag;
		this.ms = ms;
		this.type = type;
	}

	public Run( String runtag, String type ) {
		this.runtag = runtag;
		this.type = type;
	}

	public void setShowTime( boolean showTime ) {
		this.showTime = showTime;
	}

	public void setTime( Long ms ) {
		this.ms = ms;
	}

	public void addResult( String num, Long ms ) {
		results.add( new Result( num, ms ) );
	}

	public void addResult( Result result ) {
		results.add( result );
	}

	public String toXML() {
		final StringBuilder resultXMLBuilder = new StringBuilder();
		for ( final Result result : results ) {
			result.setShowTime( showTime );
			resultXMLBuilder.append( result.toXML() );
		}

		final StringBuilder s = new StringBuilder().append( "  <run runtag=\"" ).append( runtag );
		if ( showTime ) {
			s.append( "\" runtime=\"" ).append( ms );
		}
		s.append( "\" runtype=\"" ).append( type ).append( "\">\n" ).
				append( resultXMLBuilder.toString() ).append( "  </run>\n" );
		return s.toString();
	}
}
