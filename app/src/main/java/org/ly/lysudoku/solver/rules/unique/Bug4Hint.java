/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules.unique;

import java.util.*;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Link;
import org.ly.lysudoku.Settings;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.HtmlLoader;
import org.ly.lysudoku.tools.ValuesFormatter;


public class Bug4Hint extends BugHint implements Rule {

    private final Cell bugCell1;
    private final Cell bugCell2;
    private final Map<Cell, BitSet> extraValues;
    private final BitSet allExtraValues;
    private final int value; // removable value appearing on both cells
    private final Grid.Region region;


    public Bug4Hint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            Cell bugCell1, Cell bugCell2, Map<Cell, BitSet> extraValues,
            BitSet allExtraValues, int value, Grid.Region region) {
        super(rule, removablePotentials);
        this.bugCell1 = bugCell1;
        this.bugCell2 = bugCell2;
        this.extraValues = extraValues;
        this.allExtraValues = allExtraValues;
        this.value = value;
        this.region = region;
    }

    @Override
    public int getViewCount() {
        return 1;
    }

    @Override
    public Cell[] getSelectedCells() {
        return new Cell[] {bugCell1, bugCell2};
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        BitSet b1 = (BitSet)extraValues.get(bugCell1).clone();
        b1.set(value); // orange
        result.put(bugCell1, b1);
        BitSet b2 = (BitSet)extraValues.get(bugCell2).clone();
        b2.set(value); // orange
        result.put(bugCell2, b2);
        return result;
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> removable = super.getRemovablePotentials();
        Map<Cell, BitSet> result = new HashMap<Cell, BitSet>();
        for (Map.Entry<Cell, BitSet> entry : removable.entrySet()) {
        	Cell cell = entry.getKey();
            BitSet values = (BitSet)entry.getValue().clone();
            values.set(value); // orange
            result.put(cell, values);
        }
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        return null;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {this.region};
    }

    public String getName() {
        return "BUG type 4";
    }

    public String getShortName() {
        return "BUG4";
    }

    public double getDifficulty() {
        return 5.7;
    }

    @Override
    public String toString() {
        return "BUG type 4: " + bugCell1.toString() + "," + bugCell2.toString() + " on " + value;
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
        String result = HtmlLoader.loadHtml(this, "BivalueUniversalGrave4.html");
        String bugValuesAnd = ValuesFormatter.formatValues(allExtraValues, " and ");
        String bugCellsAnd = ValuesFormatter.formatCells(new Cell[] {bugCell1, bugCell2}, " and ");
        String bugCellsOr = ValuesFormatter.formatCells(new Cell[] {bugCell1, bugCell2}, " or ");
        String bugValuesOr = ValuesFormatter.formatValues(allExtraValues, " or ");
        String lockedValue = Integer.toString(value);
        String regionName = region.toString();
        BitSet removable = new BitSet();
        for (BitSet r : getRemovablePotentials().values())
            removable.or(r);
        String removableValues = ValuesFormatter.formatValues(removable, " and ");
        return HtmlLoader.format(result, bugValuesAnd, bugCellsAnd, bugCellsOr, bugValuesOr,
                lockedValue, regionName, removableValues, sharedRegions());
    }

}
