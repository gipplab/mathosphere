package com.formulasearchengine.mathosphere.mlp.pojos;

import java.util.List;

public interface SpecialToken {
    List<Position> getPositions();

    void addPosition(Position pos);

    String getContent();

    String placeholder();

    String getContentHash();

    int hashCode();
}
