package com.strolink.whatsUp.database.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.strolink.whatsUp.models.groups.MembersModel;

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
public interface MembersDao {

    @Query("SELECT * FROM members WHERE groupId = :id ORDER BY _id")
    Single<List<MembersModel>> loadAllMembers(String id);

    @Query("SELECT * FROM members WHERE groupId = :id   ORDER BY _id")
    List<MembersModel> loadAllGroupMembers(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MembersModel membersModel);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(MembersModel... membersModels);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(MembersModel membersModel);

    @Delete
    void delete(MembersModel membersModel);

    @Query("SELECT * FROM members WHERE _id = :id")
    Single<MembersModel> loadMemberById(String id);

    @Query("SELECT * FROM members WHERE _id = :id")
    MembersModel loadSingleMemberById(String id);

    @Query("SELECT * FROM members WHERE ownerId = :id")
    MembersModel loadSingleMemberByOwnerId(String id);

    @Query("SELECT * FROM members WHERE groupId=:groupId AND ownerId = :id")
    MembersModel loadSingleMemberByOwnerIdAndGroupId(String groupId, String id);

    @Query("SELECT COUNT(_id) FROM  members WHERE  groupId =:groupId AND _id = :id AND `left` = 0 ")
    int memberExistence(String id, String groupId);

    @Query("SELECT COUNT(_id) FROM  members WHERE  groupId =:groupId AND ownerId = :id AND `left` = 0 ")
    int userIsMemberExistence(String id, String groupId);

    @Query("SELECT COUNT(_id) FROM  members WHERE   groupId =:groupId AND ownerId = :id AND `left` = 1 ")
    int memberIsLeft(String id, String groupId);

    @Query("SELECT COUNT(_id) FROM  members WHERE  groupId =:groupId")
    int groupMemberCount(String groupId);
}