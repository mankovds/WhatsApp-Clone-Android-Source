package com.strolink.whatsUp.api.apiServices;

import com.strolink.whatsUp.BuildConfig;
import com.strolink.whatsUp.api.APIContact;
import com.strolink.whatsUp.api.APIService;
import com.strolink.whatsUp.app.WhatsCloneApplication;
import com.strolink.whatsUp.database.AppDatabase;
import com.strolink.whatsUp.models.messages.ConversationModel;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Abderrahim El imame on 20/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsService {

    private APIContact apiContact;
    private APIService mApiService;


    public ConversationsService(APIService mApiService) {

        this.mApiService = mApiService;
    }


    private APIContact initializeApiConversations() {
        if (apiContact == null) {
            apiContact = this.mApiService.RootService(APIContact.class, BuildConfig.BACKEND_BASE_URL);
        }
        return apiContact;
    }

    /**
     * method to get Conversations list
     *
     * @return return value
     */
    public Single<List<ConversationModel>> getConversations() {

        return AppDatabase.getInstance(WhatsCloneApplication.getInstance()).chatsDao().loadAllChats().subscribeOn(Schedulers.computation());

    }


}
