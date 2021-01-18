package com.strolink.whatsUp.adapters.recyclerView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.activities.popups.StatusDelete;
import com.strolink.whatsUp.activities.status.StatusActivity;
import com.strolink.whatsUp.models.users.status.StatusModel;
import com.strolink.whatsUp.helpers.AppHelper;
import com.strolink.whatsUp.helpers.UtilsString;
import com.strolink.whatsUp.presenters.users.StatusPresenter;
import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity mActivity;
    private List<StatusModel> mStatusModel;
    private StatusPresenter statusPresenter;
    private String oldStatusId ="undefined";

    public void setStatus(List<StatusModel> statusModelList) {
        this.mStatusModel = statusModelList;
        notifyDataSetChanged();
    }


    public StatusAdapter(@NonNull StatusActivity mActivity) {
        this.mActivity = mActivity;
        statusPresenter = new StatusPresenter(mActivity);
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_status, parent, false);
        return new StatusViewHolder(itemView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        StatusViewHolder statusViewHolder = (StatusViewHolder) holder;
        StatusModel statusModel = mStatusModel.get(position);
        try {


            if (statusModel.getBody() != null) {

                statusViewHolder.setStatus(statusModel.getBody());
            }

            if (statusModel.isCurrent()) {
                statusViewHolder.setStatusColorCurrent();
                oldStatusId = statusModel.getS_id();
            } else {
                statusViewHolder.setStatusColor();
            }


        } catch (Exception e) {
            AppHelper.LogCat("" + e.getMessage());
        }
        statusViewHolder.setOnLongClickListener(v -> {
            if (!statusModel.isIs_default()) {
                Intent mIntent = new Intent(mActivity, StatusDelete.class);
                mIntent.putExtra("statusID", statusModel.getS_id());
                mActivity.startActivity(mIntent);
            }else {
                AppHelper.CustomToast(mActivity,mActivity.getString(R.string.you_cant_delete_dflt_status));
            }
            return true;
        });
        statusViewHolder.setOnClickListener(v -> statusPresenter.UpdateCurrentStatus(statusModel.getBody(), oldStatusId, statusModel.getS_id()));

    }

    private void removeStatusItem(int position) {
        if (position != 0) {
            try {
                mStatusModel.remove(position);
                notifyItemRemoved(position);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }

    public void DeleteStatusItem(String StatusID) {
        try {
            int arraySize = mStatusModel.size();
            for (int i = 0; i < arraySize; i++) {
                StatusModel model = mStatusModel.get(i);

                if (StatusID.equals(model.getS_id())) {
                    removeStatusItem(i);
                    break;
                }

            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    @Override
    public int getItemCount() {
        if (mStatusModel != null)
            return mStatusModel.size();
        else
            return 0;
    }

    class StatusViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.status)
        EmojiTextView status;

        StatusViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);

        }


        void setStatus(String Status) {
            String finalStatus = UtilsString.unescapeJava(Status);
            status.setText(finalStatus);
        }

        void setStatusColorCurrent() {
            status.setTextColor(mActivity.getResources().getColor(R.color.colorBlueLight));
        }

        void setStatusColor() {
            status.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
        }

        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);

        }

        void setOnLongClickListener(View.OnLongClickListener listener) {
            itemView.setOnLongClickListener(listener);

        }
    }


}
