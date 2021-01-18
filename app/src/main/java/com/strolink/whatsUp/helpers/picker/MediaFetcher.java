package com.strolink.whatsUp.helpers.picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.strolink.whatsUp.models.MediaPicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MediaFetcher extends AsyncTask<Cursor, Void, ArrayList<MediaPicker>> {
    private ArrayList<MediaPicker> LIST = new ArrayList<>();
     Context context;

    protected MediaFetcher(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<MediaPicker> doInBackground(Cursor... cursors) {
        Cursor cursor = cursors[0];
        if (cursor != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
            int date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int data = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            int contentUrl = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int MediaType = cursor.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE);
            String header = "";
            int limit = 100;
            if (cursor.getCount() < 100) {
                limit = cursor.getCount();
            }
            cursor.move(limit - 1);
            for (int i = limit; i < cursor.getCount(); i++) {
                cursor.moveToNext();
                Uri curl;
                String type;
                if (cursor.getInt(MediaType) == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                    type = "image";
                    curl = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(contentUrl));
                } else {
                    type = "video";
                    curl = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "" + cursor.getInt(contentUrl));
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(cursor.getLong(date));
                String dateDifference = PickerHelper.getDateDifference(context, calendar);

                if (!header.equalsIgnoreCase(dateDifference)) {
                    header = dateDifference;
                    LIST.add(new MediaPicker(dateDifference, "", "", dateFormat.format(calendar.getTime()), type));
                }
                LIST.add(new MediaPicker(header, curl.toString(), cursor.getString(data), dateFormat.format(calendar.getTime()), type));
            }
            cursor.close();
        }
        return LIST;
    }

}