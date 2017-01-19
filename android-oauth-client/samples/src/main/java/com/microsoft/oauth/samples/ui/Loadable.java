
package com.microsoft.oauth.samples.ui;

import android.support.v4.app.LoaderManager.LoaderCallbacks;

import com.microsoft.oauth.samples.AsyncResourceLoader;

public interface Loadable<T> extends LoaderCallbacks<AsyncResourceLoader.Result<T>> {

    boolean hasMore();

    boolean hasError();

    void init();

    void destroy();

    boolean isReadyToLoadMore();

    void loadMore();
}
