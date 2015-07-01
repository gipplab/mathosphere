package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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

	public Result( String queryIDNum, Long ms ) {
		this.ms = ms == null ? "" : String.valueOf( ms );
		this.queryID = queryIDNum;
		this.hits = new ArrayList<>();
	}

	public Result( String queryIDNum ) {
		this.queryID = queryIDNum;
		this.hits = new ArrayList<>();
	}

	public Long getTime() {
		return ms != null && ms.isEmpty() ? null : Long.valueOf( ms );
	}

	public void setTime( Long ms ) {
		this.ms = ms == null ? "" : String.valueOf( ms );
	}

	public void addHit( Hit hit ) {
		hits.add( hit );
	}

	public void setHits( List<Hit> hits ) {
		this.hits = new ArrayList<>( hits );
	}

	public List<Hit> getHits() {
		return new ArrayList<>( hits );
	}

	public int getNumHits() {
		return hits.size();
	}
}
