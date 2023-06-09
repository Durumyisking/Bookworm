package com.example.bookworm.bottomMenu.profile.follow.modules

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.bookworm.LoadState
import com.example.bookworm.core.dataprocessing.repository.UserRepository
import com.example.bookworm.core.userdata.UserInfo
import com.example.bookworm.bottomMenu.profile.follow.interfaces.FollowViewModelInterface
import kotlinx.coroutines.launch

//FollowViewModel 구현체
class FollowViewModel(val context: Context) : ViewModel(), FollowViewModelInterface {
    var data = MutableLiveData<UserInfo?>()
    private val userRepository = UserRepository(context)
    private val followDataRepository = FollowDataRepository(context)

    class Factory(val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return FollowViewModel(context) as T
        }
    }

    fun followCheck(liveData: MutableLiveData<Boolean>, userToken: String) {
        viewModelScope.launch {
            liveData.value = followDataRepository.isFollowNow(userToken)
        }
    }

    fun getUser(liveData: MutableLiveData<UserInfo>, token: String) {
        viewModelScope.launch {
            liveData.value = userRepository.getUser(token, true)
        }
    }

    fun getFollowList(
        stateLiveData: MutableLiveData<LoadState>,
        type: Boolean,
        targetToken: String,
        isRefreshing: Boolean,
        resultList: MutableList<UserInfo>
    ) {
        stateLiveData.value = LoadState.Loading
        viewModelScope.launch {
            val result =
                followDataRepository.getFollowList(targetToken = targetToken, type = type, isRefreshing)
            if (result == null) {
                stateLiveData.value = LoadState.Error
            } else {
                resultList.addAll(result)
                stateLiveData.value = LoadState.Done
            }
        }
    }


    override fun follow(
        toUserInfo: UserInfo,
        type: Boolean,
        stateLiveData: MutableLiveData<LoadState>,
        resultUserInfo: UserInfo
    ) {
        stateLiveData.value = LoadState.Loading
        viewModelScope.launch {
            val result = followDataRepository.setFollowUser(
                toUserInfo,
                type
            ) //팔로우 처리 결과 -> 제대로 처리되는 경우, 업데이트 된 사용자의 정보가 넘어온다.
            if (result != null) {
                resultUserInfo.followerCounts = result.followerCounts
                resultUserInfo.followingCounts = result.followingCounts
                stateLiveData.value = LoadState.Done
            } else {
                stateLiveData.value = LoadState.Error
            }
        }
    }
}