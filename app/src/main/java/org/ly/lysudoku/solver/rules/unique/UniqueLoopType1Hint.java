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

import java.util.*;




public class UniqueLoopType1Hint extends UniqueLoopHint {

    private final Cell target;


    public UniqueLoopType1Hint(UniqueLoops rule, List<Cell> loop, int v1, int v2,
            Map<Cell, BitSet> removablePotentials, Cell target) {
        super(rule, loop, v1, v2, removablePotentials);
        this.target = target;
    }


    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        BitSet removable = new BitSet(10);
        removable.set(v1);
        removable.set(v2);
        return Collections.singletonMap(target, removable);
    }

    @Override
    public int getType() {
        return 1;
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
        String result = HtmlLoader.loadHtml(this, "UniqueLoopType1.html");
        String type = getTypeName();
        String cellName = target.toString();
        Cell[] cells = new Cell[loop.size()];
        loop.toArray(cells);
        String allCells = Cell.toString(cells);
        result = HtmlLoader.format(result, type, v1, v2, allCells, cellName, sharedRegions());
        return result;
    }

}
