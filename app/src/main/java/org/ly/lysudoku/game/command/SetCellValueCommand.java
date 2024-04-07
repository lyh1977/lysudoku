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

package org.ly.lysudoku.game.command;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;

import java.util.StringTokenizer;

public class SetCellValueCommand extends AbstractSingleCellCommand {

    private int mValue;
    private int mOldValue;

    public SetCellValueCommand(Grid grid, Cell cell, int value) {
        super(grid,cell);
        mValue = value;
    }

    public SetCellValueCommand() {

    }


    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mValue).append("|");
        data.append(mOldValue).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mValue = Integer.parseInt(data.nextToken());
        mOldValue = Integer.parseInt(data.nextToken());
    }

    @Override
    public void execute() {
        Cell cell = getCell();
        mOldValue = getCells().getCellValue(cell.getIndex());
        //getCells().setCellValue(cell.getIndex(),mValue);
        cell.setValueAndCancel(mValue,getCells());
    }

    @Override
    public void undo() {
        Cell cell = getCell();
        //getCells().setCellValue(cell.getIndex(),mOldValue);
        cell.setValueAndCancel(mOldValue,getCells());
    }

}
