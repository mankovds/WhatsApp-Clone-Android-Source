package com.strolink.whatsUp.models;

import java.io.Serializable;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MediaPicker implements Serializable {
    private String headerDate;
    private String contentUrl;
    private String url;
    private String type;
    private Boolean isSelected;
    private String scrollerDate;
    private int position;

    public MediaPicker(String headerDate, String contentUrl, String url, String scrollerDate, String type) {
        this.headerDate = headerDate;
        this.contentUrl = contentUrl;
        this.url = url;
        this.isSelected = false;
        this.scrollerDate = scrollerDate;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getHeaderDate() {
        return headerDate;
    }

    public void setHeaderDate(String headerDate) {
        this.headerDate = headerDate;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getSelected() {
        return isSelected;
    }

    public void setSelected(Boolean selected) {
        isSelected = selected;
    }

    public String getScrollerDate() {
        return scrollerDate;
    }

    public void setScrollerDate(String scrollerDate) {
        this.scrollerDate = scrollerDate;
    }
}

