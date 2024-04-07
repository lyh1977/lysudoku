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

package org.ly.lysudoku.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import org.ly.lysudoku.Grid;
import org.ly.lysudoku.Link;
import org.ly.lysudoku.R;
import org.ly.lysudoku.Cell;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.utils.LogUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;

/**
 * Sudoku board widget.
 *
 * @author romario
 */
public class SudokuBoardView extends View {

    public static final int DEFAULT_BOARD_SIZE = 100;

    /**
     * "Color not set" value. (In relation to {@link Color}, it is in fact black color with
     * alpha channel set to 0 => that means it is completely transparent).
     */
    private static final int NO_COLOR = 0;

    private float mCellWidth;
    private float mCellHeight;

    private Cell mTouchedCell;
    // TODO: should I synchronize access to mSelectedCell?
    private Cell mSelectedCell;
    private int mHighlightedValue = 0;
    private boolean mReadonly = false;
    private boolean mHighlightWrongVals = true;
    private boolean mHighlightTouchedCell = true;
    private boolean mAutoHideTouchedCellHint = true;
    private HighlightMode mHighlightSimilarCells = HighlightMode.NONE;

    private SudokuGame mGame;
    private Grid mCells;
    private OnCellTappedListener mOnCellTappedListener;
    private OnCellSelectedListener mOnCellSelectedListener;
    private Paint mLinePaint;
    private Paint mSectorLinePaint;
    private Paint mCellValuePaint;
    private Paint mCellValueReadonlyPaint;
    private Paint mCellNotePaint;
    private int mNumberLeft;
    private int mNumberTop;
    private float mNoteTop;
    private int mSectorLineWidth;
    private Paint mBackgroundColorSecondary;
    private Paint mBackgroundColorReadOnly;
    private Paint mBackgroundColorTouched;
    private Paint mBackgroundColorSelected;
    private Paint mBackgroundColorHighlighted;
    private Paint mCellValueInvalidPaint;
    private boolean mShowLabel = false;

    private Map<Cell, BitSet> redPotentials;
    private Map<Cell, BitSet> greenPotentials;
    private Map<Cell, BitSet> bluePotentials;
    private Collection<Cell> greenCells;
    private Collection<Cell> redCells;
    private Grid.Region[] blueRegions;
    //SudokuMonster: Modification to yellow background cells getSelectedCells()
    private Cell[] highlightedCells;
    private Collection<Link> links;

    public Collection<Cell> getGreenCells() {
        return greenCells;
    }

    public void setGreenCells(Collection<Cell> greenCells) {
        this.greenCells = greenCells;
    }

    public Collection<Cell> getRedCells() {
        return redCells;
    }

    public void setRedCells(Collection<Cell> redCells) {
        this.redCells = redCells;
    }

    public Map<Cell, BitSet> getGreenPotentials() {
        return greenPotentials;
    }

    public void setGreenPotentials(Map<Cell, BitSet> greenPotentials) {
        this.greenPotentials = greenPotentials;
    }

    public Map<Cell, BitSet> getRedPotentials() {
        return redPotentials;
    }

    public void setRedPotentials(Map<Cell, BitSet> redPotentials) {
        this.redPotentials = redPotentials;
    }
    public void clearRedPotentials()
    {
        if(redPotentials!=null) redPotentials.clear();
    }

    public Map<Cell, BitSet> getBluePotentials() {
        return bluePotentials;
    }

    public void setBluePotentials(Map<Cell, BitSet> bluePotentials) {
        this.bluePotentials = bluePotentials;
    }

    public void setBlueRegions(Grid.Region... regions) {
        this.blueRegions = regions;
    }

    //SudokuMonster: Modification to yellow background cells getSelectedCells()
    public void setHighlightedCells(Cell[] cells) {
        this.highlightedCells = cells;
    }

    public void setLinks(Collection<Link> links) {
        this.links = links;
    }

    private boolean mShowHint = false;

    public void setShowHint(boolean isShow) {
        mShowHint = isShow;
    }
    private boolean mShowErrorPv = false;

    public void setShowErrorPv(boolean isShow) {
        mShowErrorPv = isShow;
    }
    public boolean getShowHint() {
        return mShowHint;
    }

    public void setShowLabel(boolean showLabel) {
        this.mShowLabel = showLabel;
    }

    public boolean getShowLabel() {
        return mShowLabel;
    }

    public SudokuBoardView(Context context) {
        this(context, null);
    }

    // TODO: do I need an defStyle?
    public SudokuBoardView(Context context, AttributeSet attrs/*, int defStyle*/) {
        super(context, attrs/*, defStyle*/);

        setFocusable(true);
        setFocusableInTouchMode(true);

        mLinePaint = new Paint();
        mSectorLinePaint = new Paint();
        mCellValuePaint = new Paint();
        mCellValueReadonlyPaint = new Paint();
        mCellValueInvalidPaint = new Paint();
        mCellNotePaint = new Paint();
        mBackgroundColorSecondary = new Paint();
        mBackgroundColorReadOnly = new Paint();
        mBackgroundColorTouched = new Paint();
        mBackgroundColorSelected = new Paint();
        mBackgroundColorHighlighted = new Paint();

        mCellValuePaint.setAntiAlias(true);
        mCellValueReadonlyPaint.setAntiAlias(true);
        mCellValueInvalidPaint.setAntiAlias(true);
        mCellNotePaint.setAntiAlias(true);
        mCellValueInvalidPaint.setColor(Color.RED);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudokuBoardView/*, defStyle, 0*/);

        setLineColor(a.getColor(R.styleable.SudokuBoardView_lineColor, Color.BLACK));
        setSectorLineColor(a.getColor(R.styleable.SudokuBoardView_sectorLineColor, Color.BLACK));
        setTextColor(a.getColor(R.styleable.SudokuBoardView_textColor, Color.BLACK));
        setTextColorReadOnly(a.getColor(R.styleable.SudokuBoardView_textColorReadOnly, Color.BLACK));
        setTextColorNote(a.getColor(R.styleable.SudokuBoardView_textColorNote, Color.BLACK));
        setBackgroundColor(a.getColor(R.styleable.SudokuBoardView_backgroundColor, Color.WHITE));
        setBackgroundColorSecondary(a.getColor(R.styleable.SudokuBoardView_backgroundColorSecondary, NO_COLOR));
        setBackgroundColorReadOnly(a.getColor(R.styleable.SudokuBoardView_backgroundColorReadOnly, NO_COLOR));
        setBackgroundColorTouched(a.getColor(R.styleable.SudokuBoardView_backgroundColorTouched, Color.rgb(50, 50, 255)));
        setBackgroundColorSelected(a.getColor(R.styleable.SudokuBoardView_backgroundColorSelected, Color.YELLOW));
        setBackgroundColorHighlighted(a.getColor(R.styleable.SudokuBoardView_backgroundColorHighlighted, Color.GREEN));

