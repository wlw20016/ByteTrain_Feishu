package com.bytetrain.feishuclone.shared.list

sealed interface PagingUiState<out T> {
    data object Loading : PagingUiState<Nothing>
    data object Empty : PagingUiState<Nothing>

    data class Error(
        val message: String,
    ) : PagingUiState<Nothing>

    data class Content<T>(
        val items: List<T>,
        val hasMore: Boolean,
    ) : PagingUiState<T>

    data class LoadingMore<T>(
        val items: List<T>,
        val hasMore: Boolean = true,
    ) : PagingUiState<T>

    data class LoadMoreError<T>(
        val items: List<T>,
        val message: String,
        val hasMore: Boolean = true,
    ) : PagingUiState<T>
}
