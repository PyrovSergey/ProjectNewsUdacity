package com.test.projectnewsudacity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String PAGE_SIZE = "page-size";
    private static final String API_KEY = "api-key";
    private static final String KEY = "test";
    private static final String SHOW_FIELDS = "show-fields";
    private static final String THUMBNAIL_TRAIL_TEXT_BYLINE = "thumbnail,trailText,byline";
    private static final String NONE = "none";
    private static final String SECTION = "section";
    private static final String ORDER_BY = "order-by";
    private static final String NEWEST = "newest";
    private static final String RELEVANCE = "relevance";
    private static final String QUERY = "q";
    private static final String HTTPS_PLAY_GOOGLE_COM_STORE_APPS_DETAILS_ID_COM_ANDROID_CHROME = "https://play.google.com/store/apps/details?id=com.android.chrome";
    private NewsAdapter newsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NewsLoader loader;

    private static final String USGS_REQUEST_URL = "http://content.guardianapis.com/search";
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
                PackageManager packageManager = MainActivity.this.getPackageManager();
                Intent newsIntent = new Intent(Intent.ACTION_VIEW, newsUri);
                if (newsIntent.resolveActivity(packageManager) != null) {
                    startActivity(newsIntent);
                } else {
                    Uri googlePlay = Uri.parse(HTTPS_PLAY_GOOGLE_COM_STORE_APPS_DETAILS_ID_COM_ANDROID_CHROME);
                    Intent intentGooglePlay = new Intent(Intent.ACTION_VIEW, googlePlay);
                    startActivity(intentGooglePlay);
                    Toast.makeText(getBaseContext(), R.string.install_the_browser, Toast.LENGTH_LONG).show();
                }
            }
        });

        loader = (NewsLoader) getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        if (i == NEWS_LOADER_ID) {
            loader = new NewsLoader(this, searchResult(null));
        }
        return loader;
    }

    private void alertMessage(String title, String message, int icon) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(icon)
                .setCancelable(false)
                .setNegativeButton(R.string.ok,
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
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        String pageSize = sharedPreferences.getString(getString(R.string.settings_page_size_key), getString(R.string.settings_page_size_default));
        if (TextUtils.isEmpty(pageSize)) {
            pageSize = "0";
        }
        uriBuilder.appendQueryParameter(PAGE_SIZE, pageSize);
        uriBuilder.appendQueryParameter(API_KEY, KEY);
        uriBuilder.appendQueryParameter(SHOW_FIELDS, THUMBNAIL_TRAIL_TEXT_BYLINE);
        String section = sharedPreferences.getString(getString(R.string.settings_only_show_key), getString(R.string.settings_only_show_default));
        if (!section.equals(NONE)) {
            uriBuilder.appendQueryParameter(SECTION, section);
        }

        if (query == null) {
            uriBuilder.appendQueryParameter(ORDER_BY, NEWEST);
            return uriBuilder.toString();
        }
        uriBuilder.appendQueryParameter(ORDER_BY, RELEVANCE);
        uriBuilder.appendQueryParameter(QUERY, query);

        return uriBuilder.toString();
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
