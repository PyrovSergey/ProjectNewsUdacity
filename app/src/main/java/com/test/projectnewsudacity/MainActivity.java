package com.test.projectnewsudacity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>, SwipeRefreshLayout.OnRefreshListener {

    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsLoader loader;

    private static final String STR_1 = "http://content.guardianapis.com/search?order-by=newest&page-size=200&q=";
    private String searchQuery = "";
    private static final String STR_3 = "&api-key=test&order-by=newest&show-fields=thumbnail,trailText,byline";

    private static final int NEWS_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources()
                .getColor(R.color.blue), getResources()
                .getColor(R.color.purple), getResources()
                .getColor(R.color.yellow), getResources()
                .getColor(R.color.red));
        mSwipeRefreshLayout.setRefreshing(true);

        ListView newsListView = (ListView) findViewById(R.id.list);
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());

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

        Log.e("MyTAGS", "сработал метод initLoader()");
        loader = (NewsLoader) getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        Log.e("MyTAGS", "сработал метод onCreateLoader()");
        if (i == NEWS_LOADER_ID) {
            loader = new NewsLoader(this, searchResult(null));
            //Log.e("MyTAGS", result);
        }
        return loader;
    }

    private void alertMessage(String title, String message, int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(icon)
                .setCancelable(false)
                .setNegativeButton("ОК",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        newsAdapter.clear();
        if (!QueryUtils.isAnyNews) {
            alertMessage(getString(R.string.no_news), getString(R.string.swipe_to_repeat_request), R.drawable.ic_bubble_chart_deep_purple_a400_48dp);
        }
        if (news != null && !news.isEmpty()) {
            newsAdapter.addAll(news);
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mSwipeRefreshLayout.setRefreshing(false);
        Log.e("MyTAGS", "сработал метод onLoadFinished()");
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
        Log.e("MyTAGS", "сработал метод onLoaderReset()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            alertMessage(getString(R.string.no_internet_connection), getString(R.string.Check_connection_settings), R.drawable.ic_signal_wifi_off_deep_purple_400_48dp);
            mSwipeRefreshLayout.setRefreshing(false);
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        Log.e("MyTAGS", "сработал метод onRefresh()");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            newsAdapter.clear();
            loader.setUrl(searchResult(null));
            loader.forceLoad();
        } else {
            newsAdapter.clear();
            mSwipeRefreshLayout.setRefreshing(false);
            alertMessage(getString(R.string.no_internet_connection), getString(R.string.Check_connection_settings), R.drawable.ic_signal_wifi_off_deep_purple_400_48dp);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (null != searchManager) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String inputQuery) {
                // тут получаем строку для поиска
                mSwipeRefreshLayout.setRefreshing(true);
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    newsAdapter.clear();
                    String query = inputQuery.replaceAll(" ", "+");
                    loader.setUrl(searchResult(query));
                    loader.forceLoad();
                } else {
                    newsAdapter.clear();
                    mSwipeRefreshLayout.setRefreshing(false);
                    alertMessage(getString(R.string.no_internet_connection), getString(R.string.Check_connection_settings), R.drawable.ic_signal_wifi_off_deep_purple_400_48dp);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        return true;
    }

    private String searchResult(String query) {
        if (query == null) {
            return STR_1 + STR_3;
        }
        return STR_1 + query + STR_3;
    }

    private void inputText(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
