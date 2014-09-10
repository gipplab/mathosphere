package de.tuberlin.dima.schubotz.utils;

import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

/**
 * Created by mas9 on 8/29/14.
 * from http://stackoverflow.com/questions/15233826/how-do-i-run-an-xquery-against-xml-in-a-string
 */
public class XQueryTest {
    @Test
    public void testMain() {
        try {
            final String tableXml =  TestUtils.getFileContents("de/tuberlin/dima/schubotz/utils/q1.xml");
            final String queryString =  TestUtils.getFileContents("de/tuberlin/dima/schubotz/utils/q1.xq");

            Configuration saxonConfig = new Configuration();
            Processor processor = new Processor(saxonConfig);

            XQueryCompiler xqueryCompiler = processor.newXQueryCompiler();
            XQueryExecutable xqueryExec = xqueryCompiler.compile(queryString);
            XQueryEvaluator xqueryEval = xqueryExec.load();
            xqueryEval.setSource(new SAXSource(new InputSource(
                    new StringReader(tableXml))));

            XdmDestination destination = new XdmDestination();

            xqueryEval.setDestination(destination);

            // Avert your eyes!
            xqueryEval.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) {
                    return new StreamSource(new StringReader(tableXml));
                }
            });

            xqueryEval.run();

            System.out.println(destination.getXdmNode());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
