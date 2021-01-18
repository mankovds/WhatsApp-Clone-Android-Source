package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.users.contacts.UsersModel;

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
public interface UserDao {

    @Query("SELECT * FROM users WHERE exist = 1 AND _id != :userId ORDER BY activate DESC ,displayed_name ASC ,linked DESC")
    Single<List<UsersModel>> loadAllUsers(String userId);

    @Query("SELECT * FROM users WHERE exist = 1 AND linked = 1 AND activate = 1 AND _id != :userId ORDER BY activate DESC ,displayed_name ASC ,linked DESC")
    Single<List<UsersModel>> loadLinkedContacts(String userId);

    @Query("SELECT * FROM users WHERE exist = 1 AND linked = 1 AND activate = 1 AND _id != :userId ORDER BY activate DESC ,displayed_name ASC ,linked DESC")
    List<UsersModel> loadAllLinkedUsers(String userId);

    @Query("SELECT  COUNT(_id)  FROM users WHERE exist = 1 AND linked = 1 AND activate = 1 AND _id != :userId ORDER BY activate DESC ,displayed_name ASC ,linked DESC")
    int loadLinkedContactsSize(String userId);

    @Query("SELECT * FROM users WHERE   _id != :userId AND displayed_name LIKE '%' || :query  || '%' ORDER BY activate DESC , displayed_name ASC , linked DESC")
    List<UsersModel> loadAllUsersQuery(String userId, String query);

    @Query("SELECT * FROM users WHERE exist = 1 AND linked = 1 AND activate = 1 AND _id != :userId AND displayed_name LIKE '%' || :query  || '%' ORDER BY activate DESC , displayed_name ASC , linked DESC")
    List<UsersModel> loadAllLinkedUsersQuery(String userId, String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UsersModel usersModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UsersModel... users);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UsersModel usersModel);

    @Delete
    void delete(UsersModel usersModel);

    @Query("SELECT * FROM users WHERE _id = :id")
    Observable<UsersModel> loadUserById(String id);

    @Query("SELECT * FROM users WHERE _id = :id")
    UsersModel loadSingleUserById(String id);

    @Query("SELECT COUNT(_id) FROM  users WHERE _id = :id ")
    int userExistence(String id);
}