package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.stories.StoriesHeaderModel;

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
public interface StoriesMineDao {

    @Query("SELECT * FROM stories_mine   ORDER BY id DESC ")
    Single<List<StoriesHeaderModel>> loadAllStoriesMine();

    @Query("SELECT * FROM stories_mine   ORDER BY id DESC ")
    List<StoriesHeaderModel> loadAllUserStoriesMine();


    @Query("SELECT * FROM stories_mine WHERE  username LIKE '%' || :query  || '%' ORDER BY id DESC  ")
    List<StoriesHeaderModel> loadAllStoriesMineQuery(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StoriesHeaderModel storiesHeaderModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(StoriesHeaderModel... storiesHeaderModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(StoriesHeaderModel storiesHeaderModel);

    @Delete
    void delete(StoriesHeaderModel storiesHeaderModel);

    @Query("SELECT * FROM stories_mine WHERE _id = :id")
    Single<StoriesHeaderModel> loadStoriesMineById(String id);

    @Query("SELECT * FROM stories_mine WHERE _id = :id")
    StoriesHeaderModel loadSingleStoriesMineById(String id);

    @Query("SELECT * FROM stories_mine ")
    Single<StoriesHeaderModel> loadSingleStoriesMine();

    @Query("SELECT * FROM stories_mine WHERE _id = :id")
    StoriesHeaderModel loadSingleStoriesMine(String id);

    @Query("SELECT _id FROM stories_mine WHERE _id = :id  ")
    String loadSingleStoriesMineId(String id);

    @Query("SELECT COUNT(_id) FROM  stories_mine WHERE _id = :id ")
    int storiesMineExistence(String id);
}