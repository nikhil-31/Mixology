package com.example.nik.mixology.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by nik on 12/17/2016.
 */

@ContentProvider(authority = DrinkProvider.AUTHORITY, database = DrinkDatabase.class)
public final class DrinkProvider {

    public static final String AUTHORITY =
            "com.example.nik.mixology.data.DrinkProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String ALCOHOLIC = "Alcoholic";
        String NON_ALCOHOLIC = "Non_Alcoholic";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = DrinkDatabase.ALCOHOLIC)
    public static class Alcoholic {
        @ContentUri(
                path = Path.ALCOHOLIC,
                type = "vnd.android.cursor.dir/drink")
        public static final Uri CONTENT_URI = buildUri(Path.ALCOHOLIC);

    }
    @TableEndpoint(table = DrinkDatabase.ALCOHOLIC)
    public static class NonAlcoholic {
        @ContentUri(
                path = Path.NON_ALCOHOLIC,
                type = "vnd.android.cursor.dir/drink")
        public static final Uri CONTENT_URI = buildUri(Path.NON_ALCOHOLIC);

    }




}
