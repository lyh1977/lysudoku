package org.ly.lysudoku;

import org.ly.lysudoku.Cell;

/**
 * A link between two potential values (candidates) of two cells.
 */
public class Link {

    private final Cell srcCell;
    private final int srcValue;
    private final Cell dstCell;
    private final int dstValue;


    public Link(Cell srcCell, int srcValue, Cell dstCell, int dstValue) {
        this.srcCell = srcCell;
        this.srcValue = srcValue;
        this.dstCell = dstCell;
        this.dstValue = dstValue;
    }

    public Cell getSrcCell() {
        return srcCell;
    }

    public int getSrcValue() {
        return srcValue;
    }

    public Cell getDstCell() {
        return dstCell;
    }

    public int getDstValue() {
        return dstValue;
    }

}
