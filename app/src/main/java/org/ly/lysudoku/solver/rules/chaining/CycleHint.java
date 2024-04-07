/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules.chaining;

import java.util.*;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Link;
import org.ly.lysudoku.Settings;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.HtmlLoader;
import org.ly.lysudoku.tools.ValuesFormatter;


/**
 * Bidirectional Cycle hint.
 */
public class CycleHint extends ChainingHint {

    private final Potential dstOn;
    private final Potential dstOff;

    // Cache
    private int _complexity = -1;


    public CycleHint(IndirectHintProducer rule, Map<Cell, BitSet> removablePotentials,
            boolean isYChain, boolean isXChain, Potential dstOn, Potential dstOff) {
        super(rule, removablePotentials, isYChain, isXChain);
        this.dstOn = dstOn;
        this.dstOff = dstOff;
    }

    @Override
    public int getFlatViewCount() {
        return 2;
    }

    @Override
    public Cell[] getSelectedCells() {
        Collection<Cell> cells = new LinkedHashSet<Cell>();
        Potential current = this.dstOff;
        while (!current.parents.isEmpty()) {
            current = current.parents.get(0);
            cells.add(current.cell);
        }
        Cell[] result = new Cell[cells.size()];
        cells.toArray(result);
        return result;
    }

    private Map<Cell, BitSet> getColorPotentials(int viewNum, boolean state) {
        return getColorPotentials(
                (viewNum == 0 ? this.dstOn : this.dstOff), state, false);
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        return getColorPotentials(viewNum, true);
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        Map<Cell, BitSet> result = getColorPotentials(viewNum, false);
        Map<Cell, BitSet> removable = getRemovablePotentials();
        for (Map.Entry<Cell, BitSet> entry : removable.entrySet()) {
        	Cell c = entry.getKey();
            BitSet values = entry.getValue();
            BitSet reds = result.get(c);
            if (reds == null) {
                reds = new BitSet(10);
                result.put(c, reds);
            }
            reds.or(values);
        }
        return result;
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        Potential start = (viewNum == 0 ? this.dstOn : this.dstOff);
        return getLinks(start);
    }

    @Override
    public int getFlatComplexity() {
        if (_complexity < 0)
            _complexity = getAncestorCount(dstOn);
        return _complexity;
    }

    @Override
    protected Collection<Potential> getChainsTargets() {
        Collection<Potential> result = new ArrayList<Potential>(2);
        result.add(this.dstOn);
        result.add(this.dstOff);
        return result;
    }

    @Override
    protected Potential getChainTarget(int viewNum) {
        /*
         * This is only used to collect blue potential.
         * Because the reversed chain misses some causes, and
         * because the two chains are equivalent, we always
         * return the first one.
         */
        return this.dstOn;
    }

    @Override
    public int getSortKey() {
        if (isYChain && isXChain)
            return 4;
        else if (isYChain)
            return 3;
        else
            return 2;
    }

    public double getDifficulty() {
        double result;
        if (isYChain && isXChain)
            result = 7.0;
        else
            result = 6.5;
        return result + getLengthDifficulty();
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    protected Potential getResult() {
        return null; // No single result
    }

    public String getName() {
        if (isXChain && isYChain)
            return "Bidirectional Cycle";
        else if (isYChain)
            return "Bidirectional Y-Cycle";
        else {
            if (getSelectedCells().length == 4)
                return "Generalized X-Wing";
            else
                return "Bidirectional X-Cycle";
        }
    }

    public String getShortName() {
        if (isXChain && isYChain)
            return "BiCy";
        else if (isYChain)
            return "BiYCy";
        else {
            if (getSelectedCells().length == 4)
                return "GXW";
            else
                return "BiXCy";
        }
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            if (isXChain && !isYChain) {
                return "Look for a " + getName() +
                " with the value <b>" + dstOn.value + "</b>";
            } else {
                return "Look for a " + getName() +
                " touching the cell <b>" + dstOn.cell.toString() + "</b>";
            }
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() + ": " + Cell.toString(getSelectedCells());
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
        String fileName;
        if (isXChain && isYChain)
            fileName = "XY-Cycle.html";
        else if (isXChain)
            fileName = "X-Cycle.html";
        else
            fileName = "Y-Cycle.html";
        String result = HtmlLoader.loadHtml(this, fileName);
        String cells = ValuesFormatter.formatCells(getSelectedCells(), " and ");
        String value = Integer.toString(dstOn.value);
        String commonName = "";
        if (!isYChain && getSelectedCells().length == 4)
            commonName = "(Generalized X-Wing)";
        String onChain = getHtmlChain(dstOn);
        String offChain = getHtmlChain(dstOff);
        result = HtmlLoader.format(result, cells, value, commonName,
                onChain, offChain, sharedRegions());
        return result;
    }

}
