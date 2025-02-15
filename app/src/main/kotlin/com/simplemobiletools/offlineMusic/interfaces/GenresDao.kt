package com.simplemobiletools.offlineMusic.interfaces

import androidx.room.*
import com.simplemobiletools.offlineMusic.models.Genre

@Dao
interface GenresDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(genre: Genre): Long

    @Query("SELECT * FROM genres")
    fun getAll(): List<Genre>

    @Query("DELETE FROM genres WHERE id = :id")
    fun deleteGenre(id: Long)
}
