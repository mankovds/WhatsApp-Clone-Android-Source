package com.strolink.whatsUp.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.users.status.StatusModel;

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
public interface StatusDao {

    @Query("SELECT * FROM status WHERE userId = :userId ORDER BY created DESC")
    Single<List<StatusModel>> loadAllStatus(String userId);

    @Query("SELECT * FROM status WHERE userId = :userId ORDER BY created DESC")
    List<StatusModel> loadAllUserStatusByUserId(String userId);

    @Query("SELECT * FROM status WHERE userId = :userId AND current = 1 ORDER BY created DESC")
    Single<StatusModel> loadCurrentStatus(String userId);

    @Query("SELECT * FROM status WHERE userId = :userId  ORDER BY created DESC")
    StatusModel loadUserStatusByUserId(String userId);

    @Query("SELECT * FROM status WHERE s_id = :statusId  ORDER BY created DESC")
    StatusModel loadUserStatusById(String statusId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StatusModel> statusModels);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StatusModel statusModel);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(StatusModel statusModel);

    @Delete
    void delete(StatusModel statusModel);

    @Delete
    void deleteAllOldStatus(List<StatusModel> statusModel);

    @Query("SELECT * FROM status WHERE s_id = :id")
    StatusModel loadStatusById(String id);

    @Query("SELECT COUNT(s_id) FROM  status WHERE s_id = :id ")
    int statusExistence(String id);
}
