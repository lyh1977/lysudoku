package org.ly.lysudoku.game.command;


import org.ly.lysudoku.Grid;

import java.util.GregorianCalendar;

public class FillInNotesCommand extends AbstractMultiNoteCommand {

    public FillInNotesCommand(Grid grid) {
        super(grid);
    }

    public FillInNotesCommand() {

    }

    @Override
    public void execute() {
        Grid cells = getCells();

        mOldNotes.clear();
        saveOldNotes();

        cells.rebuildPotentialValues();
    }
}
