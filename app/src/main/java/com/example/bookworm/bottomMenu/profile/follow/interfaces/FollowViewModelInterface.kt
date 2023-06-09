package com.example.bookworm.bottomMenu.profile.follow.interfaces

import androidx.lifecycle.MutableLiveData
import com.example.bookworm.LoadState
import com.example.bookworm.core.userdata.UserInfo


//팔로우 관련 뷰 모델
//각 함수에 대한 정보
interface FollowViewModelInterface {
    fun follow(
        toUserInfo: UserInfo,
        type: Boolean,
        stateLiveData: MutableLiveData<LoadState>,
        resultUserInfo: UserInfo
    )//type : true 면 팔로우 , false 면 언팔로우


}