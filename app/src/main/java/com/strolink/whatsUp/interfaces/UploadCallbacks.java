package com.strolink.whatsUp.interfaces;


import com.strolink.whatsUp.models.messages.MessageModel;

/**
 * Created by Abderrahim El imame on 7/28/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/bencherif_el
 */

public interface UploadCallbacks {

    void onStart(String type, String messageId);

    void onUpdate(int percentage, String type, String messageId);

    void onError(String type, String messageId);


    void onFinish(String type, MessageModel messagesModel);

}
