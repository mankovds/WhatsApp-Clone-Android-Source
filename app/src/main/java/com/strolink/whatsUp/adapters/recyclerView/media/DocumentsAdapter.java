package com.strolink.whatsUp.adapters.recyclerView.media;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.app.EndPoints;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.Files.FilesManager;
import com.strolink.whatsUp.helpers.PreferenceManager;



import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class DocumentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<MessageModel> mMessagesModel;
    private LayoutInflater mInflater;

    public DocumentsAdapter(Activity mActivity) {
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
        View view = mInflater.inflate(R.layout.row_documents, parent, false);
        return new MediaProfileViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final MediaProfileViewHolder mediaProfileViewHolder = (MediaProfileViewHolder) holder;
        final MessageModel messagesModel = this.mMessagesModel.get(position);
        try {

            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                long filesSize = Long.parseLong(messagesModel.getFile_size());
                mediaProfileViewHolder.documentSize.setText(FilesManager.getFileSize(filesSize));
                mediaProfileViewHolder.mediaDocument.setVisibility(View.VISIBLE);
            } else {
                mediaProfileViewHolder.mediaDocument.setVisibility(View.GONE);
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

    public class MediaProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.media_document)
        AppCompatImageView mediaDocument;
        @BindView(R.id.document_size)
        TextView documentSize;


        MediaProfileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mediaDocument.setOnClickListener(this);

        }



        @Override
        public void onClick(View view) {
            MessageModel messagesModel = mMessagesModel.get(getAdapterPosition());
            if (messagesModel.getSenderId().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (FilesManager.isFileDocumentsSentExists(mActivity, FilesManager.getDocument(messagesModel.getFile()))) {
                    openDocument(FilesManager.getFileDocumentSent(mActivity, messagesModel.getFile()));
                } else {
                    File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getFile());
                    openDocument(file);
                }
            } else {
                if (FilesManager.isFileDocumentsExists(mActivity, FilesManager.getDocument(messagesModel.getFile()))) {
                    openDocument(FilesManager.getFileDocument(mActivity, messagesModel.getFile()));
                } else {
                    File file = new File(EndPoints.MESSAGE_DOCUMENT_URL + messagesModel.getFile());
                    openDocument(file);
                }
            }


        }

        private void openDocument(File file) {
            if (file.exists()) {
                Uri path = FilesManager.getFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(path, "application/pdf");
                if (AppHelper.isAndroid7()) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                try {
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    AppHelper.CustomToast(mActivity, mActivity.getString(R.string.no_application_to_view_pdf));
                }
            }
        }

    }
}

