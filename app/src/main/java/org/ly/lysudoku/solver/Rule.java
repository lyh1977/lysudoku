/*
 * Project: Sudoku Explainer
 * Copyright (C) 2006-2007 Nicolas Juillerat
 * Available under the terms of the Lesser General Public License (LGPL)
 */
package org.ly.lysudoku.solver;


import org.ly.lysudoku.Grid;

/**
 * A "classified hint" that can be used to advance one step in the
 * solving process of a Sudoku.
 * "Pseudo" hints such as warnings, analyses and informations do not
 * implement this interface.
 */
public interface Rule {

    /**
     * Get the name of this rule.
     * <p>
     * This method will return the name of well-known rules such as
     * "naked pair", "X-Wing", etc.
     * @return the name of this rule
     */
    public String getName();

    /**
     * Get the short version of Rule Name
     */
	public String getShortName();

    /**
     * Get the difficulty rating of this rule.
     * <p>
     * Currently, the following classification is used:
     * <ul>
     * <li>1.2: Hidden single (1.5 if not in block)
     * <li>1.7: Direct Pointing
     * <li>1.9: Direct Claiming
     * <li>2.0: Direct Hidden Pair
     * <li>2.3: Naked single
     * <li>2.5: Direct Hidden Triplet
     * <li>2.6: Pointing
     * <li>2.8: Claiming
     * <li>3.0, 3.2, 3.4: Naked pair, X-Wing, Hidden pair
     * <li>3.6, 3.8, 4.0: Naked triplet, Swordfish, Hidden triplet
     * <li>4.0, 4.1, 4.2: Skyscraper, 2-String Kite, Turbot Fish
     * <li>4.2, 4.4: XY-Wing, XYZ-Wing
//     * <li>4.4: W-Wing
     * <li>4.5 - 5.0: Unique Rectangles and Loops
     * <li>5.0, 5.2, 5.4: Naked quad, Jellyfish, Hidden quad
     * <li>5.4, 5.5, 5.6: Skyscraper (3SL) , 3-String Kite, Turbot Fish (3SL)
	 * <li>5.5: WXYZ-Wing
     * <li>5.6 - 6.0: Bivalue Universal Graves
	 * <li>6.2 - 6.4: VWXYZ-Wing
     * <li>6.2: Aligned Pair Exclusion
     * <li>6.5 - 7.5: X-Cycles, Y-Cycles
     * <li>6.6 - 7.6: Forcing X-Chains
     * <li>7.0 - 8.0: Forcing Chains, XY-Cycles
     * <li>7.5: Aligned Triplet Exclusion
     * <li>7.5 - 8.5: Nishio
     * <li>8.0 - 9.0: Multiple chains
     * <li>8.5 - 9.5: Dynamic chains
     * <li>9.0 - 10.0: Dynamic chains (+)
     * <li>&gt; 9.5: Nested Forcing Chains
     * </ul>
     * Upper bound for chains is actually unbounded: the longer chain, the higher rating.
     * @return the difficulty rating of this rule.
     */

//New changes
    /**
     * Get the difficulty rating of this rule.
     * <p>
     * Currently, the following classification is used:
     * <ul>
     * <li>1.2: Hidden single (1.5 if not in block)
     * <li>1.6: Naked single//2.3 ---> 1.6
     * <li>1.7: Direct Pointing
     * <li>1.9: Direct Claiming
     * <li>2.0: Direct Hidden Pair
     * <li>2.6: Pointing
     * <li>2.8: Claiming
     * <li>2.9: Hidden pair//3.4 ---> 2.9
     * <li>3.0: Naked pair//3.0 ---> 3.1 ---> 3.0
     * <li>3.1: Direct Hidden Triplet//2.5 ---> 3.0 ---> 3.1
     * <li>3.2: X-Wing
     * <li>3.6, 3.8, 4.0: Naked triplet, Hidden triplet, Swordfish//3.8 ---> 4.0 4.0 ---> 3.8
     * <li>4.0, 4.1, 4.2: Skyscraper, 2-String Kite, Turbot Fish (placed before Swordfish)
     * <li>4.2, 4.4: XY-Wing, XYZ-Wing
//     * <li>4.4: W-Wing
     * <li>4.5 - 5.0: Unique Rectangles and Loops
     * <li>5.0, 5.2, 5.4: Naked quad, Hidden quad, Jellyfish//5.2 ---> 5.4 5.4 ---> 5.2
     * <li>5.4, 5.5, 5.6: Skyscraper (3SL) , 3-String Kite, Turbot Fish (3SL)
	 * <li>5.5: WXYZ-Wing
     * <li>5.6 - 6.0: Bivalue Universal Graves
	 * <li>6.2 - 6.4: VWXYZ-Wing
     * <li>6.2: Aligned Pair Exclusion
     * <li>6.5 - 7.5: X-Cycles, Y-Cycles
     * <li>6.6 - 7.6: Forcing X-Chains
     * <li>7.0 - 8.0: Forcing Chains, XY-Cycles
     * <li>7.5: Aligned Triplet Exclusion
     * <li>7.5 - 8.5: Nishio
     * <li>8.0 - 9.0: Multiple chains
     * <li>8.5 - 9.5: Dynamic chains
     * <li>9.0 - 10.0: Dynamic chains (+)
     * <li>&gt; 9.5: Nested Forcing Chains
     * </ul>
     * Upper bound for chains is actually unbounded: the longer chain, the higher rating.
     * @return the difficulty rating of this rule.
     */

    public double getDifficulty();

    /**
     * Get a clue, or a "partial hint", as an HTML string.
     * @param isBig <tt>true</tt> to get a big clue, that is, a
     * nearly complete hint; <tt>false</tt> to get a small clue
     * that is, a very partial hint.
     * @return a clue, in HTML
     */
    public String getClueHtml(Grid grid, boolean isBig);


}
