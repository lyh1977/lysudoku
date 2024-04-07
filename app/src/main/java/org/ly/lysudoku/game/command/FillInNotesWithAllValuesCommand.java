package org.ly.lysudoku.game.command;


import org.ly.lysudoku.Grid;

public class FillInNotesWithAllValuesCommand extends AbstractMultiNoteCommand {

    public FillInNotesWithAllValuesCommand(Grid grid) {
        super(grid);
    }

    public FillInNotesWithAllValuesCommand() {

    }

    @Override
    public void execute() {
        Grid cells = getCells();

        mOldNotes.clear();
        saveOldNotes();

        cells.rebuildPotentialValues();
    }
}
