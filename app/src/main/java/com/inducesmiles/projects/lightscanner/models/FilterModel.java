package com.inducesmiles.projects.lightscanner.models;

import android.graphics.Bitmap;

import com.inducesmiles.projects.lightscanner.utils.Util;

import net.alhazmy13.imagefilter.ImageFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lekan Adigun on 1/30/2018.
 */

public class FilterModel {

    public Bitmap bitmap;
    public ImageFilter.Filter filter = ImageFilter.Filter.GOTHAM;

    public static final ImageFilter.Filter[] allFilters =
            {ImageFilter.Filter.GOTHAM, ImageFilter.Filter.GRAY,
            ImageFilter.Filter.RELIEF,
            ImageFilter.Filter.NEON,
            ImageFilter.Filter.TV,
            ImageFilter.Filter.INVERT,
            ImageFilter.Filter.BLOCK,
            ImageFilter.Filter.OLD,
            ImageFilter.Filter.SHARPEN,
            ImageFilter.Filter.SKETCH,};

    public FilterModel() {}

    public static List<FilterModel> create(String path) {

        Bitmap bitmap = Util.decodeImageFromFiles(path, 600, 600);
        List<FilterModel> models = new ArrayList<>();

        for (int i = 0; i < allFilters.length; i++) {
            FilterModel model = new FilterModel();
            model.bitmap = bitmap;
            model.filter = allFilters[i];

            models.add(model);
        }

        return models;
    }
}
