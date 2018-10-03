package com.inducesmiles.projects.lightscanner.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;

/**
 * Created by Lekan Adigun on 2/8/2018.
 */

@Entity
public class LocalPdf {

    @PrimaryKey(autoGenerate = true)
    public long pdfId = 0;
    public String name = "";
    public String path = "";
    public String timeCreated = "";
    public String thumbPath = "";
}
