package com.formulasearchengine.mathosphere.basex.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Stores Result in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
public class Result {
	private Long ms;
	private final String num;
	private final List<Hit> hits = new ArrayList<>();
	private boolean showTime = true;

	private final Pattern NTCIR_MATH_PATTERN = Pattern.compile( "NTCIR11-Math-", Pattern.LITERAL );

	public Result( String num, Long ms ) {
		this.ms = ms;
		this.num = num;
	}

	public Result( String num ) {
		this.num = num;
	}

	/**
	 * Overriden by Run's setShowTime (this will be refactored later)
	 * @param showTime
	 */
	public void setShowTime( boolean showTime ) {
		this.showTime = showTime;
	}

	public int size() {
		return hits.size();
	}

	public Long getTime() {
		return ms;
	}

	public void setTime( Long ms ) {
		this.ms = ms;
	}

	public void addHit( Hit hit ) {
		hits.add( hit );
	}

	public String toXML() {
		final StringBuilder hitXMLBuilder = new StringBuilder();
		for ( final Hit hit : hits ) {
			hitXMLBuilder.append( hit.toXML() );
		}

		final StringBuilder s = new StringBuilder().append( "    <result for=\"NTCIR11-Math-" ).append( num );
		if ( showTime ) {
			s.append( "\" runtime=\"" ).append( ms );
		}
		s.append( "\">\n" ).append( hitXMLBuilder.toString() ).append( "    </result>\n" ).toString();
		return s.toString();
	}

	public void addHit( String item, String filename, int score, int rank ) {
		addHit( item, filename, Integer.toString( score ), Integer.toString( rank ) );
	}

	public void addHit( String id, String filename, String score, String rank ) {
		hits.add( new Hit( id, filename, score, rank ) );
	}

	public void addHit( String xmlString ) {
		hits.add( new Hit( xmlString ) );
	}
}
