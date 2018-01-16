package com.formulasearchengine.mathosphere.pomlp.gouldi;

import com.formulasearchengine.mathosphere.pomlp.GoldStandardLoader;
import com.formulasearchengine.mathosphere.pomlp.comparison.RTEDTreeComparator;
import com.formulasearchengine.mathosphere.pomlp.convertor.LatexToMMLConverter;
import com.formulasearchengine.mathosphere.pomlp.xml.MathMLDocumentReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;

/**
 * @author Andre Greiner-Petter
 */
public class LatexmlImprovements {

    private JsonGouldiBean bean;
    private RTEDTreeComparator RTED;
    private LatexToMMLConverter latexml;
    private Node goldTree;

    @BeforeAll
    public void init() throws Exception {
        GoldStandardLoader goldLoader = GoldStandardLoader.getInstance();
        goldLoader.initLocally();

        latexml = new LatexToMMLConverter();
        latexml.init();

        RTED = new RTEDTreeComparator();

        bean = goldLoader.getGouldiJson( 101 );
        bean.getMml();
        //MathMLDocumentReader.getDocumentFromXML();
    }

    @Test
    public void noImprovementsTest(){

    }
}
