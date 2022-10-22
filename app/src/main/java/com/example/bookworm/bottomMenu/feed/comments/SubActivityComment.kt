package com.example.bookworm.bottomMenu.feed.comments

import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookworm.DdayCounter
import com.example.bookworm.LoadState
import com.example.bookworm.bottomMenu.feed.Feed
import com.example.bookworm.bottomMenu.feed.FeedViewModel
import com.example.bookworm.bottomMenu.feed.SubActivityModifyPost
import com.example.bookworm.bottomMenu.profile.UserInfoViewModel
import com.example.bookworm.core.dataprocessing.image.ImageProcessing
import com.example.bookworm.core.userdata.UserInfo
import com.example.bookworm.databinding.SubactivityCommentBinding
import com.example.bookworm.notification.MyFCMService
import java.lang.NullPointerException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

//메커니즘 순서
//1. 이전 화면(fragmentFeed)에서 넘어온 Feed데이터를 가지고 화면에 세팅해준다.
//2. 댓글을 불러와야 하는데, Paging3를 이용하여 데이터를 가지고 온다.
//3. 가지고 온 댓글을 리사이클러뷰에 뿌려준다.
//4. 사용자가 댓글을 단 경우, 파이어스토어 서버에 해당 댓글을 저장하고, (1) 화면을 새로고침한다. or (2) 맨 위에 아이템을 세팅한다.


//댓글 삭제 및 수정 부분을 ItemTouchHelper를 이용하여 작업한다.

//댓글, 상세 화면
class SubActivityComment : AppCompatActivity() {
    private val binding by lazy {
        SubactivityCommentBinding.inflate(layoutInflater)
    }

    companion object {
        const val FEED_MODIFIED = 111
        const val FEED_DELETE = 112

        const val COMMENT_DELETE = 113
    }

