package com.formulasearchengine.mathosphere.mlp.pojos;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.google.common.collect.Multiset;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A graph implementation of the {@link MathTagGraph}. It builds a graph
 * structure based on the identifiers in each formula. If two formulae share
 * an identifier, both formulae are connected by an edge.
 *
 * @author Andre Greiner-Petter
 */
public class IdentifierGraphImpl implements MathTagGraph {
    private final BaseConfig config;

    private final Set<MathTag> nodes;

    private final Map<MathTag, Collection<MathTag>> edges;

//    private final

    public IdentifierGraphImpl(BaseConfig config) {
        this.config = config;
        this.nodes = new HashSet<>();
        this.edges = new HashMap<>();
    }

    @Override
    public void addFormula(MathTag mathTag) {
        if ( nodes.contains(mathTag) ) return;

        Collection<MathTag> connections = nodes.stream()
                .filter( m -> isSubexpression(mathTag, m) )
                .collect(Collectors.toSet());

        nodes.add(mathTag);
        edges.put(mathTag, connections);
    }

    private boolean isSubexpression(MathTag tag, MathTag reference) {
        Multiset<String> identifiers = tag.getIdentifiers(config);
        return reference.getIdentifiers(config)
                .stream()
                .anyMatch(identifiers::contains);
    }

    @Override
    public MathTag removeFormula(MathTag mathTag) {
        Collection<MathTag> connections = edges.get(mathTag);
        if ( connections != null ) {
            connections.forEach(m -> {
                Collection<MathTag> con = edges.get(m);
                if (con != null) con.remove(mathTag);
            });
        }

        nodes.remove(mathTag);
        return mathTag;
    }

    @Override
    public boolean contains(MathTag mathTag) {
        return nodes.contains(mathTag);
    }

    @Override
    public void appendMOIRelation(MathTag mathTag, Relation relation) {
        // TODO
    }

    @Override
    public List<Relation> getRelations(MathTag mathTag) {
        // TODO
        return null;
    }

    @Override
    public Collection<MathTag> getOutgoingEdges(MathTag mathTag) {
        return edges.getOrDefault( mathTag, new HashSet<>() );
    }

    @Override
    public Collection<MathTag> getIngoingEdges(MathTag mathTag) {
        return edges.getOrDefault( mathTag, new HashSet<>() );
    }
}
