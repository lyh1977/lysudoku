/*
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

package org.ly.lysudoku.game;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.game.command.AbstractCommand;
import org.ly.lysudoku.game.command.ClearAllNotesCommand;
import org.ly.lysudoku.game.command.EditCellNoteCommand;
import org.ly.lysudoku.game.command.FillInNotesCommand;
import org.ly.lysudoku.game.command.FillInNotesWithAllValuesCommand;
import org.ly.lysudoku.game.command.SetCellValueAndRemoveNotesCommand;
import org.ly.lysudoku.game.command.SetCellValueCommand;
import org.ly.lysudoku.solver.DirectHint;
import org.ly.lysudoku.solver.Hint;
import org.ly.lysudoku.solver.HintsAccumulator;
import org.ly.lysudoku.solver.IndirectHint;
import org.ly.lysudoku.solver.Solver;
import org.ly.lysudoku.solver.WarningHint;
import org.ly.lysudoku.tools.Asker;
import org.ly.lysudoku.tools.StrongReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuGame {

    public static final int GAME_STATE_PLAYING = 0;
    public static final int GAME_STATE_NOT_STARTED = 1;
    public static final int GAME_STATE_COMPLETED = 2;

    private long mId;
    private long mCreated;
    private int mState;
    private long mTime;
    private long mLastPlayed;
    private String mNote;

    private Grid mGrid;
    //private Solver mSolver;
    private boolean mUsedSolver = false;
    private boolean mRemoveNotesOnEntry = false;

    private OnPuzzleSolvedListener mOnPuzzleSolvedListener;
    private List<Hint> unfilteredHints = null; // All hints (unfiltered)
    private List<Hint> filteredHints = null; // All hints (filtered)
    private boolean isFiltered = true;
    private List<Hint> selectedHints = new ArrayList<Hint>(); // Currently selected hint
    private Stack<Grid> gridStack = new Stack<Grid>(); // Stack for undo

    CommandStack mCommandStack;

    // Cache for filter
    Set<Cell> givenCells = new HashSet<Cell>(); // Cell values already encountered
    Map<Cell, BitSet> removedPotentials = new HashMap<Cell, BitSet>(); // Removable potentials already encountered
    // Time when current activity has become active.
    private long mActiveFromTime = -1;

    private Asker asker;

    public void setAsker(Asker a) {
        asker = a;
    }

    public SudokuGame() {
        mTime = 0;
        mLastPlayed = 0;
        mCreated = 0;

        mState = GAME_STATE_NOT_STARTED;
        mGrid = new Grid();
        //gridStack = new Stack<Grid>();	// fix #101 - reset Undo stack (in constructor)

        //mSolver = new Solver(mGrid);
        //mSolver.rebuildPotentialValues();
        mCommandStack = new CommandStack(mGrid);
    }

    public static SudokuGame createEmptyGame() {
        SudokuGame game = new SudokuGame();

        // set creation time
        game.setCreated(System.currentTimeMillis());
        // game.rebuildPotentialValues();
        return game;
    }

    public static SudokuGame pass(String str81) {
        SudokuGame game = new SudokuGame();
        game.getGrid().fromString(str81);
        // set creation time
        game.setCreated(System.currentTimeMillis());

        return game;
    }

    public void rebuildPotentialValues() {
        Solver solver = new Solver(getGrid());

        solver.rebuildPotentialValues();
        getGrid().setAutoPotentialValues(true);
    }

    public void saveState(Bundle outState) {
        outState.putLong("id", mId);
        outState.putString("note", mNote);
        outState.putLong("created", mCreated);
        outState.putInt("state", mState);
        outState.putLong("time", mTime);
        outState.putLong("lastPlayed", mLastPlayed);
        outState.putString("cells", mGrid.serialize());
        outState.putString("command_stack", mCommandStack.serialize());
    }

    public void restoreState(Bundle inState) {
        mId = inState.getLong("id");
        mNote = inState.getString("note");
        mCreated = inState.getLong("created");
        mState = inState.getInt("state");
        mTime = inState.getLong("time");
        mLastPlayed = inState.getLong("lastPlayed");
        //mCells = CellCollection.deserialize(inState.getString("cells"));
        mGrid.fromString(inState.getString("cells"));
        mCommandStack = CommandStack.deserialize(inState.getString("command_stack"), mGrid);
        //rebuildPotentialValues();
        validate();
    }


    public void setOnPuzzleSolvedListener(OnPuzzleSolvedListener l) {
        mOnPuzzleSolvedListener = l;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public long getCreated() {
        return mCreated;
    }

    public void setCreated(long created) {
        mCreated = created;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
    }


    /**
     * Gets time of game-play in milliseconds.
     *
     * @return
     */
    public long getTime() {
        if (mActiveFromTime != -1) {
            return mTime + SystemClock.uptimeMillis() - mActiveFromTime;
        } else {
            return mTime;
        }
    }

    /**
     * Sets time of play in milliseconds.
     *
     * @param time
     */
    public void setTime(long time) {
        mTime = time;
    }

    public long getLastPlayed() {
        return mLastPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        mLastPlayed = lastPlayed;
    }

    public Grid getGrid() {
        return mGrid;
    }

    public void setGrid(Grid grid) {
        mCommandStack.setGrid(grid);
        mGrid = grid;
        validate();
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public CommandStack getCommandStack() {
        return mCommandStack;
    }

    public void setCommandStack(CommandStack commandStack) {
        mCommandStack = commandStack;
    }

    public void setRemoveNotesOnEntry(boolean removeNotesOnEntry) {
        mRemoveNotesOnEntry = removeNotesOnEntry;
    }

    public void setCellValue(int row, int col, int value) {
        Cell cel = mGrid.getCell(col, row);
        setCellValue(cel, value);
    }

    /**
     * Sets value for the given cell. 0 means empty cell.
     *
     * @param cell
     * @param value
     */
    public void setCellValue(Cell cell, int value) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Value must be between 0-9.");
        }

        if (mGrid.isEditable(cell.getIndex())) {
            if (mRemoveNotesOnEntry) {
                executeCommand(new SetCellValueAndRemoveNotesCommand(mGrid, cell, value));
            } else {
                executeCommand(new SetCellValueCommand(mGrid, cell, value));
            }

            validate();
            if (isCompleted()) {
                finish();
                if (mOnPuzzleSolvedListener != null) {
                    mOnPuzzleSolvedListener.onPuzzleSolved();
                }
            }
        }
    }

    /**
     * Sets note attached to the given cell.
     *
     * @param cell
     * @param note
     */
    public void setCellNote(Cell cell, BitSet note) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell cannot be null.");
        }
        if (note == null) {
            throw new IllegalArgumentException("Note cannot be null.");
        }

        if (mGrid.isEditable(cell.getIndex())) {
            executeCommand(new EditCellNoteCommand(mGrid, cell, note));
        }
    }

    private void executeCommand(AbstractCommand c) {
        mCommandStack.execute(c);
    }

    /**
     * Undo last command.
     */
    public void undo() {
        mCommandStack.undo();
    }

    public boolean hasSomethingToUndo() {
        return mCommandStack.hasSomethingToUndo();
    }

    public void solve(Grid g) {

        SudokuSolver mSolver = new SudokuSolver();
        mSolver.setPuzzle(g);
        ArrayList<int[]> finalValues = mSolver.solve();
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int col = rowColVal[1];
            int val = rowColVal[2];
            //Cell cell = g.getCell(col, row);
            g.setCellValue(col,row, val);
        }
    }
    public  boolean checkCustomPotentials(Map<Cell,BitSet> bList)
    {
        boolean isOk=true;
        createSolverd();
        for (int i=0;i<81;i++)
        {
            if(mGrid.isEditable(i)&&mGrid.getCellValue(i)==0)
            {
                BitSet bt=mGrid.getCellPotentialValues(i);
                BitSet sBt=theSolved.getCellPotentialValues(i);
                for (int j = 0; j < bt.size(); j++) {
                    if (bt.get(j) &&sBt.get(j)==false) {
                        isOk=false;
                        if(bList.containsKey(i)==false)
                        {
                            BitSet nBt=new BitSet(10);
                            nBt.set(j);
                            bList.put(Grid.getCell(i),nBt);
                        }
                        else {
                            bList.get(Grid.getCell(i)).set(j);
                        }
                    }
                }
            }
            /*
            if(bList.size()>0)
            {
                // 通过Map.keySet遍历key,然后通过key得到对应的value值
                for (Integer key : bList.keySet()) {
                    mS
                }
            }*/
        }
        return  isOk;
    }
    Grid theSolved=null;

    public  boolean chekCustomValue(ArrayList<Integer> rList)
    {
        boolean isOk=true;

        createSolverd();

        for (int i=0;i<81;i++)
        {
            if(mGrid.isEditable(i)&&mGrid.getCellValue(i)>0)
            {
                if(theSolved.getCellValue(i)!=mGrid.getCellValue(i))
                {
                    //不一样呀，那就是错的，
                    mGrid.setCallValid(i,false);
                    rList.add(i);
                    isOk=false;
                }
            }
        }
        return  isOk;
    }
    public boolean setUndoCheckpoint(ArrayList<Integer> rList) {
        boolean isOk=chekCustomValue(rList);
        if(isOk==true) {
            mCommandStack.setCheckpoint();
        }
        return isOk;
    }

    private Grid createSolverd() {
       // if(theSolved==null) {
            //先验证所有的
            theSolved = new Grid();
            mGrid.copySourceValueTo(theSolved);
            theSolved.reSetEditable();
            Solver solver = new Solver(theSolved);

            solver.rebuildPotentialValues();
            solve(theSolved);
return theSolved;
       // }
    }
    public void deleteErroPv(Map<Cell,BitSet> pList)
    {
        if(pList.size()>0)
        {
            // 通过Map.keySet遍历key,然后通过key得到对应的value值
            for (Cell key : pList.keySet()) {
                BitSet bs=pList.get(key);
                for (int j = 0; j < bs.size(); j++) {
                    if(bs.get(j))
                    {
                        mGrid.removeCellPotentialValue(key.getIndex(),j);
                    }
                }
            }
        }

    }
    public void undo2NoErr(ArrayList<Integer> rList)
    {
        int max=0;
        boolean isOk=false;
        do{
            undo();
            boolean isAll0=true;
            for(Integer j:rList)
            {
                if(mGrid.getCellValue(j)!=0) isAll0=false;

            }
            isOk= isAll0;
        }while (isOk==false&&max++<500);
    }

    public void undoToCheckpoint() {
        mCommandStack.undoToCheckpoint();
    }

    public boolean hasUndoCheckpoint() {
        return mCommandStack.hasCheckpoint();
    }

    public void undoToBeforeMistake() {
        mCommandStack.undoToSolvableState();
    }

    @Nullable
    public Cell getLastChangedCell() {
        return mCommandStack.getLastChangedCell();
    }

    /**
     * Start game-play.
     */
    public void start() {
        mState = GAME_STATE_PLAYING;
        resume();
    }

    public void resume() {
        // reset time we have spent playing so far, so time when activity was not active
        // will not be part of the game play time
        mActiveFromTime = SystemClock.uptimeMillis();
    }

    /**
     * Pauses game-play (for example if activity pauses).
     */
    public void pause() {
        // save time we have spent playing so far - it will be reseted after resuming
        mTime += SystemClock.uptimeMillis() - mActiveFromTime;
        mActiveFromTime = -1;

        setLastPlayed(System.currentTimeMillis());
    }

    /**
     * Checks if a solution to the puzzle exists
     */
    public boolean isSolvable() {
        SudokuSolver mSolver = new SudokuSolver();
        mSolver.setPuzzle(mGrid);
        ArrayList<int[]> finalValues = mSolver.solve();
        return !finalValues.isEmpty();
    }

    /**
     * Solves puzzle from original state
     */
    public void solve() {
        mUsedSolver = true;
        SudokuSolver mSolver = new SudokuSolver();
        mSolver.setPuzzle(mGrid);
        ArrayList<int[]> finalValues = mSolver.solve();
        for (int[] rowColVal : finalValues) {
            int row = rowColVal[0];
            int col = rowColVal[1];
            int val = rowColVal[2];
            Cell cell = mGrid.getCell(col, row);
            this.setCellValue(cell, val);
        }
    }

    public boolean usedSolver() {
        return mUsedSolver;
    }

    /**
     * Solves puzzle and fills in correct value for selected cell
     */
    public void solveCell(Cell cell) {
        SudokuSolver mSolver = new SudokuSolver();
        mSolver.setPuzzle(mGrid);
        ArrayList<int[]> finalValues = mSolver.solve();

        int row = cell.getY();
        int col = cell.getX();
        for (int[] rowColVal : finalValues) {
            if (rowColVal[0] == row && rowColVal[1] == col) {
                int val = rowColVal[2];
                this.setCellValue(cell, val);
            }
        }
    }

    /*
    应用solver解出的数，及hint
    hint处理方式，如果手工加有才处理，如果没有手工加过不处理
     */
    public void applySolver(Grid g) {
        for (int i = 0; i < 81; i++) {
            int value = g.getCellValue(i);
            Cell cell = Grid.getCell(i);
            if (value != 0) {
                if (mGrid.getCellValue(i) != g.getCellValue(i) ) {
                    //不等，要更新。
                    setCellValue(cell, g.getCellValue(i));
                }
            }
            else {
                //要处理hint,只删除用户加的，
                BitSet mbit=mGrid.getCellPotentialValues(i);
                if(mbit.isEmpty()==false)
                {

                    for(int j=1;j<10;j++)
                    {
                        if(mGrid.getCellPotentialValues(i).get(j))
                        {
                            if(g.getCellPotentialValues(i).get(j)==false)
                            {
                                mGrid.removeCellPotentialValue(i,j);
                            }
                        }
                    }
                    //不能直接传值
                    //mGrid.setCellPotentialValues(i,g.getCellPotentialValues(i));
                }
            }
        }
    }

    /**
     * Finishes game-play. Called when puzzle is solved.
     */
    public void finish() {
        pause();
        mState = GAME_STATE_COMPLETED;
    }

    /**
     * Resets game.
     */
    public void reset() {
        for (int r = 0; r < 81; r++) {
            if (mGrid.isEditable(r)) {
                mGrid.setCellValue(r, 0);
                mGrid.clearCellPotentialValues(r);
            }

        }
        // mSolver.rebuildPotentialValues();
        mCommandStack = new CommandStack(mGrid);
        validate();
        setTime(0);
        setLastPlayed(0);
        mState = GAME_STATE_NOT_STARTED;
        mUsedSolver = false;
    }

    /**
     * Returns true, if puzzle is solved. In order to know the current state, you have to
     * call validate first.
     *
     * @return
     */
    public boolean isCompleted() {

        return mGrid.isSolved();
    }

    public void clearAllNotes() {
        executeCommand(new ClearAllNotesCommand());
    }

    /**
     * Fills in possible values which can be entered in each cell.
     */
    public void fillInNotes() {
        //executeCommand(new FillInNotesCommand());
        rebuildPotentialValues();

    }

    /**
     * Fills in all values which can be entered in each cell.
     */
    public void fillInNotesWithAllValues() {
        executeCommand(new FillInNotesWithAllValuesCommand());
    }

    public void validate() {
        mGrid.validate();
    }

    public interface OnPuzzleSolvedListener {
        /**
         * Occurs when puzzle is solved.
         *
         * @return
         */
        void onPuzzleSolved();
    }


}
