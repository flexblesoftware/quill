package com.shahid.quill.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shahid.quill.app.data.local.entity.UsageCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageCacheDao {
    @Query("SELECT * FROM usage_cache WHERE id = 1 LIMIT 1")
    fun getUsageCache(): Flow<UsageCacheEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUsageCache(cache: UsageCacheEntity)

    @Query("DELETE FROM usage_cache")
    suspend fun clear()
}
