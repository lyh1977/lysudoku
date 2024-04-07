package org.ly.lysudoku.game.command;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.StringTokenizer;

public abstract class AbstractMultiNoteCommand extends AbstractCellCommand {

    public AbstractMultiNoteCommand(Grid grid) {
        super(grid);

    }

    public AbstractMultiNoteCommand() {

    }

    protected List<NoteEntry> mOldNotes = new ArrayList<>();

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mOldNotes.size()).append("|");

        for (NoteEntry ne : mOldNotes) {
            data.append(ne.index).append("|");
            data.append(serializeBitSet(ne.note));
        }
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        int notesSize = Integer.parseInt(data.nextToken());
        for (int i = 0; i < notesSize; i++) {
            int row = Integer.parseInt(data.nextToken());

            mOldNotes.add(new NoteEntry(row, deserializeBitset(data)));
        }
    }

    @Override
    public void undo() {
        Grid cells = getCells();

        for (NoteEntry ne : mOldNotes) {
            cells.setCellPotentialValues(ne.index, ne.note);
        }
    }

    protected void saveOldNotes() {
        Grid cells = getCells();
        for (int r = 0; r < 81; r++) {

            mOldNotes.add(new NoteEntry(r, cells.getCellPotentialValues(r)));
        }
    }

    protected static class NoteEntry {
        public int index;

        public BitSet note;

        public NoteEntry(int index, BitSet note) {
            this.index = index;

            this.note = (BitSet) note.clone();
        }

    }
}
