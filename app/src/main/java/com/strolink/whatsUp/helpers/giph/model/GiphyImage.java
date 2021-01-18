package com.strolink.whatsUp.helpers.giph.model;


public class GiphyImage {


    //@JsonProperty
    private ImageTypes images;


    public String getGifUrl() {
        return images.downsized.url;
    }

    public long getGifSize() {
        return images.downsized.size;
    }


    public float getGifAspectRatio() {
        return (float) images.downsized.width / (float) images.downsized.height;
    }

    public String getStillUrl() {
        return images.downsized_still.url;
    }

    public long getStillSize() {
        return images.downsized_still.size;
    }

    public static class ImageTypes {
        //
        private ImageData fixed_height;
        //
        private ImageData fixed_height_still;
        //
        private ImageData fixed_height_downsampled;
        //
        private ImageData fixed_width;
        //
        private ImageData fixed_width_still;
        //
        private ImageData fixed_width_downsampled;
        //
        private ImageData fixed_width_small;

        private ImageData downsized_medium;

        private ImageData downsized;

        private ImageData downsized_still;
    }

    public static class ImageData {

        private String url;


        private int width;


        private int height;


        private int size;


        private String mp4;


        private String webp;
    }

}
