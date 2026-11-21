package com.shahid.quill.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shahid.quill.app.data.local.entity.PermissionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PermissionLogDao {
    @Query("SELECT * FROM permission_logs ORDER BY timestamp DESC LIMIT 200")
    fun getRecentLogs(): Flow<List<PermissionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: PermissionLogEntity)

    @Query("DELETE FROM permission_logs")
    suspend fun deleteAll()
}
