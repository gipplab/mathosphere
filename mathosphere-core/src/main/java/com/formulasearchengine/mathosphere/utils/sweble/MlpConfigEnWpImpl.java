package com.formulasearchengine.mathosphere.utils.sweble;

import org.sweble.wikitext.engine.config.WikiConfigImpl;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;

/**
 * Created by Moritz on 27.08.2017.
 */
public class MlpConfigEnWpImpl extends DefaultConfigEnWp {

    public static WikiConfigImpl generate()
    {
        WikiConfigImpl c = new WikiConfigImpl();
        new MlpConfigEnWpImpl().configureWiki(c);
        return c;
    }

    @Override
    protected void addTagExtensions(WikiConfigImpl c) {
        super.addTagExtensions(c);
        ChemTagExtension.addToGroup(c);
    }


}
