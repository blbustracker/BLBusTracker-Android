package org.unibl.etf.blbustracker.datahandlers.jsonhandlers;

import org.unibl.etf.blbustracker.datahandlers.database.announcement.Announcement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementJSON extends JSONHandler
{

    private final String NEWS_JSONARRAY = "news";
    private final String TITLE = "title";
    private final String CONTENT = "content";
    private final String PUBLISHED = "published";

    public AnnouncementJSON(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    //convert JSONArray to List<Announcement>
    public List<Announcement> getAllAnnouncements()
    {
        List<Announcement> announcements = null;

        JSONArray jsonArray = super.getJsonArray(NEWS_JSONARRAY);
        if (jsonArray != null)
        {
            announcements = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++)
            {
                try
                {
                    JSONObject announcementObj = jsonArray.getJSONObject(i);
                    String title = announcementObj.getString(TITLE);
                    String content = announcementObj.getString(CONTENT);
                    String published = announcementObj.getString(PUBLISHED);

                    announcements.add(new Announcement(title, content, published));
                } catch (JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        return announcements;
    }

}
