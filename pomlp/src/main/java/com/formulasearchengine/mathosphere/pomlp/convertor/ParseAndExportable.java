package com.formulasearchengine.mathosphere.pomlp.convertor;


import org.w3c.dom.Document;

import java.nio.file.Path;

public interface ParseAndExportable {

    void init() throws Exception;

    Document parse( String latex ) throws Exception;

    void parseToFile( String latex, Path outputFile ) throws Exception;
}
