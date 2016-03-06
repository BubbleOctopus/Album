package app.com.cn.album.adapter;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;

import java.util.List;

import app.com.cn.album.dao.Photo;
import app.com.cn.album.net.GlideHelper;
import app.com.cn.album.net.R;

public class FlickrPhotoPagerAdapter
        extends RecyclePagerAdapter<FlickrPhotoPagerAdapter.ViewHolder> {

    private static final long PROGRESS_DELAY = 300L;

    private final ViewPager mViewPager;
    private List<Photo> mPhotos;

    private boolean mActivated;

    public FlickrPhotoPagerAdapter(ViewPager viewPager) {
        mViewPager = viewPager;
    }

    public void setPhotos(List<Photo> photos) {
        mPhotos = photos;
        notifyDataSetChanged();
    }

    public Photo getPhoto(int pos) {
        return mPhotos == null || pos < 0 || pos >= mPhotos.size() ? null : mPhotos.get(pos);
    }

    /**
     * To prevent ViewPager from holding heavy views (with bitmaps)  while it is not showing
     * we may just pretend there are no items in this adapter ("activate" = false).
     * But once we need to run opening animation we should "activate" this adapter again.<br/>
     * Adapter is not activated by default.
     */
    public void setActivated(boolean activated) {
        if (mActivated != activated) {
            mActivated = activated;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return !mActivated || mPhotos == null ? 0 : mPhotos.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final ViewHolder holder = new ViewHolder(container);
        holder.image.getController().getSettings().setFillViewport(true).setMaxZoom(3f);
        holder.image.getController().enableScrollInViewPager(mViewPager);
        holder.image.getPositionAnimator().addPositionUpdateListener(
                new ViewPositionAnimator.PositionUpdateListener() {
                    @Override
                    public void onPositionUpdate(float state, boolean isLeaving) {
                        holder.progress.setVisibility(state == 1f ? View.VISIBLE : View.INVISIBLE);
                    }
                });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

//        holder.image.getController().getSettings()
//                .setPanEnabled(false)
//                .setZoomEnabled(true)
//                .setDoubleTapEnabled(true)
//                .setOverscrollDistance(holder.image.getContext(), 0.f, 0.f)
//                .setOverzoomFactor(Settings.OVERZOOM_FACTOR)
//                .setRotationEnabled(false)
//                .setRestrictRotation(false)
//                .setFillViewport(true)
//                .setFitMethod(Settings.Fit.INSIDE)
//                .setGravity(Gravity.CENTER);
        // Temporary disabling touch controls
        if (!holder.gesturesDisabled) {
            holder.image.getController().getSettings().disableGestures();
            holder.gesturesDisabled = true;
        }

        holder.progress.animate().setStartDelay(PROGRESS_DELAY).alpha(1f);

        Photo photo = mPhotos.get(position);

        // Loading image
        GlideHelper.loadFlickrFull(photo, holder.image,
                new GlideHelper.ImageLoadingListener() {
                    @Override
                    public void onLoaded() {
                        holder.progress.animate().cancel();
                        holder.progress.animate().alpha(0f);
                        // Re-enabling touch controls
                        if (holder.gesturesDisabled) {
                            holder.image.getController().getSettings().enableGestures();
                            holder.gesturesDisabled = false;
                        }
                    }

                    @Override
                    public void onFailed() {
                        holder.progress.animate().alpha(0f);
                    }
                });
    }

    @Override
    public void onRecycleViewHolder(@NonNull ViewHolder holder) {
        super.onRecycleViewHolder(holder);

        if (holder.gesturesDisabled) {
            holder.image.getController().getSettings().enableGestures();
            holder.gesturesDisabled = false;
        }

        Glide.clear(holder.image);

        holder.progress.animate().cancel();
        holder.progress.setAlpha(0f);

        holder.image.setImageDrawable(null);
    }

    public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        public final GestureImageView image;
        public final View progress;

        public boolean gesturesDisabled;

        public ViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flickr_full_image, (ViewGroup) parent, false));
//            super(View.inflate(parent.getContext(), R.layout.item_flickr_full_image, null));
            image = (GestureImageView) itemView.findViewById(R.id.flickr_full_image);
            progress = itemView.findViewById(R.id.flickr_full_progress);
        }
    }

}
