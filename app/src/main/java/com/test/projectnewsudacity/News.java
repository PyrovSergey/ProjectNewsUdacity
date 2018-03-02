package com.test.projectnewsudacity;

public class News {

    private String title;

    private String sectionName;

    private String date;

    private String url;

    private String byline;

    private String trailText;

    private String thumbnail;

    public News(String title, String sectionName, String date, String url, String byline, String trailText, String thumbnail) {
        this.title = title;
        this.sectionName = sectionName;
        this.date = date;
        this.url = url;
        this.byline = byline;
        this.trailText = trailText;
        this.thumbnail = thumbnail;
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

    public String getTrailText() {
        return trailText;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
