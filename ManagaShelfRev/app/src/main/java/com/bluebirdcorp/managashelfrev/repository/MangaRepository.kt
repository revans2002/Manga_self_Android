package com.bluebirdcorp.managashelfrev.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.bluebirdcorp.managashelfrev.api.MangaApi
import com.bluebirdcorp.managashelfrev.api.PagedResponse
import com.bluebirdcorp.managashelfrev.db.MangaDao
import com.bluebirdcorp.managashelfrev.model.Manga
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

class MangaRepository(private val mangaDao: MangaDao) {

    fun getMangas(page: Int, pageSize: Int): Flow<List<Manga>> = flow {
        try {
            var currentPage = page
            val offset = (page - 1) * pageSize
            val newMangas = mutableListOf<Manga>()
            val existingMangas = mangaDao.getAllMangas().first().map { it.id }

            while (newMangas.size < pageSize) {
                // Fetch data from API
                val response = MangaApi.retrofitService.getMangaList(currentPage, pageSize)

                if (response.isSuccessful) {
                    val mangas = response.body() ?: emptyList()

                    // Filter only new mangas
                    val freshMangas = mangas.filter { it.id !in existingMangas }

                    if (freshMangas.isNotEmpty()) {
                        newMangas.addAll(freshMangas)
                        mangaDao.insertAll(freshMangas)
                    }

                    // Break if API returns fewer mangas than requested (meaning no more data)
                    if (mangas.size < pageSize) break
                } else {
                    Log.e("MangaRepository", "API call failed: ${response.errorBody()}")
                    break
                }

                // Move to the next page
                currentPage++
            }

            Log.d("TAG", "Fetched ${newMangas.size} new mangas for page $page")

            // Emit the required page of data from the database
            emit(mangaDao.getMangasPaginated(pageSize, offset).first())
        } catch (e: IOException) {
            Log.e("MangaRepository", "Network error: ${e.message}")
            emit(emptyList())
        } catch (e: HttpException) {
            Log.e("MangaRepository", "HTTP error: ${e.message}")
            emit(emptyList())
        }
    }


    // Get paginated manga data from the database
    fun getMangasFromDb(page: Int, pageSize: Int): Flow<PagedResponse<Manga>> = flow {
        val totalMangaCount = mangaDao.getMangaCount()
        val totalPages = (totalMangaCount + pageSize - 1) / pageSize
        val offset = (page - 1) * pageSize

        val mangas = mangaDao.getMangasPaginated(pageSize, offset).first()
        emit(PagedResponse(mangas, page, totalPages))
    }

    suspend fun updateManga(manga: Manga) {
        mangaDao.updateManga(manga)
    }

    fun getLikedManga(): LiveData<List<Manga>> {
        return mangaDao.getLikedManga()
    }

    fun getAllMangasWithoutPaging(): Flow<List<Manga>> {
        return mangaDao.getAllMangas()
    }

}