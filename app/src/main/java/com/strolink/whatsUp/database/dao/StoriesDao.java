package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.stories.StoriesModel;

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
public interface StoriesDao {

    @Query("SELECT * FROM stories   ORDER BY downloaded ASC ")
    Single<List<StoriesModel>> loadAllStories();

    @Query("SELECT * FROM stories   ORDER BY downloaded ASC ")
    List<StoriesModel> loadAllUserStories();


    @Query("SELECT * FROM stories WHERE  username LIKE '%' || :query  || '%' ORDER BY downloaded ASC  ")
    List<StoriesModel> loadAllStoriesQuery(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoriesModel storiesModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(StoriesModel... storiesModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(StoriesModel storiesModel);

    @Delete
    void delete(StoriesModel storiesModel);

    @Query("SELECT * FROM stories WHERE _id = :id")
    Single<StoriesModel> loadStoriesById(String id);

    @Query("SELECT * FROM stories WHERE _id = :id")
    StoriesModel loadSingleStoriesById(String id);

    @Query("SELECT _id FROM stories WHERE _id = :id  ")
    String loadSingleStoriesId(String id);

    @Query("SELECT COUNT(_id) FROM  stories WHERE _id = :id ")
    int storiesExistence(String id);
}