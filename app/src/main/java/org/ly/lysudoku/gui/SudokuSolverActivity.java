package org.ly.lysudoku.gui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;

import org.json.JSONException;
import org.ly.lysudoku.Cell;
import org.ly.lysudoku.Grid;
import org.ly.lysudoku.R;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.solver.DirectHint;
import org.ly.lysudoku.solver.Hint;
import org.ly.lysudoku.solver.HintsAccumulator;
import org.ly.lysudoku.solver.IndirectHint;
import org.ly.lysudoku.solver.Solver;
import org.ly.lysudoku.solver.WarningHint;
import org.ly.lysudoku.tools.Asker;
import org.ly.lysudoku.tools.SingletonBitSet;
import org.ly.lysudoku.tools.StrongReference;
import org.ly.lysudoku.trans.BaiduTrans;
import org.ly.lysudoku.trans.ResponseCallBack;
import org.ly.lysudoku.trans.TransResult;
import org.ly.lysudoku.utils.LogUtil;
import org.ly.lysudoku.utils.ThemeUtils;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SudokuSolverActivity extends ThemedActivity implements Asker {
    private Menu mOptionsMenu;

    private SudokuGame mGame;
    private Grid mGrid;
    private Solver mSolver;
    private SudokuBoardView mSudokuBoard;
    private static final int SOLVER_STEP = 10;
    public ArrayList<Hint> lastHint = new ArrayList<>();
    private List<Hint> unfilteredHints = null; // All hints (unfiltered)
    private List<Hint> filteredHints = null; // All hints (filtered)
    private boolean isFiltered = true;
    private List<Hint> selectedHints = new ArrayList<Hint>(); // Currently selected hint
    // Cache for filter
    Set<Cell> givenCells = new HashSet<Cell>(); // Cell values already encountered
    Map<Cell, BitSet> removedPotentials = new HashMap<Cell, BitSet>(); // Removable potentials already encountered
    HtmlTextView htmlTextView;
    WebView webView;
    private Hint currentHint = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activiti_solver);

        mSudokuBoard = findViewById(R.id.sudoku_board);
        htmlTextView = (HtmlTextView) findViewById(R.id.html_text);
        webView = (WebView) findViewById(R.id.webview);
        webView.setVisibility(View.GONE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        /*
        *https://github.com/SufficientlySecure/html-textview
        // loads html from string and displays cat_pic.png from the app's drawable folder
        htmlTextView.setHtml("<h2>Hello wold</h2><ul><li>cats</li><li>dogs</li></ul><img src=\"cat_pic\"/>",
                new HtmlResImageGetter(htmlTextView));
        HtmlTextView htmlTextView = (HtmlTextView) view.findViewById(R.id.html_text);

        // loads html from string and displays cat_pic.png from the app's assets folder
        htmlTextView.setHtml("<h2>Hello wold</h2><ul><li>cats</li><li>dogs</li></ul><img src=\"cat_pic\"/>",
                new HtmlAssetsImageGetter(htmlTextView));*/
        htmlTextView.setHtml("Solver step by step",
                new HtmlResImageGetter(htmlTextView.getContext()));


        mSudokuBoard.setShowLabel(true);
        mSudokuBoard.setShowHint(true);
        mSudokuBoard.setReadOnly(true);
        Bundle bundle = this.getIntent().getExtras();
        mGrid = (Grid) bundle.getSerializable("GRID");

        if (mGrid != null) {
            mGame = new SudokuGame();
            //这个copy来的是没有这个属性的
            mGrid.reSetEditable();

            mGame.setGrid(mGrid);
            mSolver = new Solver(mGrid);
            mSudokuBoard.setGame(mGame);
            mGame.setAsker(this);
            if (mGrid.getAutoPotentialValues() == false) {
                rebuildPotentialValues();
            }
            //Hint hint =solverstep();
            Hint hint = getNextHint();
            showHint(hint);
        }


    }

    String oldMsg = null;
    String oldText = null;

    private void showHint(Hint hint) {
        webView.setVisibility(View.GONE);
        htmlTextView.setVisibility(View.VISIBLE);
        if (null != hint) {
            String msg = hint.toHtml(mGrid);
            oldMsg = msg;
            //String msg=  hint.toString();
            htmlTextView.setHtml(msg);
            htmlTextView.scrollTo(0, 0);
            oldText = htmlTextView.getText().toString();
            isEngligh = true;

        }
    }

    private static final String ZHIHU = "https://zhihu.com/search?type=content&q=";
    private static final String BINGSEARCH = "https://www.bing.com/search?q=";
    private static final String BAIDUSEARCH = "https://www.baidu.com/s?wd=";//URL是根据使用百度搜索某个关键字得到的url截取得到的;

    private void search() {
        try {
            webView.setVisibility(View.VISIBLE);
            htmlTextView.setVisibility(View.GONE);
            if (TextUtils.isEmpty(oldText) == false) {
                String[] ss = oldText.split("\n");
                if (ss.length > 0) {
                    String s1 = BAIDUSEARCH + "数独 " + ss[0];
                    webView.loadUrl(s1);
                }
            }
        } catch (Exception er) {
            LogUtil.e(er.getMessage());
        }
    }

    private void translate() {
        try {
            if (TextUtils.isEmpty(oldText) == false) {
                webView.setVisibility(View.GONE);
                htmlTextView.setVisibility(View.VISIBLE);
                BaiduTrans.Trans(oldText, "zh", new ResponseCallBack() {
                    @Override
                    public void success(String json) {
                        try {
                            // LogUtil.i("返回" + json);
                            TransResult obj = JSON.parseObject(json, TransResult.class);
                            if (obj.getTrans_result() != null) {
                                StringBuffer sb = new StringBuffer("<html><body>");
                                for (int i = 0; i < obj.getTrans_result().length; i++) {
                                    String dst = obj.getTrans_result()[i].getDst();
                                    if (TextUtils.isEmpty(dst) == false) {
                                        if (i == 0)
                                            sb.append("<h2>" + dst + "</h2>");
                                        else {
                                            sb.append("<p>" + dst + "</p>");
                                        }
                                    }
                                }
                                sb.append("</body></html>");
                                LogUtil.i(sb.toString());

                                htmlTextView.setHtml(sb.toString());
                                htmlTextView.scrollTo(0, 0);
                                // LogUtil.i("显示完成!");
                            } else {
                                LogUtil.i("返回成功，解json失败！" + obj.getTrans_result());
                            }
                        } catch (Exception er) {
                            LogUtil.e(er.getMessage());
                        }
                    }

                    @Override
                    public void error(String json) {
                        LogUtil.e(json);
                    }
                });
            }
        } catch (UnsupportedEncodingException er) {
            LogUtil.e(er.getMessage());
        }
    }

    public boolean ask(String message) {
        //return JOptionPane.showConfirmDialog(this, message, getTitle(),
        //         JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        final boolean isLightTheme = ThemeUtils.isLightTheme(ThemeUtils.getCurrentThemeFromPreferences(getApplicationContext()));

        menu.add(0, 0, 0, R.string.settings)
                .setIcon(isLightTheme ? R.drawable.ic_searh_black : R.drawable.ic_searh_wite)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, 1, 0, R.string.settings)
                .setIcon(isLightTheme ? R.drawable.ic_trans_black : R.drawable.ic_trans_wite)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, 2, 0, R.string.settings)
                .setIcon(isLightTheme ? R.drawable.ic_undo_action_black : R.drawable.ic_undo_action_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, 3, 0, R.string.applyhint)
                .setIcon(isLightTheme ? R.drawable.ic_apply_black : R.drawable.ic_apply_wite)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        menu.add(0, 4, 0, R.string.nethint)
                .setIcon(isLightTheme ? R.drawable.ic_solvernext_black : R.drawable.ic_solvernext_wite)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mOptionsMenu = menu;

        return true;
    }

    boolean isEngligh = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                search();
                return true;
            case 1:
                if (isEngligh) {
                    isEngligh = false;
                    translate();
                } else {
                    isEngligh = true;
                    htmlTextView.setHtml(oldMsg);
                    htmlTextView.scrollTo(0, 0);
                }
                return true;
            case 2:
                //直接返回，
                finish();
                return true;
            case 3:
                applySelectedHints();
                //返回数据到上层
                Bundle bundle = new Bundle();
                //bundle.putSerializable("HINTS",lastHint);
                Grid g = new Grid();
                mGrid.copyTo(g);
                bundle.putSerializable("GRID", g);
                //这个只是单纯用来存储数据而新建的
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(SOLVER_STEP, intent);
                finish();
                return true;
            case 4:
                Hint hint = solverstep();

                mGame.validate();
                if (mGame.isCompleted()) {
                    htmlTextView.setHtml("The game is solvered!");
                    htmlTextView.scrollTo(0, 0);
                }
                showHint(hint);


                return true;
        }
        return true;
    }

    public void rebuildPotentialValues() {
        mSolver.rebuildPotentialValues();
    }

    public Hint getNextHint() {
        try {
            Hint hint = getNextHintImpl();
            if (hint != null) {
                addFilteredHintAndUpdateFilter(hint);
                selectedHints.add(hint);
            }
            lastHint.add(hint);
            // repaintAll();
            repaintHints();
            return hint;
        } catch (Throwable ex) {
            //displayError(ex);
            ex.printStackTrace();
        }
        return null;
    }

    private void repaintHints() {
        if (selectedHints.size() == 1) {
            //frame.setCurrentHint(grid, selectedHints.get(0), true);
            repaintHint(selectedHints.get(0));
        } else {
            //frame.setCurrentHint(grid, null, !selectedHints.isEmpty());
            if (selectedHints.size() > 1)
                paintMultipleHints(selectedHints);
        }
    }

    private void paintMultipleHints(List<Hint> hints) {
        Map<Cell, BitSet> redPotentials = new HashMap<Cell, BitSet>();
        Map<Cell, BitSet> greenPotentials = new HashMap<Cell, BitSet>();
        for (Hint hint : hints) {
            Cell cell = hint.getCell();
            if (cell != null)
                greenPotentials.put(cell, SingletonBitSet.create(hint.getValue()));
            if (hint instanceof IndirectHint) {
                IndirectHint ihint = (IndirectHint) hint;
                Map<Cell, BitSet> removable = ihint.getRemovablePotentials();
                //for (Cell rCell : removable.keySet()) {
                //BitSet values = removable.get(rCell);
                for (Map.Entry<Cell, BitSet> entry : removable.entrySet()) {
                    Cell rCell = entry.getKey();
                    BitSet values = entry.getValue();
                    if (redPotentials.containsKey(rCell))
                        redPotentials.get(rCell).or(values);
                    else
                        redPotentials.put(rCell, (BitSet) values.clone());
                }
            }
        }

        mSudokuBoard.setRedPotentials(redPotentials);
        mSudokuBoard.setGreenPotentials(greenPotentials);
        mSudokuBoard.setBluePotentials(null);
        mSudokuBoard.setGreenCells(greenPotentials.keySet());
        mSudokuBoard.repaintHint();
        //mSudokuBoard.setExplanations(HtmlLoader.loadHtml(this, "Multiple.html"));
    }

    int viewNum = 1;

    private void repaintHint(Hint hint) {
        currentHint = hint;
        Set<Cell> noCells = Collections.emptySet();
        Map<Cell, BitSet> noMap = Collections.emptyMap();
        mSudokuBoard.setRedCells(noCells);
        mSudokuBoard.setGreenCells(noCells);
        mSudokuBoard.setRedPotentials(noMap);
        mSudokuBoard.setGreenPotentials(noMap);
        // Highlight as necessary
        if (currentHint != null) {
            // mSudokuBoard.clearSelection();
            if (currentHint instanceof DirectHint) {
                DirectHint dHint = (DirectHint) currentHint;
                //SudokuMonster: Some changes in gui to limit colour salad in DG
                //if (!Settings.getInstance().isDG())
                //   mSudokuBoard.setGreenCells(Collections.singleton(dHint.getCell()));
                // else
                mSudokuBoard.setHighlightedCells(new Cell[]{dHint.getCell()});
                BitSet values = new BitSet(10);
                values.set(dHint.getValue());
                mSudokuBoard.setGreenPotentials(Collections.singletonMap(
                        dHint.getCell(), values));
                mSudokuBoard.setLinks(null);
            } else if (currentHint instanceof IndirectHint) {
                IndirectHint iHint = (IndirectHint) currentHint;
                mSudokuBoard.setGreenPotentials(iHint.getGreenPotentials(mGrid, viewNum));
                mSudokuBoard.setRedPotentials(iHint.getRedPotentials(mGrid, viewNum));
                mSudokuBoard.setBluePotentials(iHint.getBluePotentials(mGrid, viewNum));
                if (iHint.getSelectedCells() != null)
                    // if (!Settings.getInstance().isDG())
                    //     mSudokuBoard.setGreenCells(Arrays.asList(iHint.getSelectedCells()));
                    // else
                    mSudokuBoard.setHighlightedCells(iHint.getSelectedCells());
                if (iHint instanceof WarningHint)
                    mSudokuBoard.setRedCells(((WarningHint) iHint).getRedCells());
                // Set links (rendered as arrows)
                mSudokuBoard.setLinks(iHint.getLinks(mGrid, viewNum));
            }
            mSudokuBoard.setBlueRegions(currentHint.getRegions());
        }
        mSudokuBoard.repaintHint();
    }

    public Hint solverstep() {
        Hint last = null;
        if (!mGrid.isSolved()) {
            applySelectedHints();
            last = getNextHint();
        }
        return last;
    }

    public Hint applySelectedHints() {
        Hint last = null;
        if (!mGrid.isSolved()) {
            if (selectedHints.size() >= 1) {
                //pushGrid();
                for (Hint hint : selectedHints) {
                    //hint.apply();
                    last = hint;
                    hint.apply(mGrid);
                }
                clearHints();
                //repaintAll();
                repaintHints();
            }
        }
        return last;
    }

    public void clearHints() {
        unfilteredHints = null;
        resetFilterCache();
        filterHints();
        selectedHints.clear();
        // panel.clearSelection();
        //repaintAll();
    }

    /**
     * Hint filter
     */
    private boolean isWorth(Hint hint) {
        if (!isFiltered)
            return true;
        boolean isWorth; // Check if the hint yields to new outcomes
        if (hint instanceof DirectHint)
            isWorth = isWorth(givenCells, (DirectHint) hint);
        else
            isWorth = isWorth(removedPotentials, givenCells, (IndirectHint) hint);
        return isWorth;
    }

    /**
     * Test if a {@link DirectHint} allows the placement of a new cell
     * value. Returns <tt>false</tt> if the cell value given by this
     * hint has already been given by previous hints.
     * <p>
     * used for the hints tree filter
     *
     * @param givenCells The set of cells whose value have already been given
     * @param hint       the hint to test
     * @return whether the hint allows a new cell value placement
     */
    private boolean isWorth(Set<Cell> givenCells, DirectHint hint) {
        return (!givenCells.contains(hint.getCell()));
    }

    /**
     * Test if a {@link IndirectHint} allows the removal of new potentials.
     * Returns <tt>false</tt> if all the potentials remobavle with this hint
     * have already been removed by previous hints.
     * <p>
     * Used for the hints tree filter
     *
     * @param removedPotentials the previously removed potentials
     * @param hint              the hint to test
     * @return whether the hint allows the removal of new potentials
     */
    private boolean isWorth(Map<Cell, BitSet> removedPotentials, Set<Cell> givenCells,
                            IndirectHint hint) {
        if (hint instanceof WarningHint)
            return true;
        Map<Cell, BitSet> removablePotentials = hint.getRemovablePotentials();
        //for (Cell cell : removablePotentials.keySet()) {
        for (Map.Entry<Cell, BitSet> entry : removablePotentials.entrySet()) {
            Cell cell = entry.getKey();
            //BitSet removable = removablePotentials.get(cell);
            BitSet removable = entry.getValue();
            BitSet previous = removedPotentials.get(cell);
            if (previous == null)
                return true;
            BitSet newRemove = (BitSet) removable.clone();
            newRemove.andNot(previous);
            if (!newRemove.isEmpty())
                return true;
        }
        Cell cell = hint.getCell();
        if (cell != null && !givenCells.contains(cell))
            return true;
        return false;
    }

    /**
     * Copy all the hints from {@link #unfilteredHints} to
     * {@link #filteredHints}, applying the filter if active.
     */
    private void filterHints() {
        filteredHints = null;
        if (unfilteredHints != null) {
            filteredHints = new ArrayList<Hint>();
            if (isFiltered) {
                // Filter hints with similar outcome
                for (Hint hint : unfilteredHints) {
                    if (isWorth(hint))
                        addFilteredHintAndUpdateFilter(hint);
                }
            } else {
                // Copy "as is"
                for (Hint hint : unfilteredHints)
                    filteredHints.add(hint);
            }
        }
    }

    private void addFilteredHintAndUpdateFilter(Hint hint) {
        filteredHints.add(hint);
        if (hint instanceof DirectHint) {
            // Update given cells
            DirectHint dHint = (DirectHint) hint;
            givenCells.add(dHint.getCell());
        } else {
            // Update removable potentials (candidates)
            IndirectHint iHint = (IndirectHint) hint;
            Map<Cell, BitSet> removablePotentials = iHint.getRemovablePotentials();
            //for (Cell cell : removablePotentials.keySet()) {
            //BitSet removable = removablePotentials.get(cell);
            for (Map.Entry<Cell, BitSet> entry : removablePotentials.entrySet()) {
                Cell cell = entry.getKey();
                BitSet removable = entry.getValue();
                BitSet current = removedPotentials.get(cell);
                if (current == null) {
                    current = new BitSet(10);
                    removedPotentials.put(cell, current);
                }
                current.or(removable);
            }
            // Update given cells if any
            Cell cell = iHint.getCell();
            if (cell != null)
                givenCells.add(cell);
        }
    }

    private Hint getNextHintImpl() {
        if (unfilteredHints == null) {
            unfilteredHints = new ArrayList<Hint>();
            filterHints();
        }
        // Create temporary buffers for gathering all the hints again
        final List<Hint> buffer = new ArrayList<Hint>();
        final StrongReference<Hint> newHint = new StrongReference<Hint>();
        mSolver.gatherHints(unfilteredHints, buffer, new HintsAccumulator() {
            /*
             * Trick: gatherHints will get all the hints it can find, one after
             * the other, sorted by difficulty. It will call add() for every hint.
             * To get only the first hint, we throw an InterruptedException after the
             * first produced hint that was not filtered.
             */
            public void add(Hint hint) throws InterruptedException {
                if (!buffer.contains(hint)) {
                    buffer.add(hint);
                    boolean isNew = (buffer.size() > unfilteredHints.size());
                    if (isNew) {
                        unfilteredHints.add(hint); // This hint is new for the unfiltered list
                        if (isWorth(hint)) {
                            newHint.setValue(hint);
                            throw new InterruptedException();
                        }
                    }
                }
            }
        }, this);
        selectedHints.clear();
        Hint hint = null;
        if (newHint.isValueSet())
            hint = newHint.getValue();
        return hint;
    }

    private void resetFilterCache() {
        givenCells = new HashSet<Cell>(); // Cell values already encountered
        removedPotentials = new HashMap<Cell, BitSet>(); // Removable potentials already encountered
    }
}
