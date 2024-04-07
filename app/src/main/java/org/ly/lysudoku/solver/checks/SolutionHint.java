/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.checks;

import java.util.*;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.HtmlLoader;
import org.ly.lysudoku.tools.SingletonBitSet;


/**
 * Hint that allows the user to directly view the solution of a sudoku.
 */
public class SolutionHint extends WarningHint {

    //private final Grid grid;
    private final Grid solution;

    public SolutionHint(WarningHintProducer rule, Grid grid, Grid solution) {
        super(rule);
        //this.grid = grid;
        this.solution = solution;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell,BitSet>();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                int value = solution.getCellValue(x, y);
                Cell cell = Grid.getCell(x, y);
                result.put(cell, SingletonBitSet.create(value));
            }
        }
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        return getGreenPotentials(grid, viewNum);
    }

    @Override
    public String toString() {
        return "Solution";
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    public String toHtml(Grid grid) {
        return HtmlLoader.loadHtml(this, "Solution.html");
    }

//SudokuMonster: Returned the ability to apply Brute force solution hint
   @Override
    public void apply(Grid grid) {
    solution.copyTo(grid);
   }

}
