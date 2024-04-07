package org.ly.lysudoku.game.command;


import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;

import java.util.StringTokenizer;

/**
 * Created by spimanov on 30.10.17.
 */

public abstract class AbstractSingleCellCommand extends AbstractCellCommand {

    private int mCellIndex;

    public AbstractSingleCellCommand(Grid grid, Cell cell) {
        super(grid);
        mCellIndex = cell.getIndex();

    }
    public AbstractSingleCellCommand() {


    }

    @Override
    public void serialize(StringBuilder data) {
        super.serialize(data);
        data.append(mCellIndex).append("|");
    }

    @Override
    protected void _deserialize(StringTokenizer data) {
        super._deserialize(data);
        mCellIndex = Integer.parseInt(data.nextToken());

    }

    public Cell getCell() {
        return getCells().getCell(mCellIndex);
    }

}
