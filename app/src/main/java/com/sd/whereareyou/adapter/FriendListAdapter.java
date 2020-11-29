package com.sd.whereareyou.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sd.whereareyou.R;
import com.sd.whereareyou.models.FriendBlock;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendListViewHolder> {

    private List<FriendBlock> friendBlockList;
    private OnItemClickListener onItemClickListener;

    public FriendListAdapter(List<FriendBlock> list) {
        friendBlockList = list;
    }

    @NonNull
    @Override
    public FriendListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_card, parent, false);
        return new FriendListViewHolder(view, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListViewHolder holder, int position) {
        FriendBlock currentFriendBlock = friendBlockList.get(position);
        holder.tvUsername.setText(currentFriendBlock.getUserName());
        holder.tvUID.setText(currentFriendBlock.getUID());
        holder.tvTime.setText(currentFriendBlock.getTime());
    }

    @Override
    public int getItemCount() {
        return 3;
        //return friendBlockList.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public static class FriendListViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUsername;
        public TextView tvUID;
        public TextView tvTime;

        public FriendListViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            //information shows on card view(time is not using)
            tvUsername = itemView.findViewById(R.id.tvUserNameFriendCard);
            tvUID = itemView.findViewById(R.id.tvUIDFriendCard);
            tvTime = itemView.findViewById(R.id.tvTimeFriendCard);

            //check which position
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
