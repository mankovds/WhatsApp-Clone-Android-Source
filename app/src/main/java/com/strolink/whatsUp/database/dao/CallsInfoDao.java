package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.calls.CallsInfoModel;

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
public interface CallsInfoDao {

    @Query("SELECT * FROM calls_info   ORDER BY date DESC ")
    Single<List<CallsInfoModel>> loadAllCallsInfo();


    @Query("SELECT * FROM calls_info WHERE  callId = :callId   ORDER BY date DESC  ")
    Single<List<CallsInfoModel>> loadAllCallsInfo(String callId);

    @Query("SELECT * FROM calls_info WHERE  callId = :callId   ORDER BY date DESC  ")
    List<CallsInfoModel> loadAllUserCallsInfo(String callId);


    @Query("SELECT * FROM calls_info WHERE  displayed_name LIKE '%' || :query  || '%' ORDER BY date DESC  ")
    List<CallsInfoModel> loadAllCallInfoQuery(String query);

    @Query("SELECT * FROM calls_info WHERE  callId = :callId   ORDER BY date DESC  ")
    List<CallsInfoModel> loadAllCallInfoByCallId(String callId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CallsInfoModel callsInfoModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(CallsInfoModel... callsInfoModels);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(CallsInfoModel callsInfoModel);

    @Delete
    void delete(CallsInfoModel callsInfoModel);

    @Query("SELECT * FROM calls_info WHERE ci_id = :id")
    Single<CallsInfoModel> loadCallInfoById(String id);

    @Query("SELECT * FROM calls_info WHERE ci_id = :id")
    CallsInfoModel loadSingleCallInfoById(String id);

    @Query("SELECT * FROM calls_info WHERE callId = :id")
    CallsInfoModel loadSingleCallInfoByCallId(String id);

    @Query("SELECT * FROM calls_info WHERE `from` = :fromId AND `to` = :toId")
    CallsInfoModel loadSingleCallInfoByFromToId(String fromId, String toId);

    @Query("SELECT COUNT(ci_id) FROM  calls_info WHERE ci_id = :id ")
    int callInfoExistence(String id);
}