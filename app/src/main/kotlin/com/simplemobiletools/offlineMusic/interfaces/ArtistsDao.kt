package com.simplemobiletools.offlineMusic.interfaces

import androidx.room.*
import com.simplemobiletools.offlineMusic.models.Artist

@Dao
interface ArtistsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(artist: Artist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(artists: List<Artist>)

    @Query("SELECT * FROM artists")
    fun getAll(): List<Artist>

    @Query("DELETE FROM artists WHERE id = :id")
    fun deleteArtist(id: Long)
}
