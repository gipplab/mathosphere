package com.formulasearchengine.mathosphere.basex.types;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores formula in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
@XStreamAlias("formula")
public class Formula {
	@XStreamAlias("id")
	@XStreamAsAttribute
	private String id;

	@XStreamAlias("for")
	@XStreamAsAttribute
	private String queryFormulaID;

	@XStreamAlias("xref")
	@XStreamAsAttribute
	private String filename;

	//This is a string so that "" strings are deserialized correctly
	@XStreamAlias("score")
	@XStreamAsAttribute
	private String score;

	@XStreamAlias("qvar")
	@XStreamImplicit
	private List<Qvar> qvars;

	public Formula( String id, String queryFormulaID, String filenameAndFormulaID, Integer score ) {
		this.id = id;
		this.queryFormulaID = queryFormulaID;
		this.filename = filenameAndFormulaID;
		//Null assignment makes attribute disappear
		this.score = score == null ? null : String.valueOf( score );
		qvars = new ArrayList<>();
	}

	public void addQvar( Qvar qvar ) {
		qvars.add( qvar );
	}

	public void setQvars( List<Qvar> qvars ) {
		this.qvars = new ArrayList<>( qvars );
	}

	public List<Qvar> getQvars() {
		return qvars;
	}

	public void setScore( Integer score ) {
		this.score = score == null ? "" : String.valueOf( score );
	}

	public Integer getScore() {
		return score != null && score.isEmpty() ? null : Integer.valueOf( score ).intValue();
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getFor() {
		return queryFormulaID;
	}

	public void setFor( String queryFormulaID ) {
		this.queryFormulaID = queryFormulaID;
	}

	public String getXref() {
		return filename;
	}

	public void setXref( String filename ) {
		this.filename = filename;
	}
}
