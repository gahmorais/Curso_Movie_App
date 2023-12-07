package br.com.movieapp.core.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import br.com.movieapp.core.domain.model.MovieSearch
import br.com.movieapp.search_movie_feature.data.mapper.toMovieSearch
import br.com.movieapp.search_movie_feature.domain.source.MovieSearchRemoteDataSource
import retrofit2.HttpException
import java.io.IOException

class MovieSearchPagingSource(
  private val query: String,
  private val remoteDataSource: MovieSearchRemoteDataSource
) : PagingSource<Int, MovieSearch>() {
  override fun getRefreshKey(state: PagingState<Int, MovieSearch>): Int? {
    return state.anchorPosition?.let { anchorPosition ->
      val anchorPage = state.closestPageToPosition(anchorPosition)
      anchorPage?.prevKey?.plus(LIMIT) ?: anchorPage?.nextKey?.minus(LIMIT)
    }
  }

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieSearch> {
    return try {
      val pagenumber = params.key ?: 1
      val response = remoteDataSource.getSearchMovies(page = pagenumber, query = query)
      val movies = response.results
      LoadResult.Page(
        data = movies.toMovieSearch(),
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
