package com.bluebirdcorp.managashelfrev.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bluebirdcorp.managashelfrev.databinding.ItemMangaBinding
import com.bluebirdcorp.managashelfrev.model.Manga
import com.bluebirdcorp.managashelfrev.R
import com.bluebirdcorp.managashelfrev.ui.viewmodel.MangaViewModel

class MangaAdapter(
    private val mangaViewModel: MangaViewModel,
    private val onItemClick: (Manga) -> Unit
) : RecyclerView.Adapter<MangaAdapter.MangaViewHolder>(), Filterable {

    private var mangaList: List<Manga> = listOf()
    private var filteredList: List<Manga> = listOf()
    private var tempList: List<Manga> = listOf()

    inner class MangaViewHolder(
        private val binding: ItemMangaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(manga: Manga) {
            Log.d("MangaViewHolder", "Binding manga: ${manga.title}")
            binding.manga = manga
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MangaViewHolder {
        val binding = ItemMangaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.likeButton.setOnClickListener{
            val manga = binding.manga
            if (manga != null) {
                manga.isLiked = !manga.isLiked
                binding.likeButton.setImageResource(if (manga.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_unfilled)
                mangaViewModel.updateManga(manga)
            }
        }
        return MangaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MangaViewHolder, position: Int) {
        holder.bind(filteredList[position])

        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (position == filteredList.size - 1) {
            params.bottomMargin = holder.itemView.context.resources.getDimensionPixelSize(R.dimen.last_item_margin)
        } else {
            params.bottomMargin = 0
        }
        holder.itemView.layoutParams = params

        holder.itemView.setOnClickListener {
            onItemClick(filteredList[position])
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun setMangaList(mangaList: List<Manga>) {
        this.mangaList = mangaList
        filteredList = mangaList
        notifyDataSetChanged()
    }

    fun setFavorites(mangaList: List<Manga>) {
        tempList = filteredList
        this.mangaList = mangaList
        filteredList = mangaList
        notifyDataSetChanged()
    }

    fun unsetFavorites() {
        this.mangaList = tempList
        filteredList = tempList
        notifyDataSetChanged()
    }

    fun getPositionForYear(year: Int): Int {
        return filteredList.indexOfFirst { manga ->
            manga.publishedChapterDate?.let { date ->
                getYearFromTimestamp(date) == year
            } ?: false
        }
    }

    fun scrollToTop(recyclerView: RecyclerView) {
        if (filteredList.isNotEmpty()) {
            recyclerView.scrollToPosition(0)
        }
    }


    fun getYearForPosition(position: Int): Int? {
        return filteredList.getOrNull(position)?.publishedChapterDate?.let { getYearFromTimestamp(it) }
    }

    private fun getYearFromTimestamp(timestamp: Long): Int {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp * 1000
        return calendar.get(java.util.Calendar.YEAR)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase()?.trim()

                filteredList = if (query.isNullOrEmpty()) {
                    mangaList
                } else {
                    mangaList.filter {
                        it.title?.lowercase()?.contains(query) == true
                    }
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<Manga>
                notifyDataSetChanged()
            }
        }
    }
}