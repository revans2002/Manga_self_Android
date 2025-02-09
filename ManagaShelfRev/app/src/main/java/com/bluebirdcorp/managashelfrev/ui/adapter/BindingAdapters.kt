package com.bluebirdcorp.managashelfrev.ui.adapter


import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bluebirdcorp.managashelfrev.model.Manga
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bluebirdcorp.managashelfrev.R
import com.bumptech.glide.load.engine.DiskCacheStrategy

@BindingAdapter("listData")
fun setRecyclerViewData(recyclerView: RecyclerView, data: List<Manga>?) {
    val adapter = recyclerView.adapter as? MangaAdapter
    adapter?.setMangaList(data ?: emptyList())
}

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, imageUrl: String?) {
    Log.d("BindingAdapter", "Loading image: $imageUrl") // Log URL

    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(view.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache images
            .placeholder(R.drawable.burst)  // Default placeholder
            .error(R.drawable.burst)        // Error placeholder
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    } else {
        view.setImageResource(R.drawable.burst) // Set default image if URL is null
    }
}

@BindingAdapter("isLiked")
fun setLikeButton(view: ImageView, isLiked: Boolean) {
    Log.d("BindingAdapter", "Setting like button: $isLiked") // Log state

    val likedDrawable = R.drawable.ic_heart_filled // Replace with your filled heart icon
    val unlikedDrawable = R.drawable.ic_heart_unfilled // Replace with your unfilled heart icon

    view.setImageResource(if (isLiked) likedDrawable else unlikedDrawable)
}
