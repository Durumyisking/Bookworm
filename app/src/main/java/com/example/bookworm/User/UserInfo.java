package com.example.bookworm.User;

import android.net.Uri;
import android.util.Log;

import com.example.bookworm.Bw.enum_wormtype;
import com.example.bookworm.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;

import java.io.Serializable;
import java.util.GregorianCalendar;
import java.util.Vector;

import io.reactivex.Observer;

public class UserInfo implements Serializable {

    private String profileimg; // 회원가입시 프로필사진
    private String username; // 회원가입시 닉네임
    private String email; // 로그인한 이메일
    private String platform;
    private String token;

    private GregorianCalendar today;



    private int register_year;




    private enum_wormtype wormtype = enum_wormtype.디폴트;


    int enum_size = enum_wormtype.values().length;



    // wormtype을 년별로 저장할 책볼레타입 리스트
    private Vector<Integer> wormvec = new Vector<Integer>();

    // 이미지 슬라이더의 함수에 사용할 책볼레 이미지 경로 벡터
    private Vector<String> wormimgvec = new Vector<String>();


    // genre index의 1번 = enum_wormtype의 공포 2번은 추리 .... 이하 동문
    // 가장 최댓값을 가진 index를 벌레의 종류로 설정할 계획
    // 가장 최댓값의 index의 번호에 따라 최고 선호 장르를 설정합니다.
    private int [] genre = new int [enum_size];

    public UserInfo() {

        today = new GregorianCalendar();
    }

    public void add(UserAccount kakaoAccount) {
        Profile profile = kakaoAccount.getProfile();
        this.profileimg = profile.getProfileImageUrl();
        this.username = profile.getNickname();
        this.email = kakaoAccount.getEmail();
        this.platform = "Kakao";


    }

    public void add(GoogleSignInAccount account) {
        try {
            Log.d("profile", account.getPhotoUrl().toString());
            this.profileimg = account.getPhotoUrl().toString();
        } catch (NullPointerException e) {
            Log.d("profile", "Null");
        }
        this.username= account.getDisplayName();
        this.email=account.getEmail();
        this.platform="Google";

    }

    public void Initbookworm()
    {
        // 가입년도
        this.register_year = today.get(today.YEAR);
        this.wormtype = enum_wormtype.디폴트;
        this.wormvec.add(wormtype.value());
        // 이미지 슬라이더 함수를 사용하기 위해 int 경로인 drawble을 string으로 변환해준다
        this.wormimgvec.add(Uri.parse("android.resource://" + R.class.getPackage().getName() + "/" + R.drawable.ex_default).toString());
    }

    public void add(DocumentSnapshot document) {
        this.username=(String) document.get("user_name");
        this.email=(String) document.get("email");
        this.profileimg=(String) document.get("profileURL");
        this.token=document.getId();
    }

    public String getProfileimg() {
        return profileimg;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPlatform() {
        return platform;
    }

    public enum_wormtype getWormtype() {
        return wormtype;
    }

    public void setWormtype(enum_wormtype wormtype) {
        this.wormtype = wormtype;
    }

    public int[] getGenre() {
        return genre;
    }

    public void setGenre(int idx) { this.genre[idx]++; } // 로컬에서 ++해주는게 아니라 서버에 업데이트 해야한다

    public Vector<Integer> getWormvec() {  return wormvec; }

    public Vector<String> getWormimgvec() { return wormimgvec; }

    public void setToken(String token) {
        this.token = token;
    }
    public String getToken(){
        return this.token;
    }

    public int getRegister_year() {
        return register_year;
    }


}