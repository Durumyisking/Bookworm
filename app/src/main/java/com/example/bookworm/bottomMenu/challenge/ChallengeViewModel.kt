package com.example.bookworm.bottomMenu.challenge

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.bookworm.DdayCounter
import com.example.bookworm.LoadState
import com.example.bookworm.appLaunch.views.MainActivity
import com.example.bookworm.bottomMenu.challenge.items.Challenge
import com.example.bookworm.bottomMenu.profile.UserInfoViewModel
import com.example.bookworm.core.userdata.UserInfo
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList


/**Repository에서 전달받은 챌린지 데이터를 가공, 처리하는 뷰모델*/
@SuppressLint("StaticFieldLeak")
class ChallengeViewModel(val context: Context) : ViewModel() {
    private val repo by lazy {
        ChallengeDataRepository(context)
    }
    val challengeList: MutableLiveData<ArrayList<Challenge>> = MutableLiveData()
    val userInfoViewModel by lazy {
        ViewModelProvider(
            context as MainActivity,
            UserInfoViewModel.Factory(context)
        )[UserInfoViewModel::class.java]
    }
    var lastVisibleDataValue: String = ""
    val alertDialog by lazy { AlertDialog.Builder(context) }

    private val PAGESIZE = 5L

    class Factory(val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return ChallengeViewModel(context) as T
        }
    }


    //챌린지 목록을 가져온다. 토큰은 나중에 팔로워 기준으로만 챌린지를 보일 수도 있을 것 같아 적어둠.
    fun getChallengeList(
        token: String = "", keyword: String? = null, isRefreshing: Boolean = false,
        stateLiveData: MutableLiveData<LoadState>, result: ArrayList<Challenge>,
    ) {
        stateLiveData.value = LoadState.Loading //로딩 중임을 UI에 알림
        if (isRefreshing) { //만약 새로운 검색을 시도하거나, 새로고침을 진행한 경우
            lastVisibleDataValue = "" //이전에 불러온 값을 초기화 한다.
        }
        //데이터를 불러온다.
        viewModelScope.launch {
            val loadedData = repo
                .loadChallenges(lastVisible = lastVisibleDataValue, pageSize = PAGESIZE)
            try {
                result.addAll(ArrayList<Challenge>(
                    //방장의 유저 데이터를 받아서 챌린지 객체에 삽입한다.
                    loadedData!!.map { challengeData ->
                        return@map withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                            challengeData.dDay =
                                DdayCounter.getDdayByDash(challengeData.endDate) //디데이 설정
                            challengeData.masterData = try {
                                //불러온 값을 방장 데이터로 설정
                                userInfoViewModel.suspendGetUser(challengeData.masterToken)!!
                            } catch (e: NullPointerException) {
                                UserInfo() //빈 유저 데이터를 반환
                            }
                            challengeData.isUserJoined = try {
                                challengeData.currentPart.contains(
                                    userInfoViewModel.suspendGetUser(
                                        null
                                    )!!.token
                                )
                            } catch (e: Exception) {
                                false
                            }
                            return@withContext challengeData
                        }
                    }
                ))
                lastVisibleDataValue = loadedData.last().id
                stateLiveData.value = LoadState.Done //UI에 데이터 로딩이 완료되었음을 알림
            } catch (e: NoSuchElementException) {
                stateLiveData.value = LoadState.Error
            }
        }
    }

    /** Repository를 통해 Join 로직을 구현한다.
     * */
    private fun processToJoin(
        challengeId: String,
        userToken: String,
        liveData: MutableLiveData<LoadState>
    ) {
        liveData.value = LoadState.Loading
        try {
            viewModelScope.launch {
                repo.joinChallenge(challengeId, userToken = userToken)
                liveData.value = LoadState.Done
            }
        }
        //참여 초과 오류
        catch (e: ArrayIndexOutOfBoundsException) {
            liveData.value = LoadState.OverError
        }
        //파베 오류
        catch (e: Exception) {
            liveData.value = LoadState.Error
        }
    }

    fun createChallenge(challenge: Challenge, stateLiveData: MutableLiveData<LoadState>) {
        stateLiveData.value = LoadState.Loading
        viewModelScope.launch {
            try {
                repo.createChallenge(challenge)
                stateLiveData.value =  LoadState.Done
            }catch (e:FirebaseFirestoreException){
                stateLiveData.value =  LoadState.Error
            }
        }
    }

    //챌린지 참여 로직
    fun joinChallenge(id: String, userToken: String, liveData: MutableLiveData<LoadState>) {
        alertDialog.setMessage("챌린지에 참여하시겠습니까? (참여 후 탈퇴 불가)")
            .setPositiveButton("네") { dialog, which ->
                processToJoin(id, userToken, liveData)
            }.setNegativeButton("아니요") { dialog, which ->
                dialog.dismiss()
            }.show()
    }

    //챌린지에 참여가능한지 확인
    fun canJoinChallenge() {

    }


    //챌린지 인증글 보상

    //디데이 계산 메소드
    private fun countDday(date: String): String {
        try {
            val todaCal = Calendar.getInstance() //오늘날짜 가져오기
            val ddayCal = Calendar.getInstance() //오늘날짜를 가져와 변경시킴

            val year: Int = date.substring(0, 4).toInt()
            var month: Int = date.substring(5, 7).toInt()
            val day: Int = date.substring(8, 10).toInt()

            month -= 1 // 받아온날짜에서 -1을 해줘야함.

            ddayCal[year, month] = day // D-day의 날짜를 입력

            val today =
                todaCal.timeInMillis / 86400000 //->(24 * 60 * 60 * 1000) 24시간 60분 60초 * (ms초->초 변환 1000)

            val dday = ddayCal.timeInMillis / 86400000
            val count = dday - today // 오늘 날짜에서 dday 날짜를 빼주게 됨.

            return if (count < 0) {
                "종료된 챌린지입니다."
            } else {
                "${count}일 남음"
            }
        } catch (e: Exception) {
            return "error"
        }
    }
}