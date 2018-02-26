package com.test.projectnewsudacity;

import android.text.TextUtils;
import android.util.Log;

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

import static com.test.projectnewsudacity.MainActivity.LOG_TAG;

public final class QueryUtils {

    private QueryUtils() {
    }

    public static List<News> fetchNewsData(String requestUrl) {
        URL url = createUrl(requestUrl);

        String jsonResponce = null;
        try {
            jsonResponce = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        List<News> news = extractFeatureFromJson(jsonResponce);
        Log.e("QueryUtils", "сработал метод fetchEarthquakeData()");

        return news;
    }

    public static List<News> extractFeatureFromJson(String newsRequestJSON) {
        if (TextUtils.isEmpty(newsRequestJSON)) {
            return null;
        }
        List<News> news = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(newsRequestJSON);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONArray results = response.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                String title;
                String sectionName;
                String url;
                String date;
                String byline = " ";
                JSONObject currentNews = results.optJSONObject(i);
                if (currentNews == null) {
                    continue;
                }

                title = currentNews.getString("webTitle");
                if (title == null) {
                    title = "Title is missing";
                }

                sectionName = currentNews.getString("sectionName");
                if (sectionName == null) {
                    sectionName = " ";
                }

                url = currentNews.optString("webUrl");
                if (url == null) {
                    url = "https://www.theguardian.com";
                }

                String resultDate = currentNews.getString("webPublicationDate");
                if (resultDate == null) {
                    date = " ";
                } else {
                    date = resultDate.substring(11, 16) + "    " + resultDate.substring(0, 10);
                    date = date.replaceAll("-", ".");
                }

                JSONObject fields = currentNews.getJSONObject("fields");
                byline = fields.getString("byline");
                    if (byline == null) {
                        byline = " ";
                    }

                news.add(new News(title, sectionName, date, url, byline));

            }
        } catch (JSONException e) {
            Log.e("MyTAGS", "Problem parsing the earthquake JSON results", e);
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
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
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
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }
}

