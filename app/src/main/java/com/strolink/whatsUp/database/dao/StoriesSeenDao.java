package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.stories.StorySeen;

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
public interface StoriesSeenDao {

    @Query("SELECT * FROM story_seen WHERE storyId = :storyId  ORDER BY id ASC ")
    Single<List<StorySeen>> loadAllStorySeen(String storyId);

    @Query("SELECT * FROM story_seen   ORDER BY id ASC ")
    List<StorySeen> loadAllUserStorySeen();


    @Insert()
    void insert(StorySeen storySeen);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(StorySeen... storySeen);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(StorySeen storySeen);

    @Delete
    void delete(StorySeen storySeen);

    @Query("SELECT * FROM story_seen WHERE userId = :id")
    Single<StorySeen> loadStorySeenById(String id);

    @Query("SELECT * FROM story_seen WHERE userId = :id")
    StorySeen loadSingleStorySeenById(String id);

    @Query("SELECT userId FROM story_seen WHERE userId = :id  ")
    String loadSingleStorySeenId(String id);

    @Query("SELECT COUNT(id) FROM  story_seen WHERE userId = :id ")
    int storySeenExistence(String id);

    @Query("SELECT COUNT(id) FROM story_seen WHERE storyId = :storyId  ORDER BY id ASC ")
    int loadAllStorySeenCounter(String storyId);
}