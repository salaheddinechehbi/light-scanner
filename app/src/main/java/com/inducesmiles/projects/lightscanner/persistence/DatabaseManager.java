package com.inducesmiles.projects.lightscanner.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.inducesmiles.projects.lightscanner.models.LocalPdf;

/**
 * Created by Lekan Adigun on 2/13/2018.
 */

@Database(entities = {LocalPdf.class}, version = 1)
public abstract class DatabaseManager extends RoomDatabase {

    public abstract LocalPdfDao localPdfDao();
}
