package org.ly.lysudoku.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import org.ly.lysudoku.Grid;
import org.ly.lysudoku.R;
import org.ly.lysudoku.Settings;
import org.ly.lysudoku.db.SudokuDatabase;
import org.ly.lysudoku.game.SudokuGame;
import org.ly.lysudoku.generator.Generator;
import org.ly.lysudoku.generator.Symmetry;
import org.ly.lysudoku.solver.Hint;
import org.ly.lysudoku.solver.Solver;
import org.ly.lysudoku.tools.HtmlLoader;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SudokuNewActivity extends ThemedActivity {
    private enum Difficulty {
        Easy {
            @Override
            public double getMinDifficulty() {
                return 1.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 1.2;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Medium {
            @Override
            public double getMinDifficulty() {
                return 1.3;
            }

            @Override
            public double getMaxDifficulty() {
                return 1.6;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Hard {
            @Override
            public double getMinDifficulty() {
                return 1.7;
            }

            @Override
            public double getMaxDifficulty() {
                return 2.5;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Superior {
            @Override
            public double getMinDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 3.1;
                return 3.2;
            }

            @Override
            public double getMaxDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 3.8;
                return 4.0;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 0.0;
                return 3.8;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 0.0;
                return 3.4;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "Skyscraper";
            }

            @Override
            public String getexcludeTechnique2() {
                return "2-String Kite";
            }

            @Override
            public String getexcludeTechnique3() {
                return "Turbot Fish";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "Triple";
            }

            @Override
            public String getOneOfThree_2() {
                return "X-Wing";
            }

            @Override
            public String getOneOfThree_3() {
                return "X-Wing";
            }
        },
        Fiendish {
            @Override
            public double getMinDifficulty() {
                return 2.6;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.0;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Fiendish2 {
            @Override
            public double getMinDifficulty() {
                return 3.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 7.0;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "Forcing";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        SuperiorPlus {
            @Override
            public double getMinDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 4.0;
                return 3.8;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.1;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 0.0;
                return 4.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "XY";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        XYorXYZ {
            @Override
            public double getMinDifficulty() {
                return 4.2;
            }

            @Override
            public double getMaxDifficulty() {
                return 4.4;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "XY";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },

        TwoStringKite {
            @Override
            public double getMinDifficulty() {
                return 4.1;
            }

            @Override
            public double getMaxDifficulty() {
                return 4.1;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "Skyscraper";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        EmptyRectangle {
            @Override
            public double getMinDifficulty() {
                return 4.3;
            }

            @Override
            public double getMaxDifficulty() {
                return 4.3;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "Skyscraper";
            }

            @Override
            public String getexcludeTechnique2() {
                return "Kite";
            }

            @Override
            public String getexcludeTechnique3() {
                return "XY";
            }

            public String getincludeTechnique1() {
                return "Empty Rectangle";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Grouped2StrongLinks {
            @Override
            public double getMinDifficulty() {
                return 4.3;
            }

            @Override
            public double getMaxDifficulty() {
                return 4.3;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "XY";
            }

            public String getincludeTechnique1() {
                return "11";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        XLoop {
            @Override
            public double getMinDifficulty() {
                return 4.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.6;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "X-Loop";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        gXLoop {
            @Override
            public double getMinDifficulty() {
                return 4.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.6;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "(2 Strong Links) Grouped X-Loop 2";
            }

            @Override
            public String getOneOfThree_2() {
                return "(3 Strong Links) Grouped X-Loop 3";
            }

            @Override
            public String getOneOfThree_3() {
                return "(4 Strong Links) Grouped X-Loop 4";
            }
        },
        ThreeStrongLinks {
            @Override
            public double getMinDifficulty() {
                return 5.4;
            }

            @Override
            public double getMaxDifficulty() {
                return 5.7;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return " 10";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        ThreeLinkER {
            @Override
            public double getMinDifficulty() {
                return 5.7;
            }

            @Override
            public double getMaxDifficulty() {
                return 5.7;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "2-";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "Wing 2";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "type 2";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return " 30";
            }

            @Override
            public String getOneOfThree_2() {
                return " 31";
            }

            @Override
            public String getOneOfThree_3() {
                return " 2";
            }
        },



        /*ThreeLinkEmL {

            @Override
            public double getMinDifficulty() {
                return 5.7;
            }

            @Override
            public double getMaxDifficulty() {
                return 5.7;
            }
            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }
            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }
            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }
            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }
            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }
            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }
            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }
            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }
            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }
            public String getexcludeTechnique1() {
                return "";
            }
            @Override
            public String getexcludeTechnique2() {
                return "";
            }
            @Override
            public String getexcludeTechnique3() {
                return "";
            }
            public String getincludeTechnique1() {
                return "";
            }
            @Override
            public String getincludeTechnique2() {
                return "EmL";
            }
            @Override
            public String getincludeTechnique3() {
                return "";
            }
            public String getnotMaxTechnique1() {
                return "";
            }
            @Override
            public String getnotMaxTechnique2() {
                return "";
            }
            @Override
            public String getnotMaxTechnique3() {
                return "";
            }
            @Override
            public String getOneOfThree_1() {
                return "";
            }
            @Override
            public String getOneOfThree_2() {
                return "";
            }
            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        */

        FourLinks {
            @Override
            public double getMinDifficulty() {
                return 5.8;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.1;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "4-";
            }

            @Override
            public String getOneOfThree_2() {
                return "4 S";
            }

            @Override
            public String getOneOfThree_3() {
                return "4 S";
            }
        },
        WXYZ {
            @Override
            public double getMinDifficulty() {
                return 5.5;
            }

            @Override
            public double getMaxDifficulty() {
                return 5.6;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "Wing 2";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Uniqueness {
            @Override
            public double getMinDifficulty() {
                return 4.5;
            }

            @Override
            public double getMaxDifficulty() {
                return 5.3;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "Unique";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        UL10 {
            @Override
            public double getMinDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 4.8;
                return 5.0;
            }

            @Override
            public double getMaxDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 5.1;
                return 5.3;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "Unique Loop 10";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                if (Settings.getInstance().revisedRating() == 1)
                    return "Naked";
                return "Jellyfish";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        UniquenessType3 {
            @Override
            public double getMinDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 4.6;
                return 4.5;
            }

            @Override
            public double getMaxDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 5.3;
                return 5.3;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "Unique";
            }

            @Override
            public String getincludeTechnique2() {
                return "type 3";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                if (Settings.getInstance().revisedRating() == 1)
                    return "Naked";
                return "Jellyfish";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        UL12Type3 {
            @Override
            public double getMinDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 4.9;
                return 5.0;
            }

            @Override
            public double getMaxDifficulty() {
                if (Settings.getInstance().revisedRating() == 1)
                    return 5.2;
                return 5.3;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            @Override
            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "Unique Loop 12 type 3";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                if (Settings.getInstance().revisedRating() == 1)
                    return "Naked";
                return "Jellyfish";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Quintuplet {
            @Override
            public double getMinDifficulty() {
                return 5.6;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.8;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "Quintuplet";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        APE {
            @Override
            public double getMinDifficulty() {
                return 6.2;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.2;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "ligned";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        BUGs {
            @Override
            public double getMinDifficulty() {
                return 5.6;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.1;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "BUG";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        VWXYZ {
            @Override
            public double getMinDifficulty() {
                return 6.2;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.4;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "VWXYZ-Wing 2";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        UVWXYZ {
            @Override
            public double getMinDifficulty() {
                return 6.6;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.6;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "UVWXYZ-Wing 2";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        TUVWXYZ {
            @Override
            public double getMinDifficulty() {
                return 7.5;
            }

            @Override
            public double getMaxDifficulty() {
                return 7.5;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "Chain";
            }

            @Override
            public String getexcludeTechnique2() {
                return "Aligned";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "TUVWXYZ-Wing 2";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        FiveLinks {
            @Override
            public double getMinDifficulty() {
                return 6.2;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.5;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "5-";
            }

            @Override
            public String getOneOfThree_2() {
                return "5 S";
            }

            @Override
            public String getOneOfThree_3() {
                return "5 S";
            }
        },
        SixLinks {
            @Override
            public double getMinDifficulty() {
                return 6.6;
            }

            @Override
            public double getMaxDifficulty() {
                return 6.9;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "6-";
            }

            @Override
            public String getOneOfThree_2() {
                return "6 S";
            }

            @Override
            public String getOneOfThree_3() {
                return "6 S";
            }
        },
        AdvancedPlayer {
            @Override
            public double getMinDifficulty() {
                return 7.0;
            }

            @Override
            public double getMaxDifficulty() {
                return 8.0;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        DailySudoku {
            @Override
            public double getMinDifficulty() {
                return 7.1;
            }

            @Override
            public double getMaxDifficulty() {
                return 7.2;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "Kite";
            }

            @Override
            public String getexcludeTechnique2() {
                return "XY";
            }

            @Override
            public String getexcludeTechnique3() {
                return "Strong";
            }

            public String getincludeTechnique1() {
                return "Forcing Chain";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        },
        Diabolical {
            @Override
            public double getMinDifficulty() {
                return 6.1;
            }

            @Override
            public double getMaxDifficulty() {
                return 11.0;
            }

            @Override
            public double getincludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getincludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty1() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty2() {
                return 0.0;
            }

            @Override
            public double getexcludeDifficulty3() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty1() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty2() {
                return 0.0;
            }

            @Override
            public double getnotMaxDifficulty3() {
                return 0.0;
            }

            public String getexcludeTechnique1() {
                return "";
            }

            @Override
            public String getexcludeTechnique2() {
                return "";
            }

            @Override
            public String getexcludeTechnique3() {
                return "";
            }

            public String getincludeTechnique1() {
                return "";
            }

            @Override
            public String getincludeTechnique2() {
                return "";
            }

            @Override
            public String getincludeTechnique3() {
                return "";
            }

            public String getnotMaxTechnique1() {
                return "";
            }

            @Override
            public String getnotMaxTechnique2() {
                return "";
            }

            @Override
            public String getnotMaxTechnique3() {
                return "";
            }

            @Override
            public String getOneOfThree_1() {
                return "";
            }

            @Override
            public String getOneOfThree_2() {
                return "";
            }

            @Override
            public String getOneOfThree_3() {
                return "";
            }
        };

        public abstract double getMinDifficulty();

        public abstract double getMaxDifficulty();

        public abstract double getincludeDifficulty1();

        public abstract double getincludeDifficulty2();

        public abstract double getincludeDifficulty3();

        public abstract double getexcludeDifficulty1();

        public abstract double getexcludeDifficulty2();

        public abstract double getexcludeDifficulty3();

        public abstract double getnotMaxDifficulty1();

        public abstract double getnotMaxDifficulty2();

        public abstract double getnotMaxDifficulty3();

        public abstract String getexcludeTechnique1();

        public abstract String getexcludeTechnique2();

        public abstract String getexcludeTechnique3();

        public abstract String getincludeTechnique1();

        public abstract String getincludeTechnique2();

        public abstract String getincludeTechnique3();

        public abstract String getnotMaxTechnique1();

        public abstract String getnotMaxTechnique2();

        public abstract String getnotMaxTechnique3();

        public abstract String getOneOfThree_1();

        public abstract String getOneOfThree_2();

        public abstract String getOneOfThree_3();

        public String getHtmlDescription() {
            return HtmlLoader.loadHtml(this, this.name() + ".html");
        }

    }

    /**
     * Thread that generates a mew grid.
     */
    private class GeneratorThread extends Thread {

        private final List<Symmetry> symmetries;
        private final double minDifficulty;
        private final double maxDifficulty;
        private final double includeDifficulty1;
        private final double includeDifficulty2;
        private final double includeDifficulty3;
        private final double excludeDifficulty1;
        private final double excludeDifficulty2;
        private final double excludeDifficulty3;
        private final double notMaxDifficulty1;
        private final double notMaxDifficulty2;
        private final double notMaxDifficulty3;
        private final String excludeTechnique1;
        private final String excludeTechnique2;
        private final String excludeTechnique3;
        private final String includeTechnique1;
        private final String includeTechnique2;
        private final String includeTechnique3;
        private final String notMaxTechnique1;
        private final String notMaxTechnique2;
        private final String notMaxTechnique3;
        private final String getOneOfThree_1;
        private final String getOneOfThree_2;
        private final String getOneOfThree_3;
        private Generator generator;


        public GeneratorThread(List<Symmetry> symmetries, double minDifficulty, double maxDifficulty, double includeDifficulty1, double includeDifficulty2, double includeDifficulty3, double excludeDifficulty1, double excludeDifficulty2, double excludeDifficulty3, double notMaxDifficulty1, double notMaxDifficulty2, double notMaxDifficulty3, String excludeTechnique1, String excludeTechnique2, String excludeTechnique3, String includeTechnique1, String includeTechnique2, String includeTechnique3, String notMaxTechnique1, String notMaxTechnique2, String notMaxTechnique3, String getOneOfThree_1, String getOneOfThree_2, String getOneOfThree_3) {
            this.symmetries = symmetries;
            this.minDifficulty = minDifficulty;
            this.maxDifficulty = maxDifficulty;
            this.includeDifficulty1 = includeDifficulty1;
            this.includeDifficulty2 = includeDifficulty2;
            this.includeDifficulty3 = includeDifficulty3;
            this.excludeDifficulty1 = excludeDifficulty1;
            this.excludeDifficulty2 = excludeDifficulty2;
            this.excludeDifficulty3 = excludeDifficulty3;
            this.notMaxDifficulty1 = notMaxDifficulty1;
            this.notMaxDifficulty2 = notMaxDifficulty2;
            this.notMaxDifficulty3 = notMaxDifficulty3;
            this.excludeTechnique1 = excludeTechnique1;
            this.excludeTechnique2 = excludeTechnique2;
            this.excludeTechnique3 = excludeTechnique3;
            this.includeTechnique1 = includeTechnique1;
            this.includeTechnique2 = includeTechnique2;
            this.includeTechnique3 = includeTechnique3;
            this.notMaxTechnique1 = notMaxTechnique1;
            this.notMaxTechnique2 = notMaxTechnique2;
            this.notMaxTechnique3 = notMaxTechnique3;
            this.getOneOfThree_1 = getOneOfThree_1;
            this.getOneOfThree_2 = getOneOfThree_2;
            this.getOneOfThree_3 = getOneOfThree_3;
        }

        @Override
        public void interrupt() {
            generator.interrupt();
        }

        @Override
        public void run() {
            Message message = new Message();
            // message.arg1 = progress;
            message.what = 10010;
            mMainHandler.sendMessage(message);
            generator = new Generator();
            Grid result = generator.generate(symmetries, minDifficulty, maxDifficulty, includeDifficulty1, includeDifficulty2, includeDifficulty3, excludeDifficulty1, excludeDifficulty2, excludeDifficulty3, notMaxDifficulty1, notMaxDifficulty2, notMaxDifficulty3, excludeTechnique1, excludeTechnique2, excludeTechnique3, includeTechnique1, includeTechnique2, includeTechnique3, notMaxTechnique1, notMaxTechnique2, notMaxTechnique3, getOneOfThree_1, getOneOfThree_2, getOneOfThree_3);
            message = new Message();
            message.obj = result;
            message.what = 10011;
            mMainHandler.sendMessage(message);

        }

    }

    Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 10010:
                    //mGame.setGrid(new Grid());
                    //AutoBusy.setBusy(GenerateDialog.this, true);
                    //AutoBusy.setBusy(btnGenerate, false);
                    //btnGenerate.setText("Stop");
                    btGenerator.setText("Stop");
                    break;
                case 10011:
                    progressBar.setVisibility(View.GONE);
                    mGrid = (Grid) msg.obj;
                    if (mGrid != null) {
                        isStart=true;
                        mGame.setGrid(mGrid);
                        sudokuList.add(mGrid);


                        mSudokuBoard.setGame(mGame);
                        mGame.getGrid().setOnchange();
                        sudokuIndex = sudokuList.size() - 1;
                        //refreshSudokuPanel();
                    }
                    if (isVisible()) {
                        // AutoBusy.setBusy(GenerateDialog.this, false);
                        // btnGenerate.setText("Generate");
                    }
                    break;
            }
        }


    };

    private boolean isVisible() {

        return true;
    }

    private Solver mSolver;
    private SudokuGame mGame;
    private Grid mGrid;
    private SudokuBoardView mSudokuBoard;

    private EnumSet<Symmetry> symmetries = EnumSet.noneOf(Symmetry.class);
    private Difficulty difficulty = Difficulty.Easy;
    private boolean isExact = true;

    private GeneratorThread generator = null;
    private List<Grid> sudokuList = new ArrayList<Grid>();
    private int sudokuIndex = 0;
    private Map<Grid, Hint> sudokuAnalyses = new HashMap<Grid, Hint>();
    HtmlTextView htmlTextView;
    private TextView mDifficutyInfo;

    static final int SOLVER_NEN = 11;
    RadioButton rbExact;
    RadioButton rbmax;
    Button btGenerator;
    boolean isStart=false;
    ProgressBar progressBar;
    SudokuDatabase mDatabase ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sudokunew);

        mSudokuBoard = findViewById(R.id.sudoku_board);
        mDifficutyInfo = (TextView) findViewById(R.id.tv_difficulty);
        htmlTextView = (HtmlTextView) findViewById(R.id.html_text);
        rbExact = (RadioButton) findViewById(R.id.rb_exact);
        rbmax = (RadioButton) findViewById(R.id.rb_max);

         btGenerator = (Button) findViewById(R.id.bt_generator);
        Button btCancel = (Button) findViewById(R.id.bt_cancel);
        Button btSelect = (Button) findViewById(R.id.bt_select);
        progressBar =findViewById(R.id.progressbar_new);
        progressBar.setVisibility(View.GONE);
        mGame = new SudokuGame();
        mSudokuBoard.setReadOnly(true);
        mSudokuBoard.setGame(mGame);
        mDifficutyInfo.setText(difficulty.toString());

        mDatabase = new SudokuDatabase(getApplicationContext());

        mDifficutyInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //要选择Difficulty;
                showSingSelect();
            }
        });
        btGenerator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rbExact.isChecked()) isExact = true;
                else isExact = false;
                if(isStart)
                {
                    stop();
                    isStart=false;
                    //stop,
                    progressBar.setVisibility(View.GONE);
                }
                else
                {
                    isStart=true;
                    progressBar.setVisibility(View.VISIBLE);
                    generate();
                }

            }
        });
        btSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mGrid != null) {

                    //保存结果，
                    SudokuGame game=new SudokuGame();
                    //只返回数据就可以了，不用hint
                    Grid g=new Grid();
                    mGrid.copyValueTo(g);
                    //要重置一下edit,新游戏非0数字都是edit false
                    g.reSetEditable();
                    game.setGrid(g);
                    game.reset();
                    long newId=mDatabase.insertSudoku(SudokuDatabase.TEMP_FORDER_ID,game);
                    //返回数据到上层
                    Bundle bundle = new Bundle();
                    //bundle.putSerializable("HINTS",lastHint);

                    //bundle.putSerializable("GRID", g);
                    bundle.putLong(SudokuPlayActivity.EXTRA_SUDOKU_ID,newId);
                    //这个只是单纯用来存储数据而新建的
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    setResult(SOLVER_NEN, intent);
                }
                finish();
            }
        });
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        initParameters();
    }

    private void initParameters() {
        symmetries.add(Symmetry.Orthogonal);
        symmetries.add(Symmetry.BiDiagonal);
        symmetries.add(Symmetry.Rotational180);
        symmetries.add(Symmetry.Rotational90);
        symmetries.add(Symmetry.Full);

        sudokuList.add(mGame.getGrid());
        rbExact.setChecked(true);
        //这个本来是可以用用户选择的，先定死吧

    }

    int choice = -1;

    /**
     * 单选 dialog
     */
    private void showSingSelect() {
        /*
        Easy,Medium,Hard,Superior,Fiendish,Fiendish2,SuperiorPlus,XYorXYZ,TwoStringKite,EmptyRectangle,Grouped2StrongLinks,XLoop,gXLoop,ThreeStrongLinks,ThreeLinkER,
        FourLinks,WXYZ,Uniqueness,UL10,UniquenessType3,UL12Type3,Quintuplet,APE,BUGs,VWXYZ,UVWXYZ,TUVWXYZ,FiveLinks,SixLinks,AdvancedPlayer,DailySudoku,Diabolical,
         */
        //默认选中第一个
        final String[] items = {"Easy", "Medium", "Hard", "Superior", "Fiendish", "Fiendish2", "SuperiorPlus", "XYorXYZ", "TwoStringKite","EmptyRectangle",
                "Grouped2StrongLinks","XLoop","gXLoop","ThreeStrongLinks","ThreeLinkER","FourLinks","WXYZ","Uniqueness","UL10","UniquenessType3","UL12Type3",
                "Quintuplet","APE","BUGs","VWXYZ","UVWXYZ","TUVWXYZ","FiveLinks","SixLinks","AdvancedPlayer","DailySudoku","Diabolical"};
        choice = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setIcon(R.mipmap.ic_launcher).setTitle("Difficulty")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choice = i;
                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (choice != -1) {
                            // Toast.makeText(MainActivity.this, "你选择了" + items[choice], Toast.LENGTH_LONG).show();
                            difficulty = Difficulty.values()[choice];
                            mDifficutyInfo.setText(difficulty.toString());
                            htmlTextView.setHtml(difficulty.getHtmlDescription());
                        }
                    }
                });
        builder.create().show();
    }
    private void stop() {
        if (generator != null && generator.isAlive()) {
            generator.interrupt();
            try {
                generator.join();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        generator = null;
       // refreshSudokuPanel();
    }
    private void generate() {
        if (symmetries.isEmpty()) {
            //JOptionPane.showMessageDialog(this, "Please select at least one symmetry",
            //       "Generate", JOptionPane.ERROR_MESSAGE);
            //return;
        }

        // Gather parameters
        double minDifficulty = difficulty.getMinDifficulty();
        double maxDifficulty = difficulty.getMaxDifficulty();
        double includeDifficulty1 = difficulty.getincludeDifficulty1();
        double includeDifficulty2 = difficulty.getincludeDifficulty2();
        double includeDifficulty3 = difficulty.getincludeDifficulty3();
        double excludeDifficulty1 = difficulty.getexcludeDifficulty1();
        double excludeDifficulty2 = difficulty.getexcludeDifficulty2();
        double excludeDifficulty3 = difficulty.getexcludeDifficulty3();
        double notMaxDifficulty1 = difficulty.getnotMaxDifficulty1();
        double notMaxDifficulty2 = difficulty.getnotMaxDifficulty2();
        double notMaxDifficulty3 = difficulty.getnotMaxDifficulty3();
        String excludeTechnique1 = difficulty.getexcludeTechnique1();
        String excludeTechnique2 = difficulty.getexcludeTechnique2();
        String excludeTechnique3 = difficulty.getexcludeTechnique3();
        String includeTechnique1 = difficulty.getincludeTechnique1();
        String includeTechnique2 = difficulty.getincludeTechnique2();
        String includeTechnique3 = difficulty.getincludeTechnique3();
        String notMaxTechnique1 = difficulty.getnotMaxTechnique1();
        String notMaxTechnique2 = difficulty.getnotMaxTechnique2();
        String notMaxTechnique3 = difficulty.getnotMaxTechnique3();
        String getOneOfThree_1 = difficulty.getOneOfThree_1();
        String getOneOfThree_2 = difficulty.getOneOfThree_2();
        String getOneOfThree_3 = difficulty.getOneOfThree_3();
        if (!isExact)
            minDifficulty = 1.0;
        List<Symmetry> symList = new ArrayList<Symmetry>(symmetries);

        // Generate grid
        generator = new GeneratorThread(symList, minDifficulty, maxDifficulty, includeDifficulty1, includeDifficulty2, includeDifficulty3, excludeDifficulty1, excludeDifficulty2, excludeDifficulty3, notMaxDifficulty1, notMaxDifficulty2, notMaxDifficulty3, excludeTechnique1, excludeTechnique2, excludeTechnique3, includeTechnique1, includeTechnique2, includeTechnique3, notMaxTechnique1, notMaxTechnique2, notMaxTechnique3, getOneOfThree_1, getOneOfThree_2, getOneOfThree_3);
        generator.start();
    }

}
