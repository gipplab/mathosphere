package de.tuberlin.dima.schubotz.utils;

import com.google.common.collect.Multiset;
import de.tuberlin.dima.schubotz.fse.utils.XMLHelper;
import junit.framework.TestCase;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

import static de.tuberlin.dima.schubotz.fse.utils.XMLHelper.getIdentifiersFromCmml;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore
public class XMLHelperTest {
    @Test
    public void testCompactForm() throws Exception {

    }
	public void testGrading() throws Exception{
		String testFile1 = TestUtils.getTestQueryString(); //fQuery.xml
		//Get nodelist of all <math> descendants of <root><topic>
		NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math");//"/topics/topic/query/formula/math" topic/query/formula

		int count = MathMLElements.getLength();
		if (count > 0) {
			HashMap<String, Node> qvars = new HashMap<>();
			NodeList testnode = XMLHelper.String2NodeList( TestUtils.getTestResultForTest11(), "*//math" );
            //TODO assertEquals(100., XMLHelper.calculateSimilarityScore( MathMLElements.item( 11 ), testnode.item( 0 ),  qvars ) );
        } else {
			fail("no math element");
		}
	}

    @Test
    public void testCompareNode() throws Exception {
		String testFile1 = TestUtils.getTestQueryString();
		//Get nodelist of all <math> descendants of <root><topic>
		NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math");//"/topics/topic/query/formula/math" topic/query/formula
		
		//working with f1.1, recurse through and print to test if generated correctly
	    Node nl = MathMLElements.item(1);
		System.out.println(XMLHelper.printDocument(nl));
	    
	    
		int count = MathMLElements.getLength();
		if (count > 0) {
			HashMap<String, Node> qvars = new HashMap<>();
			assertFalse( XMLHelper.compareNode( MathMLElements.item( 1 ), MathMLElements.item( 2 ), true, qvars ) );
			assertTrue( XMLHelper.compareNode( MathMLElements.item( 1 ), MathMLElements.item( 1 ), true, qvars ) );
			System.out.println(qvars.toString());
		} else {
			fail("no math element");
		}
	}

    @Test
    public void testGetMMLLeaves() throws Exception {
        String testFile1 = TestUtils.getTestQueryString();
        NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math");//"/topics/topic/query/formula/math" topic/query/formula
        int count = MathMLElements.getLength();
        if (count > 0) {
            ArrayList<Tuple2<String, String>> mmlLeaves = XMLHelper.getMMLLeaves(MathMLElements.item(2));
            for (Tuple2<String, String> mmlLeaf : mmlLeaves) {
                System.out.println(mmlLeaf.toString());
            }
        }

    }

    @Test
    public void testGetIdentifiersFromCmml() throws Exception {
        String testFile1 = TestUtils.getTestQueryString();
        NodeList MathMLElements = XMLHelper.String2NodeList(testFile1, "/topics//math/semantics/*[1]");//"/topics/topic/query/formula/math" topic/query/formula
        int count = MathMLElements.getLength();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                Multiset<String> identifiersFromCmml = getIdentifiersFromCmml(MathMLElements.item(i));
                System.out.println(i+" : "+identifiersFromCmml);
            }

        }
    }

    @Test
    public void testString2NodeList() throws Exception {

    }

    @Test
    public void testGetElementB() throws Exception {

    }

    @Test
    public void testGetElementB1() throws Exception {

    }

    @Test
    public void testGetElementsB() throws Exception {

    }

    @Test
    public void testGetElementsB1() throws Exception {

    }

    @Test
    public void testString2Doc() throws Exception {

    }

    @Test
    public void testGetIdentifiersFrom() throws Exception {

    }

    @Test
    public void testGetIdentifiersFromQuery() throws Exception {

    }

    @Test
    public void testPrintDocument() throws Exception {

    }

    @Test
    public void testCompileX() throws Exception {

    }
}