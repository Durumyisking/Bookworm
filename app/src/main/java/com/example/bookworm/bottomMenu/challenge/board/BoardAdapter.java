package com.example.bookworm.bottomMenu.challenge.board;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookworm.R;
import com.example.bookworm.databinding.SubactivityChallengeBoardItemBinding;

import java.util.ArrayList;


public class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnBoardItemClickListener {
    ArrayList<Board> boardList;
    Context context;
    SubactivityChallengeBoardItemBinding binding;
    OnBoardItemClickListener listener;

    public BoardAdapter(ArrayList<Board> data, Context c) {
        boardList = data;
        context = c;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.subactivity_challenge_board_item, parent, false);
        return new BoardAdapter.ItemViewHolder(view, listener);
    }

    public void setListener(OnBoardItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int safePosition = holder.getAdapterPosition();
        Board item = boardList.get(safePosition);
        ((BoardAdapter.ItemViewHolder) holder).setItem(item);

    }


    @Override
    public int getItemCount() {
        return boardList.size();
    }

    public void onItemClick(ItemViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        public ItemViewHolder(@NonNull View itemView, final OnBoardItemClickListener listener) {
            super(itemView);
            binding = SubactivityChallengeBoardItemBinding.bind(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    //리스너 인터페이스 구현
                    if (position != RecyclerView.NO_POSITION) {
                        if (listener != null) {
                            listener.onItemClick(ItemViewHolder.this, view, position);
                        }
                    }
                }
            });
        }

        public void setItem(Board item) {
            binding.tvBoardText.setText(item.getBoardText());
            if (item.isAllowed()) { //승인된 인증글이면 체크표시가 뜨게
                binding.ivAllow.setVisibility(View.VISIBLE);
            } else {
                binding.ivAllow.setVisibility(View.INVISIBLE);
            }
            Glide.with(context).load(item.getImgurl()).into(binding.ivThumb); //썸네일 설정
        }
    }


    class BoardDiffCallback extends DiffUtil.ItemCallback<Board> {

        @Override
        public boolean areItemsTheSame(@NonNull Board oldItem, @NonNull Board newItem) {
            return oldItem.getBoardID() == newItem.getBoardID();
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Board oldItem, @NonNull Board newItem) {
            return oldItem == newItem;
        }
    }
}
