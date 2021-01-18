package com.strolink.whatsUp.adapters.recyclerView.calls;

import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.strolink.whatsUp.R;
import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.helpers.UtilsTime;

import org.joda.time.DateTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 12/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class CallsDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private Activity mActivity;
    private List<CallsInfoModel> callsModelList;

    public CallsDetailsAdapter(@NonNull Activity mActivity) {
        this.mActivity = mActivity;
    }

    public void setCalls(List<CallsInfoModel> callsModelList) {
        this.callsModelList = callsModelList;
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.row_call_details, parent, false);
        return new CallsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final CallsViewHolder callsViewHolder = (CallsViewHolder) holder;
        final CallsInfoModel callsInfoModel = callsModelList.get(position);

        if (callsInfoModel.isReceived()) {
            callsViewHolder.showIcon();
            if (callsInfoModel.getType().equals(AppConstants.VIDEO_CALL))
                callsViewHolder.setCallType(mActivity.getString(R.string.missed_video_call));
            else
                callsViewHolder.setCallType(mActivity.getString(R.string.missed_voice_call));
        } else {
            callsViewHolder.hideIcon();
            if (callsInfoModel.getType().equals(AppConstants.VIDEO_CALL))
                callsViewHolder.setCallType(mActivity.getString(R.string.outgoing_video_call));
            else
                callsViewHolder.setCallType(mActivity.getString(R.string.outgoing_voice_call));
        }


        if (callsInfoModel.getDate() != null) {
            callsViewHolder.setCallDate(callsInfoModel.getDate());
        }

        if (callsInfoModel.getDuration() != null) {
            callsViewHolder.setDurationCall(callsInfoModel.getDuration());
        }else {
            callsViewHolder.setDurationCall("00:00");
        }
    }


    @Override
    public int getItemCount() {
        if (callsModelList != null) return callsModelList.size();
        return 0;
    }


    public class CallsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.call_type)
        TextView callType;

        @BindView(R.id.icon_made)
        AppCompatImageView IconMade;

        @BindView(R.id.icon_received)
        AppCompatImageView IconReceived;

        @BindView(R.id.date_call)
        TextView CallDate;

        @BindView(R.id.duration_call)
        TextView durationCall;

        public CallsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }


        void hideIcon() {
            IconMade.setVisibility(View.VISIBLE);
            IconReceived.setVisibility(View.GONE);
        }

        void showIcon() {
            IconMade.setVisibility(View.GONE);
            IconReceived.setVisibility(View.VISIBLE);
        }


        @SuppressLint({"StaticFieldLeak", "CheckResult"})
        void setCallDate(String date) {
            DateTime messageDate = UtilsTime.getCorrectDate(date);
            CallDate.setText(UtilsTime.convertDateToStringFormat(mActivity, messageDate));
        }

        void setDurationCall(String duration) {
            durationCall.setText(duration);
        }

        void setCallType(String type) {
            callType.setText(type);
        }


    }
}
