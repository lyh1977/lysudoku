package org.ly.lysudoku.game;

import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.game.command.AbstractCellCommand;
import org.ly.lysudoku.game.command.AbstractCommand;
import org.ly.lysudoku.game.command.AbstractSingleCellCommand;
import org.ly.lysudoku.game.command.CheckpointCommand;
import org.ly.lysudoku.game.command.SetCellValueAndRemoveNotesCommand;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.StringTokenizer;

public class CommandStack {
    Stack<AbstractCommand> mCommandStack =new Stack<>();
    Grid mCells;
    public  void setGrid(Grid g)
    {
        mCells=g;

    }
    public CommandStack(Grid cells) {
        mCells = cells;
    }
    public Stack<AbstractCommand> getGridStack() {
        return mCommandStack;
    }


      private void push(AbstractCommand command) {
        if (command instanceof AbstractCellCommand) {
            ((AbstractCellCommand) command).setCells(mCells);
        }
        mCommandStack.push(command);
    }

    private AbstractCommand pop() {
        return mCommandStack.pop();
    }
    public static CommandStack deserialize(String data, Grid cells) {
        StringTokenizer st = new StringTokenizer(data, "|");
        return deserialize(st, cells);
    }

    public static CommandStack deserialize(StringTokenizer data, Grid cells) {
        CommandStack result = new CommandStack(cells);
        int stackSize = Integer.parseInt(data.nextToken());
        for (int i = 0; i < stackSize; i++) {
            AbstractCommand command = AbstractCommand.deserialize(data);
            result.push(command);
        }

        return result;
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        serialize(sb);
        return sb.toString();
    }

    public void serialize(StringBuilder data) {
        data.append(mCommandStack.size()).append("|");
        for (int i = 0; i < mCommandStack.size(); i++) {
            AbstractCommand command = mCommandStack.get(i);
            command.serialize(data);
        }
    }

    public boolean empty() {
        return mCommandStack.empty();
    }

    public void execute(AbstractCommand command) {
        push(command);
        command.execute();
    }

    public boolean hasSomethingToUndo() {
        return mCommandStack.size() != 0;
    }
    public void undo() {
        if (!mCommandStack.empty()) {
            AbstractCommand c = pop();
            c.undo();
            validateCells();
        }
    }

    public void setCheckpoint() {
        if (!mCommandStack.empty()) {
            AbstractCommand c = mCommandStack.peek();
            if (c instanceof CheckpointCommand)
                return;
        }
        push(new CheckpointCommand());
    }
    public Cell getLastChangedCell() {
        ListIterator<AbstractCommand> iter = mCommandStack.listIterator(mCommandStack.size());
        while (iter.hasPrevious()) {
            AbstractCommand o = iter.previous();
            if (o instanceof AbstractSingleCellCommand) {
                return ((AbstractSingleCellCommand) o).getCell();
            } else if (o instanceof SetCellValueAndRemoveNotesCommand) {
                return ((SetCellValueAndRemoveNotesCommand) o).getCell();
            }
        }
        return null;
    }

    public boolean hasCheckpoint() {
        for (AbstractCommand c : mCommandStack) {
            if (c instanceof CheckpointCommand)
                return true;
        }
        return false;
    }

    public void undoToCheckpoint() {
        /*
         * I originally planned to just call undo but this way it doesn't need to
         * validateCells() until the run is complete
         */
        AbstractCommand c;
        while (!mCommandStack.empty()) {
            c = mCommandStack.pop();
            c.undo();

            if (c instanceof CheckpointCommand)
                break;
        }
        validateCells();
    }

    private boolean hasMistakes(ArrayList<int[]> finalValues) {
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int val = rowColVal[1];
            int cellv = mCells.getCellValue(row);

            if (cellv != val && cellv != 0) {
                return true;
            }
        }

        return false;
    }

    public void undoToSolvableState() {
        /*
        SudokuSolver solver = new SudokuSolver();
        solver.setPuzzle(mCells);
        ArrayList<int[]> finalValues = solver.solve();

        while (!mCommandStack.empty() && hasMistakes(finalValues)) {
            mCommandStack.pop().undo();
        }

        validateCells();*/
    }
    private void validateCells() {
        mCells.validate();
    }
}
