package com.formulasearchengine.mathosphere.pomlp.pom.example;

import com.formulasearchengine.mathosphere.pomlp.util.PomlpPathConstants;
import gov.nist.drmf.interpreter.common.GlobalPaths;
import gov.nist.drmf.interpreter.examples.MLP;
import mlp.PomParser;
import mlp.PomTaggedExpression;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MLPWrapper {

    public static void main(String[] args) throws Exception  {
        Path referenceDir = Paths
                .get("")            // local path       -> mathosphere/pomlp
                .toAbsolutePath()   // to absolute path
                .getParent()        // parent directory -> mathosphere/
                .resolve( PomlpPathConstants
                        .LatexGrammarBaseDir // -> mathosphere/lib/latex-grammar
                        .resolve(
                                GlobalPaths.PATH_REFERENCE_DATA
                        )
                );
        PomParser parser = new PomParser(referenceDir);

        String test = "R_{a}\\left(\\mathbf{b};\\mathbf{z}\\right)R_{-a}\\left(\\mathbf{b};\\mathbf{z}\\right)>1";
        PomTaggedExpression pte = parser.parse(test);
        pte = MLP.clean(pte);
        System.out.println(pte.toString("  "));
    }

}
