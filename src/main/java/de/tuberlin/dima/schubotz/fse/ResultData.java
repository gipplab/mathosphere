package de.tuberlin.dima.schubotz.fse;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResultData {
	String input_file = "/home/jjl4/Documents/input.csv";
	String formula_file = "/home/jjl4/Documents/formula.csv";
	ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
	ArrayList<ArrayList<String>> formula = new ArrayList<ArrayList<String>>();
	
	Map<String,Hits> formattedData;
	
	
	public ResultData() {
		//get arrays
		try {
			getData();
		}catch (IOException e) {
			System.out.printf("IOException: %s%n",e);
		}
		//parse file into map
		formattedData = new HashMap<String,Hits>();
		
	}
	
	
	private class Hits<filename,score,formulae> implements Comparable<Hits> {
		final String filename;
		final int score;
		final List<Formula> formulae;
		
		public Hits (String filename, int score, List<Formula> formulae) {
			this.filename = filename;
			this.score = score;
			this.formulae = formulae;
		}
		
		public int compareTo(Hits hit) {
			if (this.score > hit.score) {
				return 1;
			} else if (this.score < hit.score) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	private class Formula<forTag,xref,score,qvars> implements Comparable<Formula> {
		final String forTag;
		final String xref;
		final int score;
		final List<String> qvars;
		
		public Formula (String forTag, String xref, int score, List<String> qvars) {
			this.forTag = forTag;
			this.xref = xref;
			this.score = score;
			this.qvars = qvars;
		}
		
		public int compareTo(Formula formula) {
			if (this.score > formula.score) {
				return 1;
			} else if (this.score < formula.score) {
				return -1;
			} else {
				return 0;
			}
		}
	}
	
	public ArrayList<ArrayList<String>> getDataAsArray() {
		return data;
	}
	
	public ArrayList<ArrayList<String>> getFormulaAsArray() {
		return formula;
	}
	
	
	
	private void getData() throws IOException {
		//REQUIREMENTS:DATA:1000 results max; 
		//RESULT ID should be this format: QUERYID_ID#
		//FORMULA ID should be this format: QUERYID_ID#_FORMULAID# and/or contain RESULTID
		//QVAR ID should be this format: QUERYID_ID#_FORMULAID#_QVARID# and/or contain FORMULA_ID
		
		//need to generate RUNTIME, RUNTAG (id #), RUN_TYPE (automatic)
		
		
		
		//DATA FORMAT:
		//input_file: query id, result id, filename, score, runtag (group-id_run_id), runtime??
		BufferedReader br = null;
		BufferedReader br2 = null;
		String line = "";
		String csvSplit = ",";
		br = new BufferedReader(new FileReader(input_file));
		while ((line = br.readLine()) != null) {
			// use comma as separator
			data.add(new ArrayList<String>(Arrays.asList(line.split(csvSplit))));
		}			
		if (br != null) {
			br.close();
		}
		//FORMAT: sort, add rank
		//ranked: query id, result id, rank, filename, score, runtag (group-id_run_id), runtime??
		Collections.sort(data, new ScoreComparator()); //sort by score
		int i=1; //rank starts at 1
		for (ArrayList<String> temp : data) {
			temp.add(2, Integer.toString(i)); //add rank
			i++;
		}
		
		//FORMULA FORMAT:
		//formula: formula id, for, xref, score, qvar(id, for, xref), qvar...?
		br2 = new BufferedReader(new FileReader(formula_file));
		line = "";
		while ((line = br2.readLine()) != null) {
			// use comma as separator
			formula.add(new ArrayList<String>(Arrays.asList(line.split(csvSplit))));
			line = br2.readLine();
		}			
		if (br2 != null) {
			br2.close();
		}
		//FORMAT: sort by formula id and score
		Collections.sort(formula, new FormulaComparator()); //sort by query id and score
	}
	
	private class ScoreComparator implements Comparator<ArrayList<String>> {
		//DATA FORMAT:
		//input_file: query id, result id, filename, score, runtag (group-id_run_id), runtime??
		public int compare (ArrayList<String> y1, ArrayList<String> y2) {
			String y1_ID = y1.get(0);
			String y2_ID = y2.get(0);
			int y1_SCORE = Integer.parseInt(y1.get(3));
			int y2_SCORE = Integer.parseInt(y2.get(3));
			if (y1_ID.compareTo(y2_ID)==1) { //sort by query id with lowest at top
				return 1;
			}else if (y1_ID.compareTo(y2_ID)==-1) {
				return -1;
			}else { //sort by score
				if (y1_SCORE > y2_SCORE) { //sort by score with highest at top
					return -1;
				}else if (y1_SCORE < y2_SCORE) {
					return 1;
				}else {
					return 0;
				}
			}
		}
	}
	
	private class FormulaComparator implements Comparator<ArrayList<String>> {
		//FORMULA FORMAT:
		//formula: formula id, for, xref, score, qvar(id, for, xref), qvar...?
		public int compare (ArrayList<String> y1, ArrayList<String> y2) {
			String y1_ID = y1.get(0);
			String y2_ID = y2.get(0);
			int y1_SCORE = Integer.parseInt(y1.get(3));
			int y2_SCORE = Integer.parseInt(y2.get(3));
			if (y1_ID.compareTo(y2_ID)==1) { //sort by formula id with lowest at top
				return 1;
			}else if (y1_ID.compareTo(y2_ID)==-1) {
				return -1;
			}else { //sort by score
				if (y1_SCORE > y2_SCORE) { //sort by highest score at top
					return -1;
				}else if (y1_SCORE < y2_SCORE) {
					return 1;
				}else {
					return 0;
				}
			}
		}
	}
	
	

}
