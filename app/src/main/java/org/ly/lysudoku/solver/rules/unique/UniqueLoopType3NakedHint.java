/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules.unique;

import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Settings;
import org.ly.lysudoku.tools.HtmlLoader;
import org.ly.lysudoku.tools.ValuesFormatter;

import java.util.*;



public class UniqueLoopType3NakedHint extends UniqueLoopHint {

    private final Cell c1;
    private final Cell c2;
    private final int[] otherValues;
    private final Grid.Region region;
    private final Cell[] cells; // other cells of the naked set
    private final int[] nakedValues; // values of the naked set


    public UniqueLoopType3NakedHint(UniqueLoops rule, List<Cell> loop, int v1, int v2,
            Map<Cell, BitSet> removablePotentials, Cell c1, Cell c2, int[] otherValues,
            Grid.Region region, Cell[] cells, int[] values) {
        super(rule, loop, v1, v2, removablePotentials);
        this.c1 = c1;
        this.c2 = c2;
        this.otherValues = otherValues;
        this.region = region;
        this.cells = cells;
        this.nakedValues = values;
    }

    @Override
    public double getDifficulty() {
        double toAdd = (nakedValues.length - 1) * 0.1; // Pair=0.1, Quad=0.3
        return super.getDifficulty() + toAdd;
    }

    private Map<Cell, BitSet> appendOrangePotentials(Map<Cell, BitSet> potentials) {
        BitSet nakedSet = new BitSet(10);
        for (int i = 0; i < nakedValues.length; i++)
            nakedSet.set(nakedValues[i]);
        for (Cell cell : cells)
            potentials.put(cell, nakedSet);

        BitSet otherSet = new BitSet(10);
        for (int i = 0; i < otherValues.length; i++)
            otherSet.set(otherValues[i]);
        BitSet prevSet = potentials.get(c1);
        if (prevSet == null)
            prevSet = otherSet;
        else
            prevSet.or(otherSet);
        potentials.put(c1, prevSet);
        prevSet = potentials.get(c2);
        if (prevSet == null)
            prevSet = otherSet;
        else
            prevSet.or(otherSet);
        potentials.put(c2, prevSet);
        return potentials;
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        return appendOrangePotentials(super.getGreenPotentials(grid, viewNum));
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>(super.getRemovablePotentials());
        return appendOrangePotentials(result);
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {region};
    }

    @Override
    public int getType() {
        return 3;
    }

	private String sharedRegions(){
		if (Settings.getInstance().isVanilla())
			return "row, column or block";
		else {
			String res[] = new String[10];
			int i = 0;
			String finalRes = "row";
			if (Settings.getInstance().isVLatin())
				return "row or column";
			else
				res[i++]= "column";
			if (Settings.getInstance().isBlocks())
				res[i++]= "block";
			if (Settings.getInstance().isDG())
				res[i++]= "disjoint group";
			if (Settings.getInstance().isWindows())
				res[i++]= "window group";
			if (Settings.getInstance().isX())
				res[i++]= "diagonal";
			if (Settings.getInstance().isGirandola())
				res[i++]= "girandola group";
			if (Settings.getInstance().isAsterisk())
				res[i++]= "asterisk group";
			if (Settings.getInstance().isCD())
				res[i++]= "center dot group";
			i--;
			for (int j = 0; j < i; j++)
				finalRes += ", " + res[j];
			finalRes += " or " + res[i];
			return finalRes;
		}
	}

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, "UniqueLoopType3Naked.html");
        String type = getTypeName();
        Cell[] loopCells = new Cell[loop.size()];
        loop.toArray(loopCells);
        String allCells = Cell.toString(loopCells);
        String cell1 = c1.toString();
        String cell2 = c2.toString();
        String valuesOrName = ValuesFormatter.formatValues(otherValues, " or ");
        final String[] setNames = new String[] {"Pair", "Triplet", "Quad", "Quintuplet",
                "Sextuplet", "Septuplet"};
        String setName = setNames[nakedValues.length - 2];
        String otherCells = ValuesFormatter.formatCells(cells, " and ");
        String valuesAndName = ValuesFormatter.formatValues(nakedValues, " and ");
        String regionName = region.toString();
        result = HtmlLoader.format(result, type, v1, v2, allCells, cell1,
                cell2, valuesOrName, setName, otherCells, valuesAndName, regionName, sharedRegions());
        return result;
    }

    /**
     * Overriden to differentiate hints with different naked sets.
     * <p>
     * Because we only make different objects that are equal according
     * to <tt>super.equals()</tt>, <tt>hashCode()</tt> does not need
     * to be overriden.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UniqueLoopType3NakedHint))
            return false;
        if (!super.equals(o))
            return false;
        UniqueLoopType3NakedHint other = (UniqueLoopType3NakedHint)o;
        if (!this.region.equals(other.region))
            return false;
        if (this.nakedValues.length != other.nakedValues.length)
            return false;
        for (int i = 0; i < nakedValues.length; i++) {
            if (this.nakedValues[i] != other.nakedValues[i])
                return false;
        }
        for (int i = 0; i < cells.length; i++) {
            if (!this.cells[i].equals(other.cells[i]))
                return false;
        }
        return true;
    }

}
