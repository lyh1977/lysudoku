/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules;

import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import  org.ly.lysudoku.solver.DirectHintProducer;
import org.ly.lysudoku.solver.HintsAccumulator;

import java.util.BitSet;

/**
 * Implementation of the Naked Single solving techniques.
 */
public class NakedSingle implements DirectHintProducer {

    /**
     * Check if a cell has only one potential value, and accumulate
     * corresponding hints
     */
    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
//        Grid.Region[] parts = grid.getRegions(Grid.Row.class);
//        // Iterate on parts
//        for (Grid.Region part : parts) {
//            // Iterate on cells
//            for (int index = 0; index < 9; index++) {
//                Cell cell = part.getCell(index);
//                // Get the cell's potential values
//                BitSet potentialValues = cell.getPotentialValues();
//                if (potentialValues.cardinality() == 1) {
//                    // One potential value -> solution found
//                    int uniqueValue = potentialValues.nextSetBit(0);
//                    accu.add(new NakedSingleHint(this, null, cell, uniqueValue));
//                }
//            }
//        }
        for (int i = 0; i < 81; i++) {
            if(grid.getCellValue(i) != 0) continue;
            //BitSet potentialValues = cell.getPotentialValues();
            BitSet potentialValues = grid.getCellPotentialValues(i);
            if (potentialValues.cardinality() == 1) {
                // One potential value -> solution found
                int uniqueValue = potentialValues.nextSetBit(0);
                Cell cell = Grid.getCell(i);
                accu.add(new NakedSingleHint(this, null, cell, uniqueValue));
            }
        }
    }

    @Override
    public String toString() {
        return "Naked Singles";
    }

}
