package de.tuberlin.dima.schubotz.fse;

import java.util.ArrayList;

import eu.stratosphere.api.java.tuple.Tuple2;

public class KeyWordTuple extends Tuple2<String,ArrayList<String>> {
	public enum fields {
		docName, keywordList
	}
	public KeyWordTuple(String name) {
		this.f0 = name;
	}
	public Object getNamedField(fields f) {
		return getField(f.ordinal());
	}
	public void addKeyword(String keyword) {
		this.f1.add(keyword);
	}
}
