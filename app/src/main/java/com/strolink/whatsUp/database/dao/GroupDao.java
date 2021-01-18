package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.groups.GroupModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * Created by Abderrahim El imame on 2019-07-26.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@Dao
public interface GroupDao {

    @Query("SELECT * FROM groups ORDER BY _id")
    Single<List<GroupModel>> loadAllgroups();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GroupModel groupModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(GroupModel... groupModels);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(GroupModel groupModel);

    @Delete
    void delete(GroupModel groupModel);

    @Query("SELECT * FROM groups WHERE _id = :id")
    Observable<GroupModel> loadGroupById(String id);

    @Query("SELECT * FROM groups WHERE _id = :id")
     GroupModel loadSingleGroupById(String id);


    @Query("SELECT COUNT(_id) FROM  groups WHERE _id = :id ")
    int groupExistence(String id);
}