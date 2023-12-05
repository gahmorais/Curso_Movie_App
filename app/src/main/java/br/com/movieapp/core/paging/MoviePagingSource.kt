package br.com.movieapp.core.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import br.com.movieapp.core.domain.model.Movie
import br.com.movieapp.movie_popular_feature.data.mapper.toMovie
import br.com.movieapp.movie_popular_feature.domain.source.MoviePopularRemoteDataSource
import retrofit2.HttpException
import java.io.IOException

class MoviePagingSource(private val remoteDataSource: MoviePopularRemoteDataSource) :
  PagingSource<Int, Movie>() {
  override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
    return null
  }

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
    return try {
      val pagenumber = params.key ?: 1
      val response = remoteDataSource.getPopularmovies(page = pagenumber)
      val movies = response.results

      LoadResult.Page(
        data = movies.toMovie(),
        prevKey = if (pagenumber == 1) null else pagenumber - 1,
        nextKey = if (movies.isEmpty()) null else pagenumber + 1
      )
    } catch (e: IOException) {
      e.printStackTrace()
      return LoadResult.Error(e)
    } catch (e: HttpException) {
      e.printStackTrace()
      return LoadResult.Error(e)
    }
  }

  companion object {
    private const val LIMIT = 20
  }
}