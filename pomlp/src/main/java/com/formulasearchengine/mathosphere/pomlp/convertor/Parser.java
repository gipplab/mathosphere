package com.formulasearchengine.mathosphere.pomlp.convertor;


import com.formulasearchengine.mathosphere.pomlp.convertor.extensions.NativeResponse;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.nio.file.Path;

public interface Parser {

    void init() throws Exception;

    Document parse( String latex ) throws Exception;

    void parseToFile( String latex, Path outputFile ) throws Exception;

    default int handleResponseCode(NativeResponse response, String name, Logger logger){
        if ( response.getStatusCode() != 0 ){
            logger.warn(name
                            + " finished with exit "
                            + response.getStatusCode()
                            + ": "
                            + response.getMessage(),
                    response.getThrowedException());
        }
        return response.getStatusCode();
    }

    default String getNativeCommand(){
        return null;
    }
}
