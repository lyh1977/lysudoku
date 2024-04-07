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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import org.ly.lysudoku.Grid;
import org.ly.lysudoku.R;
import org.ly.lysudoku.Cell;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.gui.HintsQueue;
import org.ly.lysudoku.gui.InputButton;
import org.ly.lysudoku.gui.SudokuBoardView;
import org.ly.lysudoku.gui.SudokuPlayActivity;
import org.ly.lysudoku.gui.inputmethod.IMControlPanelStatePersister.StateBundle;
import org.ly.lysudoku.utils.ThemeUtils;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents following type of number input workflow: Number buttons are displayed
 * in the sidebar, user selects one number and then fill values by tapping the cells.
 *
 * @author romario
 */
public class IMSingleNumber extends InputMethod {

    private static final int MODE_EDIT_VALUE = 0;
    private static final int MODE_EDIT_NOTE = 1;

    private boolean mHighlightCompletedValues = true;
    private boolean mShowNumberTotals = true;
    private boolean mBidirectionalSelection = true;
    private boolean mHighlightSimilar = true;

    private int mSelectedNumber = 1;
    private int mEditMode = MODE_EDIT_VALUE;

    private Handler mGuiHandler;
    private Map<Integer, InputButton> mNumberButtons;
    private ImageButton mSwitchNumNoteButton;

    private SudokuPlayActivity.OnSelectedNumberChangedListener mOnSelectedNumberChangedListener = null;
    private View.OnTouchListener mNumberButtonTouched = (view, motionEvent) -> {
        mSelectedNumber = (Integer) view.getTag();
        onSelectedNumberChanged();
        update();
        return true;
    };
    private OnClickListener mNumberButtonClicked = v -> {
        mSelectedNumber = (Integer) v.getTag();
        onSelectedNumberChanged();
        update();
    };
    private Grid.OnChangeListener mOnCellsChangeListener = () -> {
        if (mActive) {
            update();
        }
    };

    public IMSingleNumber() {
        super();

        mGuiHandler = new Handler();
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

    public boolean getBidirectionalSelection() {
        return mBidirectionalSelection;
    }

    public void setBidirectionalSelection(boolean bidirectionalSelection) {
        mBidirectionalSelection = bidirectionalSelection;
    }

    public boolean getHighlightSimilar() {
        return mHighlightSimilar;
    }

    public void setHighlightSimilar(boolean highlightSimilar) {
        mHighlightSimilar = highlightSimilar;
    }

    public void setmOnSelectedNumberChangedListener(SudokuPlayActivity.OnSelectedNumberChangedListener l) {
        mOnSelectedNumberChangedListener = l;
    }

    @Override
    protected void initialize(Context context, IMControlPanel controlPanel,
                              SudokuGame game, SudokuBoardView board, HintsQueue hintsQueue) {
        super.initialize(context, controlPanel, game, board, hintsQueue);

        game.getGrid().addOnChangeListener(mOnCellsChangeListener);
    }

    @Override
    public int getNameResID() {
        return R.string.single_number;
    }

    @Override
    public int getHelpResID() {
        return R.string.im_single_number_hint;
    }

    @Override
    public String getAbbrName() {
        return mContext.getString(R.string.single_number_abbr);
    }

    @Override
    protected View createControlPanelView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View controlPanel = inflater.inflate(R.layout.im_single_number, null);

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
            Button b = mNumberButtons.get(num);
            b.setTag(num);
            b.setOnClickListener(mNumberButtonClicked);
            b.setOnTouchListener(mNumberButtonTouched);
        }

        mSwitchNumNoteButton = controlPanel.findViewById(R.id.switch_num_note);
        mSwitchNumNoteButton.setOnClickListener(v -> {
            mEditMode = mEditMode == MODE_EDIT_VALUE ? MODE_EDIT_NOTE : MODE_EDIT_VALUE;
            update();
        });

        return controlPanel;
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

