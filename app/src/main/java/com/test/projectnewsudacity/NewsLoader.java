package com.test.projectnewsudacity;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    String url;

    public NewsLoader(Context context, String url) {
        super(context);
        this.url = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        if (url == null) {
            return null;
        }
        List<News> news = QueryUtils.fetchNewsData(url);
        return news;
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }
}
