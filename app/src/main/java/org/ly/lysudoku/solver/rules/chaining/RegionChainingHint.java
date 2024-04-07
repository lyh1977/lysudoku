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
import org.ly.lysudoku.solver.*;
import org.ly.lysudoku.tools.HtmlLoader;


/**
 * Region Forcing Chain hint.
 */
public class RegionChainingHint extends ChainingHint {

    private final Grid.Region region;
    private final int value;
    private Map<Integer, Potential> chains;


    public RegionChainingHint(IndirectHintProducer rule,
                              Map<Cell, BitSet> removablePotentials, Grid.Region region, int value,
                              Map<Integer, Potential> chains) {
        super(rule, removablePotentials, true, true);
        this.region = region;
        this.value = value;
        this.chains = chains;
    }

    private int getValue(int index) {
        Iterator<Integer> iter = chains.keySet().iterator();
        while (index > 0) {
            iter.next();
            index--;
        }
        return iter.next();
    }

    private Potential getTargetPotential(int viewNum) {
        int value = getValue(viewNum);
        Potential target = chains.get(value);
        return target;
    }

    @Override
    public int getFlatViewCount() {
        return chains.size();
    }

    @Override
    public Cell[] getSelectedCells() {
        Cell dstCell = chains.values().iterator().next().cell;
        return new Cell[] {dstCell};
    }

    @Override
    public Map<Cell, BitSet> getGreenPotentials(Grid grid, int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedGreenPotentials(grid, viewNum);
        Potential target = getTargetPotential(viewNum);
        return super.getColorPotentials(target, true, true);
    }

    @Override
    public Map<Cell, BitSet> getRedPotentials(Grid grid, int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedRedPotentials(grid, viewNum);
        Potential target = getTargetPotential(viewNum);
        return super.getColorPotentials(target, false, false);
    }

    @Override
    public Collection<Link> getLinks(Grid grid, int viewNum) {
        if (viewNum >= getFlatViewCount())
            return super.getNestedLinks(grid, viewNum);
        Potential target = getTargetPotential(viewNum);
        return super.getLinks(target);
    }

    @Override
    public int getFlatComplexity() {
        int result = 0;
        for (Potential target : chains.values())
            result += super.getAncestorCount(target);
        return result;
    }

    @Override
    protected Collection<Potential> getChainsTargets() {
        return Collections.unmodifiableCollection(this.chains.values());
    }

    @Override
    protected Potential getChainTarget(int viewNum) {
        return getTargetPotential(viewNum);
    }

    @Override
    public int getSortKey() {
        return 6;
    }

    @Override
    public Grid.Region[] getRegions() {
        return new Grid.Region[] {region};
    }

    Grid.Region getRegion() {
        return this.region;
    }

    public double getDifficulty() {
        return getChainingRule().getDifficulty() + getLengthDifficulty();
    }

    public String getName() {
        String name = getChainingRule().getCommonName(this);
        if (name != null)
            return name;
        return super.getNamePrefix() + "Region Forcing" + super.getNameSuffix();
    }

    public String getShortName() {
        String name = getChainingRule().getCommonName(this);
        if (name != null)
            return name;
        return super.getShortNamePrefix() + "RF" + super.getShortNameSuffix();
    }

    @Override
    protected Potential getResult() {
        return chains.values().iterator().next();
    }

    public String getClueHtml(Grid grid, boolean isBig) {
        if (isBig) {
            return "Look for a " + getName() +
                    " with the value " + value + " on the <b1>" + region.toFullString() + "</b1>";
        } else {
            return "Look for a " + getName();
        }
    }

    @Override
    public String toString() {
        String prefix = getChainingRule().getCommonName(this);
        if (prefix == null)
            prefix = "Region Forcing Chains";
        Potential dstPotential = chains.values().iterator().next();
        return prefix + ": " + value + " in " + region.toString() + " ==> "
                + dstPotential.toString() + (dstPotential.isOn ? " on" : " off");
    }

    @Override
    public String toHtml(Grid grid) {
        String result;
        if (getChainingRule().isDynamic())
            result = HtmlLoader.loadHtml(this, "DynamicRegionReductionHint.html");
        else
            result = HtmlLoader.loadHtml(this, "StaticRegionReductionHint.html");
        String assertions = "";
        for (Potential curTarget : chains.values()) {
            Potential curSource = getSrcPotential(curTarget);
            assertions += "<li>If " + curSource.toWeakString()
                    + ", then " + curTarget.toStrongString();
        }
        String valueName = Integer.toString(value);
        String regionName = region.toString();
        Potential target = chains.values().iterator().next();
        String resultName = target.toStrongString();
        StringBuilder htmlChains = getChainsDetails();
        result = HtmlLoader.format(result, assertions, valueName, regionName,
                resultName, htmlChains);
        return super.appendNestedChainsDetails(result);
    }

    private StringBuilder getChainsDetails() {
        StringBuilder htmlChains = new StringBuilder();
        int index = 1;
        for (Potential curTarget : chains.values()) {
            Potential curSource = getSrcPotential(curTarget);
            htmlChains.append("Chain " + index + ": <b>If " + curSource.toWeakString()
                    + ", then " + curTarget.toStrongString() + "</b>"
                    + " (View " + index + "):<br>\n");
            String curChain = getHtmlChain(curTarget);
            htmlChains.append(curChain);
            htmlChains.append("<br>\n");
            index++;
        }
        return htmlChains;
    }

}
