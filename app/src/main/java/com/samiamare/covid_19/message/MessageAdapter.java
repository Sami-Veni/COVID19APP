package com.samiamare.covid_19.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.samiamare.covid_19.R;
import com.samiamare.covid_19.ui.slideshow.SlideshowFragment;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    SlideshowFragment chatActivity;
    ArrayList<Message> messageList;
    int ITEM_RECEIVE = 1;
    int ITEM_SENT = 2;

    public MessageAdapter(SlideshowFragment chatActivity, ArrayList<Message> messageList) {
        this.chatActivity = chatActivity;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.received, parent,false);
            return new ReceiveViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.sent), parent, false);
            return new SentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message currentMessage = messageList.get(position);
        if (holder.getClass() == SentViewHolder.class){
            SentViewHolder viewHolder = (SentViewHolder) holder;
            ((SentViewHolder) holder).sentMessage.setText(currentMessage.message);
        }
        else{
            ReceiveViewHolder viewHolder = (ReceiveViewHolder) holder;
            ((ReceiveViewHolder) holder).receiveMessage.setText(currentMessage.message);
        }
    }

    @Override
    public int getItemViewType(int position) {

        Message currentMessage = messageList.get(position);
        if (currentMessage.uid.equals("question")){
            return ITEM_SENT;
        } else{
            return ITEM_RECEIVE;
        }
    }
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        TextView sentMessage;
        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMessage = itemView.findViewById(R.id.sentMessage);
        }
    }
    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        TextView receiveMessage;
        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            receiveMessage = itemView.findViewById(R.id.receivedMessage);
        }
    }
}
