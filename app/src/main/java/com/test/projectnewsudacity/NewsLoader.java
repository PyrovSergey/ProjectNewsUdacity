package com.test.projectnewsudacity;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

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
        Log.e("MyTAGS", "сработал метод onStartLoading()");
    }

    @Override
    public List<News> loadInBackground() {
        if (url == null) {
            return null;
        }

        List<News> news = QueryUtils.fetchNewsData(url);
        Log.e("MyTAGS", "сработал метод loadInBackground()");
        Log.e("MyTAGS", url.toString());
        return news;
    }
}
