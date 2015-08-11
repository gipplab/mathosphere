package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores Result in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
@XStreamAlias("result")
public class Result {
	@XStreamAlias("for")
	@XStreamAsAttribute
	private final String queryID;

	//This is a string so that "" strings are deserialized correctly
	@XStreamAlias("runtime")
	@XStreamAsAttribute
	private String ms;

	@XStreamImplicit
	private List<Hit> hits;

	@XStreamOmitField
	private boolean showTime = true;

	public Result( Result result ) {
		this.queryID = result.getQueryID();
		this.ms = result.getTime();
		this.setHits( result.getHits() );
	}

	public Result( String queryIDNum, Long ms ) {
		this.ms = ms == null ? "" : String.valueOf( ms );
		this.queryID = queryIDNum;
		this.hits = new ArrayList<>();
	}

	public Result( String queryIDNum ) {
		this.queryID = queryIDNum;
		this.hits = new ArrayList<>();
		this.ms = "";
	}

	public String getQueryID() {
		return queryID;
	}
	public void setShowTime( boolean showTime ) {
		this.showTime = showTime;
	}

	public boolean getShowTime() {
		return showTime;
	}

	public String getTime() {
		return ms;
	}

	public void setTime( Long ms ) {
		this.ms = ms == null ? "" : String.valueOf( ms );
	}

	public void addHit( Hit hit ) {
		hits.add( new Hit( hit ) );
	}

	public void setHits( List<Hit> hits ) {
		this.hits = cloneHits( hits );
	}

	public List<Hit> getHits() {
		return hits;
	}

	private static List<Hit> cloneHits( List<Hit> hits ) {
		if ( hits != null ) {
			final List<Hit> out = new ArrayList<>();
			for ( final Hit hit : hits ) {
				final Hit hitCopy = new Hit( hit );
				out.add( hit );
			}
			return out;
		} else {
			return null;
		}
	}

	public int getNumHits() {
		return hits.size();
	}
}
