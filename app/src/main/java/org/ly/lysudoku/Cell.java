/*
 * Copyright (C) 2009 Roman Masek
 *
 * This file is part of OpenSudoku.
 *
 * OpenSudoku is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenSudoku is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenSudoku.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.ly.lysudoku;

import org.ly.lysudoku.tools.CellSet;

import java.io.Serializable;
import java.util.BitSet;
import java.util.StringTokenizer;

public class Cell implements Serializable {

    private final int index;

    /**
     * Create a new cell
     */
    public Cell(int index) {
        this.index = index;
    }

    /**
     * Get the x coordinate of this cell.
     * 0 = leftmost, 8 = rightmost
     *
     * @return the x coordinate of this cell
     */
    public int getX() {
        return this.index % 9;
    }

    /**
     * Get the y coordinate of this cell.
     * 0 = topmost, 8 = bottommost
     *
     * @return the y coordinate of this cell
     */
    public int getY() {
        return this.index / 9;
    }

    /**
     * Get the b (box) index of this cell.
     * 0 = top left, 8 = bottomm right
     *
     * @return the b coordinate of this cell
     */
    public int getB() {
        return this.index % 9 / 3 + this.index / 27 * 3;
    }

    /**
     * Get the d (disjoint group, box position group) index of this cell.
     * 0 = top left box position, 8 = bottomm right box position
     *
     * @return the y coordinate of this cell
     */
    public int getD() {
        return this.index % 3 + ((this.index / 9) % 3) * 3;
    }

    /**
     * Get the W (window) index of this cell.
     * regionsWindows in Settings has the configuration
     *
     * @return the W coordinate of this cell
     */
    public int getW() {
        return Settings.regionsWindows[this.index];
    }

    /**
     * Get the Main Diagonal index of this cell. [1 or 0]
     * regionsMainDiagonal in Settings has the configuration
     *
     * @return the y coordinate of this cell
     */
    public int getMD() {
        return Settings.regionsMainDiagonal[this.index];
    }

    /**
     * Get the Anti Diagonal index of this cell. [1 or 0]
     * regionsWindows in Settings has the configuration
     *
     * @return the AD coordinate of this cell
     */
    public int getAD() {
        return Settings.regionsAntiDiagonal[this.index];
    }

    /**
     * Get the G (girandola) index of this cell. [1 or 0]
     * regionsWindows in Settings has the configuration
     *
     * @return the G coordinate of this cell
     */
    public int getG() {
        return Settings.regionsGirandola[this.index];
    }

    /**
     * Get the A (Asterisk) index of this cell. [1 or 0]
     * regionsWindows in Settings has the configuration
     *
     * @return the A coordinate of this cell
     */
    public int getA() {
        return Settings.regionsAsterisk[this.index];
    }

    /**
     * Get the CD (Center Dot) index of this cell. [1 or 0]
     * regionsWindows in Settings has the configuration
     *
     * @return the CD coordinate of this cell
     */
    public int getCD() {
        return Settings.regionsCD[this.index];
    }

    /**
     * Get the index of this cell.
     * 0 ..8 = top row, ... 81 = bottom right
     *
     * @return the index of this cell
     */
    public int getIndex() {
        return index;
    }

    //SudokuMonster: Believe it or not this method is the most important one when you think of adding variants :)
