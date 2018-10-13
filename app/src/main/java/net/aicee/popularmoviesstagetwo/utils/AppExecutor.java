package net.aicee.popularmoviesstagetwo.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public class AppExecutor implements Executor {
    @Override
    public void execute(@NonNull Runnable runnable) {
        new Thread(runnable).start();
    }
}