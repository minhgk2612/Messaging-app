package com.example.myappchat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private Context context;
    private List<MessageModel> messageModelList;

    public MessageAdapter(Context context) {
        this.context = context;
        this.messageModelList = new ArrayList<>();
    }

    public void add(MessageModel messageModel) {
        messageModelList.add(messageModel);
        notifyDataSetChanged();
        Log.d("MessageAdapter", "Message added. New size: " + messageModelList.size());
    }

    public void clear() {
        messageModelList.clear();
        notifyDataSetChanged();
        Log.d("MessageAdapter", "Messages cleared. New size: " + messageModelList.size());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.message_row_sent, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.message_row_received, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MyViewHolder holder, int position) {
        MessageModel messageModel = messageModelList.get(position);

        if (messageModel.getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            if (holder.textViewSentMessage != null) {
                holder.textViewSentMessage.setText(messageModel.getMessage());
            }
        } else {
            if (holder.textViewReceivedMessage != null) {
                holder.textViewReceivedMessage.setText(messageModel.getMessage());
            }
        }
    }


    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messageModelList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public void addAll(List<MessageModel> messages) {
        messageModelList.addAll(messages);
        notifyDataSetChanged();
        Log.d("MessageAdapter", "Messages added. New size: " + messageModelList.size());
    }

    // ... (Các phần mã nguồn khác không thay đổi)

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewSentMessage, textViewReceivedMessage;

        // Thêm ánh xạ cho textViewReceivedMessage
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSentMessage = itemView.findViewById(R.id.textViewSentMessage);
            textViewReceivedMessage = itemView.findViewById(R.id.textViewReceivedMessage);
        }
    }
}
