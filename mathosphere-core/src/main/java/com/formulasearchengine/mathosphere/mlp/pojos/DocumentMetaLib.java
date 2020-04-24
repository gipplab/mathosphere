package com.formulasearchengine.mathosphere.mlp.pojos;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author Andre Greiner-Petter
 */
public class DocumentMetaLib {

    private Map<String, MathTag> formulaLib;
    private Map<String, SpecialToken> linkLib;
    private Map<String, SpecialToken> citeLib;

    public DocumentMetaLib() {
        formulaLib = Maps.newHashMap();
        linkLib = Maps.newHashMap();
        citeLib = Maps.newHashMap();
    }

    public void addFormula(MathTag mathTag) {
        formulaLib.put( mathTag.placeholder(), mathTag );
    }

    public void setAllFormula(List<MathTag> mathTags) {
        mathTags.clear();
        addAllFormula(mathTags);
    }

    public void addAllFormula(List<MathTag> mathTags) {
        mathTags.forEach(
                f -> formulaLib.put(f.placeholder(), f)
        );
    }

    public void addLink(SpecialToken link) {
        linkLib.put(link.placeholder(), link);
    }

    public void addAllLinks(List<SpecialToken> links) {
        links.forEach(
                f -> linkLib.put(f.placeholder(), f)
        );
    }

    public void addCite(SpecialToken cite) {
        citeLib.put(cite.placeholder(), cite);
    }

    public void addAllCites(List<SpecialToken> cites) {
        cites.forEach(
                f -> citeLib.put(f.placeholder(), f)
        );
    }

    public Map<String, MathTag> getFormulaLib() {
        return formulaLib;
    }

    public Map<String, SpecialToken> getLinkLib() {
        return linkLib;
    }

    public Map<String, SpecialToken> getCiteLib() {
        return citeLib;
    }
}
