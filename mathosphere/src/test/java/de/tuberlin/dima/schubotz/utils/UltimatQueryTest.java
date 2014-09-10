package de.tuberlin.dima.schubotz.utils;

import de.tuberlin.dima.schubotz.fse.utils.XMLHelper;
import junit.framework.TestCase;
import org.apache.flink.api.java.tuple.Tuple3;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by mas9 on 9/11/14.
 */
public class UltimatQueryTest extends TestCase {
    private String getQueryFile() throws IOException {
        return TestUtils.getFileContents("de/tuberlin/dima/schubotz/fse/fQuery.xml");
    }
    private Collection<String> getTests() throws Exception {
        Document doc = XMLHelper.String2Doc(getQueryFile(), true);
        

       return Arrays.asList( getQueryFile().split("</topic>") );
    }
    @Test
    public Collection<Tuple3<Integer,String,String>> getNamedPatterns() throws IOException {
        for (String s : getTests()) {

        }

        return  null;
    }

    public void testMyTest() throws IOException {
        getNamedPatterns();
    }
}
