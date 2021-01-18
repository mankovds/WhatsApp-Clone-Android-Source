package com.strolink.whatsUp.adapters.recyclerView.picker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.strolink.whatsUp.R;
import com.strolink.whatsUp.helpers.glide.GlideApp;
import com.strolink.whatsUp.helpers.picker.HeaderItemDecoration;
import com.strolink.whatsUp.helpers.picker.PickerHelper;
import com.strolink.whatsUp.interfaces.picker.OnSelectionListener;
import com.strolink.whatsUp.interfaces.picker.SectionIndexer;
import com.strolink.whatsUp.models.MediaPicker;

import java.util.ArrayList;

/**
 * Created by Abderrahim El imame on 7/28/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class MediaFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements HeaderItemDecoration.StickyHeaderInterface, SectionIndexer {

    public static final int HEADER = 1;
    public static final int ITEM = 2;
    public static final int SPAN_COUNT = 3;
    private static final int MARGIN = 2;

    private Context context;
    private ArrayList<MediaPicker> list;
    private OnSelectionListener onSelectionListener;
    private FrameLayout.LayoutParams layoutParams;
    private RequestManager glide;
    private RequestOptions options;

    public MediaFilesAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();

        int size = PickerHelper.WIDTH / SPAN_COUNT;
        layoutParams = new FrameLayout.LayoutParams(size, size);
        layoutParams.setMargins(MARGIN, MARGIN - 1, MARGIN, MARGIN - 1);
        options = new RequestOptions().override(360).transform(new CenterCrop()).transform(new FitCenter());
        glide = GlideApp.with(context);
    }

    public ArrayList<MediaPicker> getItemList() {
        return list;
    }

    public MediaFilesAdapter addImage(MediaPicker image) {
        list.add(image);
        notifyDataSetChanged();
        return this;
    }

    public void addOnSelectionListener(OnSelectionListener onSelectionListener) {
        this.onSelectionListener = onSelectionListener;
    }

    public void addImageList(ArrayList<MediaPicker> images) {
        list.addAll(images);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        MediaPicker i = list.get(position);
        return (i.getContentUrl().equalsIgnoreCase("")) ?
                HEADER : ITEM;
    }

    public void clearList() {
        list.clear();
    }

    public void select(boolean selection, int pos) {
        list.get(pos).setSelected(selection);
        notifyItemChanged(pos);
    }

    @Override
    public long getItemId(int position) {
        return list.get(position).getContentUrl().hashCode();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER) {
            return new HeaderHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.row_header_camera_gallery, parent, false));
        } else {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.row_media_files_picker, parent, false);
            return new MediaPickerHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MediaPicker mediaPicker = list.get(position);
        if (holder instanceof MediaPickerHolder) {
            MediaPickerHolder mediaPickerHolder = (MediaPickerHolder) holder;
            if (mediaPicker.getType().equals("video")) {

                long interval = 5000 * 1000;
                RequestOptions options = new RequestOptions()
                        .frame(interval)
                        .override(360)
                        .transform(new CenterCrop())
                        .transform(new FitCenter());

                glide.load(mediaPicker.getContentUrl())
                        .apply(options)
                        .into(mediaPickerHolder.preview);
                mediaPickerHolder.play_btn.setVisibility(View.VISIBLE);
            } else {
                glide.load(mediaPicker.getContentUrl())
                        .apply(options)
                        .into(mediaPickerHolder.preview);
                mediaPickerHolder.play_btn.setVisibility(View.GONE);
            }
            mediaPickerHolder.selection.setVisibility(mediaPicker.getSelected() ? View.VISIBLE : View.GONE);
        } else if (holder instanceof HeaderHolder) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            headerHolder.header.setText(mediaPicker.getHeaderDate());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getHeaderPositionForItem(int itemPosition) {
        int headerPosition = 0;
        do {
            if (this.isHeader(itemPosition)) {
                headerPosition = itemPosition;
                break;
            }
            itemPosition -= 1;
        } while (itemPosition >= 0);
        return headerPosition;
    }

    @Override
    public int getHeaderLayout(int headerPosition) {
        return R.layout.row_header_camera_gallery;
    }

    @Override
    public void bindHeaderData(View header, int headerPosition) {
        MediaPicker image = list.get(headerPosition);
        ((TextView) header.findViewById(R.id.header)).setText(image.getHeaderDate());
    }

    @Override
    public boolean isHeader(int itemPosition) {
        return getItemViewType(itemPosition) == 1;
    }

    @Override
    public String getSectionText(int position) {
        return list.get(position).getHeaderDate();
    }

    public String getSectionMonthYearText(int position) {
        return list.get(position).getScrollerDate();
    }

    public class MediaPickerHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        AppCompatImageView preview;
        AppCompatImageView play_btn;
        AppCompatImageView selection;

        MediaPickerHolder(View itemView) {
            super(itemView);
            preview = itemView.findViewById(R.id.preview);
            selection = itemView.findViewById(R.id.selection);
            play_btn = itemView.findViewById(R.id.play_btn);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            preview.setLayoutParams(layoutParams);
        }

        @Override
        public void onClick(View view) {
            int id = this.getLayoutPosition();
            onSelectionListener.OnClick(list.get(id), view, id);
        }

        @Override
        public boolean onLongClick(View view) {
            int id = this.getLayoutPosition();
            onSelectionListener.OnLongClick(list.get(id), view, id);
            return true;
        }
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {
        TextView header;

        HeaderHolder(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.header);
        }
    }
}