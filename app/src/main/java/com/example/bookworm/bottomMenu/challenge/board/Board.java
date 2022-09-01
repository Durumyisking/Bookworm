package com.example.bookworm.bottomMenu.challenge.board;


import com.example.bookworm.bottomMenu.search.items.book.Book;

import java.io.Serializable;
import java.util.Map;

public class Board implements Serializable {
    private String boardID; //인증글 ID
    private String imgurl; //업로드 이미지 url
    private String boardText; //인증글 내용
    private String date; //현재 날짜
    private String userToken; //작성자 토큰
    private String challengeName; //챌린지 명
    private String masterToken; //챌린지 생성자 토큰
    private Book book; //챌린지에 사용된 책
    private long likeCount; //좋아요 수
    private long commentsCount; //사용자 댓글 수
    private boolean allowed; //인증글 승인 여부


    public Board(Map data) {
        book = new Book(null);
        if (data != null) {
            this.boardID = (String) data.get("boardID");
            if (data.get("imgurl") != null) this.imgurl = (String) data.get("imgurl");
            this.boardText = (String) data.get("boardText");
            this.date = (String) data.get("date");
            this.userToken = (String) data.get("UserToken");
            this.challengeName = (String) data.get("challengeName");
            this.masterToken = (String) data.get("masterToken");
            this.book.setBook((Map) data.get("book"));
            this.likeCount = (long) data.get("likeCount");
            this.commentsCount = (long) data.get("commentsCount");
            this.allowed = (boolean) data.get("allowed");
        }
    }

    public String getBoardID() {
        return boardID;
    }

    public String getImgurl() {
        return imgurl;
    }

    public String getBoardText() {
        return boardText;
    }

    public String getDate() {
        return date;
    }

    public String getUserToken() {
        return userToken;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public Book getBook() {
        return book;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public String getMasterToken() {
        return masterToken;
    }

    public boolean isAllowed() {
        return allowed;
    }
}
