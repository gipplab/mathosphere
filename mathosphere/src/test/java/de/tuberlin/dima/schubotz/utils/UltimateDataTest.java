package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.ArxivCleaner;
import de.tuberlin.dima.schubotz.fse.types.RawDataTuple;
import de.tuberlin.dima.schubotz.fse.types.RawSearchPattern;
import de.tuberlin.dima.schubotz.fse.types.SearchPattern;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper.NdLst;
import junit.framework.TestCase;
import org.apache.flink.api.java.tuple.Tuple3;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class UltimateDataTest extends TestCase {

	private static final int LIMIT = Integer.MAX_VALUE;

	public Map<String, String> getDocs () throws IOException {
        final Collection<String> files = new ArrayList<>();
        try (final InputStream is =
                     getClass().getClassLoader().getResourceAsStream("de/tuberlin/dima/schubotz/gold.xml")) {
            final Scanner s = new Scanner(is, "UTF-8");
            s.useDelimiter( ArxivCleaner.DELIM );
            int i = 0;
            while (s.hasNext()) {
                final String t = s.next().trim();
                if (!t.isEmpty()) {
                    files.add(t);
                }
                i++;
                if (i >= LIMIT) {
                    break;
                }
            }
        }

	    final Map<String,String> docs = new HashMap<>(  );
        final GenericCollector<RawDataTuple> rawDataCollector = new GenericCollector<>();
        final ArxivCleaner cleaner = new ArxivCleaner();
        for (final String file : files) {
            cleaner.flatMap(file, rawDataCollector);
	    }
        final Iterable<RawDataTuple> dataList = rawDataCollector.getDatalist();
        for (final RawDataTuple tuple : dataList) {
            docs.put(tuple.getNamedField(RawDataTuple.fields.ID), tuple.getNamedField(RawDataTuple.fields.rawData));
        }
	    return docs;
    }
	public Map<String,Node> getEquationsFromDoc(String s) throws IOException, ParserConfigurationException {
		final Document doc = XMLHelper.String2Doc(s, true);
		final Map<String, Node> out = new HashMap<>();
		final NdLst lst = new NdLst( doc.getElementsByTagNameNS( "*", "math" ) );
		for ( final Node node : lst ) {
			final String id = node.getAttributes().getNamedItem( "id" ).getTextContent();
			out.put( id, node );
		}
		return out;
	}

	public void  testWriteToDB() throws TransformerException, SQLException, IOException, ParserConfigurationException {
		final Connection cn = TestUtils.getConnection();
        try (Statement stmt = cn.createStatement()) {
            stmt.execute("truncate table formulae_fulltext");
        }
        try (PreparedStatement preparedStmp = cn.prepareStatement(
                "insert into formulae_fulltext (sectionname, formula_name,  value  ) values (?,?,?)")) {
            for (final Map.Entry<String, String> docs : getDocs().entrySet()) {
                for (final Map.Entry<String, Node> entry : getEquationsFromDoc(docs.getValue()).entrySet()) {
                    preparedStmp.setString(1, docs.getKey());
                    preparedStmp.setString(2, entry.getKey());
                    preparedStmp.setString(3, XMLHelper.printDocument(entry.getValue()));
                    preparedStmp.execute();
                }
            }
        }
	}
	public Collection<Tuple3<String, String, Node>> getEquations() throws IOException, ParserConfigurationException {
		final Collection<Tuple3<String, String, Node>> out = new ArrayList<>(  );
		for ( final Map.Entry<String, String> docs : getDocs().entrySet() ) {
			for ( final Map.Entry<String, Node> entry : getEquationsFromDoc( docs.getValue() ).entrySet() ) {
				out.add( new Tuple3<>(docs.getKey(),entry.getKey(),entry.getValue()));
			}
		}
		return out;
	}
	public void testConvertToForms() throws TransformerException, SQLException, IOException, ParserConfigurationException {
		final Connection cn = TestUtils.getConnection();
        try (PreparedStatement preparedStmp = cn.prepareStatement(
                "insert ignore into formulae_fulltext (sectionname, formula_name,  value  ) values (?,?,?)")) {
            for (final Tuple3<String, String, Node> m : getEquations()) {
                preparedStmp.setString(1, m.f0);
                preparedStmp.setString(2, m.f1);
                preparedStmp.setString(3, XMLHelper.printDocument(m.f2));
                preparedStmp.execute();
            }
        }

	}



	public void testConvertDoc() throws Exception {
		Integer i =0;
		for ( Map.Entry<String, String> docs : getDocs().entrySet() ) {
			for ( Map.Entry<String, Node> entry : getEquationsFromDoc( docs.getValue() ).entrySet() ) {
				i++;
			}
		}
		assertEquals( (Integer) 49662,i );
	}


    public Collection<RawSearchPattern> getNamedPatterns() throws Exception {
        int i =1;
        ArrayList<RawSearchPattern> out = new ArrayList<>();
        for (Node node : getTests()) {
            NdLst children = new NdLst( node.getChildNodes() );
            String num = children.getFirstChild("num").getTextContent();
            Integer qId = Integer.parseInt(num.replaceAll("NTCIR11-Math-", ""));
            NdLst query = new NdLst( children.getFirstChild("query").getChildNodes() );
            for (Node formula : query.filter("formula")) {
                String fid = formula.getAttributes().getNamedItem("id").getNodeValue();
                Node math = (new NdLst(formula.getChildNodes())).item(0);
                out.add( new RawSearchPattern(qId,fid,math));
            }
        }
        return out;
    }

	private Node[] getTests () {
		return new Node[0];
	}

	public SearchPattern processSearchPattern(RawSearchPattern p) throws TransformerException, IOException, ParserConfigurationException {
        SearchPattern r = new SearchPattern(p);
        CMMLInfo cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
	    r.setNamedField(SearchPattern.fields.tokens,cmml.getElements());
        r.setQuery(SearchPattern.fields.xQuery,cmml.getXQuery());
	    cmml.toStrictCmml().abstract2CDs();
	    //reset cmml
	    cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
	    cmml.abstract2DTs();
        r.setQuery(SearchPattern.fields.xDtQuery,cmml.getXQuery());

        return r;
    }

    public Collection<SearchPattern> processSearchPatterns(Collection<RawSearchPattern> in) throws ParserConfigurationException, TransformerException, IOException {
        ArrayList<SearchPattern> out = new ArrayList<>();
        for (RawSearchPattern rawSearchPattern : in) {
            out.add(processSearchPattern(rawSearchPattern));
        }
        return out;
    }

    public Collection<SearchPattern> processSearchPatterns() throws Exception {
        return processSearchPatterns(getNamedPatterns());
    }
    private void dPring(Node n){
	    String s = XMLHelper.CompactForm( n );
	    s=s.replace( "#document[math[semantics[", "\\Tree[ " );
	    s=s.replace( ";"," " ).replace( "]" ," ] ").replace( "[" ," [.");
	    s=s.substring( 0,s.length()-2 );
	    System.out.println( s );
    }
    public void testCMMLGen() throws Exception{
        for (RawSearchPattern p : getNamedPatterns()) {
            CMMLInfo cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
	        System.out.println( "Info for " + p.getNamedField( RawSearchPattern.fields.formulaID )+ p.getNamedField( RawSearchPattern.fields.queryNumber ));
	        System.out.println(cmml.getElements());
	        System.out.println( cmml.isEquation());
	        dPring( cmml );
            dPring(cmml.toStrictCmml());
            dPring(cmml.abstract2CDs());
	        cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
            dPring(cmml.abstract2DTs());

	        System.out.println("\n");
	        //System.out.println(cmml.toStrictCmml().getXQueryString());
        }

    }
    public void testMyTest() throws Exception {
        for (SearchPattern searchPattern : processSearchPatterns()) {
            System.out.println(searchPattern.getNamedField(SearchPattern.fields.queryNumber).toString()+searchPattern.getNamedField(SearchPattern.fields.formulaID));
        }
    }

}