        a.recycle();
    }

    //	public SudokuBoardView(Context context, AttributeSet attrs) {
    //		this(context, attrs, R.attr.sudokuBoardViewStyle);
    //	}

    public int getLineColor() {
        return mLinePaint.getColor();
    }

    public void setLineColor(int color) {
        mLinePaint.setColor(color);
    }

    public int getSectorLineColor() {
        return mSectorLinePaint.getColor();
    }

    public void setSectorLineColor(int color) {
        mSectorLinePaint.setColor(color);
    }

    public int getTextColor() {
        return mCellValuePaint.getColor();
    }

    public void setTextColor(int color) {
        mCellValuePaint.setColor(color);
    }

    public int getTextColorReadOnly() {
        return mCellValueReadonlyPaint.getColor();
    }

    public void setTextColorReadOnly(int color) {
        mCellValueReadonlyPaint.setColor(color);
    }

    public int getTextColorNote() {
        return mCellNotePaint.getColor();
    }

    public void setTextColorNote(int color) {
        mCellNotePaint.setColor(color);
    }

    public int getBackgroundColorSecondary() {
        return mBackgroundColorSecondary.getColor();
    }

    public void setBackgroundColorSecondary(int color) {
        mBackgroundColorSecondary.setColor(color);
    }

    public int getBackgroundColorReadOnly() {
        return mBackgroundColorReadOnly.getColor();
    }

    public void setBackgroundColorReadOnly(int color) {
        mBackgroundColorReadOnly.setColor(color);
    }

    public int getBackgroundColorTouched() {
        return mBackgroundColorTouched.getColor();
    }

    public void setBackgroundColorTouched(int color) {
        mBackgroundColorTouched.setColor(color);
    }

    public int getBackgroundColorSelected() {
        return mBackgroundColorSelected.getColor();
    }

    public void setBackgroundColorSelected(int color) {
        mBackgroundColorSelected.setColor(color);
    }

    public int getBackgroundColorHighlighted() {
        return mBackgroundColorHighlighted.getColor();
    }

    public void setBackgroundColorHighlighted(int color) {
        mBackgroundColorHighlighted.setColor(color);
    }

    public void setGame(SudokuGame game) {
        mGame = game;
        setCells(game.getGrid());
    }

    public Grid getCells() {
        return mGame.getGrid();
    }

    public void setCells(Grid cells) {
        mCells = cells;

        if (cells != null) {
            if (!mReadonly) {
                mSelectedCell = Grid.getCell(0, 0); // first cell will be selected by default
                onCellSelected(mCells, mSelectedCell);
            }

            mCells.addOnChangeListener(this::postInvalidate);
        }

        postInvalidate();
    }

    public Cell getSelectedCell() {
        return mSelectedCell;
    }

    public boolean isReadOnly() {
        return mReadonly;
    }

    public void setReadOnly(boolean readonly) {
        mReadonly = readonly;
        postInvalidate();
    }

    public boolean getHighlightWrongVals() {
        return mHighlightWrongVals;
    }

    public void setHighlightWrongVals(boolean highlightWrongVals) {
        mHighlightWrongVals = highlightWrongVals;
        postInvalidate();
    }

    public boolean getHighlightTouchedCell() {
        return mHighlightTouchedCell;
    }

    public void setHighlightTouchedCell(boolean highlightTouchedCell) {
        mHighlightTouchedCell = highlightTouchedCell;
    }

    public boolean getAutoHideTouchedCellHint() {
        return mAutoHideTouchedCellHint;
    }

    public void setAutoHideTouchedCellHint(boolean autoHideTouchedCellHint) {
        mAutoHideTouchedCellHint = autoHideTouchedCellHint;
    }

    public void setHighlightSimilarCell(HighlightMode highlightSimilarCell) {
        mHighlightSimilarCells = highlightSimilarCell;
    }

    public int getHighlightedValue() {
        return mHighlightedValue;
    }

    public void setHighlightedValue(int value) {
        mHighlightedValue = value;
    }

    /**
     * Registers callback which will be invoked when user taps the cell.
     *
     * @param l
     */
    public void setOnCellTappedListener(OnCellTappedListener l) {
        mOnCellTappedListener = l;
    }

    protected void onCellTapped(Cell cell) {
        if (mOnCellTappedListener != null) {
            mOnCellTappedListener.onCellTapped(mCells, cell);
        }
    }

    /**
     * Registers callback which will be invoked when cell is selected. Cell selection
     * can change without user interaction.
     *
     * @param l
     */
    public void setOnCellSelectedListener(OnCellSelectedListener l) {
        mOnCellSelectedListener = l;
    }

    public void hideTouchedCellHint() {
        mTouchedCell = null;
        postInvalidate();
    }

    protected void onCellSelected(Grid grid, Cell cell) {
        if (mOnCellSelectedListener != null) {
            mOnCellSelectedListener.onCellSelected(grid, cell);
        }
    }

    public void invokeOnCellSelected() {
        onCellSelected(mCells, mSelectedCell);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);


