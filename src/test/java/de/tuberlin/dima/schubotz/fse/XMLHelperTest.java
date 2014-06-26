package de.tuberlin.dima.schubotz.fse;

import com.google.common.collect.Multiset;
import eu.stratosphere.api.java.tuple.Tuple2;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

import static de.tuberlin.dima.schubotz.fse.XMLHelper.getIdentifiersFromCmml;

public class XMLHelperTest extends TestCase {

    public void testCompactForm() throws Exception {

    }
	public void testCompareNode() throws Exception {
		String testFile1 = TestUtils.getTestQueryString();
		Document doc = XMLHelper.String2Doc(testFile1, false);
		NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math");//"/topics/topic/query/formula/math" topic/query/formula
		int count = MathMLElements.getLength();
		if (count > 0) {
			assertFalse( XMLHelper.compareNode( MathMLElements.item(  1 ), MathMLElements.item( 2 ) ,true,null));
			assertTrue( XMLHelper.compareNode( MathMLElements.item(  1 ), MathMLElements.item( 1 ) ,true,null));
		} else {
			fail("no math element  ");
		}
	}

    public void testGetMMLLeaves() throws Exception {
        String testFile1 = TestUtils.getTestQueryString();
        Document doc = XMLHelper.String2Doc(testFile1, false);
        NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math");//"/topics/topic/query/formula/math" topic/query/formula
        int count = MathMLElements.getLength();
        if (count > 0) {
            ArrayList<Tuple2<String, String>> mmlLeaves = XMLHelper.getMMLLeaves(MathMLElements.item(2));
            for (Tuple2<String, String> mmlLeaf : mmlLeaves) {
                System.out.println(mmlLeaf.toString());
            }
        }

    }

    public void testGetIdentifiersFromCmml() throws Exception {
        String testFile1 = TestUtils.getTestQueryString();
        Document doc = XMLHelper.String2Doc(testFile1, false);
        NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math/semantics/*[1]");//"/topics/topic/query/formula/math" topic/query/formula
        int count = MathMLElements.getLength();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Multiset<String> identifiersFromCmml = getIdentifiersFromCmml(MathMLElements.item(i));
                System.out.println(identifiersFromCmml);
            }

        }
    }

    public void testString2NodeList() throws Exception {

    }

    public void testGetElementB() throws Exception {

    }

    public void testGetElementB1() throws Exception {

    }

    public void testGetElementsB() throws Exception {

    }

    public void testGetElementsB1() throws Exception {

    }

    public void testString2Doc() throws Exception {

    }

    public void testGetIdentifiersFrom() throws Exception {

    }

    public void testGetIdentifiersFromQuery() throws Exception {

    }

    public void testPrintDocument() throws Exception {

    }

    public void testCompileX() throws Exception {

    }
}