package com.test.projectnewsudacity;

/**
 * Created by pyrov on 26.02.2018.
 */

public class News {

    private String title;

    private String sectionName;

    private String date;

    private String url;

    private String byline;

    public News(String title, String sectionName, String date, String url, String byline) {
        this.title = title;
        this.sectionName = sectionName;
        this.date = date;
        this.url = url;
        this.byline = byline;
    }

    public String getTitle() {
        return title;
    }

    public String getSectionName() {
        return sectionName;
    }

    public String getDate() {
        return date;
    }

    public String getUrl() {
        return url;
    }

    public String getByline() {
        return byline;
    }
}
