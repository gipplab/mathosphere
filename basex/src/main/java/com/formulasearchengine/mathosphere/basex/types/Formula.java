package com.formulasearchengine.mathosphere.basex.types;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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

	public Formula( Formula formula ) {
		this.id = formula.getId();
		this.queryFormulaID = formula.getQueryFormulaID();
		this.filename = formula.getFilename();
		this.score = formula.getScore();
		this.qvars = cloneQvars( formula.getQvars() );
	}

	public Formula( String id, String queryFormulaID, String filenameAndFormulaID, Integer score ) {
		this.id = id;
		this.queryFormulaID = queryFormulaID;
		this.filename = filenameAndFormulaID;
		//Null assignment makes attribute disappear
		this.score = score == null ? null : String.valueOf( score );
		qvars = new ArrayList<>();
	}

	public void addQvar( Qvar qvar ) {
		qvars.add( new Qvar( qvar ) );
	}

	public void setQvars( List<Qvar> qvars ) {
		this.qvars = cloneQvars( qvars );
	}

	public List<Qvar> getQvars() {
		return qvars;
	}

	private static List<Qvar> cloneQvars( List<Qvar> qvars ) {
		if ( qvars != null ) {
			final List<Qvar> out = new ArrayList<>();
			for ( final Qvar qvar : qvars ) {
				final Qvar qvarCopy = new Qvar( qvar );
				out.add( qvarCopy );
			}
			return out;
		} else {
			return null;
		}
	}

	public void setScore( Integer score ) {
		this.score = score == null ? "" : String.valueOf( score );
	}

	public String getScore() {
		return score;
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getQueryFormulaID() {
		return queryFormulaID;
	}

	public void setQueryFormulaID( String queryFormulaID ) {
		this.queryFormulaID = queryFormulaID;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename( String filename ) {
		this.filename = filename;
	}
}
