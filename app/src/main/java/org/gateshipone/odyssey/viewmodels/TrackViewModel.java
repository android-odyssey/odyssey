/*
 * Copyright (C) 2020 Team Gateship-One
 * (Hendrik Borghorst & Frederik Luetkes)
 *
 * The AUTHORS.md file contains a detailed contributors list:
 * <https://github.com/gateship-one/odyssey/blob/master/AUTHORS.md>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.gateshipone.odyssey.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.gateshipone.odyssey.R;
import org.gateshipone.odyssey.models.TrackModel;
import org.gateshipone.odyssey.utils.MusicLibraryHelper;

import java.lang.ref.WeakReference;
import java.util.List;

public class TrackViewModel extends GenericViewModel<TrackModel> {

    /**
     * The album key if tracks of a specific album should be loaded.
     */
    private final long mAlbumId;

    private TrackViewModel(@NonNull final Application application, final long albumId) {
        super(application);

        mAlbumId = albumId;
    }

    @Override
    void loadData() {
        new TrackLoaderTask(this).execute();
    }

    private static class TrackLoaderTask extends AsyncTask<Void, Void, List<TrackModel>> {

        private final WeakReference<TrackViewModel> mViewModel;

        TrackLoaderTask(final TrackViewModel viewModel) {
            mViewModel = new WeakReference<>(viewModel);
        }

        @Override
        protected List<TrackModel> doInBackground(Void... voids) {
            final TrackViewModel model = mViewModel.get();

            if (model != null) {
                final Application application = model.getApplication();

                if (model.mAlbumId == -1) {
                    // load all tracks
                    return MusicLibraryHelper.getAllTracks(null, application);
                } else {
                    // load album tracks

                    // read order preference
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(application);
                    final String orderKey = sharedPref.getString(application.getString(R.string.pref_album_tracks_sort_order_key), application.getString(R.string.pref_album_tracks_sort_default));

                    return MusicLibraryHelper.getTracksForAlbum(model.mAlbumId, orderKey, application);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<TrackModel> result) {
            final TrackViewModel model = mViewModel.get();

            if (model != null) {
                model.setData(result);
            }
        }
    }


    public static class TrackViewModelFactory extends ViewModelProvider.NewInstanceFactory {

        private final Application mApplication;

        private final long mAlbumId;

        public TrackViewModelFactory(final Application application, final long albumId) {
            mApplication = application;
            mAlbumId = albumId;
        }

        public TrackViewModelFactory(final Application application) {
            this(application, -1);
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new TrackViewModel(mApplication, mAlbumId);
        }
    }
}
