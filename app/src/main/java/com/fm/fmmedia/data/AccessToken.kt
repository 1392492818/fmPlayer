package com.fm.fmmedia.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fm_access_token")
class AccessToken(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "accessToken") val accessToken: String,
    @ColumnInfo(name = "expiresTime") val expiresTime: Long,
    @ColumnInfo(name = "refreshToken") val refreshToken: String,
    @ColumnInfo(name = "userId") val userId: Int,
    @ColumnInfo(name = "status") val status: Int
)