package com.bluebirdcorp.managashelfrev.api

import com.bluebirdcorp.managashelfrev.model.Manga
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MangaApiService {
    @GET("b/KEJO")
    suspend fun getMangaList(
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<List<Manga>>
}
