package de.tuberlin.dima.schubotz.fse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
 
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.lang.StringUtils;

public class ResultOutput {
	String filename;
	String input_file;
	String formula_file;
	String output_path;
	ArrayList<ArrayList<String>> data;
	ArrayList<ArrayList<String>> formula; 
	
	public ResultOutput(String[] args) {
		filename = "group-id.ext";
		input_file = (args.length > 0 ? args[0] : "/home/jjl4/Documents/input.csv");
		output_path = (args.length > 1 ? args[1] : "/home/jjl4/Documents/");
		formula_file = (args.length > 2 ? args[2] : "/home/jjl4/Documents/formula.csv");
		data = new ArrayList<ArrayList<String>>(); //1000 results max
	}
	
	public void outputSimple () throws IOException {	
		ArrayList<String> writing = null;
		String towrite = null;
		PrintWriter bw = new PrintWriter(new FileWriter(output_path.concat((filename))));
		//OUTPUT FORMAT:
		//query id, 1, filename, rank, score, runtag (group-id_run_id)
		while(!data.isEmpty()) {
			writing = data.remove(0);
			writing.subList(4,writing.size()).clear(); //clear all entries after runtag
			writing.add(1, "1"); //add 1
			towrite = StringUtils.join(writing," ");
			bw.println(towrite);
		}
		if (bw != null) {
			bw.close();
		}
		
	}
	
	public void outputXML () {
		try {
			DocumentBuilderFactory outFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = outFactory.newDocumentBuilder();
			
		}catch (ParserConfigurationException pfe) {
			pfe.printStackTrace();
		}catch (TransformerException te) {
			te.printStackTrace();
		}
	}
		
	public void getData () throws IOException {
		//DATA FORMAT:
		//input_file: query id, filename, score, runtag (group-id_run_id), runtime??
		//formula: query id, id, for, xref, score, qvar(for, xref), qvar...?
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
		Collections.sort(data, new ScoreComparator()); //sort by score
		int i=0;
		for (ArrayList<String> temp : data) {
			temp.add(2, Integer.toString(i)); //add rank
			i++;
		}
		br2 = new BufferedReader(new FileReader(formula_file));
		while ((line = br2.readLine()) != null) {
			// use comma as separator
			formula.add(new ArrayList<String>(Arrays.asList(line.split(csvSplit))));
		}			
		if (br != null) {
			br2.close();
		}
	}
	
	private class ScoreComparator implements Comparator<ArrayList<String>> {
		public int compare (ArrayList<String> y1, ArrayList<String> y2) {
			int x = Integer.parseInt(y1.get(2));
			int y = Integer.parseInt(y2.get(2));
			if (x < y) { //sort by greatest score at top
				return 1;
			}else if (x > y) {
				return -1;
			}else {
				return 0;
			}
		}
	}
	
	public static void main(String[] args) {
		ResultOutput run = new ResultOutput(args);
		try {
			run.getData();
			run.outputSimple();
		}catch (IOException e) {
			System.out.printf("IOException: %s%n",e);
		}
	}
}