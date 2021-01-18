package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.users.contacts.UsersBlockModel;

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
public interface UsersBlockedDao {


    @Query("SELECT * FROM users_blocked WHERE exist = 1 AND linked = 1 AND activate = 1 AND _id != :userId ORDER BY activate DESC ,displayed_name ASC ,linked DESC")
    Single<List<UsersBlockModel>> loadBlockedContacts(String userId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UsersBlockModel usersBlockModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UsersBlockModel... users);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UsersBlockModel usersBlockModel);

    @Delete
    void delete(UsersBlockModel usersBlockModel);

    @Query("SELECT * FROM users_blocked WHERE _id = :id")
    UsersBlockModel loadUserBlockedById(String id);

    @Query("SELECT COUNT(_id) FROM  users_blocked WHERE _id = :id ")
    int userExistence(String id);
}