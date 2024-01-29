package com.fm.fmmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.data.Word
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessTokenDao {
    @Query("SELECT * FROM fm_access_token")
    fun getAccessToken(): Flow<List<AccessToken>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(accessToken: AccessToken)

    @Query("DELETE FROM fm_access_token WHERE id = :id")
    suspend fun deleteAll(id:Int)
}