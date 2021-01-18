package com.strolink.whatsUp.adapters.recyclerView.media;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.giph.model.GiphyImage;
import com.strolink.whatsUp.helpers.giph.model.GiphyPaddedUrl;
import com.strolink.whatsUp.helpers.giph.ui.AspectRatioImageView;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.glide.GlideRequests;
import com.strolink.whatsUp.helpers.utils.ViewUtil;
import com.strolink.whatsUp.ui.MaterialColor;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class GiphyAdapter extends RecyclerView.Adapter<GiphyAdapter.GiphyViewHolder> {

    private static final String TAG = GiphyAdapter.class.getSimpleName();

    private final Context context;
    private final GlideRequests glideRequests;

    private List<GiphyImage> images;
    private OnItemClickListener listener;

    public class GiphyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, RequestListener<Drawable> {

        public AspectRatioImageView thumbnail;
        public GiphyImage image;
        public ProgressBar gifProgress;
        public volatile boolean modelReady;

        GiphyViewHolder(View view) {
            super(view);
            thumbnail = ViewUtil.findById(view, R.id.thumbnail);
            gifProgress = ViewUtil.findById(view, R.id.gif_progress);
            thumbnail.setOnClickListener(this);
            gifProgress.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) listener.onClick(this);
        }

        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            Log.w(TAG, e);

            synchronized (this) {
                if (new GiphyPaddedUrl(image.getGifUrl(), image.getGifSize()).equals(model)) {
                    this.modelReady = true;
                    notifyAll();
                }
            }

            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            synchronized (this) {
                if (new GiphyPaddedUrl(image.getGifUrl(), image.getGifSize()).equals(model)) {
                    this.modelReady = true;
                    notifyAll();
                }
            }

            return false;
        }


        public File getFile() throws ExecutionException, InterruptedException {
            synchronized (this) {
                while (!modelReady) {
                    AppHelper.wait(this, 0);
                }
            }

            File originalFile = glideRequests
                    .downloadOnly()
                    .load(new GiphyPaddedUrl(image.getGifUrl(), image.getGifSize()))
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();

            String path = FilesManager.copyDocumentToCache(Uri.fromFile(originalFile), ".gif");
            assert path != null;
            return new File(path);

        }

        public synchronized void setModelReady() {
            this.modelReady = true;
            notifyAll();
        }
    }

    public GiphyAdapter(@NonNull Context context, @NonNull GlideRequests glideRequests, @NonNull List<GiphyImage> images) {
        this.context = context.getApplicationContext();
        this.glideRequests = glideRequests;
        this.images = images;
    }

    public void setImages(@NonNull List<GiphyImage> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public void addImages(List<GiphyImage> images) {
        this.images.addAll(images);
        notifyDataSetChanged();
    }

    @Override
    public GiphyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.giphy_thumbnail, parent, false);

        return new GiphyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GiphyViewHolder holder, int position) {
        GiphyImage image = images.get(position);

        holder.modelReady = false;
        holder.image = image;
        holder.thumbnail.setAspectRatio(image.getGifAspectRatio());
        holder.gifProgress.setVisibility(View.GONE);

        RequestBuilder<Drawable> thumbnailRequest = GlideApp.with(context)
                .load(new GiphyPaddedUrl(image.getStillUrl(), image.getStillSize()))
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        if (AppHelper.isLowMemory(context)) {
            glideRequests.load(new GiphyPaddedUrl(image.getStillUrl(), image.getStillSize()))
                    .placeholder(new ColorDrawable(AppHelper.getRandomElement(MaterialColor.values()).toConversationColor(context)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(holder)
                    .into(holder.thumbnail);

            holder.setModelReady();
        } else {
            glideRequests.load(new GiphyPaddedUrl(image.getGifUrl(), image.getGifSize()))
                    .thumbnail(thumbnailRequest)
                    .placeholder(new ColorDrawable(AppHelper.getRandomElement(MaterialColor.values()).toConversationColor(context)))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(holder)
                    .into(holder.thumbnail);
        }
    }

    @Override
    public void onViewRecycled(GiphyViewHolder holder) {
        super.onViewRecycled(holder);
        glideRequests.clear(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onClick(GiphyViewHolder viewHolder);
    }
}