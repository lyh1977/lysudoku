/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules.unique;

import java.util.*;



import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Settings;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.CellSet;
import org.ly.lysudoku.tools.CommonTuples;
import org.ly.lysudoku.tools.Permutations;
import org.ly.lysudoku.tools.SingletonBitSet;


/**
 * Implementation of the Unique Rectangle/Loops solving techniques.
 * Support types 1-4.
 * Skewed (non-orthogonal) loops (very rare) are also detected.
 */
public class UniqueLoops implements IndirectHintProducer {

    private Grid lastGrid = new Grid();
    private List<UniqueLoopHint> lastResult = null;

    public void getHints(Grid grid, HintsAccumulator accu) throws InterruptedException {
        List<UniqueLoopHint> hints;
        if (grid.equals(lastGrid))
            hints = lastResult;
        else {
            hints = getHints(grid);
        }
        // Sort the result
        Collections.sort(hints, new Comparator<UniqueLoopHint>() {
            public int compare(UniqueLoopHint h1, UniqueLoopHint h2) {
                double d1 = h1.getDifficulty();
                double d2 = h2.getDifficulty();
                if (d1 < d2)
                    return -1;
                else if (d1 > d2)
                    return 1;
                else {
                    int t1 = h1.getType();
                    int t2 = h2.getType();
                    return t1 - t2;
                }
            }
        });
        grid.copyTo(lastGrid);
        lastResult = hints;
        for (UniqueLoopHint hint : hints)
            accu.add(hint);
    }

