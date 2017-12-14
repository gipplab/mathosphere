package com.formulasearchengine.mathosphere.pomlp;

import it.unibz.inf.rted.distance.RTED_InfoTree_Opt;
import it.unibz.inf.rted.util.LblTree;

import javax.xml.soap.Node;
import java.util.Comparator;
import java.util.LinkedList;

public class RTEDTreeComparator {

    public static final double DEFAULT_INSERT_COSTS = 1.0;
    public static final double DEFAULT_DELETE_COSTS = 1.0;
    public static final double DEFAULT_RENAME_COSTS = 0.0;

    private RTED_InfoTree_Opt rted;
    private LinkedList<int[]> operations;

    private boolean autoComputeMapping;
    private static final boolean AUTO_COMP = false;

    public RTEDTreeComparator(){
        this(AUTO_COMP);
    }

    public RTEDTreeComparator( boolean autoComputeMapping ){
        this(
                autoComputeMapping,
                DEFAULT_DELETE_COSTS,
                DEFAULT_INSERT_COSTS,
                DEFAULT_RENAME_COSTS
        );
    }

    public RTEDTreeComparator( double deleteCosts, double insertCosts, double renameCosts ){
        this(
                AUTO_COMP,
                deleteCosts,
                insertCosts,
                renameCosts
        );
    }

    public RTEDTreeComparator( boolean autoComputeMapping, double deleteCosts, double insertCosts, double renameCosts ){
        rted = new RTED_InfoTree_Opt(
                deleteCosts,
                insertCosts,
                renameCosts
        );
        this.autoComputeMapping = autoComputeMapping;
    }

    public double computeDistance( Node n1, Node n2 ){
        double dist = rted.nonNormalizedTreeDist( LblTree.fromXML(n1), LblTree.fromXML(n2) );
        if ( autoComputeMapping ) this.operations = rted.computeEditMapping();
        else this.operations = null;
        return dist;
    }

    /**
     * @return can be null, if auto compute mappings is deactivated
     */
    public LinkedList<int[]> getOperationsOfLastComputation() {
        return operations;
    }

    public void switchAutoMappings( boolean autoComputeMapping ){
        this.autoComputeMapping = autoComputeMapping;
    }
}
