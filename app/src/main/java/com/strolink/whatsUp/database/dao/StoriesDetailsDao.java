package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.stories.StoryModel;

import java.util.List;

import io.reactivex.Single;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@Dao
public interface StoriesDetailsDao {

    @Query("SELECT * FROM stories_details WHERE deleted = 0 AND userId = :userId    ORDER BY date ASC ")
    Single<List<StoryModel>> loadAllStoriesDetails(String userId);

    @Query("SELECT * FROM stories_details WHERE deleted = 0     ORDER BY date ASC ")
    List<StoryModel> loadAllStoryNotDeleted();


    @Query("SELECT * FROM stories_details WHERE deleted = 0 AND userId = :userId    ORDER BY date ASC ")
    List<StoryModel> loadAllStoryNotDeleted(String userId);

    @Query("SELECT * FROM stories_details WHERE userId = :ownerId  ORDER BY date ASC ")
    List<StoryModel> loadAllUserStoriesDetails(String ownerId);

    @Query("SELECT * FROM stories_details WHERE status = 0 AND userId = :userId ORDER BY date ASC ")
    Single<List<StoryModel>> loadAllUserWaitingStoriesDetails(String userId);

    @Query("SELECT * FROM stories_details WHERE deleted = 0  AND date <:expire_date ORDER BY date ASC ")
    List<StoryModel> loadAllExpiredStories(String expire_date);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoryModel storyModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(StoryModel... storyModels);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(StoryModel storyModel);

    @Delete
    void delete(StoryModel storyModel);

    @Query("SELECT * FROM stories_details WHERE _id = :id")
    Single<StoryModel> loadStoriesDetailsById(String id);

    @Query("SELECT * FROM stories_details WHERE _id = :id")
    StoryModel loadSingleStoriesDetailsById(String id);

    @Query("SELECT * FROM stories_details WHERE id = :id")
    StoryModel loadSingleStoriesDetailsByLongId(long id);

    @Query("SELECT _id FROM stories_details WHERE _id = :id  ")
    String loadSingleStoriesDetailsId(String id);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE _id = :id AND deleted = 0")
    int storiesDetailsExistence(String id);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE userId = :userId AND deleted = 0")
    int storiesDetailsMineExistence(String userId);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE _id = :id AND status = 0")
    int storiesDetailsExistenceWaiting(String id);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE userId = :userId  AND downloaded = 0  AND deleted = 0")
    int storiesDetailsExistenceDownload(String userId);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE userId = :userId  AND uploaded = 0 AND deleted = 0")
    int storiesDetailsExistenceUpload(String userId);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE userId = :userId AND _id = :storyId  AND uploaded = 0 AND deleted = 0 ")
    int storiesDetailsExistenceUpload(String userId, String storyId);

    @Query("SELECT COUNT(_id) FROM  stories_details WHERE  userId = :storyId  AND downloaded = 1 AND deleted = 0 ")
    int getStoryDownloadSize(String storyId);


    @Query("SELECT COUNT(_id) FROM stories_details WHERE deleted = 0   ORDER BY date ASC ")
    int loadExistStoryNotDeleted();
}