package com.strolink.whatsUp.helpers.giph.model;


import java.util.List;

public class GiphyResponse {

    // @JsonProperty
    private List<GiphyImage> data;

    public List<GiphyImage> getData() {
        return data;
    }

    public void setData(List<GiphyImage> data) {
        this.data = data;
    }
}
