package org.unibl.etf.blbustracker.navigationtabs.announcements;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.unibl.etf.blbustracker.R;
import org.unibl.etf.blbustracker.datahandlers.database.announcement.Announcement;
import org.unibl.etf.blbustracker.utils.ToolbarUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for displaying announcemtns
 */
public class AnnouncementsFragment extends Fragment
{
    private static final String TAG = "AnnouncementsFragment";

    private AnnouncementsViewModel announcementsViewModel;

    private RecyclerView recyclerView;
    private AnnouncementsAdapter announcementsAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        announcementsViewModel = new ViewModelProvider(this).get(AnnouncementsViewModel.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_announcements, container, false);
        
        String title = getString(R.string.announcements);
        ToolbarUtil.initToolbar(getActivity(), view, title);

        recyclerView = view.findViewById(R.id.announcement_recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        initRecyclerView(getContext());

        //called when content is fetched(changed) from database or server.
        announcementsViewModel.getAnnouncementsMutable().observe(getViewLifecycleOwner(), announcements ->
        {
            announcementsAdapter.setAllAnnouncements(announcements);
            announcementsAdapter.notifyDataSetChanged();
        });


        //swipe down to refresh
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresher);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue_darker);
        swipeRefreshLayout.setOnRefreshListener(() ->
        {
            if (announcementsViewModel != null)
            {
                announcementsViewModel.activatePoolExecutors();
                announcementsViewModel.startListening(getContext());
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        //show loading screen while fetching data from database/server and hide when data is ready
        announcementsViewModel.getIsUpdating().observe(getViewLifecycleOwner(), isUpdating ->
        {
            if (isUpdating)
                showProgressBar();
            else
            {
                hideProgressBar();
                //scrollViewToBottom(); // view should be at top
            }
        });

        return view;
    }

    private void initRecyclerView(Context context)
    {
        if (announcementsViewModel != null)
        {
            List<Announcement> announcements = announcementsViewModel.getAnnouncementsMutable().getValue();
            announcementsAdapter = new AnnouncementsAdapter(announcements, context);
        } else
        {
            announcementsAdapter = new AnnouncementsAdapter(new ArrayList<>(), context);
        }

        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(announcementsAdapter);
    }

    private void showProgressBar()
    {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar()
    {
        progressBar.setVisibility(View.GONE);
    }

    //scroll view to bottom.
    public void scrollViewToBottom()
    {
        recyclerView.smoothScrollToPosition(announcementsViewModel.getAnnouncementsMutable().getValue().size() - 1);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (announcementsViewModel != null)
        {
            announcementsViewModel.activatePoolExecutors();
        }
        if (recyclerView != null && announcementsAdapter != null)
            recyclerView.setAdapter(announcementsAdapter);

    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (announcementsViewModel != null)
            announcementsViewModel.shutdownPoolExecutorService();
        recyclerView.setAdapter(null);

    }
}









