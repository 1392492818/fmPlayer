package com.fm.fmmedia.repository

import androidx.annotation.WorkerThread
import com.fm.fmmedia.dao.AccessTokenDao
import com.fm.fmmedia.data.AccessToken
import com.fm.fmmedia.data.Word
import kotlinx.coroutines.flow.Flow

class AccessTokenRepository(private val accessTokenDao: AccessTokenDao) :BaseRepository(){
    val accessTokenList: Flow<List<AccessToken>> = accessTokenDao.getAccessToken()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(accessToken: AccessToken) {
        accessTokenDao.insert(accessToken)
    }

    suspend fun delete(id: Int){
        accessTokenDao.deleteAll(id)
    }
}