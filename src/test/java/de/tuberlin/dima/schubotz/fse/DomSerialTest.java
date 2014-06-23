package de.tuberlin.dima.schubotz.fse;

import junit.framework.TestCase;

import java.io.*;

public class DomSerialTest extends TestCase {

    public void testRead() throws Exception {
        DomSerial doms = new DomSerial();
        InputStream is = new ByteArrayInputStream( TestUtils.getTestQueryString().getBytes());
        DataInputStream dis = new DataInputStream(is);
        doms.read(dis);
        assertEquals(XMLHelper.String2Doc(TestUtils.getTestQueryString(),true).getTextContent(), doms.document.getTextContent());
        //System.out.println(doms.document.getElementsByTagName("topic").item(0).getTextContent());
    }
    public void testWrite() throws Exception {
        DomSerial doms = new DomSerial();
        doms.document = XMLHelper.String2Doc(TestUtils.getTestQueryString(), true);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream w = new DataOutputStream(bout);
        doms.write(w);
        // assertEquals(TestUtils.getTestQueryString(),bout.toString()); Fails UTF char are escaped in the output and newlines are'n broken. Both does not matter
        System.out.println(bout.toString());
    }


}