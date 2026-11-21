package com.shahid.quill.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shahid.quill.app.data.local.entity.GeneratedImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY createdAt DESC")
    fun getAllImages(): Flow<List<GeneratedImageEntity>>

    @Query("SELECT * FROM generated_images WHERE id = :id LIMIT 1")
    suspend fun getImageById(id: String): GeneratedImageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: GeneratedImageEntity)

    @Update
    suspend fun update(image: GeneratedImageEntity)

    @Query("DELETE FROM generated_images WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM generated_images")
    suspend fun deleteAll()
}
