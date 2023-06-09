package com.example.bookworm.bottomMenu.profile.submenu.album.AlbumCreate.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookworm.bottomMenu.feed.Feed

import com.example.bookworm.bottomMenu.profile.submenu.album.AlbumCreate.item.AlbumProcessAdapter
import com.example.bookworm.databinding.FragmentRecordItemBinding
import com.example.bookworm.databinding.SubactivityShowalbumcontentItemBinding

class AlbumProcessViewHolder(
        val binding: SubactivityShowalbumcontentItemBinding,
        val context: Context,
) : RecyclerView.ViewHolder(binding.root) {
    lateinit var feed: Feed

    fun setItem(item: Feed) {
        feed = item
        binding!!.tvBookTitle.setText(item.book!!.title)
        binding!!.tvFeedDesc.setText(item.feedText)
        Glide.with(binding!!.root).load(item.book!!.imgUrl).into(binding!!.ivBookThumb)
        binding!!.tvFeedDate.setText(item.date)
        binding!!.root.setOnClickListener {
            (bindingAdapter as AlbumProcessAdapter).applySelection(binding!!, item)
            (bindingAdapter as AlbumProcessAdapter).onItemClickListener?.let { it(feed) }
        }
    }
}
