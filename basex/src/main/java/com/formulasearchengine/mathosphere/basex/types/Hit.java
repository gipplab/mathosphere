package com.formulasearchengine.mathosphere.basex.types;

/**
 * Stores hits in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
public class Hit {
	private final String id;
	private final String filename;
	private final String score;
	private final String rank;
	//If we want to replace the above characteristics with our own XML string
	private final boolean overrideXML;
	private final String overrideString;

	public Hit( String id, String filename, String score, String rank ) {
		this.id = id;
		this.filename = filename;
		this.score = score;
		this.rank = rank;
		this.overrideXML = false;
		this.overrideString = "";
	}

	public Hit( String hit ) {
		this.id = "";
		this.filename = "";
		this.score = "";
		this.rank = "";
		this.overrideXML = true;
		this.overrideString = hit;
	}

	public String toXML() {
		if ( overrideXML ) {
			return overrideString;
		} else {
			final StringBuilder hitXMLBuilder = new StringBuilder();
			hitXMLBuilder.append( "      <hit id=\"" ).append( id ).append( "\" xref=\"" ).append( filename )
					.append( "\" score=\"" ).append( score ).append( "\" rank=\"" ).append( rank ).append( "\">\n" );
			hitXMLBuilder.append( "      </hit>" );
			return hitXMLBuilder.toString();
		}
	}

	public String toCSV() {
		return id;
	}
}
