package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.Collection;
import java.util.List;

/**
 * Represents a directed graph structure of {@link MathTag} elements.
 * A vertex is a single {@link MathTag} and an edge represents the
 * dependencies (is subexpression) between {@link MathTag}.
 *
 * Vertices might be isolated, i.e., they are not subexpressions of other
 * {@link MathTag} (no outgoing edges) and no other {@link MathTag} are
 * subexpressions of this element (no ingoing edges).
 *
 * @author Andre Greiner-Petter
 */
public interface MathTagGraph {
    /**
     * Adds a formula the graph.
     * @param mathTag formula
     */
    void addFormula(MathTag mathTag);

    /**
     * Removes a formula from the graph.
     * @param mathTag formula
     * @return the formula without edges.
     */
    MathTag removeFormula(MathTag mathTag);

    /**
     * True if the given formula is an element of this graph.
     * @param mathTag formula
     * @return true if the graph contains this formula, false otherwise.
     */
    boolean contains(MathTag mathTag);

    /**
     * Appends relation for the given node
     * @param mathTag the formula
     * @param relation the relation that should be appended
     */
    void appendMOIRelation(MathTag mathTag, Relation relation);

    /**
     * Sets the relations for the given formula all at once in contrast to
     * {@link #appendMOIRelation(MathTag, Relation)} which simply appends single relations.
     * @param mathTag the formula
     * @param relations the collection of relations
     */
    void setMOIRelation(MathTag mathTag, Collection<Relation> relations);

    /**
     * Gets the list of relations from the given formula
     * @param mathTag the formula
     * @return the appended list of relations
     */
    List<Relation> getRelations(MathTag mathTag);

    /**
     * Returns the outgoing edges of the given {@link MathTag}.
     * If the given formula does not exist, it returns an empty
     * collection (not null).
     * @param mathTag the formula
     * @return the outgoing edges of the formula (never null)
     */
    Collection<MathTag> getOutgoingEdges(MathTag mathTag);

    /**
     * Returns the ingoing edges of the given {@link MathTag}.
     * If the formula does not exist, the returned collection
     * is empty, not null.
     * @param mathTag formula
     * @return the ingoing edges of the formula (never null)
     */
    Collection<MathTag> getIngoingEdges(MathTag mathTag);
}