    private List<UniqueLoopHint> getHints(Grid grid) {
        List<UniqueLoopHint> result = new ArrayList<UniqueLoopHint>();
        for (int i = 0; i < 81; i++) {
            BitSet potentials = grid.getCellPotentialValues(i);
            if (potentials.cardinality() == 2) {
            	Cell cell = Grid.getCell(i);
                int v1 = potentials.nextSetBit(0);
                int v2 = potentials.nextSetBit(v1 + 1);
                assert v1 > 0 && v2 > 0;
                List<Cell> tempLoop = new ArrayList<Cell>();
                Collection<List<Cell>> results = new ArrayList<List<Cell>>();
                checkForLoops(grid, cell, v1, v2, tempLoop, 2, new BitSet(10), -1, results);
                for (List<Cell> loop : results) {
                    // Potential loop found. Check validity
                    if (isValidLoop(grid, loop)) {
						//If there are forbidden pairs make sure that there are no restrictions
						if (Settings.getInstance().isAntiFerz() || Settings.getInstance().isAntiKnight() || Settings.getInstance().whichNC() > 0)
							if (isRestricted(grid, loop, v1, v2))
								continue;
                        // This is a unique loop. Get cells with more than 2 potentials
                        List<Cell> extraCells = new ArrayList<Cell>(2);
                        for (Cell loopCell : loop) {
                            if (grid.getCellPotentialValues(loopCell.getIndex()).cardinality() > 2)
                                extraCells.add(loopCell);
                        }
                        if (extraCells.size() == 1) {
                            // Try a type-1 hint
                            UniqueLoopHint hint = createType1Hint(loop, extraCells.get(0), v1, v2);
                            if (!result.contains(hint) && hint.isWorth())
                                result.add(hint);
                        } else if (extraCells.size() > 2) {
                            // Only type 2 is possible
                            BitSet extraValues = new BitSet(10);
                            for (Cell c : extraCells)
                                extraValues.or(grid.getCellPotentialValues(c.getIndex()));
                            extraValues.clear(v1);
                            extraValues.clear(v2);
                            assert extraValues.cardinality() == 1;
                            UniqueLoopHint hint = createType2Hint(grid, loop, extraCells, v1, v2);
                            if (!result.contains(hint) && hint.isWorth())
                                result.add(hint);
                        } else if (extraCells.size() == 2) {
                            Cell r1 = extraCells.get(0);
                            Cell r2 = extraCells.get(1);
                            BitSet rPotentials = (BitSet)grid.getCellPotentialValues(r1.getIndex()).clone();
                            rPotentials.or(grid.getCellPotentialValues(r2.getIndex()));
                            rPotentials.clear(v1);
                            rPotentials.clear(v2);
                            if (rPotentials.cardinality() == 1) {
                                // Try type 2 hint
                                UniqueLoopHint hint = createType2Hint(grid, loop, extraCells, v1, v2);
                                if (!result.contains(hint) && hint.isWorth())
                                    result.add(hint);
                            } else if (rPotentials.cardinality() >= 2) {
                                // Try type 3 hint
                                Collection<UniqueLoopHint> hints = createType3Hints(grid, loop, r1, r2, v1, v2);
                                for (UniqueLoopHint hint : hints) {
                                    if (!result.contains(hint) && hint.isWorth())
                                        result.add(hint);
                                }
                            }
                            // Try type 4 hint
                            UniqueLoopHint hint = createType4Hint(grid, loop, r1, r2, v1, v2);
                            if (hint != null && !result.contains(hint) && hint.isWorth())
                                result.add(hint);
                        } else {
                            // Huh ? 0 rescue cell ? Sudoku has two solutions !!
                            // Do nothing (this is not our business)
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Check for potential loops with the given start and next cell.
     * @param grid the grid to search the loop in
     * @param cell the next cell of the loop
     * @param v1 the first potential value that all cells of the loop must have
     * @param v2 the 2nd potential value that all cells of the loop must have
     * @param loop the start of the loop
     * @param allowedEx the remaining number of allowed cells with more than two
     * potential values in the loop.
     * @param exValues the extra values in the current loop
     * @param lastRegionTypeIndex the region type index shared by the last two cells, or -1.
     * This region type can be skipped for the next cell. Might by <tt>null</tt>.
     * @param results the collection to fill with all loops found
     * @throws InterruptedException
     */
    private void checkForLoops(Grid grid, Cell cell, int v1, int v2,
            List<Cell> loop, int allowedEx, BitSet exValues,
            int lastRegionTypeIndex, Collection<List<Cell>> results) {
        loop.add(cell);
		if (Settings.getInstance().islkSudokuURUL()) {
//@SudokuMonster: Variants changes //regionTypeIndex < 3 && regionTypeIndex checks in all 6 loops
        for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
        	if (!Settings.getInstance().isVLatin()) {
				if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
				if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
				if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
				if (Grid.cellRegions[cell.getIndex()][regionTypeIndex] < 0)
						continue;
			}
			if (regionTypeIndex != lastRegionTypeIndex) {
                Grid.Region region = Grid.getRegionAt(regionTypeIndex, cell.getIndex());
                for (int i = 0; i < 9; i++) {
                    Cell next = region.getCell(i);
                    if (loop.get(0).equals(next) && loop.size() >= 4) {
                        // Yeah, the loop is closed. Save a copy
                        results.add(new ArrayList<Cell>(loop));
                    } else if (!loop.contains(next)) {
                        //BitSet potentials = next.getPotentialValues();
                        BitSet potentials = grid.getCellPotentialValues(next.getIndex());
                        if (potentials.get(v1) && potentials.get(v2)) {
                        	BitSet newExValues = (BitSet)exValues.clone(); // Ensure we cleanup ourself
                            newExValues.or(potentials);
                            newExValues.clear(v1);
                            newExValues.clear(v2);
                            int cardinality = potentials.cardinality();
                            /*
                             * We can continue if
                             * (1) The cell has exactly the two values of the loop
                             * (2) The cell has one extra value, the same as all previous cells with
                             * an extra value (for type 2 only)
                             * (3) The cell has extra values and the maximum number of cells with
                             * extra values, 2, is not reached
                             */
                            if (cardinality == 2 || newExValues.cardinality() == 1 || allowedEx > 0) {
                                int newAllowedEx = allowedEx;
                                if (cardinality > 2)
                                    newAllowedEx -= 1;
                                checkForLoops(grid, next, v1, v2, loop, newAllowedEx, newExValues,
                                        regionTypeIndex, results);
                            }
                        }
                    } // Not in the loop yet
                } // for i
            } // not last region type
        } // for regionType
		}
		else {
        exValues = (BitSet)exValues.clone(); // Ensure we cleanup ourself
        for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
        	if (!Settings.getInstance().isVLatin()) {
				if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
				if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
				if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
				if (Grid.cellRegions[cell.getIndex()][regionTypeIndex] < 0)
						continue;
			}
			if (regionTypeIndex != lastRegionTypeIndex) {
                Grid.Region region = Grid.getRegionAt(regionTypeIndex, cell.getIndex());
                for (int i = 0; i < 9; i++) {
                    Cell next = region.getCell(i);
                    if (loop.get(0).equals(next) && loop.size() >= 4) {
                        // Yeah, the loop is closed. Save a copy
                        results.add(new ArrayList<Cell>(loop));
                    } else if (!loop.contains(next)) {
                        //BitSet potentials = next.getPotentialValues();
                        BitSet potentials = grid.getCellPotentialValues(next.getIndex());
                        if (potentials.get(v1) && potentials.get(v2)) {
                            exValues.or(potentials);
                            exValues.clear(v1);
                            exValues.clear(v2);
                            int cardinality = potentials.cardinality();
                            /*
                             * We can continue if
                             * (1) The cell has exactly the two values of the loop
                             * (2) The cell has one extra value, the same as all previous cells with
                             * an extra value (for type 2 only)
                             * (3) The cell has extra values and the maximum number of cells with
                             * extra values, 2, is not reached
                             */
                            if (cardinality == 2 || exValues.cardinality() == 1 || allowedEx > 0) {
                                int newAllowedEx = allowedEx;
                                if (cardinality > 2)
                                    newAllowedEx -= 1;
                                checkForLoops(grid, next, v1, v2, loop, newAllowedEx, exValues,
                                        regionTypeIndex, results);
                            }
                        }
                    } // Not in the loop yet
                } // for i
            } // not last region type
        } // for regionType
		}
        // Rollback
        loop.remove(loop.size() - 1);
    }

    /**
     * Check if the given list of cells is a candidate for a unique loop.
     * <p>
     * The cells are already checked to all have the same two potentials
     * (with at most two exceptions that can have more potentials).
     * <p>
     * This methods checks that every regions visited by a cell of the loop are
     * visited exactly by two cells, and those two cells have an index of different
     * parity.
     * @param grid the grid
     * @param loop the cells of the loop
     * @return whether the given loop is a candidate for a unique loop
     */
    private boolean isValidLoop(Grid grid, List<Cell> loop) {
        HashSet<Grid.Region> visitedOdd = new HashSet<Grid.Region>();
        HashSet<Grid.Region> visitedEven = new HashSet<Grid.Region>();
        boolean isOdd = false;
        for (Cell cell : loop) {
			for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
				if (!Settings.getInstance().isVLatin()) {
					if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
					if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
					if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
					if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
					if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
					if (Grid.cellRegions[cell.getIndex()][regionTypeIndex] < 0)
						continue;
				}
				Grid.Region region = Grid.getRegionAt(regionTypeIndex, cell.getIndex());
                if (isOdd) {
                    if (visitedOdd.contains(region))
                        return false;
                    else
                        visitedOdd.add(region);
                } else {
                    if (visitedEven.contains(region))
                        return false;
                    else
                        visitedEven.add(region);
                }
            }
            isOdd = !isOdd;
        }
        // All regions must have been visited once with each parity (or never)
        return visitedOdd.equals(visitedEven);
    }

    //checks if loop cells can be restricted by forbidden pairs removing the deadly pattern
	private boolean isRestricted(Grid grid, List<Cell> loop, int v1, int v2) {
		for (Cell cell : loop) {
			if (Settings.getInstance().isAntiFerz() || Settings.getInstance().isAntiKnight()){
				CellSet visible = new CellSet (Grid.antiVisibleCellsSet[cell.getIndex()]);
				for (Cell vCell : visible) {
					if (grid.hasCellPotentialValue(vCell.getIndex(), v1)){
						return true;
					}
					if (grid.hasCellPotentialValue(vCell.getIndex(), v2)){
						return true;
					}
				}
			}
			if (Settings.getInstance().whichNC() > 0){
				int[] ncVisible = null;
				if (Settings.getInstance().whichNC() < 3)
					if (Settings.getInstance().isToroidal())
						ncVisible = Grid.wazirCellsToroidal[cell.getIndex()];
					else
						ncVisible = Grid.wazirCellsRegular[cell.getIndex()];
				else if (Settings.getInstance().whichNC() > 2)
					if (Settings.getInstance().isToroidal())
						ncVisible = Grid.ferzCellsToroidal[cell.getIndex()];
					else
						ncVisible = Grid.ferzCellsRegular[cell.getIndex()];
				for (int nextVisible : ncVisible){
					if (v1 < 9 || Settings.getInstance().whichNC() == 2  || Settings.getInstance().whichNC() == 4)
						if (grid.hasCellPotentialValue(nextVisible, v1 == 9 ? 1 : v1 + 1))
							return true;
					if (v1 > 1 || Settings.getInstance().whichNC() == 2  || Settings.getInstance().whichNC() == 4)
						if (grid.hasCellPotentialValue(nextVisible, v1 == 1 ? 9 : v1 - 1))
							return true;
					if (v2 < 9 || Settings.getInstance().whichNC() == 2  || Settings.getInstance().whichNC() == 4)
						if (grid.hasCellPotentialValue(nextVisible, v2 == 9 ? 1 : v2 + 1))
							return true;
					if (v2 >1 || Settings.getInstance().whichNC() == 2  || Settings.getInstance().whichNC() == 4)
						if (grid.hasCellPotentialValue(nextVisible, v2 == 1 ? 9 : v2 - 1))
							return true;
				}
			}
		}
		return false;
	}

    private UniqueLoopHint createType1Hint(List<Cell> loop, Cell rescueCell,
            int v1, int v2) {
        Map<Cell, BitSet> removable = new HashMap<Cell, BitSet>();
        BitSet values = new BitSet(10);
        values.set(v1);
        values.set(v2);
        removable.put(rescueCell, values);
        UniqueLoopType1Hint hint = new UniqueLoopType1Hint(this, loop, v1, v2, removable,
                rescueCell);
        return hint;
    }

    private UniqueLoopHint createType2Hint(Grid grid, List<Cell> loop, List<Cell> extraCells,
            int v1, int v2) {
        // Get the extra value
        BitSet common = (BitSet)grid.getCellPotentialValues(extraCells.get(0).getIndex()).clone();
        common.clear(v1);
        common.clear(v2);
        int value = common.nextSetBit(0);
        // Get removable potentials
        Map<Cell, BitSet> removable = new HashMap<Cell, BitSet>();
        CellSet commonCells = null;
        for (Cell extraCell : extraCells) {
            if (commonCells == null)
                commonCells = new CellSet(extraCell.getVisibleCells());
            else
                commonCells.retainAll(extraCell.getVisibleCells());
        }
        for (Cell cell : commonCells) {
            if (!extraCells.contains(cell)) {
                if (grid.hasCellPotentialValue(cell.getIndex(), value))
                    removable.put(cell, SingletonBitSet.create(value));
            }
        }
        Cell[] cells = new Cell[extraCells.size()];
        extraCells.toArray(cells);
        return new UniqueLoopType2Hint(this, loop, v1, v2, removable, cells, value);
    }

    private boolean containsFirst(int[] indexes, int index1, int index2) {
        boolean contains1 = false;
        for (int i = 0; i < indexes.length; i++) {
            if (indexes[i] == index1)
                contains1 = true;
            else if (indexes[i] == index2)
                return false;
        }
        return contains1;
    }

    private Collection<UniqueLoopHint> createType3Hints(Grid grid, List<Cell> loop,
            Cell c1, Cell c2, int v1, int v2) {
        Collection<UniqueLoopHint> result = new ArrayList<UniqueLoopHint>();
        // Get the extra values
        BitSet extra = (BitSet)grid.getCellPotentialValues(c1.getIndex()).clone();
        extra.or(grid.getCellPotentialValues(c2.getIndex()));
        extra.clear(v1);
        extra.clear(v2);
        // Look for Naked and hidden Sets. Iterate on degree
		if (Settings.getInstance().islkSudokuURUL()) {
        for (int degree = 2; degree <= 7; degree++) {
			for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
				if (!Settings.getInstance().isVLatin()) {
					if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
					if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
					if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
					if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
					if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
					if (Grid.cellRegions[c1.getIndex()][regionTypeIndex] < 0 || Grid.cellRegions[c2.getIndex()][regionTypeIndex] < 0)
						continue;
				}
                Grid.Region region = Grid.getRegionAt(regionTypeIndex, c1.getIndex());
				if (region.equals(Grid.getRegionAt(regionTypeIndex, c2.getIndex()))) {
                    // Region common to c1 and c2
                    int nbEmptyCells = region.getEmptyCellCount(grid);
                    int index1 = region.indexOf(c1);
                    int index2 = region.indexOf(c2);

                    // Look for naked sets
                    if ( (degree * 2 <= nbEmptyCells) && (degree >= extra.cardinality()) ) {
                        // Look on combinations of cells that include c1 but not c2
                        Permutations perm2 = new Permutations(degree, 9);
                        while (perm2.hasNext()) {
                            int[] indexes = perm2.nextBitNums();
                            assert indexes.length == degree;
                            if (containsFirst(indexes, index1, index2)) {
                                // This permutation contains c1 (but not c2)
                                BitSet[] potentials = new BitSet[degree];
                                // We have to ensure (c1 AND c2) OR otherCells = fullSet
                                // else, this is not a naked set with both c1 or c2
                                BitSet nakedSet = (BitSet)extra.clone();
                                nakedSet.and(grid.getCellPotentialValues(c1.getIndex()));
                                nakedSet.and(grid.getCellPotentialValues(c2.getIndex())); // Common to c1 and c2

                                Cell[] otherCells = new Cell[degree - 1];
                                int otherIndex = 0;
                                for (int i = 0; i < indexes.length; i++) {
                                    if (indexes[i] == index1)
                                        potentials[i] = extra; // Index of cell c1. Use extra potentials
                                    else {
                                        // Other cell. Use actual potentials
                                        Cell cell = region.getCell(indexes[i]);
                                        potentials[i] = grid.getCellPotentialValues(cell.getIndex());
                                        nakedSet.or(potentials[i]);
                                        otherCells[otherIndex++] = cell;
                                    }
                                }
                                if (nakedSet.cardinality() == degree) {
                                    // Look for a common tuple of potential values, with same degree
                                    BitSet commonPotentialValues =
                                        CommonTuples.searchCommonTuple(potentials, degree);
                                    if (commonPotentialValues != null) {
                                        // Potential naked set found
                                        UniqueLoopHint hint = createType3NakedHint(grid, loop, v1, v2, extra, region, c1, c2,
                                                otherCells, commonPotentialValues);
                                        if (hint.isWorth())
                                            result.add(hint);
                                    }
                                }
                            } // if containstFirst
                        } // while (perm.hasNext())
                    }

                    if (degree * 2 < nbEmptyCells) {
                        // Look for hidden sets
                        int[] remValues = new int[7 - extra.cardinality()];
                        for (int value = 1, dstIndex = 0; value <= 9; value++) {
                            if (value != v1 && value != v2 && !extra.get(value))
                                remValues[dstIndex++] = value;
                        }
                        if (degree - 2 <= remValues.length) {
                            Permutations perm1 = new Permutations(degree - 2, remValues.length);
                            while (perm1.hasNext()) {
                                int[] pValues = perm1.nextBitNums();
                                int[] values = new int[degree];
                                for (int i = 0; i < pValues.length; i++)
                                    values[i] = remValues[pValues[i]];
                                values[degree - 2] = v1;
                                values[degree - 1] = v2;
                                BitSet[] potentialIndexes = new BitSet[degree];
                                for (int i = 0; i < degree; i++) {
                                    potentialIndexes[i] = region.copyPotentialPositions(grid, values[i]);
                                    potentialIndexes[i].clear(index2); // Remove one of the two cells
                                }
                                BitSet commonPotentialPositions =
                                    CommonTuples.searchCommonTupleLight(potentialIndexes, degree);
                                if (commonPotentialPositions != null) {
                                    // Potential hidden set found
                                    BitSet hiddenValues = new BitSet(10);
                                    for (int i = 0; i < values.length; i++)
                                        hiddenValues.set(values[i]);
                                    UniqueLoopHint hint = createType3HiddenHint(grid, loop, v1, v2, extra, hiddenValues, region,
                                            c1, c2, commonPotentialPositions);
                                    if (hint.isWorth())
                                        result.add(hint);
                                }
                            }
                        }
                    }

                } // region common to c1 and c2
            } // for regionType
        } // for degree
		}
		else {
        for (int degree = extra.cardinality(); degree <= 7; degree++) {
			for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
				if (!Settings.getInstance().isVLatin()) {
					if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
					if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
					if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
					if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
					if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
					if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
					if (Grid.cellRegions[c1.getIndex()][regionTypeIndex] < 0 || Grid.cellRegions[c2.getIndex()][regionTypeIndex] < 0)
						continue;
				}
				Grid.Region region = Grid.getRegionAt(regionTypeIndex, c1.getIndex());
                if (region.equals(Grid.getRegionAt(regionTypeIndex, c2.getIndex()))) {
                    // Region common to c1 and c2
                    int nbEmptyCells = region.getEmptyCellCount(grid);
                    int index1 = region.indexOf(c1);
                    int index2 = region.indexOf(c2);
                    // Look for naked sets
                    if (degree * 2 <= nbEmptyCells) {
                        // Look on combinations of cells that include c1 but not c2
                        Permutations perm2 = new Permutations(degree, 9);
                        while (perm2.hasNext()) {
                            int[] indexes = perm2.nextBitNums();
                            assert indexes.length == degree;
                            if (containsFirst(indexes, index1, index2)) {
                                // This permutation contains c1 (but not c2)
                                BitSet[] potentials = new BitSet[degree];
                                // We have to ensure (c1 AND c2) OR otherCells = fullSet
                                // else, this is not a naked set with both c1 or c2
                                BitSet nakedSet = (BitSet)extra.clone();
                                nakedSet.and(grid.getCellPotentialValues(c1.getIndex()));
                                nakedSet.and(grid.getCellPotentialValues(c2.getIndex())); // Common to c1 and c2

                                Cell[] otherCells = new Cell[degree - 1];
                                int otherIndex = 0;
                                for (int i = 0; i < indexes.length; i++) {
                                    if (indexes[i] == index1)
                                        potentials[i] = extra; // Index of cell c1. Use extra potentials
                                    else {
                                        // Other cell. Use actual potentials
                                        Cell cell = region.getCell(indexes[i]);
                                        potentials[i] = grid.getCellPotentialValues(cell.getIndex());
                                        nakedSet.or(potentials[i]);
                                        otherCells[otherIndex++] = cell;
                                    }
                                }
                                if (nakedSet.cardinality() == degree) {
                                    // Look for a common tuple of potential values, with same degree
                                    BitSet commonPotentialValues =
                                        CommonTuples.searchCommonTuple(potentials, degree);
                                    if (commonPotentialValues != null) {
                                        // Potential naked set found
                                        UniqueLoopHint hint = createType3NakedHint(grid, loop, v1, v2, extra, region, c1, c2,
                                                otherCells, commonPotentialValues);
                                        if (hint.isWorth())
                                            result.add(hint);
                                    }
                                }
                            } // if containstFirst
                        } // while (perm.hasNext())
                    }

                    if (degree * 2 < nbEmptyCells) {
                        // Look for hidden sets
                        int[] remValues = new int[7 - extra.cardinality()];
                        for (int value = 1, dstIndex = 0; value <= 9; value++) {
                            if (value != v1 && value != v2 && !extra.get(value))
                                remValues[dstIndex++] = value;
                        }
                        if (degree - 2 <= remValues.length) {
                            Permutations perm1 = new Permutations(degree - 2, remValues.length);
                            while (perm1.hasNext()) {
                                int[] pValues = perm1.nextBitNums();
                                int[] values = new int[degree];
                                for (int i = 0; i < pValues.length; i++)
                                    values[i] = remValues[pValues[i]];
                                values[degree - 2] = v1;
                                values[degree - 1] = v2;
                                BitSet[] potentialIndexes = new BitSet[degree];
                                for (int i = 0; i < degree; i++) {
                                    potentialIndexes[i] = region.copyPotentialPositions(grid, values[i]);
                                    potentialIndexes[i].clear(index2); // Remove one of the two cells
                                }
                                BitSet commonPotentialPositions =
                                    CommonTuples.searchCommonTupleLight(potentialIndexes, degree);
                                if (commonPotentialPositions != null) {
                                    // Potential hidden set found
                                    BitSet hiddenValues = new BitSet(10);
                                    for (int i = 0; i < values.length; i++)
                                        hiddenValues.set(values[i]);
                                    UniqueLoopHint hint = createType3HiddenHint(grid, loop, v1, v2, extra, hiddenValues, region,
                                            c1, c2, commonPotentialPositions);
                                    if (hint.isWorth())
                                        result.add(hint);
                                }
                            }
                        }
                    }

                } // region common to c1 and c2
            } // for regionType
        } // for degree
		}
        return result;
    }

    private UniqueLoopHint createType3HiddenHint(Grid grid, List<Cell> loop, int v1, int v2,
            BitSet otherValues, BitSet hiddenValues,
            Grid.Region region, Cell c1, Cell c2, BitSet potentialIndexes) {
        // Build other value list
        int[] oValues = new int[otherValues.cardinality()];
        int dstIndex = 0;
        for (int value = 1; value <= 9; value++) {
            if (otherValues.get(value))
                oValues[dstIndex++] = value;
        }
        int index1 = region.indexOf(c1);
        int index2 = region.indexOf(c2);
        potentialIndexes.clear(index1);
        potentialIndexes.clear(index2);
        Map<Cell, BitSet> removable = new HashMap<Cell, BitSet>();
        for (int i = 0; i < 9; i++) {
            if (potentialIndexes.get(i)) {
                Cell cell = region.getCell(i);
                if (!cell.equals(c1) && !cell.equals(c2)) {
                    BitSet values = new BitSet(10);
                    for (int value = 1; value <= 9; value++) {
                        if (!hiddenValues.get(value) && grid.hasCellPotentialValue(cell.getIndex(), value)) {
                            values.set(value);
                        }
                    }
                    if (!values.isEmpty())
                        removable.put(cell, values);
                }
            }
        }
        int[] indexes = new int[potentialIndexes.cardinality()];
        for (int i = 0, j = 0; i < 9; i++) {
            if (potentialIndexes.get(i))
                indexes[j++] = i;
        }
        return new UniqueLoopType3HiddenHint(this, loop, v1, v2, removable, c1, c2, oValues, hiddenValues,
                region, indexes);
    }

    private UniqueLoopHint createType3NakedHint(Grid grid, List<Cell> loop, int v1, int v2, BitSet otherValues,
            Grid.Region region, Cell c1, Cell c2, Cell[] cells, BitSet commonPotentialValues) {
        // Build other value list
        int[] oValues = new int[otherValues.cardinality()];
        int dstIndex = 0;
        for (int value = 1; value <= 9; value++) {
            if (otherValues.get(value))
                oValues[dstIndex++] = value;
        }
        // Build naked set value list
        int[] nValues = new int[commonPotentialValues.cardinality()];
        dstIndex = 0;
        for (int value = 1; value <= 9; value++) {
            if (commonPotentialValues.get(value))
                nValues[dstIndex++] = value;
        }
        // Build removable potentials
		Map<Cell,BitSet> removable = new HashMap<Cell,BitSet>();
		if (Settings.getInstance().isVLatin()){
			for (int i = 0; i < 9; i++) {
				Cell otherCell = region.getCell(i);
				if (!Arrays.asList(cells).contains(otherCell)
						&& !c1.equals(otherCell) && !c2.equals(otherCell)) {
					// Get removable potentials
					BitSet removablePotentials = new BitSet(10);
					for (int value = 1; value <= 9; value++) {
						if (commonPotentialValues.get(value) && grid.hasCellPotentialValue(otherCell.getIndex(), value))
							removablePotentials.set(value);
					}
					if (!removablePotentials.isEmpty())
						removable.put(otherCell, removablePotentials);
				}
			}
		}
		else {
			//SudokuMonster: Genralized Naked sets if Variants
			for(int i = commonPotentialValues.nextSetBit(0); i >= 0; i = commonPotentialValues.nextSetBit(i + 1)) {
				CellSet Victims = null;
				for (Cell cell : cells)
					if (grid.hasCellPotentialValue(cell.getIndex(),i))
							if (Victims == null)
								Victims = new CellSet (cell.getVisibleCells());
							else
								Victims.retainAll(cell.getVisibleCells());
				if (grid.hasCellPotentialValue(c1.getIndex(),i))
					if (Victims == null)
						Victims = new CellSet (c1.getVisibleCells());
					else
						Victims.retainAll(c1.getVisibleCells());
				if (grid.hasCellPotentialValue(c2.getIndex(),i))
					if (Victims == null)
						Victims = new CellSet (c2.getVisibleCells());
					else
						Victims.retainAll(c2.getVisibleCells());
				for (Cell cell : Victims)
					if (grid.hasCellPotentialValue(cell.getIndex(), i)) {
						//eliminationsTotal++;
						if (removable.containsKey(cell))
							removable.get(cell).set(i);
						else
							removable.put(cell, SingletonBitSet.create(i));
					}
			}
		}
        return new UniqueLoopType3NakedHint(this, loop, v1, v2, removable, c1, c2,
                oValues, region, cells, nValues);
    }

    private UniqueLoopHint createType4Hint(Grid grid, List<Cell> loop, Cell c1, Cell c2,
            int v1, int v2) {
        // Look for v1 or v2 locked in a region of c1 and c2
        Grid.Region r1 = null;
        Grid.Region r2 = null;
        for (int regionTypeIndex = (Settings.getInstance().isBlocks() ? 0 : 1); regionTypeIndex < (Settings.getInstance().isVLatin() ? 3 : 10); regionTypeIndex++) {
        	if (!Settings.getInstance().isVLatin()) {
				if (regionTypeIndex == 3 && !Settings.getInstance().isDG()) continue;
				if (regionTypeIndex == 4 && !Settings.getInstance().isWindows()) continue;
				if (regionTypeIndex == 5 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 6 && !Settings.getInstance().isX()) continue;
				if (regionTypeIndex == 7 && !Settings.getInstance().isGirandola()) continue;
				if (regionTypeIndex == 8 && !Settings.getInstance().isAsterisk()) continue;
				if (regionTypeIndex == 9 && !Settings.getInstance().isCD()) continue;
					if (Grid.cellRegions[c1.getIndex()][regionTypeIndex] < 0 || Grid.cellRegions[c2.getIndex()][regionTypeIndex] < 0)
						continue;
			}
            Grid.Region region = Grid.getRegionAt(regionTypeIndex, c1.getIndex());
            if (region.equals(Grid.getRegionAt(regionTypeIndex, c2.getIndex()))) {
                // Region common to c1 and c2
                boolean hasValue1 = false;
                boolean hasValue2 = false;
                for (int i = 0; i < 9; i++) {
                    Cell cell = region.getCell(i);
                    if (!cell.equals(c1) && !cell.equals(c2)) {
                        if (grid.hasCellPotentialValue(cell.getIndex(), v1))
                            hasValue1 = true;
                        if (grid.hasCellPotentialValue(cell.getIndex(), v2))
                            hasValue2 = true;
                    }
                }
                if (!hasValue1)
                    r1 = region;
                if (!hasValue2)
                    r2 = region;
            }
        }
        Grid.Region region = null;
        int lockValue = -1;
        int remValue = -1;
        Map<Cell, BitSet> removable = new HashMap<Cell, BitSet>();
        if (r1 != null) {
            region = r1;
            lockValue = v1;
            remValue = v2;
            removable.put(c1, SingletonBitSet.create(v2));
            removable.put(c2, SingletonBitSet.create(v2));
        } else if (r2 != null) {
            region = r2;
            lockValue = v2;
            remValue = v1;
            removable.put(c1, SingletonBitSet.create(v1));
            removable.put(c2, SingletonBitSet.create(v1));
        }
        if (region != null)
            return new UniqueLoopType4Hint(this, loop, lockValue, remValue, removable,
                    c1, c2, region);
        return null;
    }

    @Override
    public String toString() {
        return "Unique patterns";
    }

}
