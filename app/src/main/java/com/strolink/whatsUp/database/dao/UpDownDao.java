package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.UploadInfo;

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
public interface UpDownDao {


    @Query("SELECT * FROM files_up_down_manager   ORDER BY uploadId ASC ")
    List<UploadInfo> loadAllFiles();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UploadInfo uploadInfo);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(UploadInfo... uploadInfo);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UploadInfo uploadInfo);

    @Delete
    void delete(UploadInfo uploadInfo);

    @Query("SELECT * FROM files_up_down_manager WHERE uploadId = :id")
    Single<UploadInfo> loadFileById(String id);

    @Query("SELECT * FROM files_up_down_manager WHERE uploadId = :id")
    UploadInfo loadSingleFileById(String id);

    @Query("SELECT uploadId FROM files_up_down_manager WHERE uploadId = :id  ")
    String loadSingleFileId(String id);

    @Query("SELECT COUNT(uploadId) FROM  files_up_down_manager WHERE uploadId = :id ")
    int fileExistence(String id);
}