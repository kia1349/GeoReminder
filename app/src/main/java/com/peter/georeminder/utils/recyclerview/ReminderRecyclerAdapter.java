package com.peter.georeminder.utils.recyclerview;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.peter.georeminder.R;
import com.peter.georeminder.models.Reminder;

import java.util.List;

/**
 * Created by Peter on 10/6/15.
 * Adapter for the list of reminders on the first tap of ViewPager
 */
public class ReminderRecyclerAdapter extends RecyclerView.Adapter<ReminderRecyclerAdapter.RecyclerViewHolder> {
    private List<Reminder> reminderList;
    private Context context;
    private LayoutInflater inflater;

    /**
     * Implement OnClick and OnLongClick
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    // implement onItemClick and onItemLongClick
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }





    public ReminderRecyclerAdapter(Context context, List<Reminder> reminderList){
        this.reminderList = reminderList;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ReminderRecyclerAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.layout_reminder_recycler_item, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        //TODO: do what? figure out later
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder holder, final int position) {
        holder.mapScreenshot.setImageResource(R.mipmap.ic_launcher);
        holder.reminderTitle.setText(getItem(position).getTitle());
        holder.reminderContent.setText("Content Content Content Content Content");
        // TODO: Change this to use information from reminderList


        // set OnItemClick/LongClick listener
        // implement in calling activity (in this case, MainScreen)

        if(listener != null){
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder.cardView, position);
                }
            });

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.onItemLongClick(holder.cardView, position);
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Reminder getItem(int position){
        return reminderList.get(position);
    }

    public void addReminder(int position, Reminder addedReminder){
        //TODO: what kind of reminder and where to add them
        reminderList.add(position, addedReminder);
        notifyItemInserted(position);
    }

    public void deleteReminder(int position){
        reminderList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged((int) getItemId(position), getItemCount() + 1);
    }




    /**
     * ViewHolder
     */
    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        ImageView mapScreenshot;
        TextView reminderTitle;
        TextView reminderContent;

        public RecyclerViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.reminder_holder_cardview);
            mapScreenshot = (ImageView) itemView.findViewById(R.id.reminder_item_map_screenshot);
            reminderTitle = (TextView) itemView.findViewById(R.id.reminder_recycler_item_title);
            reminderContent = (TextView) itemView.findViewById(R.id.reminder_recycler_item_content);
        }
    }
}