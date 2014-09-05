import junit.framework.TestCase;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.*;
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
public class XQuery15233826 extends TestCase {
    public void testMain() {
        try {
            final String tableXml =
                    "<table>" +
                            "  <columns>" +
                            "    <column>Foo</column><column>Bar</column>" +
                            "  </columns>" +
                            "  <rows>" +
                            "    <row><cell>Foo1</cell><cell>Bar1</cell></row>" +
                            "    <row><cell>Foo2</cell><cell>Bar2</cell></row>" +
                            "  </rows>" +
                            "</table>";

            Configuration saxonConfig = new Configuration();
            Processor processor = new Processor(saxonConfig);

            XQueryCompiler xqueryCompiler = processor.newXQueryCompiler();
            XQueryExecutable xqueryExec = xqueryCompiler
                    .compile("<result>{"
                            + "/table/rows/row/cell/text()='Foo2'"
                            + "}</result>");

            XQueryEvaluator xqueryEval = xqueryExec.load();
            xqueryEval.setSource(new SAXSource(new InputSource(
                    new StringReader(tableXml))));

            XdmDestination destination = new XdmDestination();

            xqueryEval.setDestination(destination);

            // Avert your eyes!
            xqueryEval.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) throws TransformerException {
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
