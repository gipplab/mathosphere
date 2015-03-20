package de.tuberlin.dima.schubotz.utils;

import com.google.common.collect.Multiset;
import de.tuberlin.dima.schubotz.fse.types.RawSearchPattern;
import de.tuberlin.dima.schubotz.fse.types.SearchPattern;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper.NdLst;
import junit.framework.TestCase;
import net.sf.saxon.s9api.XQueryExecutable;
import org.apache.flink.api.java.tuple.Tuple5;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.tuberlin.dima.schubotz.utils.TestUtils.getConnection;

public class UltimatQueryTest extends TestCase {
	final List<String> tblHeight = Arrays.asList( "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\TX\\B", "\\TX\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\TY\\BY", "\\T\\BX", "\\T\\BX", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\BX", "\\T\\BX", "\\TZ\\BY", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B", "\\T\\B" );
	final List<String> texStart = new ArrayList<>( 55 );
	final String ltxTblHead = "\\begin{longtable}{|c|c||c|c|c|c|c||c|c|c|c|c|c|}\n" +
		"\\caption{Query data. This table lists the query IDs, queries, $0-4$ lists\n" +
		"the relevance ranking, and columns A through E represent the\n" +
		"number Content Directory matches, number of data type matches,  number of exact matches,\n" +
		"average coverage, and number of formulae\n" +
		"respectively.}\\\\\n" +
		"\\hline\n" +
		"ID & query & 0 & 1 & 2 & 3 & 4 & A & B & C & D & E \\\\\\hline";
	final String ltxTblFooter = "\\end{longtable}";
	final String search = "1024k^{1}0-2560k^{9}+3840k^{8}-4480k^{7}+4096k^{6}-2944k^{5}+1696k^{4}-760k^{3%\n" +
		"                            }+236k^{2}-40k";
	final String replace = "\\begin{array}{ccc} \n" +
		"\\T 1024k^{1}0-2560k^{9}+3840k^{8}\\\\\n" +
		"-4480k^{7}+4096k^{6}-2944k^{5}\\\\\n" +
		"+1696k^{4}-760k^{3}+236k^{2}-40k\\B\n" +
		"\\end{array} \\footnote{We added linebreaks to the query.} ";
	public void getTableStart() throws Exception {
		Integer i =0;
		Document doc = XMLHelper.String2Doc(getQueryFile(), true);
		final NdLst lst = new NdLst(doc.getElementsByTagNameNS("*", "annotation"));
		for (Node node : lst) {
			texStart.add( i, "$"+node.getTextContent().trim().replace( search,replace )+"$" );
			++i;
		}
	}
	private String getQueryFile() throws IOException {
        return TestUtils.getFileContents("de/tuberlin/dima/schubotz/fse/fQuery.xml");
    }
    public void testallQueries() throws Exception {
        Document doc = XMLHelper.String2Doc(getQueryFile(), true);
        final NdLst lst = new NdLst(doc.getElementsByTagNameNS("*", "annotation"));
        for (Node node : lst) {
            System.out.println("$$"+node.getTextContent().trim()+"$$");
        }
    }
    private NdLst getTests() throws Exception {
        Document doc = XMLHelper.String2Doc(getQueryFile(), true);
        return new XMLHelper.NdLst(doc.getElementsByTagName("topic"));
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

    public SearchPattern processSearchPattern(RawSearchPattern p) throws TransformerException, IOException, ParserConfigurationException {
        SearchPattern r = new SearchPattern(p);
        CMMLInfo cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
	    r.setNamedField(SearchPattern.fields.tokens,cmml.getElements());
        r.setQuery(SearchPattern.fields.xQuery,cmml.getXQuery());
	    cmml.toStrictCmml().abstract2CDs();
	    r.setQuery(SearchPattern.fields.xCdQuery,cmml.getXQuery());
	    //reset cmml
	    cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
	    cmml.abstract2DTs();
        r.setQuery(SearchPattern.fields.xDtQuery,cmml.getXQuery());

        return r;
    }
	public void executePattern(SearchPattern p) throws IOException, SQLException, ParserConfigurationException, XPathExpressionException, SAXException, TransformerException {
		Integer num = p.getNamedField( SearchPattern.fields.queryNumber );
		String queryFormulaId = p.getNamedField( SearchPattern.fields.formulaID );
		XQueryExecutable xQuery = p.getNamedField( SearchPattern.fields.xQuery );
		XQueryExecutable xCdQuery = p.getNamedField( SearchPattern.fields.xCdQuery );
		XQueryExecutable xDtQuery = p.getNamedField( SearchPattern.fields.xDtQuery );
		Multiset<String> queryTokes = p.getNamedField( SearchPattern.fields.tokens );
		 String SQL = "INSERT INTO `results` (" +
			 "`queryNum`," + //1
			 " `patternName`," + //2
			 " `queryFormulaId`, " + //3
			 "`cdMatch`, " + //4
			 "`dataMatch`, " + //5
			 "`matchDepth`, " + //6
			 "`queryCoverage`, " + //7
			 "`isFormulae` ," + //8
			 "`vote`," + //9
			 "`pageID`) " + //10
			 "VALUES (?,?,?,?,?,?,?,?,?,?);";
		PreparedStatement stmt = getConnection().prepareStatement( SQL );
		// pageId (0) ,vote (1),sectionname (2=0),formula_name (3),value(4)
		ArrayList<Tuple5<String, Integer, String, String, String>> data		= getFormulaForQuery( num );
		for ( Tuple5<String, Integer, String, String, String> e : data ) {
			stmt.setInt( 1, num);
			stmt.setString( 2, e.f3 );
			stmt.setString( 3,queryFormulaId);
			stmt.setInt( 9, e.f1 );
			stmt.setString( 10,e.f0 );
			CMMLInfo cmml = new CMMLInfo( e.f4 );
			Boolean isFormulae = cmml.isEquation();
			stmt.setBoolean( 8,isFormulae );
			Integer depth = cmml.getDepth( xQuery );
			if(depth==null){
				stmt.setNull( 6, Types.INTEGER );
			} else {
				stmt.setInt( 6,depth );
			}
			Double coverage = cmml.getCoverage( queryTokes );
			stmt.setDouble( 7, coverage );
			CMMLInfo cDT = cmml.clone();
			cmml.toStrictCmml().abstract2CDs();
			final Boolean cdMatch = cmml.isMatch( xCdQuery );
			stmt.setBoolean( 4,cdMatch );
			cDT.abstract2DTs();
			final Boolean dataMatch = cDT.isMatch( xDtQuery );
			stmt.setBoolean( 5,dataMatch );
			stmt.execute();

		}


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

	public void testCMMLGen2() throws Exception{
		Statement statement = getConnection().createStatement();
		statement.execute( "TRUNCATE TABLE results;" );
		for (RawSearchPattern p : getNamedPatterns()) {
			SearchPattern a = processSearchPattern( p );
			executePattern( a );
		}
	}
	public void testCMMLGen() throws Exception{
		getTableStart();
		Integer i =0;
        for (RawSearchPattern p : getNamedPatterns()) {
            CMMLInfo cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
	        System.out.println(tblHeight.get( i ));
	        System.out.println( "Info for " + p.getNamedField(  RawSearchPattern.fields.formulaID )+ p.getNamedField( RawSearchPattern.fields.queryNumber ));
			System.out.println( texStart.get( i )  );
	        System.out.println(cmml.getElements());
	        System.out.println( cmml.isEquation());
	        TestUtils.dPring( cmml );
            TestUtils.dPring( cmml.toStrictCmml() );
            TestUtils.dPring( cmml.abstract2CDs() );
	        cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
            TestUtils.dPring( cmml.abstract2DTs() );

	        System.out.println("\n");
	        //System.out.println(cmml.toStrictCmml().getXQueryString());
	        i++;
        }

    }

	public ArrayList<Tuple5<String, Integer, String, String, String>> getFormulaForQuery (Integer qId) throws IOException, SQLException {
		ArrayList<Tuple5<String, Integer, String, String, String>> out = new ArrayList<>();
		Connection cn = getConnection();
		PreparedStatement p = cn.prepareStatement( "select pageId,vote,sectionname,formula_name,value\n" +
			" from referee_votes \n" +
			" join formulae_fulltext \n" +
			" on pageId = sectionname\n" +
			" WHERE qId = ?;" );
		p.setInt( 1,qId );
		p.execute();
		ResultSet r = p.getResultSet();
		while ( r.next() ){
			out.add( new Tuple5<>(
				r.getString( 1 ),
				r.getInt( 2 ),
				r.getString( 3 ),
				r.getString( 4 ),
				r.getString( 5 )) );
		}
		return out;
	}
    public void testMyTest() throws Exception {
        for (SearchPattern searchPattern : processSearchPatterns()) {
            System.out.println(searchPattern.getNamedField(SearchPattern.fields.queryNumber).toString()+searchPattern.getNamedField(SearchPattern.fields.formulaID));
        }
    }
	private String getRelevanceRankings(Integer qID, String fID) throws IOException, SQLException {
		String out = "";
		String sql = "SELECT count(*) FROM results where queryNum = ? AND queryFormulaId= ? and vote = ?";
		PreparedStatement stmt = getConnection().prepareStatement( sql );
		stmt.setInt( 1, qID );
		stmt.setString( 2, fID );
		for ( int i = 0; i <=4 ; i++ ) {
			stmt.setInt( 3, i );
			stmt.execute();
			if (stmt.getResultSet().next()){
				out += stmt.getResultSet().getInt( 1 );
			} else {
				out += 0;
			}
			out += " & ";
		}
		sql = "SELECT sum(cdMatch), sum(dataMatch),count(matchDepth),avg(queryCoverage),sum(isFormulae) FROM results where queryNum = ? AND queryFormulaId= ?";
		stmt = getConnection().prepareStatement( sql );
		stmt.setInt( 1, qID );
		stmt.setString( 2, fID );
		stmt.execute();
		if (stmt.getResultSet().next()){
			out += stmt.getResultSet().getInt( 1 ) + " & ";
			out += stmt.getResultSet().getInt( 2 ) + " & ";
			out += stmt.getResultSet().getInt( 3 ) + " & ";
			out += String.format("%1.2f",stmt.getResultSet().getDouble( 4 ) ) + " & ";
			out += stmt.getResultSet().getInt( 5 ) + " & ";
		} else {
			out += " &  &  &  &  & ";
		}
		out= out.substring( 0,out.length()-2 );
		return out;
	}
	public void testGetRelevanceRankings() throws IOException, SQLException {
		System.out.println(getRelevanceRankings( 1, "f1.0" ));
	}

	public void testPrintLaTeX() throws Exception{
		getTableStart();
		Integer i =0;
		System.out.println(ltxTblHead);
		for (RawSearchPattern p : getNamedPatterns()) {
			String fId = p.getNamedField(  RawSearchPattern.fields.formulaID );
			Integer qId = p.getNamedField( RawSearchPattern.fields.queryNumber );

			String s= tblHeight.get(i) +" "+ qId+fId+" & "+texStart.get( i ) + " & " +
				getRelevanceRankings( qId,fId ) + "\\\\\\hline";

			System.out.println(s);
			//System.out.println("\n");
			//System.out.println(cmml.toStrictCmml().getXQueryString());
			i++;
		}
		System.out.println(ltxTblFooter);
	}

	public void testPrintLaTeXTrees() throws Exception{
		getTableStart();
		Integer i =0;
		String s = "";
		for (RawSearchPattern p : getNamedPatterns()) {
			String fId = p.getNamedField(  RawSearchPattern.fields.formulaID );
			Integer qId = p.getNamedField( RawSearchPattern.fields.queryNumber );
			CMMLInfo cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
			s += "\\subsection{Info for pattern" + qId+fId + "}";
			s +="\n";
			s += "Rendering:"+texStart.get( i )  ;
			s +="\n";
			s +="Elements:\\begin{verbatim}\n"+cmml.getElements()+"\n\\end{verbatim}";
			s +="\n";
			s += "Is equation:"+cmml.isEquation();
			s +="\n";
			s +=TestUtils.dPring( cmml );
			s +="\n";
			s +=TestUtils.dPring( cmml.toStrictCmml() );
			s +="\n";
			s +=TestUtils.dPring( cmml.abstract2CDs() );
			s +="\n";
			cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
			s +=TestUtils.dPring( cmml.abstract2DTs() );

			s +="\n";
			//s +=cmml.toStrictCmml().getXQueryString());
			i++;
			if ( i > 100) break;
	}
		System.out.println(s);
}
}