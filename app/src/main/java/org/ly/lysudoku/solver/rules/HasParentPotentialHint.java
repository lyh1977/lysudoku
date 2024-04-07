/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules;

import java.util.*;


import org.ly.lysudoku.Grid;
import org.ly.lysudoku.solver.rules.chaining.*;


/**
 * Interface for indirect hints that are able to tell what
 * {@link org.ly.lysudoku.solver.rules.chaining.Potential Potential}s
 * have been set to off before this rule could be applied.
 * <p>
 * Used for chaining only. See package {@link org.ly.lysudoku.solver.rules.chaining}.
 */
public interface HasParentPotentialHint {

    /**
     * Get the potentials that were removed from the initial grid
     * before this rule could be applied.
     * @param initialGrid the initial grid, on which this rule
     * cannot be applied.
     * @param currentGrid the currewnt grid, on which this rule
     * is revealed.
     * @return the potentials that were removed from the initial grid.
     */
    public Collection<Potential> getRuleParents(Grid initialGrid, Grid currentGrid);

}
