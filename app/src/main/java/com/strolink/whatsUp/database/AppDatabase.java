package com.strolink.whatsUp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.strolink.whatsUp.app.AppConstants;
import com.strolink.whatsUp.database.dao.CallsDao;
import com.strolink.whatsUp.database.dao.CallsInfoDao;
import com.strolink.whatsUp.database.dao.ChatsDao;
import com.strolink.whatsUp.database.dao.GroupDao;
import com.strolink.whatsUp.database.dao.MembersDao;
import com.strolink.whatsUp.database.dao.MessageStatusDao;
import com.strolink.whatsUp.database.dao.MessagesDao;
import com.strolink.whatsUp.database.dao.StatusDao;
import com.strolink.whatsUp.database.dao.StoriesDao;
import com.strolink.whatsUp.database.dao.StoriesDetailsDao;
import com.strolink.whatsUp.database.dao.StoriesMineDao;
import com.strolink.whatsUp.database.dao.StoriesSeenDao;
import com.strolink.whatsUp.database.dao.UpDownDao;
import com.strolink.whatsUp.database.dao.UserDao;
import com.strolink.whatsUp.database.dao.UsersBlockedDao;
import com.strolink.whatsUp.database.dao.UsersPrivacyDao;
import com.strolink.whatsUp.models.calls.CallsInfoModel;
import com.strolink.whatsUp.models.calls.CallsModel;
import com.strolink.whatsUp.models.messages.ConversationModel;
import com.strolink.whatsUp.models.groups.GroupModel;
import com.strolink.whatsUp.models.groups.MembersModel;
import com.strolink.whatsUp.models.messages.MessageModel;
import com.strolink.whatsUp.models.messages.MessageStatus;
import com.strolink.whatsUp.models.users.status.StatusModel;
import com.strolink.whatsUp.models.stories.StoriesHeaderModel;
import com.strolink.whatsUp.models.stories.StoriesModel;
import com.strolink.whatsUp.models.stories.StoryModel;
import com.strolink.whatsUp.models.stories.StorySeen;
import com.strolink.whatsUp.models.UploadInfo;
import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;
import com.strolink.whatsUp.models.users.contacts.UsersModel;
import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@Database(entities =
        {UsersModel.class,
                UsersBlockModel.class,
                StatusModel.class,
                ConversationModel.class,
                MessageModel.class,
                GroupModel.class,
                MembersModel.class,
                CallsModel.class,
                CallsInfoModel.class,
                UsersPrivacyModel.class,
                StoriesModel.class,
                StoriesHeaderModel.class,
                StoryModel.class,
                StorySeen.class,
                UploadInfo.class,
                MessageStatus.class
        }, version = AppConstants.DatabaseVersion, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String LOG_TAG = AppDatabase.class.getSimpleName();

    private static final String DATABASE_NAME = AppConstants.DatabaseName;
    private static volatile AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
                synchronized (AppDatabase.class) {
                    if (sInstance == null) {
                        sInstance = Room.databaseBuilder(context.getApplicationContext(),
                                AppDatabase.class, AppDatabase.DATABASE_NAME)
                                .fallbackToDestructiveMigration()
                                .build();
                    }
            }
        }
        return sInstance;
    }


    public static void destroyInstance() {
        sInstance = null;
    }


    public abstract UserDao userDao();


    public abstract CallsDao callsDao();


    public abstract CallsInfoDao callsInfoDao();


    public abstract UsersPrivacyDao usersPrivacyDao();


    public abstract StoriesDao storiesDao();


    public abstract StoriesMineDao storiesMineDao();

    public abstract StoriesDetailsDao storiesDetailsDao();

    public abstract StoriesSeenDao storiesSeenDao();

    public abstract UpDownDao upDownDao();


    public abstract UsersBlockedDao usersBlockedDao();


    public abstract StatusDao statusDao();


    public abstract ChatsDao chatsDao();


    public abstract MessagesDao messagesDao();


    public abstract MessageStatusDao messageStatusDao();


    public abstract GroupDao groupDao();

    public abstract MembersDao membersDao();
}