//package com.bluebirdcorp.managashelfrev.paging
//
//import androidx.paging.PagingSource
//import androidx.paging.PagingState
//import com.bluebirdcorp.managashelfrev.api.MangaApiService
//import com.bluebirdcorp.managashelfrev.model.Manga
//import retrofit2.HttpException
//import java.io.IOException
//
//class MangaPagingSource(
//    private val apiService: MangaApiService
//) : PagingSource<Int, Manga>() {
//
//    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Manga> {
//        val page = params.key ?: 1 // Start from page 1
//        return try {
//            val response = apiService.getMangaList(page, params.loadSize)
//            if (response.isSuccessful) {
//                val mangaList = response.body() ?: emptyList()
//                LoadResult.Page(
//                    data = mangaList,
//                    prevKey = if (page == 1) null else page - 1, // Previous page
//                    nextKey = if (mangaList.isEmpty()) null else page + 1 // Next page
//                )
//            } else {
//                LoadResult.Error(Exception("API Error: ${response.message()}"))
//            }
//        } catch (e: IOException) {
//            LoadResult.Error(e) // Network error
//        } catch (e: HttpException) {
//            LoadResult.Error(e) // API error
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Manga>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
//                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
//        }
//    }
//}
