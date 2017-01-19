
package com.microsoft.oauth.samples.ui;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.SparseBooleanArray;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.microsoft.oauth.samples.AsyncResourceLoader;
import com.microsoft.oauth.samples.SamplesConstants;
import com.google.api.client.util.Preconditions;
import com.wuman.oauth.samples.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoadableDecorator<T> implements LoaderCallbacks<AsyncResourceLoader.Result<T>> {

    static final Logger LOGGER = Logger.getLogger(SamplesConstants.TAG);

    private final LoaderManager.LoaderCallbacks<AsyncResourceLoader.Result<T>> mCallbacks;
    private final int mLoaderId;
    private final ListFragment mListFragment;
    private final AdapterView<?> mAdapterView;
    private final SparseBooleanArray mActive = new SparseBooleanArray();

    public LoadableDecorator(LoaderCallbacks<AsyncResourceLoader.Result<T>> callbacks, int loaderId,
                             ListFragment listFragment) {
        super();
        this.mCallbacks = Preconditions.checkNotNull(callbacks);
        this.mLoaderId = loaderId;
        this.mListFragment = Preconditions.checkNotNull(listFragment);
        this.mAdapterView = Preconditions.checkNotNull(listFragment.getListView());

        mListFragment.setListShown(false);
    }

    @Override
    public Loader<AsyncResourceLoader.Result<T>> onCreateLoader(int id, Bundle args) {
        mActive.put(id, true);
        if (mLoaderId == id) {
            mListFragment.setListShown(!isEmpty());
        }
        updateWindowIndeterminateProgress();
        return mCallbacks.onCreateLoader(id, args);
    }

    @Override
    public void onLoadFinished(Loader<AsyncResourceLoader.Result<T>> loader, AsyncResourceLoader.Result<T> result) {
        mCallbacks.onLoadFinished(loader, result);
        mActive.delete(loader.getId());
        if (mLoaderId == loader.getId()) {
            if (mListFragment.isResumed()) {
                mListFragment.setListShown(true);
            } else {
                mListFragment.setListShownNoAnimation(true);
            }
            if (!result.success) {
                Crouton.makeText(mListFragment.getActivity(),
                        result.errorMessage, Style.ALERT).show();
                LOGGER.log(Level.WARNING, "error: " + result.errorMessage, result.exception);
            }
            mListFragment.setEmptyText(mListFragment.getString(R.string.empty));
        }
        updateWindowIndeterminateProgress();
    }

    @Override
    public void onLoaderReset(Loader<AsyncResourceLoader.Result<T>> loader) {
        mCallbacks.onLoaderReset(loader);
        mActive.delete(loader.getId());
        if (mLoaderId == loader.getId()) {
            if (mListFragment.isVisible()) {
                mListFragment.setListShown(true);
            }
        }
        updateWindowIndeterminateProgress();
    }

    private void updateWindowIndeterminateProgress() {
        if (mListFragment.isVisible()) {
            mListFragment.getActivity().setProgressBarIndeterminateVisibility(mActive.size() != 0);
        }
    }

    private boolean isEmpty() {
        Adapter adapter = mAdapterView.getAdapter();
        return adapter == null || adapter.isEmpty();
    }

}
