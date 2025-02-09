package com.bluebirdcorp.managashelfrev.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bluebirdcorp.managashelfrev.model.Manga
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga_table ORDER BY publishedChapterDate ASC")
    fun getAllMangas(): Flow<List<Manga>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mangas: List<Manga>)

    @Query("DELETE FROM manga_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM manga_table ORDER BY id LIMIT :pageSize OFFSET :offset")
    fun getMangasPaginated(pageSize: Int, offset: Int): Flow<List<Manga>>

    @Query("SELECT COUNT(*) FROM manga_table")
    suspend fun getMangaCount(): Int

    @Update
    suspend fun updateManga(manga: Manga)

    @Query("SELECT * FROM manga_table WHERE isLiked==1")
    fun getLikedManga(): LiveData<List<Manga>>


}
