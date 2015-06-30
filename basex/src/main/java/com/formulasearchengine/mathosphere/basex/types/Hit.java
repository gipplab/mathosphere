package com.formulasearchengine.mathosphere.basex.types;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores hits in Ntcir format.
 * Created by jjl4 on 6/24/15.
 */
@XStreamAlias("hit")
public class Hit {
	@XStreamAlias("id")
	@XStreamAsAttribute
	private final String id;

	@XStreamAlias("xref")
	@XStreamAsAttribute
	private final String filename;

	//These are strings so that "" strings are deserialized correctly
	@XStreamAlias("score")
	@XStreamAsAttribute
	private final String score;

	@XStreamAlias("rank")
	@XStreamAsAttribute
	private final String rank;

	@XStreamImplicit
	private List<Formula> formulae;

	public Hit( String id, String filename, Integer score, Integer rank ) {
		this.id = id;
		this.filename = filename;
		this.score = score == null ? "" : String.valueOf( score );
		this.rank = rank == null ? "" : String.valueOf( rank );
		this.formulae = new ArrayList<>();
	}

	public void addFormula( Formula formula ) {
		formulae.add( formula );
	}

	public void setFormulae( List<Formula> formulae ) {
		this.formulae = new ArrayList<>( formulae );
	}

	public List<Formula> getFormulae() {
		return new ArrayList<>( formulae );
	}

}
