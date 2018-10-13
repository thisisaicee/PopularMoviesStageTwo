package net.aicee.popularmoviesstagetwo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import net.aicee.popularmoviesstagetwo.R;

public class Preferences {
    public static int getSorting(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getString(R.string.pref_sort_key), 0);
    }

    public static void setSorting(Context context, int selectedItem) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.pref_sort_key), selectedItem);
        editor.apply();
    }

    public static int getChangedMovie(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getInt(context.getString(R.string.pref_changed_movie), -1);
    }

    public static void setChangedMovie(Context context, int movieNumber) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(context.getString(R.string.pref_changed_movie), movieNumber);
        editor.apply();
    }
}