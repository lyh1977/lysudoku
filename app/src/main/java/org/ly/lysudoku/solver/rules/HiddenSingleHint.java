/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.rules;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.HtmlLoader;


/**
 * Hidden Single hint
 */
public class HiddenSingleHint extends DirectHint implements Rule {

    private final boolean isAlone; // Last empty cell in a region


    public HiddenSingleHint(DirectHintProducer rule, Grid.Region region, Cell cell, int value,
                            boolean isAlone) {
        super(rule, region, cell, value);
        this.isAlone = isAlone;
    }

    public double getDifficulty() {
        if (isAlone)
            return 1.0;
        //else if (getRegion() instanceof Grid.Block)
        else if (getRegion().getRegionTypeIndex() == 0) //block
            return 1.2;
        else
            return 1.5;
    }

    public String getName() {
        return "Hidden Single";
    }

    public String getShortName() {
        return "HS";
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
            " in the <b1>" + getRegion().toFullString() + "</b1>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        return getName() + ": " + super.toString();
    }

    @Override
    public String toHtml(Grid grid) {
        String result;
        if (isAlone)
            result = HtmlLoader.loadHtml(this, "Single.html");
        else
            result = HtmlLoader.loadHtml(this, "HiddenSingleHint.html");
        return HtmlLoader.format(result, super.getCell().toString(),
                Integer.toString(super.getValue()), super.getRegions()[0].toString());
    }

}
