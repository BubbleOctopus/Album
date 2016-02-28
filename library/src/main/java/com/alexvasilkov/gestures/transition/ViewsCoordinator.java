package com.alexvasilkov.gestures.transition;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.alexvasilkov.gestures.animation.ViewPosition;
import com.alexvasilkov.gestures.internal.GestureDebug;
import com.alexvasilkov.gestures.views.interfaces.AnimatorView;

/**
 * Main purpose of this class is to synchronize views of same item in two different sources
 * to correctly start or update view transition animation.
 * <p/>
 * I.e. we need to have both 'to' and 'from' views which represent same item in
 * {@link RecyclerView} and {@link ViewPager} to start transition between them.
 * But when {@link ViewPager} is scrolled we may also need to scroll {@link RecyclerView} to reveal
 * corresponding item's view.
 * <p/>
 * Method {@link #request(Object)} should be called when particular item needs to be synced.
 * This method will trigger methods {@link OnRequestViewListener#onRequestView(Object)} of
 * listeners set by {@link #setFromListener(OnRequestViewListener) setFromListener} and
 * {@link #setToListener(OnRequestViewListener) setToListener} methods.
 * <p/>
 * When views were requested this class starts waiting for 'from' and 'to' views to be provided
 * with {@link #setFromView(Object, View)} (or {@link #setFromPos(Object, ViewPosition)}) and
 * {@link #setToView(Object, AnimatorView)} methods. When both views are ready method
 * {@link OnViewsReadyListener#onViewsReady(Object)} will be triggered.
 */
public class ViewsCoordinator<ID> {

    private static final String TAG = ViewsCoordinator.class.getSimpleName();

    private OnRequestViewListener<ID> mFromListener, mToListener;
    private OnViewsReadyListener<ID> mReadyListener;

    private ID mRequestedId, mFromId, mToId;

    private View mFromView;
    private ViewPosition mFromPos;
    private AnimatorView mToView;

    public void setFromListener(@NonNull OnRequestViewListener<ID> listener) {
        mFromListener = listener;
    }

    public void setToListener(@NonNull OnRequestViewListener<ID> listener) {
        mToListener = listener;
    }

    public void setReadyListener(@Nullable OnViewsReadyListener<ID> listener) {
        mReadyListener = listener;
    }

    public void request(@NonNull ID id) {
        cleanupRequest();

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Requesting " + id);
        }

        mRequestedId = id;
        mFromListener.onRequestView(id);
        mToListener.onRequestView(id);
    }

    public View getFromView() {
        return mFromView;
    }

    public ViewPosition getFromPos() {
        return mFromPos;
    }

    public void setFromView(@NonNull ID id, @NonNull View fromView) {
        setFromInternal(id, fromView, null);
    }

    public void setFromPos(@NonNull ID id, @NonNull ViewPosition fromPos) {
        setFromInternal(id, null, fromPos);
    }

    private void setFromInternal(@NonNull ID id, View fromView, ViewPosition fromPos) {
        if (mRequestedId == null || !mRequestedId.equals(id)) {
            return;
        }
        if (mFromView == fromView && mFromPos == fromPos) {
            return; // Already set
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Setting 'from' view for " + id);
        }

        mFromId = id;
        mFromView = fromView;
        mFromPos = fromPos;
        notifyWhenReady();
    }

    public AnimatorView getToView() {
        return mToView;
    }

    public void setToView(@NonNull ID id, @NonNull AnimatorView toView) {
        if (mRequestedId == null || !mRequestedId.equals(id)) {
            return;
        }
        if (mToView == toView) {
            return; // Already set
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Setting 'to' view for " + id);
        }

        mToId = id;
        mToView = toView;
        notifyWhenReady();
    }

    private void notifyWhenReady() {
        if (mRequestedId == null || !mRequestedId.equals(mFromId) || !mRequestedId.equals(mToId)) {
            return;
        }

        onViewsReady(mRequestedId);
    }

    protected void cleanupRequest() {
        if (mRequestedId == null) {
            return;
        }

        if (GestureDebug.isDebugAnimator()) {
            Log.d(TAG, "Cleaning up request " + mRequestedId);
        }

        mFromView = null;
        mFromPos = null;
        mToView = null;
        mRequestedId = mFromId = mToId = null;
    }

    /**
     * Called when both 'from' and 'to' views are ready for given index. At this point
     * transition is ready to be started.
     * <p/>
     * Note, that this method will be called each time 'from' or 'to' views are changed.
     *
     * @see #getFromView()
     * @see #getFromPos()
     * @see #getToView()
     */
    protected void onViewsReady(@NonNull ID id) {
        if (mReadyListener != null) {
            mReadyListener.onViewsReady(id);
        }
    }


    public interface OnRequestViewListener<ID> {
        /**
         * Implementation should find corresponding {@link View} (or {@link ViewPosition})
         * for given {@code index} and provide it back to {@link ViewsCoordinator}.
         * <p/>
         * Note, that it may not be possible to provide view right now (i.e. because
         * we should scroll source view to reveal correct view), but it should be provided
         * as soon as it's ready.
         */
        void onRequestView(@NonNull ID id);
    }

    public interface OnViewsReadyListener<ID> {
        /**
         * Will be called when both 'from' and 'to' views for given item index are ready.
         * <p/>
         * Note, that this method will be called each time 'from' or 'to' views are changed.
         */
        void onViewsReady(@NonNull ID id);
    }

}
