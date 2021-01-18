package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.calls.CallsModel;

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
public interface CallsDao {

    @Query("SELECT * FROM calls GROUP BY `to`,received,type  ORDER BY date  DESC ")
    Single<List<CallsModel>> loadAllCalls();

    @Query("SELECT * FROM calls   ORDER BY date DESC ")
    List<CallsModel> loadAllUserCalls();


    @Query("SELECT * FROM calls WHERE  displayed_name LIKE '%' || :query  || '%' ORDER BY date DESC  ")
    List<CallsModel> loadAllCallQuery(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CallsModel callsModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CallsModel... callsModels);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(CallsModel callsModel);

    @Delete
    void delete(CallsModel callsModel);

    @Query("SELECT * FROM calls WHERE c_id = :id")
    Single<CallsModel> loadCallById(String id);

    @Query("SELECT * FROM calls WHERE c_id = :id")
    CallsModel loadSingleCallById(String id);

    @Query("SELECT c_id FROM calls WHERE `to` = :to AND `from` = :from AND type = :type AND received = :received")
    String loadSingleCallId(String to, String from, String type, int received);

    @Query("SELECT COUNT(c_id) FROM  calls WHERE c_id = :id ")
    int callExistence(String id);
}