package com.example.bookworm.bottomMenu.search.bookitems

import android.view.View
import androidx.recyclerview.widget.RecyclerView

interface OnBookItemClickListener {
    fun onItemClick(holder: RecyclerView.ViewHolder, view: View, position: Int) }