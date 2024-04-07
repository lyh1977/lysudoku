/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules.unique;

import java.util.*;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.solver.*;


public abstract class BugHint extends IndirectHint implements Rule {

    public BugHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials) {
        super(rule, removablePotentials);
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName();
        } else {
            return "Look for a Bivalue Universal Grave (BUG)";
        }
    }

}
