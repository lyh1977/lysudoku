package org.ly.lysudoku.game.command;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;

import java.util.BitSet;
import java.util.StringTokenizer;

public class SetCellValueAndRemoveNotesCommand extends AbstractMultiNoteCommand {

    private int mCellIndex;
    private int mValue;
    private int mOldValue;

    public SetCellValueAndRemoveNotesCommand(Grid grid, Cell cell, int value) {
        super(grid);
        mCellIndex = cell.getIndex();

        mValue = value;
    }

    public SetCellValueAndRemoveNotesCommand() {

    }


    public Cell getCell() {
        return getCells().getCell(mCellIndex);
    }

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);

        data.append(mCellIndex).append("|");

        data.append(mValue).append("|");
        data.append(mOldValue).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);

        mCellIndex = Integer.parseInt(data.nextToken());

        mValue = Integer.parseInt(data.nextToken());
        mOldValue = Integer.parseInt(data.nextToken());
    }

    @Override
    public void execute() {
        mOldNotes.clear();
        saveOldNotes();
        Grid grid = getCells();
        Cell cell = getCell();
        grid.removeCellPotentialValue(cell.getIndex(), mValue);
        mOldValue = grid.getCellValue(cell.getIndex());
        //getCells().setCellValue(cell.getIndex(),mValue);

        cell.setValueAndCancel(mValue, grid);
    }

    @Override
    public void undo() {
        super.undo();
        Cell cell = getCell();
        Grid grid = getCells();

        grid.setCellValueAndSetChange(cell.getIndex(), mOldValue);

        //cell.setValueAndCancel(mOldValue,grid);

    }
}
