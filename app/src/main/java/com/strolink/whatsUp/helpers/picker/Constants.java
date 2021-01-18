package com.strolink.whatsUp.helpers.picker;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class Constants {
    public static final int sScrollbarAnimDuration = 300;
    public static String[] PROJECTION = new String[]{
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE
    };


    // Return only video and image metadata.
    public static    String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
            + " OR "
            + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

    public static    Uri queryUri = MediaStore.Files.getContentUri("external");

    public static Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static String ORDERBY = MediaStore.Images.Media.DATE_TAKEN + " DESC";

}
