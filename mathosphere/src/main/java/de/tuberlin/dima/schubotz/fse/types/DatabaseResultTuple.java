package de.tuberlin.dima.schubotz.fse.types;

import org.apache.flink.api.java.tuple.Tuple9;

/**
 * Tuple that stores scores and justifications for each query and document combo. 
 */
public class DatabaseResultTuple extends Tuple9<Integer,String,Integer,Integer,Integer,Integer,Double,Integer,Integer> {
	public final static String SQL = "INSERT INTO `results` (`queryNum`, `queryFormulaId`, `fId`, `cdMatch`, `dataMatch`, `matchDepth`, `queryCoverage`, `isFormulae` ,`vote`) VALUES (?,?,?,?,?,?,?,?,?);";
	static final Integer FLINK_NULL_WORKAROUND = -1;
	public enum fields {
		queryNum,
		queryFormulaID,
		fId,
		cdMatch,
		dataMatch,
		matchDepth,
		queryCoverage,
		isFormulae,
		vote
	}

	public <T> T getNamedField(fields field) {
		return (T) getField( field.ordinal() );
	}
	public <T> void setNamedField(fields field, T value) {setField(value, field.ordinal());}
	public void setNamedFieldB(fields field, Boolean value) {
	if (value == null){
			setNamedField( field, FLINK_NULL_WORKAROUND );
		} else if (value == true) {
			setField(1 , field.ordinal());
		} else {
		setField( 0, field.ordinal() );
	}
	}
	public void setNamedFieldD(fields field, Double value) {
		if (value == null){
			setNamedField( field, -1. );
		}  else {
			setField( value, field.ordinal() );
		}
	}
	public void setNamedFieldI(fields field, Integer value) {
		if (value == null){
			setNamedField( field, -1 );
		}  else {
			setField( value, field.ordinal() );
		}
	}
}
