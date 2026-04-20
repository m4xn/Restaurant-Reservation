package com.bookstore.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple in-process notifier to refresh dashboard data after CRUD actions.
 */
public final class DashboardRefreshNotifier {

    private static final List<Runnable> listeners = new ArrayList<>();

    private DashboardRefreshNotifier() {}

    public static void subscribe(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public static void unsubscribe(Runnable listener) {
        listeners.remove(listener);
    }

    public static void notifyRefresh() {
        List<Runnable> snapshot = new ArrayList<>(listeners);
        snapshot.forEach(Runnable::run);
    }
}
