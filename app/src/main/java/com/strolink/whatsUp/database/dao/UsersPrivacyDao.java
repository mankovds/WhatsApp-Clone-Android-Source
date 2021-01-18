package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.users.contacts.UsersPrivacyModel;

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
public interface UsersPrivacyDao {

    @Query("SELECT * FROM users_stories_privacy   ORDER BY up_id DESC ")
    Single<List<UsersPrivacyModel>> loadAllUsers();

    @Query("SELECT * FROM users_stories_privacy WHERE exclude = 0  ORDER BY up_id DESC ")
    List<UsersPrivacyModel> loadAllUsersPrivacy();


    @Query("SELECT * FROM users_stories_privacy WHERE  displayed_name LIKE '%' || :query  || '%' ORDER BY up_id DESC  ")
    List<UsersPrivacyModel> loadAllUsersPrivacyQuery(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UsersPrivacyModel usersPrivacyModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UsersPrivacyModel... usersPrivacyModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UsersPrivacyModel usersPrivacyModel);

    @Delete
    void delete(UsersPrivacyModel usersPrivacyModel);

    @Query("SELECT * FROM users_stories_privacy WHERE up_id = :id")
    Single<UsersPrivacyModel> loadUserPrivacyById(String id);

    @Query("SELECT * FROM users_stories_privacy WHERE up_id = :id")
    UsersPrivacyModel loadSingleUserPrivacyById(String id);

    @Query("SELECT up_id FROM users_stories_privacy WHERE up_id = :id  ")
    String loadSingleUserPrivacyId(String id);

    @Query("SELECT COUNT(up_id) FROM  users_stories_privacy WHERE up_id = :id ")
    int userPrivacyExistence(String id);
}