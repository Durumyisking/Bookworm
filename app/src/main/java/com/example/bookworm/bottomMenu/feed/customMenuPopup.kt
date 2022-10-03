package com.example.bookworm.bottomMenu.feed

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bookworm.LoadState
import com.example.bookworm.R
import com.example.bookworm.appLaunch.views.MainActivity
import com.example.bookworm.bottomMenu.feed.comments.SubActivityComment
import com.example.bookworm.bottomMenu.feed.oldItems.subActivity_Feed_Modify

//메뉴 팝업

class customMenuPopup(val context: Context, val anchor: View) : PopupMenu(context, anchor) {

    var liveState = MutableLiveData<Int>()

    //피드 관련 메뉴 변수
    val FEED_DELETE = 299 // 삭제
    val FEED_MODIFY = 1 // 수정

    //피드 메뉴인 경우
    fun setItem(data: Any) {
        this.menuInflater.inflate(
                when (data) {
                    is Feed -> R.menu.feed_menu
                    else -> R.menu.comment_menu
                }, this.menu)
        this.setOnMenuItemClickListener(menuItemClickListener(data, context))
        //리스너 설정
        this.show() //팝업 메뉴 설정
    }

    //리스너 내부 클래스
    inner class menuItemClickListener(val data: Any, val context: Context)
        : OnMenuItemClickListener {
        lateinit var vm: ViewModel //뷰모델 설정


        //클릭 시
        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when (item!!.itemId) {
                R.id.menu_modify -> modifyData(data)
                R.id.menu_delete -> {
                    if (data is Feed) createAlertBuilder(FEED_DELETE)
                }
                else -> return true
            }
            return false
        }

        //수정 메소드
        fun modifyData(item: Any) {
            if (item is Feed) {
                //수정을 담당하는 액티비티로 이동해야함.
                var intent = Intent(context as MainActivity, subActivity_Feed_Modify::class.java)
                intent.putExtra("Feed", item)

                //현재 화면이 댓글 화면인 경우
                if (context is SubActivityComment)
//                    context.startActivityResult.launch(intent)
                //피드화면인 경우
                else
                    (context.supportFragmentManager.findFragmentByTag("0") as FragmentFeed)
                            .startActivityResult.launch(intent)
            }
        }

        // 알림 창 만들어주는 함수
        fun createAlertBuilder(code: Int) =
                AlertDialog.Builder(context)
                        .setMessage(
                                when (code) {
                                    FEED_DELETE -> "게시물을 삭제하시겠습니까?" //피드(게시물) 삭제
                                    else -> ""
                                })
                        //참인 경우
                        .setPositiveButton("네") { dialog: DialogInterface, which: Int ->
                            dialog.dismiss() //창 닫기
                            //피드 삭제
                            if (code == FEED_DELETE) {
                                //뷰모델 생성
                                vm = ViewModelProvider(if (context is MainActivity) context
                                else context as SubActivityComment)[FeedViewModel::class.java]

                                //뷰모델을 이용하여 서버에서 피드 데이터 삭제 진행
                                val state = MutableLiveData<LoadState>()
                                (vm as FeedViewModel).deleteFeed(data as Feed,state)
                                //어댑터 Refresh
                                //삭제된 내용을 현재 액티비티에 반영해야함.
                                state.observe(context as AppCompatActivity) {
                                    if (context is SubActivityComment) context.finish()
                                    else liveState.value = FEED_DELETE
                                }


                            }
                        }.setNegativeButton("아니오") { dialog: DialogInterface, which: Int ->
                            dialog.dismiss()
                        }.show()
    }
}



