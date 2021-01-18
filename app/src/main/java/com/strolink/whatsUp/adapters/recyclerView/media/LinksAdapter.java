package com.strolink.whatsUp.adapters.recyclerView.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.UtilsString;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class LinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<MessageModel> mMessagesModel;
    private LayoutInflater mInflater;

    public LinksAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        mInflater = LayoutInflater.from(mActivity);
    }

    public void setMessages(List<MessageModel> mMessagesList) {
        this.mMessagesModel = mMessagesList;
        notifyDataSetChanged();
    }


    public List<MessageModel> getMessages() {
        return mMessagesModel;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_links, parent, false);
        return new LinksViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final LinksViewHolder linksViewHolder = (LinksViewHolder) holder;
        final MessageModel messagesModel = this.mMessagesModel.get(position);
        try {
            if (messagesModel.getMessage() != null) {
                if (UtilsString.checkForUrls(messagesModel.getMessage())) {
                    String url = UtilsString.getUrl(messagesModel.getMessage());
                    if (url != null)
                        if (!url.startsWith("http://")) {
                            if (!url.startsWith("https://")) {
                                url = (new StringBuilder()).append("http://").append(url).toString();
                            }
                        }
                    AppHelper.LogCat(" valid " + url);
                    linksViewHolder.urlLink.setText(url);

                }
            }

        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mMessagesModel != null) {
            return mMessagesModel.size();
        } else {
            return 0;
        }
    }


    public class LinksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.image_preview)
        AppCompatImageView imagePreview;

        @BindView(R.id.title_link)
        TextView titleLink;

        @BindView(R.id.description)
        TextView descriptionLink;

        @BindView(R.id.url)
        TextView urlLink;

        LinksViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View view) {
            MessageModel messagesModel = mMessagesModel.get(getAdapterPosition());
            String url = UtilsString.getUrl(messagesModel.getMessage());
            if (url != null)
                if (!url.startsWith("http://")) {
                    if (!url.startsWith("https://")) {
                        url = (new StringBuilder()).append("http://").append(url).toString();
                    }
                }

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mActivity.startActivity(intent);
        }
    }
}