        // TODO: sometimes I change background too early and button stays in pressed state
        // this is just ugly workaround
        mGuiHandler.postDelayed(() -> {
            for (InputButton b : mNumberButtons.values()) {
                if (b.getTag().equals(mSelectedNumber)) {
                    //没有必要修改字体大小
                    //b.setTextAppearance(mContext, ThemeUtils.getCurrentThemeStyle(mContext, android.R.attr.textAppearanceLarge));
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.ACCENT);
                    b.requestFocus();
                } else {
                    //没有必要修改字体大小
                    //b.setTextAppearance(mContext, ThemeUtils.getCurrentThemeStyle(mContext, android.R.attr.textAppearanceButton));
                    ThemeUtils.applyIMButtonStateToView(b, ThemeUtils.IMButtonStyle.DEFAULT);
                }
            }

            Map<Integer, Integer> valuesUseCount = null;
            if (mHighlightCompletedValues || mShowNumberTotals)
                valuesUseCount = mGame.getGrid().getValuesUseCount();

            if (mHighlightCompletedValues) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    boolean highlightValue = entry.getValue() >= Grid.SUDOKU_SIZE;
                    boolean selected = entry.getKey() == mSelectedNumber;
                    if (highlightValue && !selected) {
                        ThemeUtils.applyIMButtonStateToView(mNumberButtons.get(entry.getKey()), ThemeUtils.IMButtonStyle.ACCENT_HIGHCONTRAST);
                    }
                }
            }

            if (mShowNumberTotals) {
                for (Map.Entry<Integer, Integer> entry : valuesUseCount.entrySet()) {
                    InputButton b = mNumberButtons.get(entry.getKey());

                    b.setNum(9 - entry.getValue());

                    /*
                    if (!b.getTag().equals(mSelectedNumber)) {
                        //b.setText(entry.getKey() + "\n (" + (9 - entry.getValue()) + ")");
                        b.setNum(9 - entry.getValue());
                    } else
                        b.setText("" + entry.getKey());
                     */
                }
            }

            mBoard.setHighlightedValue(mBoard.isReadOnly() ? 0 : mSelectedNumber);
        }, 100);
    }

    @Override
    protected void onActivated() {
        update();
    }

    @Override
    protected void onCellSelected(Grid grid, Cell cell) {
        super.onCellSelected(grid, cell);

        if (mBidirectionalSelection && cell != null) {
            int v = mGame.getGrid().getCellValue(cell.getIndex());
            if (v != 0 && v != mSelectedNumber) {
                mSelectedNumber = v;
                update();
            }
        }

        mBoard.setHighlightedValue(mSelectedNumber);
    }

    private void onSelectedNumberChanged() {
        if (mBidirectionalSelection && mHighlightSimilar && mOnSelectedNumberChangedListener != null && !mBoard.isReadOnly()) {
            mOnSelectedNumberChangedListener.onSelectedNumberChanged(mSelectedNumber);
            mBoard.setHighlightedValue(mSelectedNumber);
        }
    }

    @Override
    protected void onCellTapped(Grid grid, Cell cell) {
        int selNumber = mSelectedNumber;

        switch (mEditMode) {
            case MODE_EDIT_NOTE:
                if (selNumber == 0) {
                    mGame.setCellNote(cell, new BitSet(10));
                } else if (selNumber > 0 && selNumber <= 9) {
                    //CellNote newNote = cell.getNote().toggleNumber(selNumber);
                    BitSet newNote = mGame.getGrid().buildCellPotentialValue(cell.getIndex(), selNumber);
                    mGame.setCellNote(cell, newNote);
                    // if we toggled the note off we want to de-select the cell
                    if (!newNote.get(selNumber)) {
                        mBoard.clearCellSelection();
                    }
                }
                break;
            case MODE_EDIT_VALUE:
                if (selNumber >= 0 && selNumber <= 9) {
                    if (!mNumberButtons.get(selNumber).isEnabled()) {
                        // Number requested has been disabled but it is still selected. This means that
                        // this number can be no longer entered, however any of the existing fields
                        // with this number can be deleted by repeated touch
                        if (selNumber == mGame.getGrid().getCellValue(cell.getIndex())) {
                            mGame.setCellValue(cell, 0);
                            mBoard.clearCellSelection();
                        }
                    } else {
                        // Normal flow, just set the value (or clear it if it is repeated touch)
                        if (selNumber == mGame.getGrid().getCellValue(cell.getIndex())) {
                            selNumber = 0;
                            mBoard.clearCellSelection();
                        }
                        mGame.setCellValue(cell, selNumber);
                    }
                }
                break;
        }

    }

    @Override
    protected void onSaveState(StateBundle outState) {
        outState.putInt("selectedNumber", mSelectedNumber);
        outState.putInt("editMode", mEditMode);
    }

    @Override
    protected void onRestoreState(StateBundle savedState) {
        mSelectedNumber = savedState.getInt("selectedNumber", 1);
        mEditMode = savedState.getInt("editMode", MODE_EDIT_VALUE);
        if (isInputMethodViewCreated()) {
            update();
        }
    }

}
