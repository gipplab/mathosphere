package com.formulasearchengine.mathosphere.utils.sweble;

import org.sweble.wikitext.engine.ExpansionFrame;
import org.sweble.wikitext.engine.TagExtensionBase;
import org.sweble.wikitext.engine.config.TagExtensionGroup;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.config.WikiConfigImpl;
import org.sweble.wikitext.parser.nodes.WtNode;
import org.sweble.wikitext.parser.nodes.WtNodeList;
import org.sweble.wikitext.parser.nodes.WtTagExtension;
import org.sweble.wikitext.parser.nodes.WtTagExtensionBody;

import java.util.Map;

public class ChemTagExtension extends TagExtensionGroup {
    private static final long serialVersionUID = 1L;

    protected ChemTagExtension(WikiConfig wikiConfig) {
        super("Extension - Math (chem tag)");
        addTagExtension(new ChemTagExtImpl(wikiConfig));
    }

    public static void addToGroup(WikiConfigImpl wikiConfig) {
        wikiConfig.addTagExtensionGroup(new ChemTagExtension(wikiConfig));
    }

    public static final class ChemTagExtImpl
            extends
            TagExtensionBase {
        private static final long serialVersionUID = 1L;

        public ChemTagExtImpl() {
            super("chem");
        }

        public ChemTagExtImpl(WikiConfig wikiConfig) {
            super(wikiConfig, "chem");
        }

        @Override
        public WtNode invoke(
                ExpansionFrame frame,
                WtTagExtension tagExt,
                Map<String, WtNodeList> attrs,
                WtTagExtensionBody body) {
            return null;
        }
    }
}
