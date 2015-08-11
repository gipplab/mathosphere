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

	public Hit( Hit hit ) {
		this.id = hit.getID();
		this.filename = hit.getFilename();
		this.score = hit.getScore();
		this.rank = hit.getRank();
		this.setFormulae( hit.getFormulae() );
	}

	public Hit( String id, String filename, Integer score, Integer rank ) {
		this.id = id;
		this.filename = filename;
		this.score = score == null ? "" : String.valueOf( score );
		this.rank = rank == null ? "" : String.valueOf( rank );
		this.formulae = new ArrayList<>();
	}

	public void addFormula( Formula formula ) {
		formulae.add( new Formula( formula ) );
	}

	public List<Formula> getFormulae() {
		return formulae;
	}

	public void setFormulae( List<Formula> formulae ) {
		this.formulae = cloneFormulae( formulae );
	}

	private List<Formula> cloneFormulae( List<Formula> formulae ) {
		if ( formulae != null ) {
			final List<Formula> out = new ArrayList<>();
			for ( final Formula formula : formulae ) {
				final Formula formulaCopy = new Formula( formula );
				out.add( formulaCopy );
			}
			return out;
		} else {
			return null;
		}
	}

	public String getID() {
		return id;
	}

	public String getFilename() {
		return filename;
	}

	public String getScore() {
		return score;
	}

	public String getRank() {
		return rank;
	}



}
