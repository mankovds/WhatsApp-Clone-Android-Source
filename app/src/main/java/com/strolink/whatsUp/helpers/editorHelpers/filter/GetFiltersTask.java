package com.strolink.whatsUp.helpers.editorHelpers.filter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.strolink.whatsUp.helpers.editorHelpers.FilterHelper;
import com.strolink.whatsUp.helpers.editorHelpers.TaskCallback;
import com.strolink.whatsUp.models.ThumbnailFilter;

import java.util.ArrayList;

public final class GetFiltersTask extends AsyncTask<Void, Void, ArrayList> {
    private final TaskCallback<ArrayList<ThumbnailFilter>> listenerRef;
    private Bitmap srcBitmap;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public GetFiltersTask(TaskCallback<ArrayList<ThumbnailFilter>> taskCallbackWeakReference, Bitmap srcBitmap, Context context) {
        this.srcBitmap = srcBitmap;
        this.listenerRef = taskCallbackWeakReference;
        this.context = context;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(ArrayList result) {
        super.onPostExecute(result);
        if (listenerRef != null) {
            listenerRef.onTaskDone(result);
        }
    }

    @Override
    protected ArrayList doInBackground(Void... params) {

        ArrayList<ThumbnailFilter> filters = new FilterHelper().getFilters(context);
        for (int index = 0; index < filters.size(); index++) {
            ThumbnailFilter thumbnailFilter = filters.get(index);
            thumbnailFilter.setImage(getScaledBitmap(srcBitmap));

        }
        return filters;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    private Bitmap getScaledBitmap(Bitmap srcBitmap) {
        // Determine how much to scale down the image
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();

        int targetWidth = 320;
        int targetHeight = 240;
        if (srcWidth < targetWidth || srcHeight < targetHeight) {
            return srcBitmap;
        }

        float scaleFactor =
                Math.max(
                        (float) srcWidth / targetWidth,
                        (float) srcHeight / targetHeight);

        return
                Bitmap.createScaledBitmap(
                        srcBitmap,
                        (int) (srcWidth / scaleFactor),
                        (int) (srcHeight / scaleFactor),
                        true);
    }
}