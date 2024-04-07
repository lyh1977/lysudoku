/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver;

/**
 * Interface for rules that are able to produce direct hints.
 * @see org.ly.lysudoku.solver.DirectHint
 */
public interface DirectHintProducer extends HintProducer {

    public String toString();

}
