package com.krishiai.app.data.common

import kotlinx.coroutines.flow.*

/**
 * A generic class that can provide a resource backed by both the SQLite database and the network.
 *
 * @param ResultType Type for the Resource data.
 * @param RequestType Type for the API response.
 */
inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> Flow<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType) -> Boolean = { true }
) = flow {
    emit(Resource.Loading)
    val data = query().first()
    
    val flow = if (shouldFetch(data)) {
        try {
            saveFetchResult(fetch())
            query().map { Resource.Success(it) }
        } catch (throwable: Throwable) {
            query().map { Resource.Error(throwable.message ?: "Network error", throwable as Exception) }
        }
    } else {
        query().map { Resource.Success(it) }
    }
    
    emitAll(flow)
}
