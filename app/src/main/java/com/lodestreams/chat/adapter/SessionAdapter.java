package com.lodestreams.chat.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lodestreams.chat.ChatApplication;
import com.lodestreams.chat.R;
import com.lodestreams.chat.activity.ChatActivity;
import com.lodestreams.chat.bean.Constant;
import com.lodestreams.chat.greendao.entity.Room;
import com.lodestreams.chat.util.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.socket.client.Ack;

import static android.support.v7.widget.StaggeredGridLayoutManager.TAG;

/**
 * Created by PuPeng on 16/7/24.
 */
public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {

    List<Room> mList;
    Context mContext;

    public SessionAdapter(Context context, List<Room> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session_room,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextName.setText(mList.get(position).getUserName());
        holder.mTextTime.setText(mList.get(position).getLastMessageTime());
        holder.mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatActivity.startChatActivity((Activity)mContext,mList.get(position).getRoomId(),mList.get(position).getId(),mList.get(position).getUserName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_session_linear_container)
        LinearLayout mLinearLayout;
        @BindView(R.id.item_session_text_name)
        TextView mTextName;
        @BindView(R.id.item_session_text_time)
        TextView mTextTime;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


}
