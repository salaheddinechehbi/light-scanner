package com.inducesmiles.projects.lightscanner.async;

import com.inducesmiles.projects.lightscanner.models.LocalPdf;

import java.util.List;

/**
 * Created by Lekan Adigun on 2/8/2018.
 */

public interface FileLoadCallback {

    void done(List<LocalPdf> list);
    void error(Throwable throwable);
}
