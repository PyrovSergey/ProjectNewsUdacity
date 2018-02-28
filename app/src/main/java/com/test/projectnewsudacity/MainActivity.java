package com.test.projectnewsudacity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>, SwipeRefreshLayout.OnRefreshListener {

    private NewsAdapter newsAdapter;
    private TextView mEmptyStateTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsLoader loader;

    private static final String str1 = "http://content.guardianapis.com/search?order-by=newest&page-size=200&q=";
    private static final String str2 = "&api-key=test&order-by=newest&show-fields=thumbnail,trailText,byline";
    private static String result;

    private static final int NEWS_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = str1 + str2;
        result = result.replaceAll(" ", "+");

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        ListView newsListView = (ListView) findViewById(R.id.list);
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyStateTextView);

        newsListView.setAdapter(newsAdapter);

        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                News currentNews = newsAdapter.getItem(i);

                Uri newsUri = Uri.parse(currentNews.getUrl());

                Intent newsIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                startActivity(newsIntent);

            }
        });

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        Log.e("MyTAGS", "сработал метод initLoader()");

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText("No internet connection");
        }
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        Log.e("MyTAGS", "сработал метод onCreateLoader()");
        if (loader == null){
            loader = new NewsLoader(this, result);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        newsAdapter.clear();
        if (news != null && !news.isEmpty()) {
            newsAdapter.addAll(news);
        }
        Log.e("MyTAGS", "сработал метод onLoadFinished()");
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
        //result = "";
        Log.e("MyTAGS", "сработал метод onLoaderReset()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectInfo();
    }

    private void connectInfo() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText("No internet connection");
        }
    }

    @Override
    public void onRefresh() {
        Log.e("MyTAGS", "сработал метод onRefresh()");
        mSwipeRefreshLayout.setRefreshing(false);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            loader.forceLoad();
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText("No internet connection");
        }
    }
}
