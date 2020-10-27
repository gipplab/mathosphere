package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author Andre Greiner-Petter
 */
public class DocumentMetaLib {

    private MathTagGraph graph;

    private Map<String, MathTag> formulaLib;
    private Map<String, SpecialToken> linkLib;
    private Map<String, SpecialToken> citeLib;

    private List<List<Integer>> sectionSentenceLengths = null;

    public DocumentMetaLib() {
        this(new BaseConfig());
    }

    /**
     * Uses the default graph implementation {@link IdentifierGraphImpl} to build
     * a graph of the given formulae.
     */
    public DocumentMetaLib(BaseConfig config) {
        this(new IdentifierGraphImpl(config));
    }

    /**
     * Uses the given graph interface to build a graph of the added formulae.
     * @param graph an empty graph that represents the dependencies of the formulae
     */
    public DocumentMetaLib(MathTagGraph graph) {
        this.graph = graph;
        formulaLib = Maps.newHashMap();
        linkLib = Maps.newHashMap();
        citeLib = Maps.newHashMap();
    }

    public void setGraph(MathTagGraph graph) {
        this.graph = graph;
    }

    public void addFormula(MathTag mathTag) {
        formulaLib.put( mathTag.placeholder(), mathTag );
        this.graph.addFormula(mathTag);
    }

    public void setAllFormula(List<MathTag> mathTags) {
        mathTags.clear();
        addAllFormula(mathTags);
    }

    public void addAllFormula(List<MathTag> mathTags) {
        mathTags.forEach(
                f -> {
                    formulaLib.put(f.placeholder(), f);
                    graph.addFormula(f);
                }
        );
    }

    public MathTag removeFormula(String id) {
        MathTag tag = this.formulaLib.remove(id);
        if ( tag != null ) this.graph.removeFormula(tag);
        return tag;
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

    public void setDocumentLength(List<List<List<Word>>> documentStructure) {
        sectionSentenceLengths = Lists.newArrayListWithCapacity(documentStructure.size());
        for ( List<List<Word>> sections : documentStructure ) {
            List<Integer> sentenceLength = Lists.newArrayListWithCapacity(sections.size());
            for ( List<Word> sentence : sections ) {
                sentenceLength.add(sentence.size());
            }
            sectionSentenceLengths.add(sentenceLength);
        }
    }

    public int getNumberOfSentencesInSection(int section) {
        return sectionSentenceLengths.get(section).size();
    }

    public int getSectionLength(int section) {
        return sectionSentenceLengths.get(section).stream().reduce(0, Integer::sum);
    }

    public MathTagGraph getGraph() {
        return graph;
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

    public void setFormulaLib(Map<String, MathTag> formulaLib) {
        this.formulaLib = formulaLib;
    }

    public void setLinkLib(Map<String, SpecialToken> linkLib) {
        this.linkLib = linkLib;
    }

    public void setCiteLib(Map<String, SpecialToken> citeLib) {
        this.citeLib = citeLib;
    }

    public List<List<Integer>> getSectionSentenceLengths() {
        return sectionSentenceLengths;
    }

    public void setSectionSentenceLengths(List<List<Integer>> sectionSentenceLengths) {
        this.sectionSentenceLengths = sectionSentenceLengths;
    }
}
