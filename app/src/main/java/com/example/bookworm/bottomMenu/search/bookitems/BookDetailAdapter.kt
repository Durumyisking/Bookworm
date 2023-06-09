package com.example.bookworm.bottomMenu.search.bookitems

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookworm.R
import com.example.bookworm.bottomMenu.feed.Feed
import com.example.bookworm.bottomMenu.profile.views.ProfileInfoActivity
import com.example.bookworm.bottomMenu.search.views.BookDetailActivity
import com.example.bookworm.databinding.LayoutBookSearchDetailReviewBinding
import com.example.bookworm.databinding.LayoutBookSearchDetailTopBinding
import com.example.bookworm.databinding.LayoutItemLoadingBinding
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class BookDetailAdapter(val context: Context) :
        ListAdapter<Any, RecyclerView.ViewHolder>(Companion) {


    private val vType = mapOf("Loading" to 0, "BookDetail" to 1, "Review" to 2)

    companion object : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Feed && newItem is Feed)
                oldItem.feedID == newItem.feedID
            else
                (oldItem as Book).itemId == (newItem as Book).itemId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View
        return when (viewType) {
            vType["Review"] -> {
                view = inflater.inflate(R.layout.layout_book_search_detail_review, parent, false)
                UserReviewViewHolder(view)
            }
            vType["BookDetail"] -> {
                view = inflater.inflate(R.layout.layout_book_search_detail_top, parent, false)
                BookDetailViewHolder(view)
            }
            else -> {
                view = inflater.inflate(R.layout.layout_item_loading, parent, false)
                LoadingViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val safePosition = holder.bindingAdapterPosition
        when (holder) {
            is UserReviewViewHolder -> {
                val item = getItem(safePosition) as Feed
                holder.bindItem(item)
            }
            is BookDetailViewHolder -> {
                val item = getItem(safePosition) as Book
                holder.bindItem(item)
            }
            else -> showLoadingView(holder as LoadingViewHolder, safePosition)
        }
    }

    private fun showLoadingView(viewHolder: LoadingViewHolder, position: Int) {
        //
    }

    //각각의 독립된 아이디를 가질 수 있게 함.
    override fun getItemId(position: Int): Long {
        return currentList[position].hashCode().toLong()
    }

    //뷰타입 설정 (0은 리뷰 뷰홀더 , 1은 로딩 뷰홀더)
    override fun getItemViewType(position: Int): Int {
        return if (currentList[position] is Book) vType["BookDetail"]!!
        else if (currentList[position] is Feed && (currentList[position] as Feed).feedID != null) vType["Review"]!!
        else vType["Loading"]!!
    }

    inner class UserReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding by lazy {
            LayoutBookSearchDetailReviewBinding.bind(itemView)
        }

        fun bindItem(item: Feed) {
            binding.apply {
//                tvDate.text = item.date
                tvDate.text = getDateDuration(item.date)
                Glide.with(itemView).load(item.creatorInfo.profileimg).circleCrop().into(imgProfile)
                tvNickname.text = item.creatorInfo.username
                tvCommentContent.text = item.feedText
                llProfile.setOnClickListener{
                    val intent = Intent(itemView.context, ProfileInfoActivity::class.java)
                    intent.putExtra("userID", item.userToken)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    inner class BookDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding by lazy {
            LayoutBookSearchDetailTopBinding.bind(itemView)
        }

        fun bindItem(item: Book) {
            binding.apply {
                lifecycleOwner = itemView.context as BookDetailActivity
                this.book = item
                executePendingBindings()
            }
        }
    }

    inner class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding by lazy {
            LayoutItemLoadingBinding.bind(itemView)
        }
    }

    //시간차 구하기 n분 전, n시간 전 등등
    fun getDateDuration(createdTime: String?): String {
        var dateDuration = ""
        val now = System.currentTimeMillis()
        val dateNow = Date(now) //현재시각
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        try {
            val dateCreated = createdTime?.let { dateFormat.parse(it) }
            val duration = dateNow.time - dateCreated!!.time //시간차이 mills
            dateDuration = if (duration / 1000 / 60 == 0L) {
                "방금"
            } else if (duration / 1000 / 60 <= 59) {
                (duration / 1000 / 60).toString() + "분 전"
            } else if (duration / 1000 / 60 / 60 <= 23) {
                (duration / 1000 / 60 / 60).toString() + "시간 전"
            } else if (duration / 1000 / 60 / 60 / 24 <= 29) {
                (duration / 1000 / 60 / 60 / 24).toString() + "일 전"
            } else if (duration / 1000 / 60 / 60 / 24 / 30 <= 12) {
                (duration / 1000 / 60 / 60 / 24 / 30).toString() + "개월 전"
            } else {
//                (duration / 1000 / 60 / 60 / 24 / 30 / 12).toString() + "년 전"
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(duration)
            }
            return dateDuration
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }
    }
}