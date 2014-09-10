package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.types.RawSearchPattern;
import de.tuberlin.dima.schubotz.fse.types.SearchPattern;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper.NdLst;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by mas9 on 9/11/14.
 */
public class UltimatQueryTest extends TestCase {
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
        r.setQuery(SearchPattern.fields.xQuery,cmml.getXQuery());
        r.setQuery(SearchPattern.fields.xCdQuery,cmml.toStrictCmml().getXQuery());
        r.setQuery(SearchPattern.fields.xDtQuery,cmml.toDataCmml().getXQuery());
        r.setNamedField(SearchPattern.fields.tokens,cmml.getElements());
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
        System.out.println(XMLHelper.CompactForm(n));
    }
    public void testCMMLGen() throws Exception{
        for (RawSearchPattern p : getNamedPatterns()) {
            CMMLInfo cmml = new CMMLInfo(XMLHelper.printDocument(p.getMath()));
            dPring(cmml);
            dPring(cmml.toStrictCmml());
            dPring(cmml.abstract2CDs());
            dPring(cmml.abstract2DTs());
            //System.out.println(cmml.toStrictCmml().getXQueryString());
        }

    }
    public void testMyTest() throws Exception {
        for (SearchPattern searchPattern : processSearchPatterns()) {
            System.out.println(searchPattern.getNamedField(SearchPattern.fields.formulaID));
        }
    }

}
