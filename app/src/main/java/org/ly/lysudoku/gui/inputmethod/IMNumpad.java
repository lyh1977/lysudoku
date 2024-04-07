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

package org.ly.lysudoku.gui.inputmethod;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import org.ly.lysudoku.Grid;
import org.ly.lysudoku.R;
import org.ly.lysudoku.Cell;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.gui.HintsQueue;
import org.ly.lysudoku.gui.InputButton;
import org.ly.lysudoku.gui.SudokuBoardView;
import org.ly.lysudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;
import org.ly.lysudoku.utils.ThemeUtils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMNumpad extends InputMethod {

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_NOTE = 1;
    private boolean moveCellSelectionOnPress = true;
    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = true;
    private Cell mSelectedCell;
    private ImageButton mSwitchNumNoteButton;

    private int mEditMode = MODE_EDIT_VALUE;

    private Map<Integer, InputButton> mNumberButtons;
    private OnClickListener mNumberButtonClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int selNumber = (Integer) v.getTag();
            Cell selCell = mSelectedCell;

            if (selCell != null) {
                switch (mEditMode) {
                    case MODE_EDIT_NOTE:
                        if (selNumber == 0) {
                            //mGame.setCellNote(selCell, CellNote.EMPTY);
                            mGame.getGrid().clearCellPotentialValues(selCell.getIndex());
                        } else if (selNumber > 0 && selNumber <= 9) {
                            mGame.setCellNote(selCell, mGame.getGrid().buildCellPotentialValue(selCell.getIndex(), selNumber));
                        }
                        break;
                    case MODE_EDIT_VALUE:
                        if (selNumber >= 0 && selNumber <= 9) {
                            if (mGame.getGrid().getCellValue(selCell.getIndex()) == selNumber) {
                                selNumber = 0;
                            }
                            mGame.setCellValue(selCell, selNumber);
                            mBoard.setHighlightedValue(selNumber);
                            if (isMoveCellSelectionOnPress()) {
                                mBoard.moveCellSelectionRight();
                            }
                        }
                        break;
                }
            }
        }

    };
    private Grid.OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    public boolean isMoveCellSelectionOnPress() {
        return moveCellSelectionOnPress;
    }

    public void setMoveCellSelectionOnPress(boolean moveCellSelectionOnPress) {
        this.moveCellSelectionOnPress = moveCellSelectionOnPress;
    }

    public boolean getHighlightCompletedValues() {
        return mHighlightCompletedValues;
    }

    /**
     * If set to true, buttons for numbers, which occur in {@link Grid}
     * more than {@link Grid#SUDOKU_SIZE}-times, will be highlighted.
     *
     * @param highlightCompletedValues
     */
    public void setHighlightCompletedValues(boolean highlightCompletedValues) {
        mHighlightCompletedValues = highlightCompletedValues;
    }

    public boolean getShowNumberTotals() {
        return mShowNumberTotals;
    }

    public void setShowNumberTotals(boolean showNumberTotals) {
        mShowNumberTotals = showNumberTotals;
    }

    @Override
    protected void initialize(Context context, IMControlPanel controlPanel,
                              SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
        super.initialize(context, controlPanel, game, board, hintsQueue);

        game.getGrid().addOnChangeListener(mOnCellsChangeListener);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controlPanel = inflater.inflate(R.layout.im_numpad, null);

        mNumberButtons = new HashMap<>();
        mNumberButtons.put(1, controlPanel.findViewById(R.id.button_1));
        mNumberButtons.put(2, controlPanel.findViewById(R.id.button_2));
        mNumberButtons.put(3, controlPanel.findViewById(R.id.button_3));
        mNumberButtons.put(4, controlPanel.findViewById(R.id.button_4));
        mNumberButtons.put(5, controlPanel.findViewById(R.id.button_5));
        mNumberButtons.put(6, controlPanel.findViewById(R.id.button_6));
        mNumberButtons.put(7, controlPanel.findViewById(R.id.button_7));
        mNumberButtons.put(8, controlPanel.findViewById(R.id.button_8));
        mNumberButtons.put(9, controlPanel.findViewById(R.id.button_9));
        mNumberButtons.put(0, controlPanel.findViewById(R.id.button_clear));

        for (Integer num : mNumberButtons.keySet()) {
            InputButton b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClick);
        }

        mSwitchNumNoteButton = controlPanel.findViewById(R.id.switch_num_note);
        mSwitchNumNoteButton.setOnClickListener(v -> {
            mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
            update();
        });

        return controlPanel;

    }

    @Override
    public int getNameResID() {
        return R.string.numpad;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_numpad_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.numpad_abbr);
    }

    @Override
    protected void onActivated() {
        onCellSelected(mGame.getGrid(), mBoard.isReadOnly() ? null : mBoard.getSelectedCell());
    }

    @Override
    protected void onCellSelected(Grid grid, Cell cell) {
        if (cell != null) {
            mBoard.setHighlightedValue(mGame.getGrid().getCellValue(cell.getIndex()));

        } else {
            mBoard.setHighlightedValue(0);
        }

        mSelectedCell = cell;
        update();
    }

    private void update() {
        switch (mEditMode) {
            case MODE_EDIT_NOTE:
                mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_white);
                break;
            case MODE_EDIT_VALUE:
                mSwitchNumNoteButton.setImageResource(R.drawable.ic_edit_grey);
                break;
        }

        if (mEditMode == MODE_EDIT_VALUE) {
            int selectedNumber = mSelectedCell == null ? 0 : mGame.getGrid().getCellValue(mSelectedCell.getIndex());
            for (InputButton b : mNumberButtons.values()) {
                if (b.getTag().equals(selectedNumber)) {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                } else {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                }
            }
        } else {
            BitSet note = mSelectedCell == null ? new BitSet(10) : mGame.getGrid().getCellPotentialValues(mSelectedCell.getIndex());


            for (InputButton b : mNumberButtons.values()) {
                if (note.get((int) b.getTag())) {//notedNumbers.contains(b.getTag())
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                } else {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                }
            }
        }

        Map<Integer, Integer> valuesUseCount = null;
        if (mHighlightCompletedValues || mShowNumberTotals)
            valuesUseCount = mGame.getGrid().getValuesUseCount();

        if (mHighlightCompletedValues && mEditMode == MODE_EDIT_VALUE) {
            int selectedNumber = mSelectedCell == null ? 0 : mGame.getGrid().getCellValue(mSelectedCell.getIndex());
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                boolean highlightValue = entry.getValue() >= Grid.SUDOKU_SIZE;
                boolean selected = entry.getKey() == selectedNumber;
                InputButton b = mNumberButtons.get(entry.getKey());
                if (highlightValue && !selected) {
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
                }
            }
        }

        if (mShowNumberTotals) {
            for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                InputButton b = mNumberButtons.get(entry.getKey());
                //b.setText(entry.getKey() + "\n (" +(9- entry.getValue()) + ")");
                b.setNum(9 - entry.getValue());
            }
        }
    }

    @Override
    protected void onSaveState(StateBundle outState) {
        outState.putInt("editMode", mEditMode);
    }

    @Override
    protected void onRestoreState(StateBundle savedState) {
        mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
        if (isInputMethodViewCreated()) {
            update();
        }
    }
}