//Anti Chess will not require any changes as they have been included in Visible cells
//Other forbidden pairs including non consecutive will require adjustments here
//@SudokuMonster: Changes to allow for FP (NC)
    public void setCellPotentialValuesAndCancel(BitSet values, Grid targetGrid) {
        targetGrid.setCellPotentialValues(this.index, values);
        //targetGrid.onChange();
    }

    /**
     * Set the value of this cell, and remove that value
     * from the potential values of all controlled cells.
     * <p>
     * This cell must be empty before this call, and the
     * given value must be non-zero.
     *
     * @param value the value to set this cell to.
     */
    public void setValueAndCancel(int value, Grid targetGrid) {
        //assert value != 0;
        targetGrid.setCellValue(this.index, value);
        targetGrid.clearCellPotentialValues(this.index);
//SudokuMonster: Grid.visibleCellIndex[this.index].length changes with Variants and can be variable in some variants (i.e. Sudoku X)
        int j = Grid.visibleCellIndex[this.index].length;
        for (int i = 0; i < j; i++) {
            targetGrid.removeCellPotentialValue(Grid.visibleCellIndex[this.index][i], value);
        }
        if (Settings.getInstance().isForbiddenPairs()) {
            int statusNC = Settings.getInstance().whichNC();
            if (statusNC > 0)
                if (Settings.getInstance().isToroidal()) {
                    if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
                        j = Grid.wazirCellsToroidal[this.index].length;
                    else
                        j = Grid.ferzCellsToroidal[this.index].length;
                    for (int i = 0; i < j; i++) {
                        if (statusNC == 2 || statusNC == 4 || value < 9)
                            if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
                                targetGrid.removeCellPotentialValue(Grid.wazirCellsToroidal[this.index][i], value == 9 ? 1 : value + 1);
                            else
                                targetGrid.removeCellPotentialValue(Grid.ferzCellsToroidal[this.index][i], value == 9 ? 1 : value + 1);
                        if (statusNC == 2 || statusNC == 4 || value > 1)
                            if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
                                targetGrid.removeCellPotentialValue(Grid.wazirCellsToroidal[this.index][i], value == 1 ? 9 : value - 1);
                            else
                                targetGrid.removeCellPotentialValue(Grid.ferzCellsToroidal[this.index][i], value == 1 ? 9 : value - 1);
                    }
                } else {
                    if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
                        j = Grid.wazirCellsRegular[this.index].length;
                    else
                        j = Grid.ferzCellsRegular[this.index].length;
                    for (int i = 0; i < j; i++) {
                        if (statusNC == 2 || statusNC == 4 || value < 9)
                            if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
                                targetGrid.removeCellPotentialValue(Grid.wazirCellsRegular[this.index][i], value == 9 ? 1 : value + 1);
                            else
                                targetGrid.removeCellPotentialValue(Grid.ferzCellsRegular[this.index][i], value == 9 ? 1 : value + 1);
                        if (statusNC == 2 || statusNC == 4 || value > 1)
                            if (Settings.getInstance().whichNC() == 1 || Settings.getInstance().whichNC() == 2)
                                targetGrid.removeCellPotentialValue(Grid.wazirCellsRegular[this.index][i], value == 1 ? 9 : value - 1);
                            else
                                targetGrid.removeCellPotentialValue(Grid.ferzCellsRegular[this.index][i], value == 1 ? 9 : value - 1);
                    }
                }
        }
        targetGrid.onChange();
    }

    /**
     * Get the cells that form the "house" of this cell. The
     * "house" cells are all the cells that are in the
     * same block, row or column. The cell itself isn't included.
     * <p>
     *
     * @return the cells that are controlled by this cell
     */
    public CellSet getVisibleCells() {
        // Use a set to prevent duplicates (cells in both block and row/column)
        //return new CellSet(Grid.visibleCellIndex[index]);
        return Grid.visibleCellsSet[index];
    }

    public boolean canSeeCell(Cell other) {
        return Grid.visibleCellsSet[index].contains(other);
    }

    public boolean canSeeAnyOfCells(CellSet cellSet) {
        return Grid.visibleCellsSet[index].containsAny(cellSet);
//		CellSet currentSet = new CellSet(Grid.visibleCellsSet[index]);
//		currentSet.retainAll(cellSet);
//		int currentSetSize = currentSet.size();
//		if (currentSetSize > 0)
//			return true;
//		return false;
    }


    /**
     * Get the cells that form the "house" of this cell. The
     * cell indexes have to be greater than this cell index. The "house"
     * cells are all the cells that are in the same block, row or column.
     * <p>
     * The iteration order is guaranteed to be the same on each
     * invocation of this method for the same cell. (this is
     * necessary to ensure that hints of the same difficulty
     * are always returned in the same order).
     *
     * @return the cells that are controlled by this cell
     */
    public CellSet getForwardVisibleCells() {
        // Use a set to prevent duplicates (cells in both block and row/column)
        //return new CellSet(Grid.visibleCellIndex[index]);
        return Grid.forwardVisibleCellsSet[index];
    }

    /**
     * Get the cell idexes that form the "house" of this cell. The
     * "house" cells are all the cells that are in the
     * same block, row or column.
     * <p>
     * The iteration order is guaranted to be the same on each
     * invocation of this method for the same cell. (this is
     * necessary to ensure that hints of the same difficulty
     * are always returned in the same order).
     *
     * @return array of the cell indexes that are controlled by this cell
     */
    public int[] getVisibleCellIndexes() {
        return Grid.visibleCellIndex[index];
    }

    /**
     * Get the cell indexes that form the "house" of this cell. The
     * cell indexes have to be greater than this cell index. The "house"
     * cells are all the cells that are in the same block, row or column.
     * <p>
     * The iteration order is guaranted to be the same on each
     * invocation of this method for the same cell. (this is
     * necessary to ensure that hints of the same difficulty
     * are always returned in the same order).
     *
     * @return array of the cell indexes that are controlled by this cell
     */
    public int[] getForwardVisibleCellIndexes() {
        return Grid.forwardVisibleCellIndex[index];
    }

    /**
     * Get a string representation of a cell. The notation that
     * is used is defined by the {@link Settings} class.
     *
     * @param x the horizontal coordinate of the cell (0=leftmost, 8=rightmost)
     * @param y the vertical coordinate of the cell (0=topmost, 8=bottommost)
     * @return a string representation of the cell
     */
    private static String toString(int x, int y) {
        Settings settings = Settings.getInstance();
        if (settings.isRCNotation())
            return "r" + (y + 1) + "c" + (x + 1);
        else
            return "" + (char) ('A' + y) + (x + 1);
    }

    /**
     * Get a complete string representation of this cell.
     * <p>
     * Returns "Cell " followed by the result of the {@link #toString()} method.
     *
     * @return a complete string representation of this cell.
     */
    public String toFullString() {
        return "Cell " + toString(getX(), getY());
    }

    /**
     * Get a string representation of this cell.
     * <p>
     * Returned strings are in the form "A1", "A2", "A3", ...
     * "I9".
     *
     * @return a string representation of this cell.
     */
    @Override
    public String toString() {
        return toString(getX(), getY());
    }

    /**
     * Get a full string representation of multiple cells.
     * <p>
     * The returned string might be, for example:
     * "Cells A1, B4, C3"
     *
     * @param cells the cells
     * @return a full string representation of the cells
     */
    public static String toFullString(Cell... cells) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cell");
        if (cells.length <= 1)
            builder.append(" ");
        else
            builder.append("s ");
        for (int i = 0; i < cells.length; i++) {
            if (i > 0)
                builder.append(",");
            Cell cell = cells[i];
            builder.append(toString(cell.getX(), cell.getY()));
        }
        return builder.toString();
    }

    /**
     * Get a string representation of multiple cells.
     * The returned string is a concatenation of the
     * result of calling {@link #toString()} on each cell,
     * separated by ",".
     *
     * @param cells the cells to convert to a string
     * @return a string representation of the given cells.
     */
    public static String toString(Cell... cells) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0)
                builder.append(",");
            Cell cell = cells[i];
            builder.append(toString(cell.getX(), cell.getY()));
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cell))
            return false;
        if (this == o) return true;
        Cell other = (Cell) o;
        if (index != other.getIndex()) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getIndex();
    }


}