//        Log.d(TAG, "widthMode=" + getMeasureSpecModeString(widthMode));
//        Log.d(TAG, "widthSize=" + widthSize);
//        Log.d(TAG, "heightMode=" + getMeasureSpecModeString(heightMode));
//        Log.d(TAG, "heightSize=" + heightSize);

        int width, height;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = DEFAULT_BOARD_SIZE;
            if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
                width = widthSize;
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = DEFAULT_BOARD_SIZE;
            if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
                height = heightSize;
            }
        }

        if (widthMode != MeasureSpec.EXACTLY) {
            width = height;
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            height = width;
        }

        if (widthMode == MeasureSpec.AT_MOST && width > widthSize) {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST && height > heightSize) {
            height = heightSize;
        }
        if (mShowLabel) {
            mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 10.0f;
            mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 10.0f;
        } else {
            mCellWidth = (width - getPaddingLeft() - getPaddingRight()) / 9.0f;
            mCellHeight = (height - getPaddingTop() - getPaddingBottom()) / 9.0f;
        }


        setMeasuredDimension(width, height);

        float cellTextSize = mCellHeight * 0.75f;
        mCellValuePaint.setTextSize(cellTextSize);
        mCellValueReadonlyPaint.setTextSize(cellTextSize);
        mCellValueInvalidPaint.setTextSize(cellTextSize);
        // compute offsets in each cell to center the rendered number
        mNumberLeft = (int) ((mCellWidth - mCellValuePaint.measureText("9")) / 2);
        mNumberTop = (int) ((mCellHeight - mCellValuePaint.getTextSize()) / 2);

        // add some offset because in some resolutions notes are cut-off in the top
        mNoteTop = mCellHeight / 50.0f;
        mCellNotePaint.setTextSize((mCellHeight - mNoteTop * 2) / 3.0f);

        computeSectorLineWidth(width, height);
    }

    private void computeSectorLineWidth(int widthInPx, int heightInPx) {
        int sizeInPx = Math.min(widthInPx, heightInPx);
        float dipScale = getContext().getResources().getDisplayMetrics().density;
        float sizeInDip = sizeInPx / dipScale;

        float sectorLineWidthInDip = 2.0f;

        if (sizeInDip > 150) {
            sectorLineWidthInDip = 3.0f;
        }

        mSectorLineWidth = (int) (sectorLineWidthInDip * dipScale);
    }

    int paddingLeft = 0;
    int paddingTop = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // some notes:
        // Drawable has its own draw() method that takes your Canvas as an argument

        // TODO: I don't get this, why do I need to substract padding only from one side?
        int width = getWidth() - getPaddingRight();
        int height = getHeight() - getPaddingBottom();

        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();

        //处理label
        if (mShowLabel) {
            paddingLeft += mCellWidth;
            paddingTop += mCellHeight;
        }

        // draw secondary background
        if (mBackgroundColorSecondary.getColor() != NO_COLOR) {
            canvas.drawRect(3 * mCellWidth, 0, 6 * mCellWidth, 3 * mCellWidth, mBackgroundColorSecondary);
            canvas.drawRect(0, 3 * mCellWidth, 3 * mCellWidth, 6 * mCellWidth, mBackgroundColorSecondary);
            canvas.drawRect(6 * mCellWidth, 3 * mCellWidth, 9 * mCellWidth, 6 * mCellWidth, mBackgroundColorSecondary);
            canvas.drawRect(3 * mCellWidth, 6 * mCellWidth, 6 * mCellWidth, 9 * mCellWidth, mBackgroundColorSecondary);
        }

        // draw cells
        int cellLeft, cellTop;
        if (mCells != null) {

            boolean hasBackgroundColorReadOnly = mBackgroundColorReadOnly.getColor() != NO_COLOR;

            float numberAscent = mCellValuePaint.ascent();
            float noteAscent = mCellNotePaint.ascent();
            float noteWidth = mCellWidth / 3f;

            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Cell cell = mCells.getCell(col, row);

                    cellLeft = Math.round((col * mCellWidth) + paddingLeft);
                    cellTop = Math.round((row * mCellHeight) + paddingTop);

                    // draw read-only field background
                    if (!mCells.isEditable(cell.getIndex()) && hasBackgroundColorReadOnly &&
                            (mSelectedCell == null || mSelectedCell != cell)) {
                        if (mBackgroundColorReadOnly.getColor() != NO_COLOR) {
                            canvas.drawRect(
                                    cellLeft, cellTop,
                                    cellLeft + mCellWidth, cellTop + mCellHeight,
                                    mBackgroundColorReadOnly);
                        }
                    }

                    // highlight similar cells
                    boolean cellIsNotAlreadySelected = (mSelectedCell == null || mSelectedCell != cell);
                    boolean highlightedValueIsValid = mHighlightedValue != 0;
                    boolean shouldHighlightCell = false;

                    switch (mHighlightSimilarCells) {
                        default:
                        case NONE: {
                            shouldHighlightCell = false;
                            break;
                        }

                        case NUMBERS: {
                            shouldHighlightCell =
                                    cellIsNotAlreadySelected &&
                                            highlightedValueIsValid &&
                                            mHighlightedValue == mCells.getCellValue(cell.getIndex());
                            break;
                        }

                        case NUMBERS_AND_NOTES: {
                            shouldHighlightCell =
                                    cellIsNotAlreadySelected &&
                                            highlightedValueIsValid &&
                                            (mHighlightedValue == mCells.getCellValue(cell.getIndex()) ||
                                                    (mCells.hasCellPotentialValue(cell.getIndex(), mHighlightedValue)) &&
                                                            mCells.getCellValue(cell.getIndex()) == 0);
                            //cell.getNote().getNotedNumbers().contains(mHighlightedValue))
                        }
                    }

                    if (shouldHighlightCell) {
                        if (mBackgroundColorHighlighted.getColor() != NO_COLOR) {
                            canvas.drawRect(
                                    cellLeft, cellTop,
                                    cellLeft + mCellWidth, cellTop + mCellHeight,
                                    mBackgroundColorHighlighted);
                        }
                    }

                }
            }

            // highlight selected cell
            if (!mReadonly && mSelectedCell != null) {

                cellLeft = Math.round(Grid.getColumnNumAt(mSelectedCell.getX(), mSelectedCell.getY()) * mCellWidth) + paddingLeft;
                cellTop = Math.round(Grid.getRowNumAt(mSelectedCell.getX(), mSelectedCell.getY()) * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft, cellTop,
                        cellLeft + mCellWidth, cellTop + mCellHeight,
                        mBackgroundColorSelected);
            }

            // visually highlight cell under the finger (to cope with touch screen
            // imprecision)
            if (mHighlightTouchedCell && mTouchedCell != null) {
                cellLeft = Math.round(Grid.getColumnNumAt(mTouchedCell.getX(), mTouchedCell.getY()) * mCellWidth) + paddingLeft;
                cellTop = Math.round(Grid.getRowNumAt(mTouchedCell.getX(), mTouchedCell.getY()) * mCellHeight) + paddingTop;
                canvas.drawRect(
                        cellLeft, paddingTop,
                        cellLeft + mCellWidth, height,
                        mBackgroundColorTouched);
                canvas.drawRect(
                        paddingLeft, cellTop,
                        width, cellTop + mCellHeight,
                        mBackgroundColorTouched);
            }
            if (mShowHint) {
                try {

                    paintHighlightedRegions(canvas, true, false);
                    paintOutlineSingleCells(canvas);
                } catch (Exception er) {
                    er.printStackTrace();
                }

            }
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    Cell cell = mCells.getCell(col, row);

                    cellLeft = Math.round((col * mCellWidth) + paddingLeft);
                    cellTop = Math.round((row * mCellHeight) + paddingTop);

                    // draw cell Text
                    int value = mCells.getCellValue(cell.getIndex());
                    if (value != 0) {
                        Paint cellValuePaint = mCells.isEditable(cell.getIndex()) ? mCellValuePaint : mCellValueReadonlyPaint;

                        if (mHighlightWrongVals && !mCells.isCellValid(cell.getIndex())) {
                            cellValuePaint = mCellValueInvalidPaint;
                        }

                        canvas.drawText(Integer.toString(value),
                                cellLeft + mNumberLeft,
                                cellTop + mNumberTop - numberAscent,
                                cellValuePaint);
                    } else {

                        if (!mCells.getCellPotentialValues(cell.getIndex()).isEmpty()) {
                            ArrayList<Integer> numbers = Grid.bitSetToInt(mCells.getCellPotentialValues(cell.getIndex()));
                            for (int number : numbers) {
                                int n = (int) number - 1;
                                int c = n % 3;
                                int r = n / 3;
                                if (mShowHint || mShowErrorPv) {
                                    Paint p = new Paint();

                                    boolean isHighlighted = initPotentialColor(p, cell, number);

                                    if (isHighlighted ) {
                                        p.setTextSize(mCellNotePaint.getTextSize());
                                        p.setStrokeWidth(mCellNotePaint.getStrokeWidth());
                                        p.setAntiAlias(mCellNotePaint.isAntiAlias());

                                        p.setTextSize(mCellNotePaint.getTextSize() + 6);
                                        p.setStrokeWidth(mCellNotePaint.getStrokeWidth() + 6);
                                        canvas.drawText(Integer.toString((int) number), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, p);
                                    }
                                    else {
                                        canvas.drawText(Integer.toString((int) number), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, mCellNotePaint);
                                    }

                                } else {
                                    canvas.drawText(Integer.toString((int) number), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, mCellNotePaint);
                                }
                            }
                        }
                    }
                }
            }
        }

        // draw vertical lines
        for (int c = 0; c <= 9; c++) {
            float x = (c * mCellWidth) + paddingLeft;
            canvas.drawLine(x, paddingTop, x, height, mLinePaint);
        }

        // draw horizontal lines
        for (int r = 0; r <= 9; r++) {
            float y = r * mCellHeight + paddingTop;
            canvas.drawLine(paddingLeft, y, width, y, mLinePaint);
        }

        int sectorLineWidth1 = mSectorLineWidth / 2;
        int sectorLineWidth2 = sectorLineWidth1 + (mSectorLineWidth % 2);

        // draw sector (thick) lines
        for (int c = 0; c <= 9; c = c + 3) {
            float x = (c * mCellWidth) + paddingLeft;
            canvas.drawRect(x - sectorLineWidth1, paddingTop, x + sectorLineWidth2, height, mSectorLinePaint);
        }

        for (int r = 0; r <= 9; r = r + 3) {
            float y = r * mCellHeight + paddingTop;
            canvas.drawRect(paddingLeft, y - sectorLineWidth1, width, y + sectorLineWidth2, mSectorLinePaint);
        }
        if (mShowHint) {
            try {
                //paintOutlineSingleCells(canvas);
                //paintHighlightedRegions(canvas,false);
            } catch (Exception er) {
                er.printStackTrace();
            }

        }
        //处理label
        if (mShowLabel) {
            cellLeft = paddingLeft - (int) mCellWidth;
            cellTop = paddingTop - mNumberTop;
            for (int i = 1; i < 10; i++) {
                canvas.drawText(i + "",
                        cellLeft + mNumberLeft + i * mCellWidth,
                        cellTop,
                        mCellValueReadonlyPaint);
            }
            cellLeft = paddingLeft - (int) mCellWidth + mNumberLeft;
            cellTop = paddingTop - mNumberTop;
            for (int i = 1; i < 10; i++) {
                canvas.drawText("" + (char) ('A' + i - 1),
                        cellLeft,
                        cellTop + i * mCellHeight,
                        mCellValueReadonlyPaint);
            }
        }
        if (mShowHint) {
            showHint(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!mReadonly) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    mTouchedCell = getCellAtPoint(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    mSelectedCell = getCellAtPoint(x, y);
                    invalidate(); // selected cell has changed, update board as soon as you can

                    if (mSelectedCell != null) {
                        onCellTapped(mSelectedCell);
                        onCellSelected(mCells, mSelectedCell);
                    }

                    if (mAutoHideTouchedCellHint) {
                        mTouchedCell = null;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mTouchedCell = null;
                    break;
            }
            postInvalidate();
        }

        return !mReadonly;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mReadonly) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_UP:
                    return moveCellSelection(0, -1);
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    return moveCellSelection(1, 0);
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    return moveCellSelection(0, 1);
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    return moveCellSelection(-1, 0);
                case KeyEvent.KEYCODE_0:
                case KeyEvent.KEYCODE_SPACE:
                case KeyEvent.KEYCODE_DEL:
                    // clear value in selected cell
                    // TODO: I'm not really sure that this is thread-safe
                    if (mSelectedCell != null) {
                        if (event.isShiftPressed() || event.isAltPressed()) {
                            setCellNote(mSelectedCell, new BitSet(10));
                        } else {
                            setCellValue(mSelectedCell, 0);
                            moveCellSelectionRight();
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if (mSelectedCell != null) {
                        onCellTapped(mSelectedCell);
                    }
                    return true;
            }

            if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9 && mSelectedCell != null) {
                int selNumber = keyCode - KeyEvent.KEYCODE_0;
                Cell cell = mSelectedCell;

                if (event.isShiftPressed() || event.isAltPressed()) {
                    // add or remove number in cell's note
                    setCellNote(cell, mCells.buildCellPotentialValue(cell.getIndex(), selNumber));
                } else {
                    // enter number in cell
                    setCellValue(cell, selNumber);
                    moveCellSelectionRight();
                }
                return true;
            }
        }


        return false;
    }

    /**
     * Moves selected cell by one cell to the right. If edge is reached, selection
     * skips on beginning of another line.
     */
    public void moveCellSelectionRight() {
        if (!moveCellSelection(1, 0)) {
            int selRow = Grid.getRowNumAt(mSelectedCell.getX(), mSelectedCell.getY());
            selRow++;
            if (!moveCellSelectionTo(selRow, 0)) {
                moveCellSelectionTo(0, 0);
            }
        }
        postInvalidate();
    }

    private void setCellValue(Cell cell, int value) {
        if (mCells.isEditable(cell.getIndex())) {
            if (mGame != null) {
                mGame.setCellValue(cell, value);
            } else {
                mCells.setCellValue(cell.getIndex(), value);
            }
        }
    }

    private void setCellNote(Cell cell, BitSet note) {
        if (mCells.isEditable(cell.getIndex())) {
            if (mGame != null) {
                mGame.setCellNote(cell, note);
            } else {
                //cell.setNote(note);
            }
        }
    }

    /**
     * Moves selected by vx cells right and vy cells down. vx and vy can be negative. Returns true,
     * if new cell is selected.
     *
     * @param vx Horizontal offset, by which move selected cell.
     * @param vy Vertical offset, by which move selected cell.
     */
    private boolean moveCellSelection(int vx, int vy) {
        int newRow = 0;
        int newCol = 0;

        if (mSelectedCell != null) {
            newRow = Grid.getRowNumAt(mSelectedCell.getX(), mSelectedCell.getY()) + vy;
            newCol = Grid.getColumnNumAt(mSelectedCell.getX(), mSelectedCell.getY()) + vx;
        }

        return moveCellSelectionTo(newRow, newCol);
    }

    /**
     * Moves selection to the cell given by row and column index.
     *
     * @param row Row index of cell which should be selected.
     * @param col Columnd index of cell which should be selected.
     * @return True, if cell was successfuly selected.
     */
    public boolean moveCellSelectionTo(int row, int col) {

        if (col >= 0 && col < 9
                && row >= 0 && row < 9) {
            mSelectedCell = mCells.getCell(col, row);
            onCellSelected(mCells, mSelectedCell);

            postInvalidate();
            return true;
        }

        return false;
    }

    public void clearCellSelection() {
        mSelectedCell = null;
        onCellSelected(mCells, mSelectedCell);
        postInvalidate();
    }

    /**
     * Returns cell at given screen coordinates. Returns null if no cell is found.
     *
     * @param x
     * @param y
     * @return
     */
    private Cell getCellAtPoint(int x, int y) {
        // take into account padding
        int lx = x - getPaddingLeft();
        int ly = y - getPaddingTop();

        int row = (int) (ly / mCellHeight);
        int col = (int) (lx / mCellWidth);
        if (mShowLabel) {
            row -= 1;
            col -= 1;
        }
        if (col >= 0 && col < 9
                && row >= 0 && row < 9) {
            return mCells.getCell(col, row);
        } else {
            return null;
        }
    }

    private void showHint(Canvas c) {
        try {
            //SurfaceView surfaceView = new SurfaceView(getContext());
            // 从SurfaceView的surfaceHolder里锁定获取Canvas
            //SurfaceHolder surfaceHolder = surfaceView.getHolder();
            //获取Canvas
            //Canvas c = surfaceHolder.lockCanvas();

            //paintOutlineSingleCells(c);
            //paintHighlightedRegions(c);
            //paintCellsPotentials(c);
            paintLinks(c);
        } catch (Exception er) {
            er.printStackTrace();
            LogUtil.e(er.getMessage());
        }
    }

    public void repaintHint() {
        //TODO 显示hint


    }

    final Color orange = Color.valueOf(225, 165, 0);

    private void paintOutlineSingleCells(Canvas g) {
        if (highlightedCells != null) {


            for (int j = 0; j < highlightedCells.length; j++) {
                if (highlightedCells[j] == null)
                    continue;
                int x = highlightedCells[j].getX();
                int y = highlightedCells[j].getY();


                int cellLeft = Math.round(Grid.getColumnNumAt(x, y) * mCellWidth) + paddingLeft;
                int cellTop = Math.round(Grid.getRowNumAt(x, y) * mCellHeight) + paddingTop;
                g.drawRect(cellLeft , cellTop ,
                        cellLeft +  mCellWidth , cellTop + mCellHeight ,
                        mBackgroundColorHighlighted);

            }
        }

    }

    private void paintHighlightedRegions(Canvas g, boolean isBack, boolean isLine) {
        if (blueRegions != null) {
            Color[] colors = new Color[]{Color.valueOf(0, 128, 0), Color.valueOf(255, 0, 0)};
            for (int rev = 0; rev < 2; rev++) {
                for (int i = 0; i < blueRegions.length; i++) {
                    int index = (rev == 0 ? i : blueRegions.length - 1 - i);
                    Grid.Region region = blueRegions[index];
                    int x, y, w, h; // coordinates, width, height (in cells)
                    if (region != null)
                        if (region.getRegionTypeIndex() < 3) {
                            if (region instanceof Grid.Row) {
                                Grid.Row row = (Grid.Row) region;
                                y = row.getRowNum();
                                h = 1;
                                x = 0;
                                w = 9;
                            } else if (region instanceof Grid.Column) {
                                Grid.Column column = (Grid.Column) region;
                                x = column.getColumnNum();
                                w = 1;
                                y = 0;
                                h = 9;
                            } else {
                                Grid.Block square = (Grid.Block) region;
                                x = square.getHIndex() * 3;
                                y = square.getVIndex() * 3;
                                w = 3;
                                h = 3;
                            }
                            Paint p = new Paint(colors[index % 2].toArgb());
                            p.setStyle(Paint.Style.STROKE);

                            int cellLeft = Math.round(Grid.getColumnNumAt(x, y) * mCellWidth) + paddingLeft;
                            int cellTop = Math.round(Grid.getRowNumAt(x, y) * mCellHeight) + paddingTop;
                            //g.setColor(colors[index % 2]);
                            if (isLine) {
                                for (int s = -2 + rev; s <= 2; s += 2) {

                                    g.drawRect(cellLeft + s, cellTop + s,
                                            cellLeft + w * mCellWidth - s * 2, cellTop + h * mCellHeight - s * 2,
                                            p);
                                    // g.drawRect(x * CELL_OUTER_SIZE + s, y * CELL_OUTER_SIZE + s,
                                    //        w * CELL_OUTER_SIZE - s * 2, h * CELL_OUTER_SIZE - s * 2);
                                }
                            }
                            if (rev == 0 && isBack) {
                                Color base = colors[index % 2];
                                p.setColor(Color.valueOf(base.red(), base.green(), base.blue(), 12).toArgb());
                                p.setStyle(Paint.Style.FILL);
                                //g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 12));
                                g.drawRect(cellLeft + 1, cellTop + 1,
                                        cellLeft + w * mCellWidth - 2, cellTop + h * mCellHeight - 2,
                                        mBackgroundColorSelected);
                                //g.fillRect(x * CELL_OUTER_SIZE + 3, y * CELL_OUTER_SIZE + 3,
                                //        w * CELL_OUTER_SIZE - 6, h * CELL_OUTER_SIZE - 6);
                            }
                        } else {
                            //@SudokuMonster: individual cells of region outlined in variants
                            if (region instanceof Grid.Window) {
                                Grid.Window window = (Grid.Window) region;
                                int wdi = window.getRegionIndex();
                                int js = 0, jend = 1, jinc = 1;
                                w = h = 3;
                                if (wdi == 0 || wdi == 1 || wdi == 2 || wdi == 3) {
                                    w = 3;
                                    h = 3;
                                }
                                if (wdi == 4 || wdi == 5) {
                                    w = 3;
                                    h = 1;
                                    jend = 9;
                                    jinc = 3;
                                }
                                if (wdi == 6 || wdi == 7) {
                                    w = 1;
                                    h = 3;
                                    jend = 9;
                                    jinc = 3;
                                }
                                if (wdi == 8) {
                                    w = 1;
                                    h = 1;
                                    jend = 9;
                                }
                                for (int j = js; j < jend; j += jinc) {
                                    x = region.getRegionCellIndexColumn(j);
                                    y = region.getRegionCellIndexRow(j);

                                    int cellLeft = Math.round(Grid.getColumnNumAt(x, y) * mCellWidth) + paddingLeft;
                                    int cellTop = Math.round(Grid.getRowNumAt(x, y) * mCellHeight) + paddingTop;
                                    //g.setColor(colors[index % 2]);
                                    Paint p = new Paint(colors[index % 2].toArgb());
                                    p.setStyle(Paint.Style.STROKE);
                                    if (isLine) {
                                        for (int s = -2 + rev; s <= 2; s += 2) {
                                            g.drawRect(cellLeft + s, cellTop + s,
                                                    cellLeft + w * mCellWidth - s * 2, cellTop + h * mCellHeight - s * 2,
                                                    p);
                                            // g.drawRect(x * CELL_OUTER_SIZE + s, y * CELL_OUTER_SIZE + s,
                                            //         w * CELL_OUTER_SIZE - s * 2, h * CELL_OUTER_SIZE - s * 2);
                                        }
                                    }
                                    if (rev == 0 && isBack) {
                                        Color base = colors[index % 2];
                                        // g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 12));
                                        p.setColor(Color.valueOf(base.red(), base.green(), base.blue(), 12).toArgb());
                                        p.setStyle(Paint.Style.FILL);
                                        //g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 12));
                                        g.drawRect(cellLeft + 1, cellTop + 1,
                                                cellLeft + w * mCellWidth - 2, cellTop + h * mCellHeight - 2,
                                                mBackgroundColorSelected);
                                        // g.fillRect(x * CELL_OUTER_SIZE + 3, y * CELL_OUTER_SIZE + 3,
                                        //         w * CELL_OUTER_SIZE - 6, h * CELL_OUTER_SIZE - 6);
                                    }
                                }
                            } else {
                                for (int j = 0; j < 9; j++) {
                                    x = region.getRegionCellIndexColumn(j);
                                    y = region.getRegionCellIndexRow(j);
                                    w = 1;
                                    h = 1;
                                    //g.setColor(colors[index % 2]);
                                    int cellLeft = Math.round(Grid.getColumnNumAt(x, y) * mCellWidth) + paddingLeft;
                                    int cellTop = Math.round(Grid.getRowNumAt(x, y) * mCellHeight) + paddingTop;
                                    //g.setColor(colors[index % 2]);
                                    Paint p = new Paint(colors[index % 2].toArgb());
                                    p.setStyle(Paint.Style.STROKE);
                                    if (isLine) {
                                        for (int s = -2 + rev; s <= 2; s += 2) {
                                            // g.drawRect(x * CELL_OUTER_SIZE + s, y * CELL_OUTER_SIZE + s,
                                            //        w * CELL_OUTER_SIZE - s * 2, h * CELL_OUTER_SIZE - s * 2);
                                            g.drawRect(cellLeft + s, cellTop + s,
                                                    cellLeft + w * mCellWidth - s * 2, cellTop + h * mCellHeight - s * 2,
                                                    p);
                                        }
                                    }
                                    if (rev == 0 && isBack) {
                                        Color base = colors[index % 2];
                                        p.setColor(Color.valueOf(base.red(), base.green(), base.blue(), 12).toArgb());
                                        p.setStyle(Paint.Style.FILL);
                                        g.drawRect(cellLeft + 1, cellTop + 1,
                                                cellLeft + w * mCellWidth - 2, cellTop + h * mCellHeight - 2,
                                                mBackgroundColorSelected);
                                        //g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 12));
                                        //g.fillRect(x * CELL_OUTER_SIZE + 3, y * CELL_OUTER_SIZE + 3,
                                        //        w * CELL_OUTER_SIZE - 6, h * CELL_OUTER_SIZE - 6);
                                    }
                                }
                            }
                        }
                    index++;
                }
            }
        }
    }

    private void paintCellsPotentials(Canvas g) {
        float noteAscent = mCellNotePaint.ascent();
        float noteWidth = mCellWidth / 3f;
        //在这只处理要特别显示的标注。要原来的位置重写一个。
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                Cell cell = mCells.getCell(x, y);
                int cellLeft = Math.round((x * mCellWidth) + paddingLeft);
                int cellTop = Math.round((y * mCellHeight) + paddingTop);
                int index = 0;
                for (int value = 1; value <= 9; value++) {
                    if (mCells.hasCellPotentialValue(cell.getIndex(), value)) {
                        Paint p = new Paint();
                        p.setTextSize(mCellNotePaint.getTextSize() + 4);
                        p.setStrokeWidth(mCellNotePaint.getStrokeWidth() + 4);
                        p.setAntiAlias(mCellNotePaint.isAntiAlias());
                        boolean isHighlighted = initPotentialColor(p, cell, value);
                        if (isHighlighted) {
                            // mSelectedCell=cell;
                            //drawStringCentered3D(g, "" + value, cx, cy);

                            int n = (int) value - 1;
                            int c = n % 3;
                            int r = n / 3;
                            g.drawText(Integer.toString((int) value), cellLeft + c * noteWidth + 2, cellTop + mNoteTop - noteAscent + r * noteWidth - 1, p);

                        }
                    }
                }
            }
        }

        /*
        Rectangle clip = g.getClipBounds();
        Rectangle cellRect = new Rectangle();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                readCellRectangle(x, y, cellRect);
                if (clip.intersects(cellRect)) {
                    Cell cell = Grid.getCell(x, y);
                    // Paint potentials
                    int index = 0;
                    g.setFont(smallFont);
                    for (int value = 1; value <= 9; value++) {
                        boolean paintIt = Settings.getInstance().isShowingCandidates();
                        if (cell == this.selectedCell && value == this.focusedCandidate) {
                            // Paint magenta selection
                            g.setColor(Color.magenta);
                            g.fillRect(
                                    x * CELL_OUTER_SIZE + CELL_PAD + (index % 3) * (CELL_INNER_SIZE / 3),
                                    y * CELL_OUTER_SIZE + CELL_PAD + (index / 3) * (CELL_INNER_SIZE / 3),
                                    CELL_INNER_SIZE / 3, CELL_INNER_SIZE / 3);
                            paintIt = true;
                        }
                        //if (cell.hasPotentialValue(value)) {
                        if (grid.hasCellPotentialValue(cell.getIndex(), value)) {
                            int cx = x * CELL_OUTER_SIZE + CELL_PAD
                                    + (index % 3) * (CELL_INNER_SIZE / 3) + CELL_INNER_SIZE / 6;
                            int cy = y * CELL_OUTER_SIZE + CELL_PAD
                                    + (index / 3) * (CELL_INNER_SIZE / 3) + CELL_INNER_SIZE / 6;
                            boolean isHighlighted = initPotentialColor(g, cell, value);
                            if (isHighlighted)
                                drawStringCentered3D(g, "" + value, cx, cy);
                            else if (paintIt)
                                drawStringCentered(g, "" + value, cx, cy);
                        }
                        index++;
                    }
                }
            }
        }*/
    }

    private boolean initPotentialColor(Paint p, Cell cell, int value) {
        boolean isHighlighted = false;
        boolean isRed = false;
        int col = p.getColor();
        if (bluePotentials != null) {
            BitSet blueValues = bluePotentials.get(cell);
            if (blueValues != null && blueValues.get(value)) {
                col = Color.BLUE;
                isHighlighted = true;
            }
        }
        if (redPotentials != null) {
            BitSet redValues = redPotentials.get(cell);
            if (redValues != null && redValues.get(value)) {
                col = Color.RED;
                isHighlighted = true;
                isRed = true;
            }
        }
        if (greenPotentials != null) {
            BitSet greenValues = greenPotentials.get(cell);
            if (greenValues != null && greenValues.get(value)) {
                if (isRed) {
                    col = Color.rgb(224, 128, 0);
                } else {
                    col = Color.rgb(0, 224, 0);
                    isHighlighted = true;
                }
            }
        }
        p.setColor(col);

        return isHighlighted;
    }

    private class Line {

        public final int sx;
        public final int sy;
        public final int ex;
        public final int ey;

        public Line(int sx, int sy, int ex, int ey) {
            this.sx = sx;
            this.sy = sy;
            this.ex = ex;
            this.ey = ey;
        }

        private int distanceUnscaled(int px, int py) {
            // Vectorial product, without normalization by length
            return (px - sx) * (ey - sy) - (py - sy) * (ex - sx);
        }

        private boolean intervalOverlaps(int s1, int e1, int s2, int e2) {
            if (s1 > e1) {
                // Swap
                s1 = s1 ^ e1;
                e1 = s1 ^ e1;
                s1 = s1 ^ e1;
            }
            if (s2 > e2) {
                // Swap
                s2 = s2 ^ e2;
                e2 = s2 ^ e2;
                s2 = s2 ^ e2;
            }
            return s1 < e2 && e1 > s2;
        }

        public boolean overlaps(Line other) {
            if (distanceUnscaled(other.sx, other.sy) == 0 &&
                    distanceUnscaled(other.ex, other.ey) == 0) {
                // Both lines are on the same right
                return intervalOverlaps(this.sx, this.ex, other.sx, other.ex)
                        || intervalOverlaps(this.sy, this.ey, other.sy, other.ey);
            }
            return false;
        }

    }

    private void paintLinks(Canvas g) {
        Paint p = new Paint();
        p.setStrokeWidth(3);
        p.setColor(orange.toArgb());
        p.setStyle(Paint.Style.STROKE);
        //g.setColor(Color.orange);
        if (links != null) {
            Collection<Line> paintedLines = new ArrayList<Line>();
            for (Link link : links) {
                double sx = link.getSrcCell().getX() * mCellWidth + paddingLeft + mCellWidth / 6;
                double sy = link.getSrcCell().getY() * mCellHeight + paddingTop + mCellHeight / 6;
                //double sx = link.getSrcCell().getX() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                // double sy = link.getSrcCell().getY() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                int srcValue = link.getSrcValue();
                if (srcValue > 0) {
                    sx += ((srcValue - 1) % 3) * (mCellWidth / 3);
                    sy += ((srcValue - 1) / 3) * (mCellHeight / 3);
                    // sx += ((srcValue - 1) % 3) * (CELL_INNER_SIZE / 3);
                    // sy += ((srcValue - 1) / 3) * (CELL_INNER_SIZE / 3);
                } else {
                    sx += mCellWidth / 3;
                    sy += mCellHeight / 3;

                    // sx += CELL_INNER_SIZE / 3;
                    // sy += CELL_INNER_SIZE / 3;

                }
                double ex = link.getDstCell().getX() * mCellWidth + paddingLeft + mCellWidth / 6;
                double ey = link.getDstCell().getY() * mCellHeight + paddingTop + mCellHeight / 6;
                // double ex = link.getDstCell().getX() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                // double ey = link.getDstCell().getY() * CELL_OUTER_SIZE + CELL_PAD + CELL_INNER_SIZE / 6;
                int dstValue = link.getDstValue();
                if (dstValue > 0) {
                    ex += ((dstValue - 1) % 3) * (mCellWidth / 3);
                    ey += ((dstValue - 1) / 3) * (mCellHeight / 3);
                    //ex += ((dstValue - 1) % 3) * (CELL_INNER_SIZE / 3);
                    //ey += ((dstValue - 1) / 3) * (CELL_INNER_SIZE / 3);
                } else {
                    ex += mCellWidth / 3;
                    ey += mCellHeight / 3;
                    //ex += CELL_INNER_SIZE / 3;
                    //ey += CELL_INNER_SIZE / 3;
                }
                // Get unity vector
                double length = Math.sqrt((ex - sx) * (ex - sx) + (ey - sy) * (ey - sy));
                double ux = (ex - sx) / length;
                double uy = (ey - sy) / length;
                // Build line object
                Line line = new Line((int) sx, (int) sy, (int) ex, (int) ey);
                // Count number of overlapping lines
                int countOverlap = 0;
                for (Line other : paintedLines) {
                    if (line.overlaps(other))
                        countOverlap++;
                }
                // Move the line perpendicularly to go away from overlapping lines
                double mx = (uy * ((countOverlap + 1) / 2) * 3.0);
                double my = (ux * ((countOverlap + 1) / 2) * 3.0);
                if (countOverlap % 2 == 0)
                    mx = -mx;
                else
                    my = -my;
                if (length >= mCellWidth / 2) {//CELL_INNER_SIZE
                    // Truncate end points
                    if (srcValue > 0) {
                        sx += ux * mCellWidth / 6;//CELL_INNER_SIZE
                        sy += uy * mCellHeight / 6;//CELL_INNER_SIZE
                    }
                    if (dstValue > 0) {
                        ex -= ux * mCellWidth / 6;//CELL_INNER_SIZE
                        ey -= uy * mCellHeight / 6;//CELL_INNER_SIZE
                    }
                    if (dstValue > 0) {
                        Paint p2 = new Paint();
                        p2.setStrokeWidth(8);
                        p2.setColor(orange.toArgb());
                        // Draw arrow
                        double lx = ex - ux * 5 + uy * 2;
                        double ly = ey - uy * 5 - ux * 2;
                        double rx = ex - ux * 5 - uy * 2;
                        double ry = ey - uy * 5 + ux * 2;
                        Path path1 = new Path();
                        path1.moveTo((int) (ex + mx), (int) (ey + my));
                        path1.lineTo((int) (rx + mx), (int) (ry + my));
                        path1.lineTo((int) (lx + mx), (int) (ly + my));
                        path1.close();
                        g.drawPath(path1, p2);
                        //  g.fillPolygon(new int[]{(int) (ex + mx), (int) (rx + mx), (int) (lx + mx)},
                        //        new int[]{(int) (ey + my), (int) (ry + my), (int) (ly + my)}, 3);
                        drawTrangle(g, p2, (int) (sx + mx), (int) (sy + my), (int) (ex + mx), (int) (ey + my), 20, 10);
                    }
                    paintedLines.add(line);
                }
                // Draw the line
                if (dstValue == 0 && srcValue == 0) {
                    int oldColor = p.getColor();

                    //g.setColor(Color.magenta);
                    g.drawLine((int) (sx + mx), (int) (sy + my), (int) (ex + mx), (int) (ey + my), p);

                    //g.setColor(oldColor);
                    p.setColor(oldColor);
                } else {
                    g.drawLine((int) (sx + mx), (int) (sy + my), (int) (ex + mx), (int) (ey + my), p);

                }
            }
        }
    }

    /**
     * 绘制三角
     *
     * @param canvas
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     * @param height
     * @param bottom
     */
    private void drawTrangle(Canvas canvas, Paint paintLine, float fromX, float fromY, float toX, float toY, int height, int bottom) {
        try {
            float juli = (float) Math.sqrt((toX - fromX) * (toX - fromX)
                    + (toY - fromY) * (toY - fromY));// 获取线段距离
            float juliX = toX - fromX;// 有正负，不要取绝对值
            float juliY = toY - fromY;// 有正负，不要取绝对值
            float dianX = toX - (height / juli * juliX);
            float dianY = toY - (height / juli * juliY);
            float dian2X = fromX + (height / juli * juliX);
            float dian2Y = fromY + (height / juli * juliY);
            //终点的箭头
            Path path = new Path();
            path.moveTo(toX, toY);// 此点为三边形的起点
            path.lineTo(dianX + (bottom / juli * juliY), dianY
                    - (bottom / juli * juliX));
            path.lineTo(dianX - (bottom / juli * juliY), dianY
                    + (bottom / juli * juliX));
            path.close(); // 使这些点构成封闭的三边形
            canvas.drawPath(path, paintLine);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public enum HighlightMode {
        NONE,
        NUMBERS,
        NUMBERS_AND_NOTES
    }

    /**
     * Occurs when user tap the cell.
     *
     * @author romario
     */
    public interface OnCellTappedListener {
        void onCellTapped(Grid grid, Cell cell);
    }

    /**
     * Occurs when user selects the cell.
     *
     * @author romario
     */
    public interface OnCellSelectedListener {
        void onCellSelected(Grid grid, Cell cell);
    }

//	private String getMeasureSpecModeString(int mode) {
//		String modeString = null;
//		switch (mode) {
//		case MeasureSpec.AT_MOST:
//			modeString = "MeasureSpec.AT_MOST";
//			break;
//		case MeasureSpec.EXACTLY:
//			modeString = "MeasureSpec.EXACTLY";
//			break;
//		case MeasureSpec.UNSPECIFIED:
//			modeString = "MeasureSpec.UNSPECIFIED";
//			break;
//		}
//
//		if (modeString == null)
//			modeString = new Integer(mode).toString();
//
//		return modeString;
//	}

}
