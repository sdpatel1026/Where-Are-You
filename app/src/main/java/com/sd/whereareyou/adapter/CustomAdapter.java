package com.sd.whereareyou.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.library.bubbleview.BubbleTextView;
import com.sd.whereareyou.R;
import com.sd.whereareyou.models.Chat;

import java.util.List;


public class CustomAdapter extends BaseAdapter {

    private static final String TAG = CustomAdapter.class.getName();
    private static LayoutInflater layoutInflater;
    private List<Chat> chatsList;
    private Context context;

    public CustomAdapter(Context context, List<Chat> list_chats) {
        chatsList = list_chats;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return chatsList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            Log.d(TAG, "getView(): " + position);
            Log.d(TAG, "getView(): " + chatsList.get(position));
            if (chatsList.get(position).isSend()) {
                convertView = layoutInflater.inflate(R.layout.bubble_item_sent, null);
                BubbleTextView tvMessage = (BubbleTextView) convertView.findViewById(R.id.tvBubbleSent);
                tvMessage.setText(chatsList.get(position).getMessage());
            } else {
                convertView = layoutInflater.inflate(R.layout.bubble_item_received, null);
                BubbleTextView tvMessage = (BubbleTextView) convertView.findViewById(R.id.tvBubbleRecieved);
                tvMessage.setText(chatsList.get(position).getMessage());
            }

        }
        return convertView;
    }
}