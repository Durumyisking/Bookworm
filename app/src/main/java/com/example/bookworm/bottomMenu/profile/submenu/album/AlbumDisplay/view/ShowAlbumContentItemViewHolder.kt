package com.example.bookworm.bottomMenu.profile.submenu.album.AlbumDisplay.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookworm.bottomMenu.feed.Feed
import com.example.bookworm.databinding.SubactivityShowalbumcontentItemBinding

class ShowAlbumContentItemViewHolder(
//    val binding: FragmentRecordItemBinding,
    val binding: SubactivityShowalbumcontentItemBinding,
    val context: Context
) :
    RecyclerView.ViewHolder(binding.root) {
    fun setItem(data: Feed) {
        binding.tvBookTitle.text = data.book!!.title
        binding.tvFeedDate.text = data.date
        binding.tvFeedDesc.text = data.feedText
        Glide.with(binding.root).load(data.book!!.imgUrl).into(binding.ivBookThumb)

    }
}