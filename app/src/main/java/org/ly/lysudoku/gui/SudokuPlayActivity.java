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

import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.ly.lysudoku.Grid;
import org.ly.lysudoku.R;
import org.ly.lysudoku.db.SudokuDatabase;
import org.ly.lysudoku.Cell;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.game.SudokuGame.OnPuzzleSolvedListener;
import org.ly.lysudoku.gui.inputmethod.IMControlPanel;
import org.ly.lysudoku.gui.inputmethod.IMControlPanelStatePersister;
import org.ly.lysudoku.gui.inputmethod.IMNumpad;
import org.ly.lysudoku.gui.inputmethod.IMPopup;
import org.ly.lysudoku.gui.inputmethod.IMSingleNumber;
import org.ly.lysudoku.tools.Asker;
import org.ly.lysudoku.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class SudokuPlayActivity extends ThemedActivity implements Asker {

    public static final String EXTRA_SUDOKU_ID = "sudoku_id";

    public static final int MENU_ITEM_RESTART = Menu.FIRST;
    public static final int MENU_ITEM_CLEAR_ALL_NOTES = Menu.FIRST + 1;
    public static final int MENU_ITEM_FILL_IN_NOTES = Menu.FIRST + 2;
    public static final int MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES = Menu.FIRST + 3;
    public static final int MENU_ITEM_UNDO_ACTION = Menu.FIRST + 4;
    public static final int MENU_ITEM_UNDO = Menu.FIRST + 5;
    public static final int MENU_ITEM_HELP = Menu.FIRST + 6;
    public static final int MENU_ITEM_SETTINGS_ACTION = Menu.FIRST + 7;
    public static final int MENU_ITEM_SETTINGS = Menu.FIRST + 8;

    public static final int MENU_ITEM_SET_CHECKPOINT = Menu.FIRST + 9;
    public static final int MENU_ITEM_UNDO_TO_CHECKPOINT = Menu.FIRST + 10;
    public static final int MENU_ITEM_UNDO_TO_BEFORE_MISTAKE = Menu.FIRST + 11;
    public static final int MENU_ITEM_SOLVE = Menu.FIRST + 12;
    public static final int MENU_ITEM_HINT = Menu.FIRST + 13;

    private static final int DIALOG_RESTART = 1;
    private static final int DIALOG_WELL_DONE = 2;
    private static final int DIALOG_CLEAR_NOTES = 3;
    private static final int DIALOG_UNDO_TO_CHECKPOINT = 4;
    private static final int DIALOG_UNDO_TO_BEFORE_MISTAKE = 5;
    private static final int DIALOG_SOLVE_PUZZLE = 6;
    private static final int DIALOG_USED_SOLVER = 7;
    private static final int DIALOG_PUZZLE_NOT_SOLVED = 8;
    private static final int DIALOG_HINT = 9;
    private static final int DIALOG_CANNOT_GIVE_HINT = 11;

    private static final int REQUEST_SETTINGS = 1;
    private static final int SOLVER_STEP = 10;
    private SudokuGame mSudokuGame;

    private SudokuDatabase mDatabase;

    private Handler mGuiHandler;

    private ViewGroup mRootLayout;
    private SudokuBoardView mSudokuBoard;
    private TextView mTimeLabel;
    private Menu mOptionsMenu;

    private IMControlPanel mIMControlPanel;
    private IMControlPanelStatePersister mIMControlPanelStatePersister;
    private IMPopup mIMPopup;
    private IMSingleNumber mIMSingleNumber;
    private IMNumpad mIMNumpad;

    private boolean mShowTime = true;
    private GameTimer mGameTimer;
    private GameTimeFormat mGameTimeFormatter = new GameTimeFormat();
    private boolean mFullScreen;
    private boolean mFillInNotesEnabled = false;

    private HintsQueue mHintsQueue;

    public boolean ask(String message) {
        //return JOptionPane.showConfirmDialog(this, message, getTitle(),
        //         JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        return true;
    }

    /**
     * Occurs when puzzle is solved.
     */
    private OnPuzzleSolvedListener onSolvedListener = new OnPuzzleSolvedListener() {

        @Override
        public void onPuzzleSolved() {
            if (mShowTime) {
                mGameTimer.stop();
            }
            mSudokuBoard.setReadOnly(true);
            mOptionsMenu.findItem(MENU_ITEM_UNDO_ACTION).setEnabled(false);
            if (mSudokuGame.usedSolver()) {
                showDialog(DIALOG_USED_SOLVER);
            } else {
                showDialog(DIALOG_WELL_DONE);
            }
        }

    };
    private OnSelectedNumberChangedListener onSelectedNumberChangedListener = new OnSelectedNumberChangedListener() {
        @Override
        public void onSelectedNumberChanged(int number) {
            if (number != 0) {
                Cell cell = mSudokuGame.getGrid().findFirstCell(number);
                mSudokuBoard.setHighlightedValue(number);
                if (cell != null) {
                    mSudokuBoard.moveCellSelectionTo(cell.getY(), cell.getX());
                } else {
                    mSudokuBoard.clearCellSelection();
                }
            } else {
                mSudokuBoard.clearCellSelection();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // go fullscreen for devices with QVGA screen (only way I found
        // how to fit UI on the screen)
        Display display = getWindowManager().getDefaultDisplay();
        if ((display.getWidth() == 240 || display.getWidth() == 320)
                && (display.getHeight() == 240 || display.getHeight() == 320)) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mFullScreen = true;
        }

        setContentView(R.layout.sudoku_play);

        mRootLayout = findViewById(R.id.root_layout);
        mSudokuBoard = findViewById(R.id.sudoku_board);
        mTimeLabel = findViewById(R.id.time_label);

        mDatabase = new SudokuDatabase(getApplicationContext());
        mHintsQueue = new HintsQueue(this);
        mGameTimer = new GameTimer();

        mGuiHandler = new Handler();

        // create sudoku game instance
        if (savedInstanceState == null) {
            // activity runs for the first time, read game from database
            long mSudokuGameID = getIntent().getLongExtra(EXTRA_SUDOKU_ID, -1);
            if (mSudokuGameID > 0) {
                mSudokuGame = mDatabase.getSudoku(mSudokuGameID);
            } else {
                /*
                还是在生成后先入库，再从库中加载比较合理，这样可以记录生成的游戏
                //Generator
                //处理生成的数独，要设置值，并生新设置editeable,然后重置数独
                Grid ng = (Grid) getIntent().getSerializableExtra("GRID");
                if (ng != null) {
                    mSudokuGame = mDatabase.getSudoku(SudokuDatabase.TEMPID);
                    ng.reSetEditable();//这个是算法生成的，只有数据。
                    mSudokuGame.setGrid(ng);
                    mSudokuGame.reset();
                    mDatabase.updateSudoku(mSudokuGame);
                }*/
            }
        } else {
            // activity has been running before, restore its state
            mSudokuGame = new SudokuGame();
            mSudokuGame.restoreState(savedInstanceState);
            mGameTimer.restoreState(savedInstanceState);

        }

        // save our most recently played sudoku
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = gameSettings.edit();
        editor.putLong("most_recently_played_sudoku_id", mSudokuGame.getId());
        editor.apply();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_NOT_STARTED) {
            mSudokuGame.start();
        } else if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();
        }

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_COMPLETED) {
            mSudokuBoard.setReadOnly(true);
        }

        mSudokuBoard.setGame(mSudokuGame);
        mSudokuGame.setOnPuzzleSolvedListener(onSolvedListener);

        mHintsQueue.showOneTimeHint("welcome", R.string.welcome, R.string.first_run_hint);

        mIMControlPanel = findViewById(R.id.input_methods);
        mIMControlPanel.initialize(mSudokuBoard, mSudokuGame, mHintsQueue);

        mIMControlPanelStatePersister = new IMControlPanelStatePersister(this);

        mIMPopup = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_POPUP);
        mIMSingleNumber = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_SINGLE_NUMBER);
        mIMNumpad = mIMControlPanel.getInputMethod(IMControlPanel.INPUT_METHOD_NUMPAD);

        Cell cell = mSudokuGame.getLastChangedCell();
        if (cell != null && !mSudokuBoard.isReadOnly())
            mSudokuBoard.moveCellSelectionTo(cell.getY(), cell.getX());
        else
            mSudokuBoard.moveCellSelectionTo(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSudokuGame.setAsker(this);
        // read game settings
        SharedPreferences gameSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        int screenPadding = gameSettings.getInt("screen_border_size", 0);
        mRootLayout.setPadding(screenPadding, screenPadding, screenPadding, screenPadding);

        mFillInNotesEnabled = gameSettings.getBoolean("fill_in_notes_enabled", true);

        String theme = gameSettings.getString("theme", "opensudoku");
        if (theme.equals("custom") || theme.equals("custom_light")) {
            ThemeUtils.applyCustomThemeToSudokuBoardViewFromContext(mSudokuBoard, getApplicationContext());
        }

        mSudokuBoard.setHighlightWrongVals(gameSettings.getBoolean("highlight_wrong_values", true));
        mSudokuBoard.setHighlightTouchedCell(gameSettings.getBoolean("highlight_touched_cell", true));

        boolean highlightSimilarCells = gameSettings.getBoolean("highlight_similar_cells", true);
        boolean highlightSimilarNotes = gameSettings.getBoolean("highlight_similar_notes", true);
        if (highlightSimilarCells) {
            mSudokuBoard.setHighlightSimilarCell(highlightSimilarNotes ?
                    SudokuBoardView.HighlightMode.NUMBERS_AND_NOTES :
                    SudokuBoardView.HighlightMode.NUMBERS);
        } else {
            mSudokuBoard.setHighlightSimilarCell(SudokuBoardView.HighlightMode.NONE);
        }

        mSudokuGame.setRemoveNotesOnEntry(gameSettings.getBoolean("remove_notes_on_input", true));

        mShowTime = gameSettings.getBoolean("show_time", true);
        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.resume();

            if (mShowTime) {
                mGameTimer.start();
            }
        }
        mTimeLabel.setVisibility(mFullScreen && mShowTime ? View.VISIBLE : View.GONE);

        mIMPopup.setEnabled(gameSettings.getBoolean("im_popup", true));
        mIMSingleNumber.setEnabled(gameSettings.getBoolean("im_single_number", true));
        mIMNumpad.setEnabled(gameSettings.getBoolean("im_numpad", true));
        mIMNumpad.setMoveCellSelectionOnPress(gameSettings.getBoolean("im_numpad_move_right", false));
        mIMPopup.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMPopup.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", true));
        mIMSingleNumber.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMSingleNumber.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", true));
        mIMSingleNumber.setBidirectionalSelection(gameSettings.getBoolean("bidirectional_selection", true));
        mIMSingleNumber.setHighlightSimilar(gameSettings.getBoolean("highlight_similar", true));
        mIMSingleNumber.setmOnSelectedNumberChangedListener(onSelectedNumberChangedListener);
        mIMNumpad.setHighlightCompletedValues(gameSettings.getBoolean("highlight_completed_values", true));
        mIMNumpad.setShowNumberTotals(gameSettings.getBoolean("show_number_totals", true));

        mIMControlPanel.activateFirstInputMethod(); // make sure that some input method is activated
        mIMControlPanelStatePersister.restoreState(mIMControlPanel);

        if (!mSudokuBoard.isReadOnly()) {
            mSudokuBoard.invokeOnCellSelected();
        }

        updateTime();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            // FIXME: When activity is resumed, title isn't sometimes hidden properly (there is black
            // empty space at the top of the screen). This is desperate workaround.
            if (mFullScreen) {
                mGuiHandler.postDelayed(() -> {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                    mRootLayout.requestLayout();
                }, 1000);
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // we will save game to the database as we might not be able to get back
        mDatabase.updateSudoku(mSudokuGame);

        mGameTimer.stop();
        mIMControlPanel.pause();
        mIMControlPanelStatePersister.saveState(mIMControlPanel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDatabase.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mGameTimer.stop();

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            mSudokuGame.pause();
        }

        mSudokuGame.saveState(outState);
        mGameTimer.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final boolean isLightTheme = ThemeUtils.isLightTheme(ThemeUtils.getCurrentThemeFromPreferences(getApplicationContext()));

        menu.add(0, MENU_ITEM_UNDO_ACTION, 0, R.string.undo)
                .setIcon(isLightTheme ? R.drawable.ic_undo_action_black : R.drawable.ic_undo_action_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, MENU_ITEM_UNDO, 0, R.string.undo)
                .setShortcut('1', 'u')
                .setIcon(R.drawable.ic_undo);

        if (mFillInNotesEnabled) {
            menu.add(0, MENU_ITEM_FILL_IN_NOTES, 1, R.string.fill_in_notes)
                    .setIcon(R.drawable.ic_edit_grey);
        }

        menu.add(0, MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES, 1, R.string.fill_all_notes)
                .setIcon(R.drawable.ic_edit_grey);

        menu.add(0, MENU_ITEM_CLEAR_ALL_NOTES, 2, R.string.clear_all_notes)
                .setShortcut('3', 'a')
                .setIcon(R.drawable.ic_delete);

        menu.add(0, MENU_ITEM_SET_CHECKPOINT, 3, R.string.set_checkpoint);
        menu.add(0, MENU_ITEM_UNDO_TO_CHECKPOINT, 4, R.string.undo_to_checkpoint);
        menu.add(0, MENU_ITEM_UNDO_TO_BEFORE_MISTAKE, 4, getString(R.string.undo_to_before_mistake));

        menu.add(0, MENU_ITEM_HINT, 5, R.string.solver_hint);
        menu.add(0, MENU_ITEM_SOLVE, 6, R.string.solve_puzzle);

        menu.add(0, MENU_ITEM_RESTART, 7, R.string.restart)
                .setShortcut('7', 'r')
                .setIcon(R.drawable.ic_restore);

        menu.add(0, MENU_ITEM_SETTINGS_ACTION, 8, R.string.settings)
                .setIcon(isLightTheme ? R.drawable.ic_settings_tip_black : R.drawable.ic_settings_tip)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, MENU_ITEM_SETTINGS, 8, R.string.settings)
                .setShortcut('9', 's')
                .setIcon(R.drawable.ic_settings);

        menu.add(0, MENU_ITEM_HELP, 9, R.string.help)
                .setShortcut('0', 'h')
                .setIcon(R.drawable.ic_help);


        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, SudokuPlayActivity.class), null, intent, 0, null);

        mOptionsMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mSudokuGame.getState() == SudokuGame.GAME_STATE_PLAYING) {
            menu.findItem(MENU_ITEM_CLEAR_ALL_NOTES).setEnabled(true);
            if (mFillInNotesEnabled) {
                menu.findItem(MENU_ITEM_FILL_IN_NOTES).setEnabled(true);
            }
            menu.findItem(MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES).setEnabled(true);
            menu.findItem(MENU_ITEM_UNDO).setEnabled(mSudokuGame.hasSomethingToUndo());
            menu.findItem(MENU_ITEM_UNDO_TO_CHECKPOINT).setEnabled(mSudokuGame.hasUndoCheckpoint());

        } else {
            menu.findItem(MENU_ITEM_CLEAR_ALL_NOTES).setEnabled(false);
            if (mFillInNotesEnabled) {
                menu.findItem(MENU_ITEM_FILL_IN_NOTES).setEnabled(false);
            }
            menu.findItem(MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO_TO_CHECKPOINT).setEnabled(false);
            menu.findItem(MENU_ITEM_UNDO_TO_BEFORE_MISTAKE).setEnabled(false);
            menu.findItem(MENU_ITEM_SOLVE).setEnabled(false);
            menu.findItem(MENU_ITEM_HINT).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_RESTART:
                showDialog(DIALOG_RESTART);
                return true;
            case MENU_ITEM_CLEAR_ALL_NOTES:
                showDialog(DIALOG_CLEAR_NOTES);
                return true;
            case MENU_ITEM_FILL_IN_NOTES:
                mSudokuGame.fillInNotes();
                return true;
            case MENU_ITEM_FILL_IN_NOTES_WITH_ALL_VALUES:
                mSudokuGame.fillInNotesWithAllValues();
                return true;
            case MENU_ITEM_UNDO_ACTION:
                if (mSudokuGame.hasSomethingToUndo()) {
                    mSudokuGame.undo();
                    selectLastChangedCell();
                }
                return true;
            case MENU_ITEM_UNDO:
                mSudokuGame.undo();
                selectLastChangedCell();
                return true;
            case MENU_ITEM_SETTINGS_ACTION:
                try {
                    ArrayList<Integer> rList = new ArrayList<>();
                    if (mSudokuGame.chekCustomValue(rList) == false) {
                        if (rList.size() > 0) {
                            mSudokuBoard.invalidate();
                            showDealErrorNum(rList);
                            return true;
                        }
                    }
                    Map<Cell, BitSet> pList = new HashMap();
                    if (mSudokuGame.checkCustomPotentials(pList) == false) {
                        if (pList.size() > 0) {
                            mSudokuBoard.setShowErrorPv(true);
                            mSudokuBoard.setRedPotentials(pList);
                            mSudokuBoard.invalidate();
                            showDealErrorPV(pList);

                            return true;
                        }
                    }

                    // bundle
                    Bundle bundle = new Bundle();
                    Grid c = new Grid();
                    if (mSudokuGame.getGrid().getAutoPotentialValues()) {
                        mSudokuGame.getGrid().copyTo(c);
                    } else {
                        mSudokuGame.getGrid().copyValueTo(c);
                    }
                    //这里只传数据过去，不要传hint，在solver中重新生成可以保证hint是没有错的。


                    bundle.putSerializable("GRID", c);
                    // intent
                    Intent intent = new Intent(this, SudokuSolverActivity.class);
                    intent.putExtras(bundle);
                    // navigate
                    startActivityForResult(intent, 10);

                } catch (Exception er) {
                    Toast toast = Toast.makeText(getApplicationContext(), er.toString(), Toast.LENGTH_SHORT);
                    //显示toast信息
                    toast.show();
                }
                break;
            case MENU_ITEM_SETTINGS:
                Intent i = new Intent();
                i.setClass(this, GameSettingsActivity.class);
                startActivityForResult(i, REQUEST_SETTINGS);

                return true;
            case MENU_ITEM_HELP:
                mHintsQueue.showHint(R.string.help, R.string.help_text);
                return true;
            case MENU_ITEM_SET_CHECKPOINT:
                showDialog(MENU_ITEM_SET_CHECKPOINT);
                //if( mSudokuGame.setUndoCheckpoint()==false)
                //{
                //    showNormalDialog("有错误数字，请纠正后再保存！");
                //}
                return true;
            case MENU_ITEM_UNDO_TO_CHECKPOINT:
                showDialog(DIALOG_UNDO_TO_CHECKPOINT);
                return true;
            case MENU_ITEM_UNDO_TO_BEFORE_MISTAKE:
                showDialog(DIALOG_UNDO_TO_BEFORE_MISTAKE);
                return true;
            case MENU_ITEM_SOLVE:
                showDialog(DIALOG_SOLVE_PUZZLE);
                return true;
            case MENU_ITEM_HINT:
                showDialog(DIALOG_HINT);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDealErrorPV(Map<Cell, BitSet> pList) {
        //创建dialog构造器
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        //设置title
        normalDialog.setTitle(R.string.app_name);
        //设置icon
        normalDialog.setIcon(R.mipmap.ic_launcher_round);
        //设置内容
        normalDialog.setMessage(R.string.check_pv_err);
        //设置按钮
        normalDialog.setPositiveButton(android.R.string.yes
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSudokuBoard.clearRedPotentials();
                        mSudokuBoard.setShowErrorPv(false);
                        dialog.dismiss();
                    }
                });
        normalDialog.setNegativeButton(R.string.check_pv_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        for(Integer i:rList)
                        {
                            mSudokuGame.getGrid().setCellValue(i,0);
                        }
                        mSudokuGame.getGrid().setOnchange();*
                         */
                        mSudokuGame.deleteErroPv(pList);
                        mSudokuBoard.clearRedPotentials();
                        mSudokuBoard.setShowErrorPv(false);
                        dialog.dismiss();
                    }
                });
        //创建并显示
        //normalDialog.create().show();
        Dialog dialog = normalDialog.create();
        dialog.show();
        //创建并显示
        //normalDialog.create().show();
        //设置弹窗在底部
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); //为获取屏幕宽、高
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); //获取对话框当前的参数值
        p.width = d.getWidth(); //宽度设置为屏幕
        dialog.getWindow().setAttributes(p); //设置生效
    }

    private void showDealErrorNum(ArrayList<Integer> rList) {

        //创建dialog构造器
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        //设置title
        normalDialog.setTitle(R.string.app_name);
        //设置icon
        normalDialog.setIcon(R.mipmap.ic_launcher_round);
        //设置内容
        normalDialog.setMessage(R.string.check_point_err);
        //设置按钮
        normalDialog.setPositiveButton(android.R.string.yes
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        normalDialog.setNegativeButton(R.string.check_point_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                        for(Integer i:rList)
                        {
                            mSudokuGame.getGrid().setCellValue(i,0);
                        }
                        mSudokuGame.getGrid().setOnchange();*
                         */
                        mSudokuGame.undo2NoErr(rList);
                        dialog.dismiss();
                    }
                });
        Dialog dialog = normalDialog.create();
        dialog.show();
        //创建并显示
        //normalDialog.create().show();
        //设置弹窗在底部
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); //为获取屏幕宽、高
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); //获取对话框当前的参数值
        p.width = d.getWidth(); //宽度设置为屏幕
        dialog.getWindow().setAttributes(p); //设置生效

    }

    private void showNormalDialog(String msg) {
        //创建dialog构造器
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(this);
        //设置title
        normalDialog.setTitle(R.string.app_name);
        //设置icon
        normalDialog.setIcon(R.mipmap.ic_launcher_round);
        //设置内容
        normalDialog.setMessage(msg);
        //设置按钮
        normalDialog.setPositiveButton(android.R.string.yes
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        //创建并显示
        //normalDialog.create().show();

        Dialog dialog = normalDialog.create();
        dialog.show();
        //创建并显示
        //normalDialog.create().show();
        //设置弹窗在底部
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); //为获取屏幕宽、高
        WindowManager.LayoutParams p = dialog.getWindow().getAttributes(); //获取对话框当前的参数值
        p.width = d.getWidth(); //宽度设置为屏幕
        dialog.getWindow().setAttributes(p); //设置生效
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS) {
            restartActivity();
        } else if (data != null && requestCode == 10 && resultCode == SOLVER_STEP) {
            Bundle bundle = data.getExtras(); // 从返回的
            Grid g = (Grid) bundle.getSerializable("GRID");
            // g.copyToOnleValue(mSudokuGame.getGrid());
            mSudokuGame.applySolver(g);

            // 从包裹中取出名叫response_time的字符串
            // ArrayList<Hint> lastHint = (ArrayList<Hint>) bundle.getSerializable("HINTS");
        }
    }

    /**
     * Restarts whole activity.
     */
    private void restartActivity() {
        startActivity(getIntent());
        finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_WELL_DONE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_info)
                        .setTitle(R.string.well_done)
                        .setMessage(getString(R.string.congrats, mGameTimeFormatter.format(mSudokuGame.getTime())))
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_RESTART:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_restore)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.restart_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            // Restart game
                            mSudokuGame.reset();
                            mSudokuGame.start();
                            mSudokuBoard.setReadOnly(false);
                            if (mShowTime) {
                                mGameTimer.start();
                            }
                            removeDialog(DIALOG_WELL_DONE);
                            MenuItem menuItemSolve = mOptionsMenu.findItem(MENU_ITEM_SOLVE);
                            menuItemSolve.setEnabled(true);
                            MenuItem menuItemHint = mOptionsMenu.findItem(MENU_ITEM_HINT);
                            menuItemHint.setEnabled(true);
                            MenuItem menuItemUndoAction = mOptionsMenu.findItem(MENU_ITEM_UNDO_ACTION);
                            menuItemUndoAction.setEnabled(true);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_CLEAR_NOTES:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_delete)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.clear_all_notes_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> mSudokuGame.clearAllNotes())
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case MENU_ITEM_SET_CHECKPOINT:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_undo)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.set_checkpoint_confim)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            ArrayList<Integer> rList = new ArrayList<>();
                            if (mSudokuGame.setUndoCheckpoint(rList) == false) {
                                showDealErrorNum(rList);
                            }
                            selectLastChangedCell();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_UNDO_TO_CHECKPOINT:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_undo)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.undo_to_checkpoint_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            mSudokuGame.undoToCheckpoint();
                            selectLastChangedCell();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_UNDO_TO_BEFORE_MISTAKE:
                return new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_undo)
                        .setTitle(R.string.app_name)
                        .setMessage(getString(R.string.undo_to_before_mistake_confirm))
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            mSudokuGame.undoToBeforeMistake();
                            selectLastChangedCell();
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_SOLVE_PUZZLE:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.solve_puzzle_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            ArrayList<Integer> rList = new ArrayList<>();
                            if (mSudokuGame.chekCustomValue(rList) == false && rList.size() > 0) {
                                showDealErrorNum(rList);
                            } else {
                                if (mSudokuGame.isSolvable()) {
                                    mSudokuGame.solve();
                                } else {
                                    showDialog(DIALOG_PUZZLE_NOT_SOLVED);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_USED_SOLVER:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.used_solver)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_PUZZLE_NOT_SOLVED:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.puzzle_not_solved)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
            case DIALOG_HINT:

                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.hint_confirm)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {

                            Cell cell = mSudokuBoard.getSelectedCell();
                            if (cell != null && mSudokuGame.getGrid().isEditable(cell)) {
                                if (mSudokuGame.isSolvable()) {
                                    mSudokuGame.solveCell(cell);
                                } else {
                                    showDialog(DIALOG_PUZZLE_NOT_SOLVED);
                                }
                            } else {
                                showDialog(DIALOG_CANNOT_GIVE_HINT);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .create();
            case DIALOG_CANNOT_GIVE_HINT:
                return new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.cannot_give_hint)
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
        }
        return null;
    }

    private void selectLastChangedCell() {
        Cell cell = mSudokuGame.getLastChangedCell();
        if (cell != null)
            mSudokuBoard.moveCellSelectionTo(cell.getY(), cell.getX());
    }

    /**
     * Update the time of game-play.
     */
    void updateTime() {
        if (mShowTime) {
            setTitle(mGameTimeFormatter.format(mSudokuGame.getTime()));
            mTimeLabel.setText(mGameTimeFormatter.format(mSudokuGame.getTime()));
        } else {
            setTitle(R.string.app_name);
        }

    }

    public interface OnSelectedNumberChangedListener {
        void onSelectedNumberChanged(int number);
    }

    // This class implements the game clock.  All it does is update the
    // status each tick.
    private final class GameTimer extends Timer {

        GameTimer() {
            super(1000);
        }

        @Override
        protected boolean step(int count, long time) {
            updateTime();

            // Run until explicitly stopped.
            return false;
        }
    }
}
