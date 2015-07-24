package com.raizlabs.datahub.util;

import android.os.Handler;
import android.os.Looper;

public class ThreadingUtils {

    private static Handler uiHandler;

    /**
     * @return A {@link Handler} that is bound to the UI thread.
     */
    public static Handler getUIHandler() {
        if (uiHandler == null) uiHandler = new Handler(Looper.getMainLooper());
        return uiHandler;
    }

    /**
     * Returns true if this function was called on the thread the given
     * {@link Handler} is bound to.
     * @param handler The {@link Handler} to check the thread of.
     * @return True if this function was called on the {@link Handler}'s
     * thread.
     */
    public static boolean isOnHandlerThread(Handler handler) {
        Looper handlerLooper = handler.getLooper();
        if (handlerLooper != null) {
            return handlerLooper.equals(Looper.myLooper());
        }
        return false;
    }

    /**
     * Runs the given {@link Runnable} on the thread the given {@link Handler}
     * is bound to. This will execute immediately, before this function returns,
     * if this function was already called on the given {@link Handler}'s thread.
     * Otherwise, the {@link Runnable} will be posted to the {@link Handler}.
     * @param handler The {@link Handler} to run the action on.
     * @param action The {@link Runnable} to execute.
     * @return True if the action was already executed before this funcion
     * returned, or false if the action was posted to be handled later.
     */
    public static boolean runOnHandler(Handler handler, Runnable action) {
        if (isOnHandlerThread(handler)) {
            action.run();
            return true;
        } else {
            handler.post(action);
            return false;
        }
    }
}
