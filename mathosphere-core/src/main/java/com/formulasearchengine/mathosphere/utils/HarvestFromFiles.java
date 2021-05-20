package com.formulasearchengine.mathosphere.utils;

import com.formulasearchengine.mathmltools.helper.XMLHelper;
import com.formulasearchengine.mathmltools.io.XmlDocumentReader;
import org.apache.commons.cli.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Moritz on 13.08.2015. <p> Creates a harvest file from a folder
 */
@SuppressWarnings("AccessStaticViaInstance")
public class HarvestFromFiles {

  public static void main(String[] args) {
    Options options = new Options();
    Option help = new Option("help", "print this message");

    Option dataSource = OptionBuilder.withArgName("file")
            .hasArg()
            .isRequired()
            .withDescription("use given file for data source")
            .withLongOpt("datasource")
            .create("d");
    Option resultSink = OptionBuilder.withArgName("file")
            .hasArg()
            .withDescription("specify file for the output")
            .withLongOpt("output")
            .create("o");
    options.addOption(dataSource)
            .addOption(resultSink)
            .addOption(help)
            .addOption("i", "ignoreUnderscores", false, "Ignores everything that comes after the last underscore in the filename.");
    CommandLineParser parser = new GnuParser();
    try {
      CommandLine line = parser.parse(options, args);
      if (line.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar FILENAME.jar", options);
      } else {
        processDocs(line);
      }
    } catch (ParseException exp) {
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
    }
  }

  private static void processDocs(CommandLine line) {
    File folder = new File(line.getOptionValue("datasource"));
    Document doc = XMLHelper.getNewDocument();
    Element har = doc.createElementNS("http://search.mathweb.org/ns", "harvest");
    for (final File fileEntry : folder.listFiles()) {
      if (!fileEntry.isDirectory()) {
        String fname = fileEntry.getName();
        if (fname.endsWith(".xml")) {
          if (line.hasOption("ignoreUnderscores")) {
            fname = fname.split("_")[0];
          }
          fname = fname.replaceFirst("\\.xml", "");
          try {
            addFile(doc, har, fileEntry, fname);
          } catch (ParserConfigurationException | IOException | SAXException e) {
            System.out.println("Can not process input " + fname);
            e.printStackTrace();
          }
        }
      }
    }
    doc.appendChild(har);
    try {
      writeOutput(line, doc);
    } catch (TransformerException | IOException e) {
      System.out.println("Can not write output.");
      e.printStackTrace();
    }
  }

  private static void addFile(Document doc, Element har, File fileEntry, String fname) throws ParserConfigurationException, IOException, SAXException {
    DocumentBuilderFactory dbf = XmlDocumentReader.getStandardDocumentBuilderFactory(false);
    DocumentBuilder builder = dbf.newDocumentBuilder();
    Document xContent = builder.parse(fileEntry.getCanonicalFile());
    Node copiedXDocument = doc.importNode(xContent.getDocumentElement(), true);
    Node mwsExpr = doc.createElementNS("http://search.mathweb.org/ns", "expr");
    Attr url = doc.createAttribute("url");
    url.setValue(fname);
    mwsExpr.getAttributes().setNamedItem(url);
    mwsExpr.appendChild(copiedXDocument);
    har.appendChild(mwsExpr);
  }

  private static void writeOutput(CommandLine line, Document doc) throws TransformerException, IOException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = null;
    transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    StringWriter sw = new StringWriter();

    transformer.transform(new DOMSource(doc), new StreamResult(sw));
    if (!line.hasOption("output")) {
      System.out.println(sw.toString());
    } else {
      File f = new File(line.getOptionValue("output"));
      f.createNewFile();
      FileOutputStream fos = new FileOutputStream(f);
      fos.write(sw.toString().getBytes());
    }
  }
}
