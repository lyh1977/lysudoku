/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules;

import java.util.*;

import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Settings;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.CellSet;
import org.ly.lysudoku.tools.SingletonBitSet;


/**
 * Implementation of the "NC Forcing Cell" solving technique by Tarek Maani.
 * This covers double consecutive claiming, double Middle claiming and triple consecutive claiming
 * http://jcbonsai.free.fr/sudoku/JSudokuUserGuide/relationalTechniques.html
 * http://forum.enjoysudoku.com/sudokuncexplainer-to-solve-and-rate-sudoku-non-consecutive-t36949.html#p285476
 */
public class forcingCellNC implements IndirectHintProducer {

	public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        int firstValue, valueIndex;
        for (int i = 0; i < 81; i++) {
            Cell ncCell = Grid.getCell(i);
            BitSet ncValues = grid.getCellPotentialValues(i);
			int ncValuesCard = ncValues.cardinality();
            if (ncValuesCard == 2 || ncValuesCard == 3) {
				// Potential NC forcing cell found
                BitSet potentialNC = (BitSet)ncValues.clone();
				firstValue = potentialNC.nextSetBit(0);
				valueIndex = firstValue;
				for (int ncValuesIndex = 1; ncValuesIndex < ncValuesCard; ncValuesIndex++)
					valueIndex = potentialNC.nextSetBit(valueIndex + 1);
				if ((valueIndex - firstValue) == ( ncValuesCard - 1 ) || (Settings.getInstance().whichNC() == 2 && (valueIndex - firstValue) == 8))
					if (ncValuesCard == 2) {
						//Found double consecutive claiming forcing cell
						forcingCellNCHint hint = createHint1(grid, ncCell, firstValue, valueIndex);
						if (hint.isWorth())
							accu.add(hint);
					}
					else {
						//Found triple consecutive claiming forcing cell
						//For NC+ check for 100000011, 110000001
						if ((Settings.getInstance().whichNC() == 2 && (valueIndex - firstValue) == 8)) {//NC+ check
							if (potentialNC.nextSetBit(firstValue + 1) == (firstValue + 1)) {
								forcingCellNCHint hint = createHint2(grid, ncCell, firstValue);
								if (hint.isWorth())
								accu.add(hint);
							}
							if (potentialNC.nextSetBit(firstValue + 1) == (valueIndex - 1)) {
								forcingCellNCHint hint = createHint2(grid, ncCell, valueIndex);
								if (hint.isWorth())
								accu.add(hint);
							}
							continue;
						}
						if ((Settings.getInstance().whichNC() == 1 && (valueIndex - firstValue) == 8))
								continue;
						forcingCellNCHint hint = createHint2(grid, ncCell, firstValue + 1);
						if (hint.isWorth())
							accu.add(hint);
					}
				if ((valueIndex - firstValue) == 2 || (Settings.getInstance().whichNC() == 2 && (valueIndex - firstValue) == 7 && ncValuesCard ==2)) {
					//Found double middle claiming forcing cell
					//For NC+ check for 100000010 and 010000001
					if ((Settings.getInstance().whichNC() == 2 && (valueIndex - firstValue) == 7)) {//NC+ check
							if (potentialNC.nextSetBit(firstValue + 1) == 8) {
								forcingCellNCHint hint = createHint2(grid, ncCell, 9);
								if (hint.isWorth())
								accu.add(hint);
							}
							else {
								forcingCellNCHint hint = createHint2(grid, ncCell, 1);
								if (hint.isWorth())
								accu.add(hint);
							}
							continue;
					}
					forcingCellNCHint hint = createHint2(grid, ncCell, firstValue + 1);
					if (hint.isWorth())
						accu.add(hint);
				}
			}//if (ncValuesCard == 2 || ncValuesCard == 3)
    	} // for (int i = 0; i < 81; i++)
    }

    private forcingCellNCHint createHint1(Grid grid, Cell ncCell, int value1, int value2) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
       CellSet victims = new CellSet(Grid.wazirCellsRegular[ncCell.getIndex()]);
	   if (Settings.getInstance().isToroidal())
		   victims = new CellSet(Grid.wazirCellsToroidal[ncCell.getIndex()]);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value1))
                removablePotentials.put(cell, SingletonBitSet.create(value1));
            if (grid.hasCellPotentialValue(cell.getIndex(), value2))
				if (removablePotentials.containsKey(cell))
					removablePotentials.get(cell).set(value2);
				else
					removablePotentials.put(cell, SingletonBitSet.create(value2));
        }
        // Create hint
        return new forcingCellNCHint(this, removablePotentials, ncCell, new int[] {value1, value2});
    }

    private forcingCellNCHint createHint2(Grid grid, Cell ncCell, int value1) {
        // Build list of removable potentials
        Map<Cell,BitSet> removablePotentials = new HashMap<Cell,BitSet>();
       CellSet victims = new CellSet(Grid.wazirCellsRegular[ncCell.getIndex()]);
	   if (Settings.getInstance().isToroidal())
		   victims = new CellSet(Grid.wazirCellsToroidal[ncCell.getIndex()]);
        for (Cell cell : victims) {
            if (grid.hasCellPotentialValue(cell.getIndex(), value1))
                removablePotentials.put(cell, SingletonBitSet.create(value1));
        }
        // Create hint
        return new forcingCellNCHint(this, removablePotentials, ncCell, new int[] {value1});
    }

    @Override
    public String toString() {
        return "NC Forcing Cell";
    }

}
