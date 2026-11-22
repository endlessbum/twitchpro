package com.endlessbum.twitchpro.repository.datasource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.endlessbum.twitchpro.model.ui.User
import com.endlessbum.twitchpro.repository.GraphQLRepository
import com.endlessbum.twitchpro.repository.HelixRepository
import com.endlessbum.twitchpro.util.C

class SearchChannelsDataSource(
    private val query: String,
    private val gqlHeaders: Map<String, String>,
    private val graphQLRepository: GraphQLRepository,
    private val helixHeaders: Map<String, String>,
    private val helixRepository: HelixRepository,
    private val enableIntegrity: Boolean,
    private val networkLibrary: String?,
) : PagingSource<Int, User>() {
    private var api: String? = null
    private var offset: String? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        return if (query.isBlank()) {
            LoadResult.Page(
                data = emptyList(),
                prevKey = null,
                nextKey = null
            )
        } else {
            if (!offset.isNullOrBlank()) {
                try {
                    loadFromApi(params)
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    DataSourceLog.log(e)
                    LoadResult.Error(e)
                }
            } else {
                try {
                    api = C.GQL
                    loadFromApi(params)
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                } catch (e: Exception) {
                    DataSourceLog.log(e)
                    try {
                        api = C.GQL_PERSISTED_QUERY
                        loadFromApi(params)
                    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        DataSourceLog.log(e)
                        try {
                            api = C.HELIX
                            loadFromApi(params)
                        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            DataSourceLog.log(e)
                            LoadResult.Error(e)
                        }
                    }
                }
            }
        }
    }

    private suspend fun loadFromApi(params: LoadParams<Int>): LoadResult<Int, User> {
        return when (api) {
            C.GQL -> gqlQueryLoad(params)
            C.GQL_PERSISTED_QUERY -> gqlLoad(params)
            C.HELIX -> if (!helixHeaders[C.HEADER_TOKEN].isNullOrBlank()) helixLoad(params) else throw Exception()
            else -> throw Exception()
        }
    }

    private suspend fun gqlQueryLoad(params: LoadParams<Int>): LoadResult<Int, User> {
        val response = graphQLRepository.loadQuerySearchChannels(networkLibrary, gqlHeaders, query, params.loadSize, offset)
        if (enableIntegrity) {
            response.errors?.find { it.message == C.FAILED_INTEGRITY_CHECK }?.let { return LoadResult.Error(Exception(it.message)) }
        }
        // no results or an error body leaves data null: an empty page instead of an NPE
        val data = response.data?.searchUsers ?: return LoadResult.Page(emptyList(), null, null)
        val list = data.edges.orEmpty().mapNotNull { item ->
            item.node?.let {
                User(
                    id = it.id,
                    login = it.login,
                    name = it.displayName,
                    profileImageURL = it.profileImageURL,
                    followerCount = it.followers?.totalCount,
                    isLive = it.stream?.viewersCount != null,
                )
            }
        }
        offset = data.edges.orEmpty().lastOrNull()?.cursor?.toString()
        val nextPage = data.pageInfo?.hasNextPage != false
        return LoadResult.Page(
            data = list,
            prevKey = null,
            nextKey = if (!offset.isNullOrBlank() && nextPage) {
                (params.key ?: 1) + 1
            } else null
        )
    }

    private suspend fun gqlLoad(params: LoadParams<Int>): LoadResult<Int, User> {
        val response = graphQLRepository.loadSearchChannels(networkLibrary, gqlHeaders, query, offset)
        if (enableIntegrity) {
            response.errors?.find { it.message == C.FAILED_INTEGRITY_CHECK }?.let { return LoadResult.Error(Exception(it.message)) }
        }
        val data = response.data!!.searchFor.channels
        val list = data.edges.map { item ->
            item.item.let {
                User(
                    id = it.id,
                    login = it.login,
                    name = it.displayName,
                    profileImageURL = it.profileImageURL,
                    followerCount = it.followers?.totalCount,
                    isLive = it.stream?.viewersCount != null,
                )
            }
        }
        offset = data.cursor
        return LoadResult.Page(
            data = list,
            prevKey = null,
            nextKey = if (!offset.isNullOrBlank()) {
                (params.key ?: 1) + 1
            } else null
        )
    }

    private suspend fun helixLoad(params: LoadParams<Int>): LoadResult<Int, User> {
        val response = helixRepository.getSearchChannels(
            networkLibrary = networkLibrary,
            headers = helixHeaders,
            query = query,
            limit = params.loadSize,
            offset = offset,
        )
        val list = response.data.map {
            User(
                id = it.id,
                login = it.login,
                name = it.displayName,
                profileImageURL = it.profileImageURL,
                isLive = it.isLive,
            )
        }
        offset = response.pagination?.cursor
        return LoadResult.Page(
            data = list,
            prevKey = null,
            nextKey = if (!offset.isNullOrBlank()) {
                (params.key ?: 1) + 1
            } else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
