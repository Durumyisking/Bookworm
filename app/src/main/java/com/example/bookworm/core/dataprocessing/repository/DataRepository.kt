package com.example.bookworm.core.dataprocessing.repository

import com.example.bookworm.bottomMenu.bookworm.BookWorm
import com.example.bookworm.bottomMenu.challenge.items.Challenge
import com.example.bookworm.bottomMenu.feed.Feed
import com.example.bookworm.bottomMenu.profile.submenu.album.AlbumData
import com.example.bookworm.core.userdata.UserInfo

interface DataRepository {

    //사용자 관련 데이터를 다루는 공간
    interface HandleUser {
        suspend fun getBookWorm(token: String): BookWorm //책볼레 정보를 가져온다.
        suspend fun updateUser(user: UserInfo) //User 갱신 => 프로필 수정
        suspend fun createUser(user: UserInfo): Boolean //사용자를 생성
        suspend fun deleteUser() //회원 탈퇴
        suspend fun getUser(token: String?, isFirst: Boolean): UserInfo?//사용자의 정보를 가져옴
        suspend fun updateBookWorm(token: String?, bookWorm: BookWorm)
        suspend fun getAlbums(token: String?): ArrayList<AlbumData> //사용자의 앨범 정보를 가져옴
        suspend fun updateAlbums(token: String?)
    }

    interface HandleChallenge {
        suspend fun getChallenges(token: String = "", keyword: String? = null, lastVisible: String = "", pageSize: Long = 5L): ArrayList<Challenge>? //챌린지 목록을 가져온다
        suspend fun createChallenge(challenge: Challenge)

    }
}