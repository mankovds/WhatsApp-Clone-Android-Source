package com.strolink.whatsUp.interfaces;


import com.strolink.whatsUp.models.messages.MessageModel;

/**
 * Created by Abderrahim El imame on 7/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface DownloadCallbacks {

    void onStartDownload(String type, String messageId);

    void onUpdateDownload(int percentage, String type, String messageId);

    void onErrorDownload(String type, String messageId);


    void onFinishDownload(String type, MessageModel messagesModel);


}
