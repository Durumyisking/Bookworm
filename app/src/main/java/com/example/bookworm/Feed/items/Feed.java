package com.example.bookworm.Feed.items;

import com.example.bookworm.Search.items.Book;
import com.example.bookworm.User.UserInfo;

import java.util.ArrayList;

public class Feed {
    private Book book; // 선택한 책
    //라벨 Array 추가
    //업로드 이미지 url
    private int userRating; //책볼레 사용자 평점
    private String feedText; //피드의 내용
    private String date; //현재 날짜
    private int likeCount;//좋아요 수
    private UserInfo Creator; //작성자 정보
    private ArrayList<String> label; //라벨 목록


    //comment: 작성자 데이터, 작성한 댓글 내용  {"usertoken":20200, "data": "안녕하세요"} => Comment 객체  => Arraylist<Comment> =[ Comment들 ];
    // 한 피드의 댓글 목록  [{"usertoken":20200, "data": "안녕하세요", add:[{ }]},{"usertoken":20100, "data": "안녕하세요"}]
    public Feed(){}

}
