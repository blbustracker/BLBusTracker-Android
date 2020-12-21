package org.unibl.etf.blbustracker.datahandlers.database.announcement;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AnnouncementDao
{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Announcement announcement);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Announcement> announcements);

    @Query("SELECT * FROM announcement_table")
    List<Announcement> getAllAnnouncement();

    @Query("DELETE FROM announcement_table")
    void deleteAllAnnouncements();

}
