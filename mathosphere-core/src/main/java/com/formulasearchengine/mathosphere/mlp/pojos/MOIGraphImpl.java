package com.formulasearchengine.mathosphere.mlp.pojos;

import gov.nist.drmf.interpreter.pom.moi.MOIDependency;
import gov.nist.drmf.interpreter.pom.moi.MOIDependencyGraph;
import gov.nist.drmf.interpreter.pom.moi.MOINode;
import mlp.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Andre Greiner-Petter
 */
public class MOIGraphImpl extends MOIDependencyGraph implements MathTagGraph {
    private static final Logger LOG = LogManager.getLogger(MOIGraphImpl.class.getName());

    @Override
    public void addFormula(MathTag mathTag) {
        try {
            super.addNode(mathTag.placeholder(), mathTag.getContent(), mathTag);
        } catch (Exception e) {
            LOG.error("Unable to add mathTag to graph: " + mathTag.getContent(), e);
        }
    }

    @Override
    public MathTag removeFormula(MathTag mathTag) {
        super.removeNode(mathTag.getContent());
        return mathTag;
    }

    @Override
    public boolean contains(MathTag mathTag) {
        return super.containsNode(mathTag.placeholder());
    }

    @Override
    public Collection<MathTag> getOutgoingEdges(MathTag mathTag) {
        MOINode<MathTag> node = (MOINode<MathTag>) super.getNode(mathTag.placeholder());
        if ( node == null ) return new HashSet<>();
        return node.getOutgoingDependencies().stream()
                .map( MOIDependency::getSink )
                .map( MOINode::getAnnotation )
                .map( o -> (MathTag)o)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<MathTag> getIngoingEdges(MathTag mathTag) {
        MOINode<MathTag> node = (MOINode<MathTag>) super.getNode(mathTag.placeholder());
        if ( node == null ) return new HashSet<>();
        return node.getIngoingDependencies().stream()
                .map( MOIDependency::getSource )
                .map( MOINode::getAnnotation )
                .map( o -> (MathTag)o)
                .collect(Collectors.toSet());
    }
}
