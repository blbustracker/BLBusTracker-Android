package org.unibl.etf.blbustracker.navigationtabs.announcements;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.unibl.etf.blbustracker.Constants;
import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.navigationtabs.mapview.arrivaltimedialog.TimeUtil;
import org.unibl.etf.blbustracker.datahandlers.database.announcement.Announcement;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder>
{
    private List<Announcement> allAnnouncements;
    private Context context;

    class AnnouncementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        ConstraintLayout expandableLayout;
        ImageView expandButton;
        TextView titleTextView;
        TextView contentTextView;
        TextView dateTextView;
        View listView;
        ConstraintLayout itemConstraintLayout;

        // Inner constructor
        public AnnouncementViewHolder(View itemView)
        {
            super(itemView);
            listView = itemView;

            expandableLayout = itemView.findViewById(R.id.expandableLayout);
            itemConstraintLayout = itemView.findViewById(R.id.announcement_constraintlayout);

            titleTextView = itemView.findViewById(R.id.title_text_view);
            contentTextView = itemView.findViewById(R.id.content_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            expandButton = itemView.findViewById(R.id.expand_button);

            expandButton.setOnClickListener(v -> titleTextView.callOnClick());  // clicking on Image behaves same as clicking on title
            dateTextView.setOnClickListener(this);
            titleTextView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v)
        {
            Announcement announcement = allAnnouncements.get(getAdapterPosition());
            announcement.setExpanded(!announcement.isExpanded());
            notifyItemChanged(getAdapterPosition());
        }
    }

    //outter constructor
    public AnnouncementsAdapter(List<Announcement> allAnnouncements, Context context)
    {
        if (allAnnouncements == null)
            allAnnouncements = new ArrayList<>();
        this.allAnnouncements = allAnnouncements;
        this.context = context;
    }

    public void setAllAnnouncements(List<Announcement> allAnnouncements)
    {
        this.allAnnouncements = allAnnouncements;
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);

        return new AnnouncementViewHolder(view);
    }

    //set content to a item_announcement and expand/collapse on click
    //and alternating color
    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position)
    {
        Announcement currentData = allAnnouncements.get(position);
        //holder.setIsRecyclable(true);

        int colorId = (position % 2 == 0) ? R.color.busline_lighter_blue : R.color.busline_darker_blue;
        holder.listView.setBackgroundColor(ContextCompat.getColor(context, colorId));
        holder.itemConstraintLayout.setBackgroundColor(ContextCompat.getColor(context, colorId));

        holder.titleTextView.setText(currentData.getTitle());
        holder.contentTextView.setText(currentData.getContent());

        String publishedDate = currentData.getPublishedDate();
        String formatedDate = TimeUtil.formatInputDate(publishedDate, Constants.SERVER_DATE_FORMAT, Constants.MY_DATE_FORMAT);
        holder.dateTextView.setText(formatedDate);

        boolean isExpanded = allAnnouncements.get(position).isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);   // expand/collapse

        //switch image for expand/collapse
        if (isExpanded)
            holder.expandButton.setImageResource(R.drawable.ic_up_arrow);
        else
            holder.expandButton.setImageResource(R.drawable.ic_down_arrow);

    }

    @Override
    public int getItemCount()
    {
        return allAnnouncements.size();
    }

}
