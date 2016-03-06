package app.com.cn.album.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import app.com.cn.album.dao.Category;
import app.com.cn.album.dao.Photo;
import app.com.cn.album.net.R;

public class CategoryPhotoListAdapter
        extends DefaultEndlessRecyclerAdapter<CategoryPhotoListAdapter.ViewHolder>
        implements View.OnClickListener {

    private List<Category> mPhotos;
    private boolean mHasMore = true;

    private final OnPhotoListener mListener;

    public CategoryPhotoListAdapter(OnPhotoListener listener) {
        super();
        mListener = listener;
    }

    public void setPhotos(List<Category> photos, boolean hasMore) {
        List<Category> old = mPhotos;
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
        Category photo = mPhotos.get(position);
        holder.image.setTag(R.id.tag_item, photo);
        int count = mPhotos.get(position).getLists().size();
        holder.count.setText(count + "");
        holder.title.setText(mPhotos.get(position).getSuffix());
//        GlideHelper.loadFlickrThumb(photo, holder.image);
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
//        if (holder instanceof ViewHolder) {
//            Glide.clear(((ViewHolder) holder).image);
//        }
    }

    @Override
    public void onClick(@NonNull View view) {
        Category photo = (Category) view.getTag(R.id.tag_item);
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
        public final TextView count;
        public final TextView title;

        public ViewHolder(ViewGroup parent) {
            super(View.inflate(parent.getContext(), R.layout.item_category_image, null));
            image = (ImageView) itemView.findViewById(R.id.aImageView);
            count = (TextView) itemView.findViewById(R.id.group_count);
            title = (TextView) itemView.findViewById(R.id.group_title);
        }
    }

    public interface OnPhotoListener {
        void onPhotoClick(Category photo, int position, ImageView image);
    }

}
