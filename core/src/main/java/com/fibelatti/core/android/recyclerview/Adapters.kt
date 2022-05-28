package com.fibelatti.core.android.recyclerview

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

fun <T : Any, B : ViewBinding> listAdapter(
    binding: (parent: ViewGroup) -> B,
    itemsTheSame: (oldItem: T, newItem: T) -> Boolean = { _, _ -> false },
    contentsTheSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
    onBind: B.(T) -> Unit,
): BaseListAdapter<T, B> = BaseListAdapter(binding, itemsTheSame, contentsTheSame, onBind)

fun <T : Any> diffUtil(
    itemsTheSame: (oldItem: T, newItem: T) -> Boolean = { _, _ -> false },
    contentsTheSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
): DiffUtil.ItemCallback<T> = object : DiffUtil.ItemCallback<T>() {

    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = itemsTheSame(oldItem, newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = contentsTheSame(oldItem, newItem)
}

fun <T : Any, B : ViewBinding> adapter(
    binding: (parent: ViewGroup) -> B,
    filter: (query: String, item: T) -> Boolean = { _, _ -> true },
    onEmptyFilterResult: () -> Unit = {},
    onBind: B.(T) -> Unit = {},
): BaseAdapter<T, B> = BaseAdapter(binding, filter, onEmptyFilterResult, onBind)

class ViewHolder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)

open class BaseListAdapter<T : Any, B : ViewBinding>(
    private val binding: (parent: ViewGroup) -> B,
    itemsTheSame: (oldItem: T, newItem: T) -> Boolean = { _, _ -> false },
    contentsTheSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
    private val onBind: B.(T) -> Unit = {},
) : ListAdapter<T, ViewHolder<B>>(diffUtil(itemsTheSame, contentsTheSame)) {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> = ViewHolder(binding(parent))

    override fun onBindViewHolder(holder: ViewHolder<B>, position: Int) {
        holder.binding.onBind(getItem(position))
    }
}

@SuppressLint("NotifyDataSetChanged")
open class BaseAdapter<T : Any, B : ViewBinding>(
    private val binding: (parent: ViewGroup) -> B,
    private val filter: (query: String, item: T) -> Boolean = { _, _ -> true },
    private val onEmptyFilterResult: () -> Unit = {},
    private val onBind: B.(T) -> Unit = {},
) : RecyclerView.Adapter<ViewHolder<B>>() {

    private val allItems: MutableList<T> = mutableListOf()
    private val filteredItems: MutableList<T> = mutableListOf()

    val items: List<T> get() = filteredItems

    override fun getItemCount(): Int = items.size

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> = ViewHolder(binding(parent))

    override fun onBindViewHolder(holder: ViewHolder<B>, position: Int) {
        holder.binding.onBind(items[position])
    }

    fun submitList(listItems: List<T>, process: () -> Unit = { notifyDataSetChanged() }) {
        allItems.clear()
        allItems.addAll(listItems)
        filteredItems.clear()
        filteredItems.addAll(listItems)

        process()
    }

    fun addAll(listItems: List<T>) {
        val currentSize = allItems.size

        allItems.addAll(listItems)
        filteredItems.addAll(listItems)

        notifyItemRangeInserted(currentSize, listItems.size)
    }

    fun addAll(index: Int, listItems: List<T>) {
        allItems.addAll(index, listItems)
        filteredItems.addAll(index, listItems)

        notifyItemRangeInserted(index, listItems.size)
    }

    fun add(item: T) {
        val currentSize = allItems.size

        allItems.add(item)
        filteredItems.add(item)

        notifyItemInserted(currentSize)
    }

    fun add(index: Int, item: T) {
        allItems.add(index, item)
        filteredItems.add(index, item)

        notifyItemInserted(index)
    }

    fun clearItems() {
        allItems.clear()
        filteredItems.clear()

        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredItems.clear()

        if (query.isBlank()) {
            filteredItems.addAll(allItems)
        } else {
            filteredItems.addAll(allItems.filter { filter(query, it) })
            if (filteredItems.isEmpty()) onEmptyFilterResult()
        }

        notifyDataSetChanged()
    }
}
