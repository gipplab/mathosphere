package de.tuberlin.dima.schubotz.fse;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ResultOutput {
	String filename;
	String input_file;
	String formula_file;
	String output_path;
	
	ArrayList<ArrayList<String>> data;
	ArrayList<ArrayList<String>> formula;
	
	private static int resultID = 0;
	private static int hitID = 0;
	private static int formulaID = 0;
	
	public ResultOutput(String[] args, ResultData formattedData) {
		filename = "group-id.ext";
		output_path = "/home/jjl4/Documents/";
		data = formattedData.getDataAsArray();
		formula = formattedData.getFormulaAsArray();
	}
	
	public void outputSimple () throws IOException {	
		ArrayList<String> writing = null;
		String towrite = null;
		PrintWriter bw = new PrintWriter(new FileWriter(output_path.concat((filename))));
		//ranked: query id, result id, rank, filename, f3, runtag (group-id_run_id), runtime??
		//OUTPUT FORMAT:
		//query id, 1, filename, rank, f3, runtag (group-id_run_id)
		while(!data.isEmpty()) {
			writing = data.remove(0);//get ArrayList<String> from data
			writing.subList(4,writing.size()).clear(); //clear all entries after runtag
			writing.add(1, "1"); //add 1
			writing.remove(1); //remove result id
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
			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true);
			
			//construct root nodes
			Element rootElement = doc.createElement("results");
			rootElement.setAttribute("xmlns", "http://ntcir−math.nii.ac.jp/");
			
			Element runElement = doc.createElement("run");
			runElement.setAttribute("runtag", "FOO"); //TODO SET RUNTAG
			runElement.setAttribute("runtime", "FOO"); //TODO SET RUNTIME
			runElement.setAttribute("run_type", "FOO"); //TODO SET RUN_TYPE
			
			
			//construct document given inputdata
			//ranked: query id, result id, rank, filename, f3, runtag (group-id_run_id), runtime??
			//formula: formula id(contains query_id), for, f1, f3, qvar(id, for, f1), qvar...?
			doc.appendChild(rootElement);
			//TODO working with one run
			rootElement.appendChild(runElement);
			
			String curQueryID = data.get(0).get(0); //first query ID
			String currentID = data.get(0).get(1); //first result ID
			String curFormulaID = formula.get(0).get(0); //first formula ID
			Node formulaElement = getFormula(doc, formula.get(0)); //first formula
			ArrayList<String> formulaTemp = formula.remove(0);
			Node hitElement = getHit(doc, data.get(0)); //first result
			Node resultElement = getResult(doc, data.get(0));
			runElement.appendChild(resultElement);//return initial result node based on query_id
			
			for (ArrayList<String> dataTemp : data) {
				if (!currentID.startsWith(curQueryID)) { //if not part of last query
					resultElement = getResult(doc, dataTemp);//add new query tag
					runElement.appendChild(resultElement);
					curQueryID = dataTemp.get(0); //get new current query id
				}	
				
				resultElement.appendChild(hitElement); //add hit tag
				while(curFormulaID.startsWith(currentID)) { //while in this resultid
					hitElement.appendChild(formulaElement); //add formula tag
					formulaTemp = formula.remove(0); //get next formula
					formulaElement = getFormula(doc, formulaTemp);
					curFormulaID = formulaTemp.get(0); //get next formula id
				}
				hitElement = getHit(doc, dataTemp); //get new hit
				curFormulaID = formulaTemp.get(0); //get next formula
				formulaElement = getFormula(doc, formulaTemp);
				currentID = dataTemp.get(0); //get new current result ID
			}
			
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("/home/jjl4/Documents/output.xml"));
			
			transformer.transform(source, result);
			
		}catch (ParserConfigurationException pfe) {
			pfe.printStackTrace();
		}catch (TransformerException te) {
			te.printStackTrace();
		}
	}
	
	private Node getResult(Document doc, ArrayList<String> resultData) {
		//ranked: query id, result id, rank, filename, f3, runtag (group-id_run_id), runtime??
		//formula: formula id, for, f1, f3, qvar(id, for, f1), qvar...?
		Element result = doc.createElement("result");
		result.setAttribute("id", ResultOutput.getID("result"));
		result.setAttribute("for", resultData.get(0)); //set query id
		result.setAttribute("runtime", "FOO"); //TODO set RUNTIME
		return result;
	}
	
	private Node getHit(Document doc, ArrayList<String> resultData) {
		//ranked: query id, result id, rank, filename, f3, runtag (group-id_run_id), runtime??
		Element hit = doc.createElement("hit");
		hit.setAttribute("id", ResultOutput.getID("hit"));
		hit.setAttribute("f1", resultData.get(3)); //set filename
		hit.setAttribute("f3", resultData.get(4)); //set f3
		hit.setAttribute("rank", resultData.get(1)); //set rank
		return hit;
	}
	
	private Node getFormula(Document doc, ArrayList<String> formulaData) {
		//formula: formula id, for, f1, f3, qvar(id, for, f1), qvar...?
		Element formula = doc.createElement("formula");
		formula.setAttribute("id", formulaData.get(0)); //set id
		formula.setAttribute("for", formulaData.get(1)); //set for
		formula.setAttribute("f1", formulaData.get(2)); //set f1
		formula.setAttribute("f3", formulaData.get(3)); //set f3
		//TODO: ADD QVAR TREE
		return formula;
	}
	
	private static String getID(String tag) {
		if (tag.equals("result")) {
			return Integer.toString(resultID++);
		}else if (tag.equals("hit")) {
			return Integer.toString(hitID++);
		}else {
			return Integer.toString(formulaID++);
		}
	}
			
	public static void main(String[] args) {
		
		ResultData formattedData = new ResultData();
		ResultOutput run = new ResultOutput(args, formattedData);
		System.out.println("data formatted");
		run.outputXML();
		System.out.println("xml created");
	}
}