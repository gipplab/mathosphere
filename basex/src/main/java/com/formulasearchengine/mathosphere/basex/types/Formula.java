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
@XStreamConverter(Formula.FormulaConverter.class)
public class Formula {
	private String id;

	private String queryFormulaID;

	private String filename;

	//This is a string so that "" strings are deserialized correctly
	private String score;

	private List<Qvar> qvars;

	public Formula( String id, String queryFormulaID, String filenameAndFormulaID, Integer score ) {
		this.id = id;
		this.queryFormulaID = queryFormulaID;
		this.filename = filenameAndFormulaID;
		this.score = score == null ? "" : String.valueOf( score );
		qvars = new ArrayList<>();
	}

	public void addQvar( Qvar qvar ) {
		qvars.add( qvar );
	}

	public void setQvars( List<Qvar> qvars ) {
		this.qvars = new ArrayList<>( qvars );
	}

	public List<Qvar> getQvars() {
		return new ArrayList<>( qvars );
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

	/**
	 * This converter class makes the "score" attribute optional.
	 */
	public static class FormulaConverter implements Converter {
		@Override
		public final void marshal( Object o, HierarchicalStreamWriter hsw, MarshallingContext marshallingContext ) {
			final Formula formula = (Formula) o;
			hsw.addAttribute( "id", formula.getId() );
			hsw.addAttribute( "for", formula.getFor() );
			hsw.addAttribute( "xref", formula.getXref() );
			if ( formula.getScore() != null ) {
				hsw.addAttribute( "score", String.valueOf( formula.getScore() ) );
			}
			marshallingContext.convertAnother( formula.getQvars() );
		}
		@Override
		public final Object unmarshal( HierarchicalStreamReader hsr, UnmarshallingContext unmarshallingContext ) {
			final String xmlScore = hsr.getAttribute( "score" );
			final Integer score = xmlScore == null || xmlScore.isEmpty() ? null : Integer.valueOf( xmlScore );
			final Formula formula = new Formula( hsr.getAttribute( "id" ),
					hsr.getAttribute( "for" ),
					hsr.getAttribute( "xref" ),
					score );

			final List<Qvar> qvars = new ArrayList<>();
			while ( hsr.hasMoreChildren() ) {
				hsr.moveDown();
				qvars.add( (Qvar) unmarshallingContext.convertAnother(formula, Qvar.class ) );
				hsr.moveUp();
			}
			formula.setQvars( qvars );
			return formula;
		}
		@Override
		public final boolean canConvert( Class aClass ) {
			return aClass.equals( Formula.class );
		}
	}
}
