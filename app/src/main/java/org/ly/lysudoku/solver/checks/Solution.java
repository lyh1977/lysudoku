/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.checks;


import org.ly.lysudoku.Grid;
import org.ly.lysudoku.solver.*;

/**
 * Class that computes the solution of a sudoku using brute-force,
 * and produces an hint that allows the user to view the solution.
 */
public class Solution implements WarningHintProducer {

    public void getHints(Grid grid, HintsAccumulator accu)
            throws InterruptedException {
        Grid solution = new Grid();
        grid.copyTo(solution);

        // First check for no, or multiple solution
        BruteForceAnalysis analyser = new BruteForceAnalysis(true);
        analyser.getHints(grid, accu);
    }

    @Override
    public String toString() {
        return "Brute force analysis";
    }

}
