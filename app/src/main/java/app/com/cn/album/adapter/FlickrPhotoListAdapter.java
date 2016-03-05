package app.com.cn.album.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.util.List;

import app.com.cn.album.dao.Photo;
import app.com.cn.album.net.GlideHelper;
import app.com.cn.album.net.R;

public class FlickrPhotoListAdapter
        extends DefaultEndlessRecyclerAdapter<FlickrPhotoListAdapter.ViewHolder>
        implements View.OnClickListener {

    private List<Photo> mPhotos;
    private boolean mHasMore = true;

    private final OnPhotoListener mListener;

    public FlickrPhotoListAdapter(OnPhotoListener listener) {
        super();
        mListener = listener;
    }

    public void setPhotos(List<Photo> photos, boolean hasMore) {
        List<Photo> old = mPhotos;
        mPhotos = photos;
        mHasMore = hasMore;

        RecyclerAdapterHelper.notifyChanges(this, old, photos, false);
    }

    @Override
    public int getCount() {
        return mPhotos == null ? 0 : mPhotos.size();
    }

    public boolean canLoadNext() {
        return mHasMore;
    }

    @Override
    protected ViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        ViewHolder holder = new ViewHolder(parent);
        holder.image.setOnClickListener(this);
        return holder;
    }

    @Override
    protected void onBindHolder(ViewHolder holder, int position) {
        Photo photo = mPhotos.get(position);
        holder.image.setTag(R.id.tag_item, photo);
        GlideHelper.loadFlickrThumb(photo, holder.image);
    }

    @Override
    protected void onBindLoadingView(TextView loadingText) {
        loadingText.setText(R.string.loading_images);
    }

    @Override
    protected void onBindErrorView(TextView errorText) {
        errorText.setText(R.string.reload_images);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ViewHolder) {
            Glide.clear(((ViewHolder) holder).image);
        }
    }

    @Override
    public void onClick(@NonNull View view) {
        Photo photo = (Photo) view.getTag(R.id.tag_item);
        int pos = mPhotos.indexOf(photo);
        mListener.onPhotoClick(photo, pos, (ImageView) view);
    }

    public static ImageView getImage(RecyclerView.ViewHolder holder) {
        if (holder instanceof ViewHolder) {
            return ((ViewHolder) holder).image;
        } else {
            return null;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;

        public ViewHolder(ViewGroup parent) {
            super(View.inflate(parent.getContext(), R.layout.item_flickr_image, null));
            image = (ImageView) itemView;
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(Photo photo, int position, ImageView image);
    }

}
