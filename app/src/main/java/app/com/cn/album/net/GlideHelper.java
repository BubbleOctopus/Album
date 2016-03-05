package app.com.cn.album.net;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.ViewPropertyAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;

import app.com.cn.album.dao.Photo;

public class GlideHelper {

    private static final ViewPropertyAnimation.Animator ANIMATOR =
            new ViewPropertyAnimation.Animator() {
                @Override
                public void animate(View view) {
                    view.setAlpha(0f);
                    view.animate().alpha(1f);
                }
            };


    public static void loadResource(@DrawableRes int drawableId, @NonNull ImageView image) {
        DisplayMetrics metrics = image.getResources().getDisplayMetrics();
        final int w = metrics.widthPixels, h = metrics.heightPixels;

        Glide.with(image.getContext())
                .load(drawableId)
                .animate(ANIMATOR)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new GlideDrawableImageViewTarget(image) {
                    @Override
                    public void getSize(final SizeReadyCallback cb) {
                        // We don't want to load very big images on devices with small screens.
                        // This will help Glide correctly choose images scale when reading them.
                        super.getSize(new SizeReadyCallback() {
                            @Override
                            public void onSizeReady(int width, int height) {
                                cb.onSizeReady(w / 2, h / 2);
                            }
                        });
                    }
                });
    }

    public static void loadFlickrThumb(@Nullable Photo photo, @NonNull final ImageView image) {
        Glide.with(image.getContext())
                .load(photo == null ? null : photo.getMediumUrl())
                .dontAnimate()
                .thumbnail(Glide.with(image.getContext())
                        .load(photo == null ? null : photo.getThumbnailUrl())
                        .animate(ANIMATOR)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .into(new GlideDrawableTarget(image));
    }

    public static void loadFlickrFull(@NonNull Photo photo,
            @NonNull final ImageView image,
            @Nullable final ImageLoadingListener listener) {

        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(image.getContext())
                .load(photoUrl)
                .dontAnimate()
                .placeholder(image.getDrawable())
                .thumbnail(Glide.with(image.getContext())
                        .load(photo.getThumbnailUrl())
                        .animate(ANIMATOR)
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .listener(new GlideDrawableListener() {
                    @Override
                    public void onSuccess(String url) {
                        if (url.equals(photoUrl)) {
                            if (listener != null) {
                                listener.onLoaded();
                            }
                        }
                    }

                    @Override
                    public void onFail(String url) {
                        if (listener != null) {
                            listener.onFailed();
                        }
                    }
                })
                .into(new GlideDrawableTarget(image));
    }


    public interface ImageLoadingListener {
        void onLoaded();

        void onFailed();
    }

}
