/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver.checks;

import org.ly.lysudoku.Grid;
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.HtmlLoader;

/**
 * A hint that just shows an arbitrary warning or information message
 */
public class WarningMessage extends WarningHint {

    private final String message;
    private final String htmlFile;
    private final Object[] args;

    public WarningMessage(WarningHintProducer rule, String message,
            String htmlFile, Object... args) {
        super(rule);
        this.message = message;
        this.htmlFile = htmlFile;
        this.args = args;
    }

    @Override
    public Grid.Region[] getRegions() {
        return null;
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String toHtml(Grid grid) {
        String result = HtmlLoader.loadHtml(this, htmlFile);
        return HtmlLoader.format(result, args);
    }

}
