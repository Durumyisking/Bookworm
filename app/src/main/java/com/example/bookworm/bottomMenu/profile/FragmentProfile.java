package com.example.bookworm.bottomMenu.profile;


import static android.app.Activity.RESULT_CANCELED;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.bookworm.R;
import com.example.bookworm.bottomMenu.profile.submenu.SubMenuPagerAdapter;
import com.example.bookworm.bottomMenu.profile.views.ProfileSettingActivity;
import com.example.bookworm.chat.newchat.Activity_chatlist;
import com.example.bookworm.core.userdata.UserInfo;
import com.example.bookworm.databinding.FragmentProfileBinding;
import com.example.bookworm.bottomMenu.profile.follow.modules.FollowViewModel;
import com.example.bookworm.bottomMenu.profile.follow.view.FollowerActivity;

public class FragmentProfile extends Fragment implements LifecycleObserver {

    private FragmentProfileBinding binding;
    private Context current_context;
    UserInfoViewModel pv;
    Boolean isUpdate = false; //업데이트만 하는 경우
    FollowViewModel fv;
    private UserInfo NowUser;
    SubMenuPagerAdapter menuPagerAdapter;
    public ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_CANCELED)
                    pv.getUser(NowUser.getToken(), true);
                if (result.getResultCode() == ProfileSettingActivity.MODIFY_OK) {
                    pv.getUser(NowUser.getToken(), true);
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        current_context = getActivity();
        assert current_context != null;
        pv = new ViewModelProvider(this, new UserInfoViewModel.Factory(current_context)).get(UserInfoViewModel.class);
        fv = new ViewModelProvider(this, new FollowViewModel.Factory(current_context)).get(FollowViewModel.class);
        //서브 메뉴를 보여주기위한 어댑터
        menuPagerAdapter = new SubMenuPagerAdapter(null, getChildFragmentManager());


//뷰모델 안에서 데이터가 배치된다.
        binding.btnSetting.setOnClickListener(view1 -> {
            Intent intent = new Intent(current_context, ProfileSettingActivity.class);
            startActivityResult.launch(intent);
//            startActivity(intent);
        });


        binding.btnChatlist.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), Activity_chatlist.class);
            startActivity(intent);
        });


        binding.subMenuViewPager.setAdapter(menuPagerAdapter);
        binding.tabLayout.setupWithViewPager(binding.subMenuViewPager);
        binding.tabLayout.getTabAt(1).setText("앨범");
        binding.tabLayout.getTabAt(0).setText("포스트");
        binding.tabLayout.getTabAt(0).select();
        //데이터를 가져옴
        pv.getUser(null, false);
        isUpdate = true;
        pv.getUser(null, true);

        //데이터 수정을 감지함
        pv.getUserInfoLiveData().observe(getViewLifecycleOwner(), userinfo -> {
            NowUser = userinfo;
            pv.getBookWorm(NowUser.getToken());
            if (!isUpdate) setUI(NowUser);
            else updateUI(NowUser);
        });
        pv.getBwdata().observe(getViewLifecycleOwner(), bookWorm -> {
            binding.tvReadBookCount.setText(String.valueOf(bookWorm.getReadCount()));
            int id = current_context.getResources().getIdentifier("bw_" + bookWorm.getWormType(), "drawable", current_context.getPackageName());
            binding.ivBookworm.setImageResource(id);
        });


        return view;
    }

    //프로필 화면으로 돌아올 떄만 실행되는 메소드
    private void updateUI(UserInfo user) {
        binding.tvFollowerCount.setText(String.valueOf(user.getFollowerCounts()));
        binding.tvFollowingCount.setText(String.valueOf(user.getFollowingCounts()));
        //진행이 다끝나면 변수를 off
        isUpdate = false;
    }

    private void setUI(UserInfo user) {
        Glide.with(current_context)
                .load(user.getProfileimg())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .into(binding.imgFragProfileProfile);
        //프로필사진 로딩후 삽입.
        binding.tvFollowerCount.setText(String.valueOf(user.getFollowerCounts()));
        binding.tvFollowingCount.setText(String.valueOf(user.getFollowingCounts()));
        binding.tvNickname.setText(user.getUsername());
        binding.edtIntroduce.setText(user.getIntroduce());


        //팔로워액티비티 실행하기
        binding.btnFollower.setOnClickListener(view -> {
            Intent intent = new Intent(current_context, FollowerActivity.class);
            intent.putExtra("token", user.getToken());
            intent.putExtra("page", 0);
            startActivityResult.launch(intent);
        });

        //팔로잉액티비티 실행하기
        binding.btnFollowing.setOnClickListener(view -> {
            Intent intent = new Intent(current_context, FollowerActivity.class);
            intent.putExtra("token", user.getToken());
            intent.putExtra("page", 1);
            startActivityResult.launch(intent);
        });

    }

    //뷰보다 프레그먼트의 생명주기가 길어서, 메모리 누수 발생가능
    //누수 방지를 위해 뷰가 Destroy될 때, binding값을 nullify 함.
    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        pv.getUser(null, true);
        isUpdate = true;
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            menuPagerAdapter.getItem(0).onResume();
            pv.getUser(null, true);
            isUpdate = true;
        }
        super.onHiddenChanged(hidden);
    }

    //메달 표시 유무에 따른 세팅
    private void setMedal(UserInfo userInfo) {
        if (userInfo.getMedalAppear()) { //메달을 표시한다면
            binding.ivMedal.setVisibility(View.VISIBLE);
            switch (Integer.parseInt(String.valueOf(userInfo.getTier()))) { //티어 0 ~ 5에 따라 다른 메달이 나오게
                case 1:
//                    binding.ivMedal.setImageResource(R.drawable.medal_bronze);
                    break;
                case 2:
                    binding.ivMedal.setImageResource(R.drawable.medal_silver);
                    break;
                case 3:
                    binding.ivMedal.setImageResource(R.drawable.medal_gold);
                    break;
                case 4:
//                    binding.ivMedal.setImageResource(R.drawable.medal_platinum);
                    break;
                case 5:
//                    binding.ivMedal.setImageResource(R.drawable.medal_diamond);
                    break;
                default: //티어가 없을때
                    binding.ivMedal.setImageResource(0);
            }
        } else { //메달을 표시하지 않을거라면
            binding.ivMedal.setVisibility(View.GONE);
            binding.ivMedal.setImageResource(0);
        }
    }


}
