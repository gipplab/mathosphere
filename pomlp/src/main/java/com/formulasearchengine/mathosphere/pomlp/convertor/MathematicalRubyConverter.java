package com.formulasearchengine.mathosphere.pomlp.convertor;

import com.formulasearchengine.mathosphere.pomlp.convertor.extensions.CommandExecutor;
import com.formulasearchengine.mathosphere.pomlp.convertor.extensions.NativeResponse;
import com.formulasearchengine.mathosphere.pomlp.util.Utility;
import com.formulasearchengine.mathosphere.pomlp.util.config.ConfigLoader;
import com.formulasearchengine.mathosphere.pomlp.xml.XmlDocumentReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import sun.security.krb5.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @author Andre Greiner-Petter
 */
public class MathematicalRubyConverter extends NativeConverter implements Canonicalizable {

    private static final Logger LOG = LogManager.getLogger( MathematicalRubyConverter.class.getName() );

    private static final String NAME = "Ruby-Mathematical";
    private static final String CMD = "ruby";

    private LinkedList<String> arguments;

    public MathematicalRubyConverter(){
        arguments = new LinkedList<>();
    }

    @Override
    public void init(){
        arguments.clear();
        arguments.add( CMD );
        String script = ConfigLoader.CONFIG.getProperty( ConfigLoader.MATHEMATICAL );
        arguments.add( script );
        internalInit( arguments, NAME );
    }

    @Override
    protected Document parseInternal(LinkedList<String> args, String latex, String name ){
        return super.parseInternal(
                args,
                "$"+latex+"$",
                name
        );
    }

    @Override
    public String getNativeCommand() {
        return CMD;
    }
}
