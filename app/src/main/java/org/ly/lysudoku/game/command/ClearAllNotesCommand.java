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

public class ClearAllNotesCommand extends AbstractMultiNoteCommand {

    public ClearAllNotesCommand(Grid grid) {
        super(grid);
    }

    public ClearAllNotesCommand() {

    }

    @Override
    public void execute() {
        Grid cells = getCells();

        mOldNotes.clear();
        for (int r = 0; r < 81; r++) {

                Cell cell = cells.getCell(r);
                BitSet note = cells.getCellPotentialValues(r);
                if (!note.isEmpty()) {
                    mOldNotes.add(new NoteEntry(r,  note));
                    cells.clearCellPotentialValues(r);
                }

        }
    }
}
