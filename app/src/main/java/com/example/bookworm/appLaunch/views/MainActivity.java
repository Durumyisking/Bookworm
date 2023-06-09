package com.example.bookworm.appLaunch.views;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.bookworm.R;
import com.example.bookworm.bottomMenu.feed.FragmentFeed;
import com.example.bookworm.bottomMenu.feed.comments.SubActivityComment;
import com.example.bookworm.bottomMenu.profile.UserInfoViewModel;
import com.example.bookworm.core.MoveFragment;
import com.example.bookworm.core.userdata.UserInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class MainActivity extends AppCompatActivity {


    Fragment Fragment_feed, fragment_search, fragment_bookworm, fragment_challenge, fragment_profile;
    Fragment[] fragments = {Fragment_feed, fragment_search, fragment_bookworm, fragment_challenge, fragment_profile};
    // 위험 권한을 부여할 권한 지정

    FragmentManager fragmentManager;

    MoveFragment MoveFragment = new MoveFragment();

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            Rect rect = new Rect();
            getCurrentFocus().getGlobalVisibleRect(rect);
            if (!rect.contains(Math.round(ev.getX()), Math.round(ev.getY()))) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                getCurrentFocus().clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom);
        UserInfoViewModel viewModel = new ViewModelProvider(this, new UserInfoViewModel.Factory(this)).get(UserInfoViewModel.class);
        bottomNavigationView.setItemIconTintList(null);
        MutableLiveData<UserInfo> liveData = new MutableLiveData<>();
        viewModel.getUser(null, liveData, false);
        liveData.observe(this, userInfo -> {
            Toast toast = Toast.makeText(MainActivity.this, String.format("%s님 환영합니다!", userInfo.getUsername()), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        });


        //딥링크로 연결되는 경우 처리되는 코드들
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink;
                        Intent intent;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            if (deepLink.getLastPathSegment().equals("profile")) {
                                intent = new Intent(MainActivity.this, SubActivityComment.class);
                                intent.putExtra("userID", deepLink.getQueryParameter("uid"));
                                Log.d("params", deepLink.getQueryParameter("uid"));
                                startActivity(intent);
//                                bottomNavigationView.setSelectedItemId(R.id.tab_profile);
                            }
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("error", "getDynamicLink:onFailure", e);
                    }
                });
        // Get deep link from result (may be null if no link is found)

        //딥링크 코드 end

        // 초기화면 설정
        fragments[0] = new FragmentFeed();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragments[0], "0").commitAllowingStateLoss();


        bottomNavigationView.setOnItemSelectedListener(
                item -> {
                    switch (item.getItemId()) {
                        case R.id.tab_feed:
                            MoveFragment.show_fragment(fragments, fragmentManager, 0);
                            return true;


                        case R.id.tab_search:
                            MoveFragment.show_fragment(fragments, fragmentManager, 1);
                            return true;


                        case R.id.tab_bookworm:
                            MoveFragment.show_fragment(fragments, fragmentManager, 2);
                            return true;


                        case R.id.tab_challenge:
                            MoveFragment.show_fragment(fragments, fragmentManager, 3);
                            return true;


                        case R.id.tab_profile:
                            MoveFragment.show_fragment(fragments, fragmentManager, 4);
                            return true;
                    }
                    return false;
                });

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("애플리케이션을 종료하시겠습니까?")
                .setPositiveButton(
                        "네", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                            super.onBackPressed();
                        }
                ).setNegativeButton("아니오", (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }
}