package com.test.projectnewsudacity;

import android.text.Html;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public final class QueryUtils {

    private static final String RESPONSE = "response";
    private static final String RESULTS = "results";
    private static final String WEB_TITLE = "webTitle";
    private static final String TITLE_IS_MISSING = "Title is missing";
    private static final String SECTION_NAME = "sectionName";
    private static final String SECTION = "section";
    private static final String WEB_URL = "webUrl";
    private static final String DEFAULT_LINK = "https://www.theguardian.com";
    private static final String WEB_PUBLICATION_DATE = "webPublicationDate";
    private static final String DATE = "date";
    private static final String FIELDS = "fields";
    private static final String BYLINE = "byline";
    private static final String TRAIL_TEXT = "trailText";
    private static final String THUMBNAIL = "thumbnail";
    private static final String GET = "GET";
    public static boolean isAnyNews;

    private QueryUtils() {
    }

    public static List<News> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);
        String jsonResponce = null;
        try {
            jsonResponce = makeHttpRequest(url);
        } catch (IOException e) {

        }

        List<News> news = extractFeatureFromJson(jsonResponce);

        return news;
    }

    public static List<News> extractFeatureFromJson(String newsRequestJSON) {
        if (TextUtils.isEmpty(newsRequestJSON)) {
            return null;
        }
        List<News> news = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(newsRequestJSON);
            JSONObject response = jsonObject.getJSONObject(RESPONSE);
            JSONArray results = response.getJSONArray(RESULTS);
            if (results.length() != 0) {
                isAnyNews = true;
            } else {
                isAnyNews = false;
            }
            for (int i = 0; i < results.length(); i++) {
                String title;
                String sectionName;
                String url;
                String date;
                String byline;
                String trailText;
                String thumbnail;
                JSONObject currentNews = results.optJSONObject(i);
                if (currentNews == null) {
                    continue;
                }

                title = currentNews.optString(WEB_TITLE);
                if (TextUtils.isEmpty(title)) {
                    title = TITLE_IS_MISSING;
                }
                if (title.contains("|")) {
                    int index = title.indexOf("|");
                    title = title.substring(0, index - 1);
                }

                sectionName = currentNews.optString(SECTION_NAME);
                if (TextUtils.isEmpty(sectionName)) {
                    sectionName = SECTION;
                }

                url = currentNews.optString(WEB_URL);
                if (TextUtils.isEmpty(url)) {
                    url = DEFAULT_LINK;
                }

                String resultDate = currentNews.optString(WEB_PUBLICATION_DATE);
                if (TextUtils.isEmpty(resultDate)) {
                    date = DATE;
                } else {
                    date = resultDate.substring(11, 16) + "    " + resultDate.substring(0, 10);
                    date = date.replaceAll("-", ".");
                }

                JSONObject fields = currentNews.optJSONObject(FIELDS);
                if (fields == null) {
                    continue;
                }

                byline = fields.optString(BYLINE);
                if (TextUtils.isEmpty(byline)) {
                    byline = "";
                }

                trailText = fields.optString(TRAIL_TEXT);
                if (TextUtils.isEmpty(byline)) {
                    trailText = "";
                }

                trailText = stripHtml(trailText);

                thumbnail = fields.optString(THUMBNAIL);
                if (TextUtils.isEmpty(byline)) {
                    trailText = null;
                }
                news.add(new News(title, sectionName, date, url, byline, trailText, thumbnail));
            }
        } catch (JSONException e) {

        }
        return news;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod(GET);
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            }
        } catch (IOException e) {

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {

        }
        return url;
    }

    private static String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return String.valueOf(Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY));
        } else {
            return String.valueOf(Html.fromHtml(html));
        }
    }
}

