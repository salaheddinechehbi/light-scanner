package com.inducesmiles.projects.lightscanner.persistence;

import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.inducesmiles.projects.lightscanner.LightScannerApplication;

/**
 * Created by Lekan Adigun on 2/13/2018.
 */

public class RepoManager {

    private static RepoManager manager;
    private DatabaseManager databaseManager;

    private RepoManager() {

        Context context =
                LightScannerApplication.getApplication().getApplicationContext();
        databaseManager =
                Room.databaseBuilder(context, DatabaseManager.class, "LightScanner.db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public static synchronized RepoManager manager() {
        if (manager == null)
            manager = new RepoManager();

        return manager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
