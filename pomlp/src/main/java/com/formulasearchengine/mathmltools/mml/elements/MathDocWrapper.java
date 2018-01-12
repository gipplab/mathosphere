package com.formulasearchengine.mathmltools.mml.elements;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * A wrapper for MathDoc from MathMLTools because of bad visibilities.
 * @author Andre Greiner-Petter
 */
public class MathDocWrapper extends MathDoc {
    public MathDocWrapper(String inputXMLString) throws ParserConfigurationException, SAXException, IOException {
        super(inputXMLString);
    }

    @Override
    public void changeTeXAnnotation(String newTeX){
        super.changeTeXAnnotation(newTeX);
    }

    @Override
    public void fixGoldCd(){
        super.fixGoldCd();
    }
}
