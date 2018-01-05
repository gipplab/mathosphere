package com.formulasearchengine.mathosphere.restd.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formulasearchengine.mathosphere.basex.Client;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import org.w3c.dom.Document;

/**
 * Created by Moritz on 14.03.2015.
 */
public class MathUpdate {
	public MathUpdate (Integer[] delete, String harvest) {
		this.delete = delete;
		this.harvest = harvest;
	}

	public MathUpdate () {
	}

	public String getResponse () {
		return response;
	}

	public void setResponse (String response) {
		this.response = response;
	}

	public Integer[] getDelete () {
		return delete;
	}

	public void setDelete (Integer[] delete) {
		this.delete = delete;
	}

	public String getHarvest () {
		return harvest;
	}

	public void setHarvest (String harvest) {
		this.harvest = harvest;
	}

	Integer[] delete = {};
	String harvest = "";
	String response = "";
	boolean success = false;

	@JsonIgnore
	public MathUpdate run () {
		Client client = new Client();
		client.setShowTime( false ); //for testing
		if ( harvest.length()>0 ){
			Document doc = XMLHelper.string2Doc( harvest, true );
			//TODO: validate document
			if ( doc == null ){
				this.response = "harvest is not valid XML.";
			} else if ( client.updateFormula( doc.getDocumentElement() ) ){
				this.response = "updated";
				success = true;
			} else {
				this.response = "update failed";
			}
		} else {
			success = true;
		}
		if ( delete.length > 0 ){
			for ( Integer s : delete ) {
				if ( client.deleteRevisionFormula(s) ) {
					response += "\nrevision " + s + " deleted";
					success &= true;
				} else {
					success = false;
					response += "\nrevision " + s + " not deleted";
				}
			}
		} else {
			if ( response.length() == 0  ){
				success = false;
			}
		}
		return this;
	}
	public boolean isSuccess () {
		return success;
	}
}
