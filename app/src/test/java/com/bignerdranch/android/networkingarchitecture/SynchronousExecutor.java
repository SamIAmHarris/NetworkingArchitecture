package com.bignerdranch.android.networkingarchitecture;

import java.util.concurrent.Executor;

/**
 * Created by SamMyxer on 4/4/16.
 */
public class SynchronousExecutor implements Executor {
    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
