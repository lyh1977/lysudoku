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
import android.os.Handler;
import android.util.Log;

import org.ly.lysudoku.db.SudokuDatabase;
import org.ly.lysudoku.game.FolderInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads details of given folders on one single background thread.
 * Results are published on GUI thread via {@link FolderDetailCallback} interface.
 * <p/>
 * Please note that instance of this class has to be created on GUI thread!
 * <p/>
 * You should explicitly call {@link #destroy()} when this object is no longer needed.
 *
 * @author romario
 */
public class FolderDetailLoader {

    private static final String TAG = "FolderDetailLoader";

    private SudokuDatabase mDatabase;
    private Handler mGuiHandler;
    private ExecutorService mLoaderService = Executors.newSingleThreadExecutor();

    public FolderDetailLoader(Context context) {
        mDatabase = new SudokuDatabase(context);
        mGuiHandler = new Handler();
    }

    public void loadDetailAsync(long folderID, FolderDetailCallback loadedCallback) {
        final long folderIDFinal = folderID;
        final FolderDetailCallback loadedCallbackFinal = loadedCallback;
        mLoaderService.execute(() -> {
            try {
                final FolderInfo folderInfo = mDatabase.getFolderInfoFull(folderIDFinal);

                mGuiHandler.post(() -> loadedCallbackFinal.onLoaded(folderInfo));
            } catch (Exception e) {
                // this is some unimportant background stuff, do not fail
                Log.e(TAG, "Error occurred while loading full folder info.", e);
            }
        });
    }

    public void destroy() {
        mLoaderService.shutdownNow();
        mDatabase.close();
    }

    public interface FolderDetailCallback {
        void onLoaded(FolderInfo folderInfo);
    }
}
