package com.fibelatti.core.android.recyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.properties.Delegates

class PagingRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr) {

    private val pagingHandler = PagingHandler()
    private var scrollListenerSet: Boolean by Delegates.vetoable(false) { _, oldValue, _ -> !oldValue }

    var onShouldRequestNextPage: (() -> Unit)? = null

    init {
        addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    when (val lm = layoutManager) {
                        is LinearLayoutManager -> {
                            handlePaging(lm.itemCount, lm.findFirstCompletelyVisibleItemPosition())
                        }
                        is GridLayoutManager -> {
                            handlePaging(lm.itemCount, lm.findFirstCompletelyVisibleItemPosition())
                        }
                        else -> {
                            throw IllegalStateException(
                                "PagingRecyclerView supports only " +
                                    "LinearLayoutManager and GridLayoutManager",
                            )
                        }
                    }
                }
            },
        )
        scrollListenerSet = true
    }

    private fun handlePaging(itemCount: Int, firstVisibleItemPosition: Int) {
        if (pagingHandler.shouldHandlePaging(itemCount, firstVisibleItemPosition)) {
            pagingHandler.setRequestingNextPage(true)
            onShouldRequestNextPage?.invoke()
        }
    }

    fun setPageSize(pageSize: Int) {
        pagingHandler.setPageSize(pageSize)
    }

    fun setMinDistanceToLastItem(minDistanceToLastItem: Int) {
        pagingHandler.setMinDistanceToLastItem(minDistanceToLastItem)
    }

    fun onRequestNextPageCompleted() {
        pagingHandler.setRequestingNextPage(false)
    }

    // region Unsupported RecyclerView overrides
    override fun addOnScrollListener(listener: OnScrollListener) {
        if (!scrollListenerSet) {
            super.addOnScrollListener(listener)
        } else {
            throw IllegalStateException("PagingRecyclerView doesn't support addOnScrollListener.")
        }
    }

    @Deprecated(message = "", replaceWith = ReplaceWith("addOnScrollListener"))
    override fun setOnScrollListener(listener: OnScrollListener?) {
        throw IllegalStateException("PagingRecyclerView doesn't support setOnScrollListener.")
    }

    override fun removeOnScrollListener(listener: OnScrollListener) {
        throw IllegalStateException("PagingRecyclerView doesn't support removeOnScrollListener.")
    }

    override fun clearOnScrollListeners() {
        throw IllegalStateException("PagingRecyclerView doesn't support clearOnScrollListeners.")
    }
    // endregion
}

class PagingHandler {

    private var requestingNextPage: Boolean = false
    private var pageSize: Int = 0
    private var minDistanceToLastItem: Int = 0

    fun setRequestingNextPage(requestingNextPage: Boolean) {
        this.requestingNextPage = requestingNextPage
    }

    fun setPageSize(pageSize: Int) {
        if (pageSize > 0) {
            this.pageSize = pageSize
        }
    }

    fun setMinDistanceToLastItem(minDistanceToLastItem: Int) {
        if (minDistanceToLastItem > 0) {
            this.minDistanceToLastItem = minDistanceToLastItem
        }
    }

    fun shouldHandlePaging(itemCount: Int, firstVisibleItemPosition: Int): Boolean {
        return pageSize > 0 &&
            minDistanceToLastItem > 0 &&
            itemCount != 0 &&
            itemCount % pageSize == 0 &&
            itemCount - firstVisibleItemPosition < minDistanceToLastItem &&
            !isRequestingNextPage()
    }

    @Synchronized
    private fun isRequestingNextPage(): Boolean = requestingNextPage
}
