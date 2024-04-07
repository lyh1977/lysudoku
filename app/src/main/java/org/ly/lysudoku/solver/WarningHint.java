/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Link;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A hint that is not really a hint for solving a sudoku, but rather
 * to give an information on the sudoku, such as the fact that the sudoku
 * is not valid.
 */
public abstract class WarningHint extends IndirectHint {

    public WarningHint(WarningHintProducer rule) {
        super(rule, new HashMap<Cell, BitSet>());
    }

//    @Override
//    public void apply() {
//    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        return Collections.emptyMap();
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        return Collections.emptyMap();
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        return null;
    }

    @Override
    public Cell[] getSelectedCells() {
        return null;
    }

    public Collection<Cell> getRedCells() {
        return Collections.emptyList();
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public boolean isWorth() {
        return true;
    }

}