    //액티비티 간 데이터 전달 핸들러(검색한 데이터의 값을 전달받는 매개체가 된다.) [책 데이터 이동]
    var startActivityResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        //수정 완료 시, 맨위 ( 피드 내용에 반영을 한다. )
        if (result.resultCode == SubActivityModifyPost.MODIFY_OK) {
            //수정된 아이템
            result.data!!.apply {
                //게시물 업로드 하는 함수
                modifiedFeed = getParcelableExtra("modifiedFeed")!!
                modifiedFeed!!.apply {
                    feedViewModel.uploadFeed(this, null, ImageProcessing(this@SubActivityComment), this.imgurl)
                    feedViewModel.nowFeedUploadState.observe(this@SubActivityComment) {
                        if (it == LoadState.Done) {
                            //수정 업데이트 적용
                            modifiedFeed!!.duration = DdayCounter.getDuration(modifiedFeed!!.date!!)
                            commentAdapter.currentList.toMutableList().apply {
                                this.removeAt(0)
                                add(0, modifiedFeed)
                                commentAdapter.submitList(this.toList()) // 현재 리사이클러뷰에 반영한다.
                            }
                            Toast.makeText(this@SubActivityComment, "게시물이 정상적으로 수정되었습니다. ", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        }
    }
    private val userInfoViewModel by lazy {
        ViewModelProvider(this, UserInfoViewModel.Factory(this))[UserInfoViewModel::class.java]
    }
    private val feedViewModel by lazy {
        ViewModelProvider(this, FeedViewModel.Factory(this))[FeedViewModel::class.java]
    }
    private var modifiedFeed: Feed? = null //수정된 게시물인 경우, 해당 데이터를 저장하는 변수
    private var isDataEnd = false
    private val myFCMService = MyFCMService.getInstance()
    private val feedItem by lazy<Feed> {
        intent.getParcelableExtra("Feed")!!
    }
    val nowUser by lazy {
        intent.getParcelableExtra<UserInfo>("NowUser")
    }
    private val commentAdapter by lazy { CommentsAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showShimmer(true)
        binding.apply {
            setContentView(root)
            setUI()
            setRecyclerView()
            loadCommentData(true)
        }

    }

    private fun setUI() {
        binding.apply {
            tvTopName.text = "${feedItem.creatorInfo.username}님의 게시물"
            btnWriteComment.setOnClickListener {
                //댓글 추가
                addComment()
            }
            btnBefore.setOnClickListener { closeActivity() }
        }

    }

    //피드를 불러오는 어댑터 장착
    override fun onBackPressed() {
        super.onBackPressed()
        closeActivity()
    }

    //댓글 추가 메소드 구현
    private fun addComment() {
        val commentText = binding.edtComment.text.toString() //댓글 내용
        binding.apply {
            if (commentText != "") {//내용이 비어있지 않을 때만 이 메소드가 작동하도록 함
                edtComment.apply {

                    val madeDate = LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")
                                            .withLocale(Locale.KOREA)
                                            .withZone(ZoneId.of("Asia/Seoul"))
                            )
                    val comment = Comment(
                            commentID = "${madeDate}_${nowUser!!.token}",
                            contents = commentText,
                            userToken = nowUser!!.token,
                            madeDate = madeDate
                    )
                    feedViewModel.manageComment(comment, feedItem.feedID!!, true) //서버에 댓글 추가
                    //게시물 작성자에게 댓글이 달렸다는 알림을 보냄
                    if (nowUser!!.token != feedItem.userToken) { //본인이 댓글을 단 경우엔 알림이 가지 않도록 함
                        myFCMService.sendPostToFCM(
                                this@SubActivityComment, feedItem.creatorInfo!!.fCMtoken,
                                "${nowUser!!.username}님이 댓글을 남겼습니다. \"${text}\" "
                        )
                    }

                    //키보드 내리기
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                            .hideSoftInputFromWindow(windowToken, 0)
                    clearFocus()
                    text = null

                    feedViewModel.nowCommentLoadState.observe(context as SubActivityComment) { state ->
                        if (state == LoadState.Done) {
                            comment.duration = feedViewModel.getDateDuration(comment!!.madeDate)
                            comment.creator = nowUser!!
                            commentAdapter.currentList.toMutableList().apply {
                                add(1, comment)
                                commentAdapter.submitList(this)
                            }
                        }

                        binding.mRecyclerView.apply {
                            smoothScrollToPosition(0) //맨 위 아이템으로 포커스를 이동 (본인 댓글 확인을 위해)
                        }
                    }
                }
            }
        }
    }

    //액티비티 종료 시
    private fun closeActivity() {
        //게시물이 수정된 경우
        try {
            if (feedItem.date != modifiedFeed!!.date) {
                intent.putExtra("modifiedFeed", modifiedFeed)
                setResult(FEED_MODIFIED, intent)
            }
            finish()
        } catch (e: NullPointerException) {
            //만약 수정된 내용이 없는 경우 그냥 종료함.
            finish()
        }
    }

    //페이지 새로고침 시 사용하는 메소드
    fun pageRefresh() {
        isDataEnd = false
        loadCommentData(true)
        commentAdapter.submitList(emptyList())
    }


    //데이터를 가져오는 메소드
    fun loadCommentData(isRefreshing: Boolean) {
        if (!isDataEnd) {
            feedViewModel.loadComment(feedItem.feedID!!, isRefreshing)
            feedViewModel.nowCommentLoadState.observe(this) { nowState ->
                //데이터 로딩이 다 되었다면
                if (nowState == LoadState.Done) {
                    val current = commentAdapter.currentList.toMutableList() //기존에 가지고 있던 아이템 목록
                    if (isRefreshing) {
                        current.clear()
                        binding.swiperefresh.isRefreshing = false //새로고침인 경우, 데이터가 다 로딩 된 후 새로고침 표시 없애기
                        current.add(0, feedItem) //피드 데이터를 가져온다.
                    }
                    //만약 현재 목록이 비어있지 않고, 마지막 아이템이 로딩 아이템 이라면 마지막 아이템을 제거
                    if (current.size > 1 && (current.last() as Comment).commentID == "") current.removeLast()
                    //데이터의 끝에 다다르지 않았다면, 현재 목록에 불러온 아이템을 추가한다.
                    val loadedData = feedViewModel.commentsData
                    if (loadedData != null && !current.containsAll(loadedData)) {
                        current.addAll(loadedData)
                        if (loadedData.size == 10) current.add(Comment())
                        else isDataEnd = true
                    }
                    //변경된 리스트를 어댑터에 반영
                    commentAdapter.submitList(current)
                    showShimmer(false)
                }
            }
        }
    }

    //리사이클러뷰를 위한 재료 세팅
    private fun setRecyclerView() {
        binding.apply {
            mRecyclerView.adapter = commentAdapter //어댑터 세팅
            mRecyclerView.itemAnimator = null //리사이클러뷰 애니메이션 제거
            mRecyclerView.isNestedScrollingEnabled = false
            mRecyclerView.setHasFixedSize(true)
            swiperefresh.setOnRefreshListener {
                pageRefresh()
            } //스와이프하여 새로고침
            with(mRecyclerView) {
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                        val lastVisibleItemPosition =
                                layoutManager!!.findLastCompletelyVisibleItemPosition()
                        if ((lastVisibleItemPosition
                                        == commentAdapter.currentList.lastIndex) && lastVisibleItemPosition > 0)
                            loadCommentData(false)
                    }
                })
            }
        }
    }

    //shimmer을 켜고 끄고 하는 메소드
    fun showShimmer(bool: Boolean) {
        binding.llComment.isVisible = !bool
        if (bool) binding.SFLComment.startShimmer() else binding.SFLComment.stopShimmer()
        binding.SFLComment.isVisible = bool
    }
}