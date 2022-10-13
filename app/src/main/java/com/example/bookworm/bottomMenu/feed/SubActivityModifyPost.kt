package com.example.bookworm.bottomMenu.feed

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.bookworm.databinding.SubactivityModifyPostBinding



//게시물수정 액티비티
class SubActivityModifyFeed : AppCompatActivity() {
    private var binding: SubactivityModifyPostBinding? = null
    private val originData by lazy {
        intent!!.getParcelableExtra<Feed>("Feed")
    }
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState)
        binding = SubactivityModifyPostBinding.inflate(layoutInflater)
        setContentView(binding!!.root)




    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}