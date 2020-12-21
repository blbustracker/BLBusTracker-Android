package org.unibl.etf.blbustracker.datahandlers.database.announcement;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "announcement_table")
public class Announcement
{
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;
    private String publishedDate;
    @Ignore
    private boolean expanded = false;

    public Announcement(String title, String content, String publishedDate)
    {
        this.title = title;
        this.content = content;
        this.publishedDate = publishedDate;
    }

    //does announcement show content
    public boolean isExpanded()
    {
        return expanded;
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getPublishedDate()
    {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate)
    {
        this.publishedDate = publishedDate;
    }
}
