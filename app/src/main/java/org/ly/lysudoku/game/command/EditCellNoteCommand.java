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

import java.util.BitSet;
import java.util.StringTokenizer;

public class EditCellNoteCommand extends AbstractSingleCellCommand {

    private BitSet mNote;
    private BitSet mOldNote;

    public EditCellNoteCommand(Grid grid, Cell cell, BitSet note) {
        super(grid, cell);
        mNote = note;
    }

    public EditCellNoteCommand() {

    }

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        serializeBitSet(data, mNote);
        serializeBitSet(data, mOldNote);
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mNote = deserializeBitset(data);
        mOldNote = deserializeBitset(data);

    }

    @Override
    public void execute() {
        Cell cell = getCell();
        mOldNote = (BitSet)getCells().getCellPotentialValues(cell.getIndex()).clone();
        //getCells().setCellPotentialValues(cell.getIndex(),mNote);
        cell.setCellPotentialValuesAndCancel((BitSet)mNote.clone(), getCells());
    }

    @Override
    public void undo() {
        Cell cell = getCell();
        //getCells().setCellPotentialValues(cell.getIndex(),mOldNote);
        //cell.setNote(mOldNote);
        cell.setCellPotentialValuesAndCancel(mOldNote, getCells());
    }

}
