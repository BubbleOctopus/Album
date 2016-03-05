package app.com.cn.album.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import app.com.cn.album.net.R;

public abstract class DefaultEndlessRecyclerAdapter<VH extends RecyclerView.ViewHolder>
        extends EndlessRecyclerAdapter<RecyclerView.ViewHolder> {

    private static final int EXTRA_LOADING_TYPE = Integer.MAX_VALUE;
    private static final int EXTRA_ERROR_TYPE = Integer.MAX_VALUE - 1;

    private final GridLayoutManager.SpanSizeLookup mSpanSizes =
            new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int pos) {
                    return pos == getCount() && (isLoading() || isError()) ? mSpanCount :
                            mOriginalSpanLookup == null ? 1 : mOriginalSpanLookup.getSpanSize(pos);
                }
            };

    private GridLayoutManager.SpanSizeLookup mOriginalSpanLookup;
    private int mSpanCount;

    private boolean mOldIsLoading, mOldIsError;


    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == EXTRA_LOADING_TYPE) {
            return new LoadingViewHolder(parent);
        } else if (viewType == EXTRA_ERROR_TYPE) {
            return new ErrorViewHolder(parent, this);
        } else {
            return onCreateHolder(parent, viewType);
        }
    }

    protected abstract VH onCreateHolder(ViewGroup parent, int viewType);

    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position,
            List<Object> payloads) {
        onBindViewHolder(holder, position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LoadingViewHolder) {
            onBindLoadingView(((LoadingViewHolder) holder).loading);
        } else if (holder instanceof ErrorViewHolder) {
            onBindErrorView(((ErrorViewHolder) holder).error);
        } else {
            onBindHolder((VH) holder, position);
        }
    }

    protected abstract void onBindLoadingView(TextView loadingText);

    protected abstract void onBindErrorView(TextView errorText);

    protected abstract void onBindHolder(VH holder, int position);

    @Override
    public final int getItemViewType(int position) {
        if (position == getCount()) {
            if (isLoading()) {
                return EXTRA_LOADING_TYPE;
            } else if (isError()) {
                return EXTRA_ERROR_TYPE;
            }
        }

        int type = getViewType(position);

        if (type == EXTRA_LOADING_TYPE) {
            throw new IllegalArgumentException(
                    "Cannot use " + EXTRA_LOADING_TYPE + " as view type");
        }
        if (type == EXTRA_ERROR_TYPE) {
            throw new IllegalArgumentException("Cannot use " + EXTRA_ERROR_TYPE + " as view type");
        }

        return type;
    }

    @SuppressWarnings("UnusedParameters")
    protected int getViewType(int position) {
        return 0;
    }


    @Override
    protected void onLoadingStateChanged() {
        super.onLoadingStateChanged();

        if (mOldIsLoading) {
            if (isError()) {
                notifyItemChanged(getCount()); // Switching to error view
            } else if (!isLoading()) {
                notifyItemRemoved(getCount()); // Loading view is removed
            }
        } else if (mOldIsError) {
            if (isLoading()) {
                notifyItemChanged(getCount()); // Switching to loading view
            } else if (!isError()) {
                notifyItemRemoved(getCount()); // Error view is removed
            }
        } else {
            if (isLoading() || isError()) {
                notifyItemInserted(getCount()); // Showing loading or error view
            }
        }

        mOldIsError = isError();
        mOldIsLoading = isLoading();
    }

    @Override
    public final int getItemCount() {
        return getCount() + (isLoading() || isError() ? 1 : 0);
    }

    public abstract int getCount();


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager gridManager = (GridLayoutManager) recyclerView.getLayoutManager();
            mSpanCount = gridManager.getSpanCount();
            mOriginalSpanLookup = gridManager.getSpanSizeLookup();
            gridManager.setSpanSizeLookup(mSpanSizes);
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager gridManager = (GridLayoutManager) recyclerView.getLayoutManager();
            gridManager.setSpanSizeLookup(mOriginalSpanLookup);
            mOriginalSpanLookup = null;
            mSpanCount = 1;
        }
    }


    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        final TextView loading;

        LoadingViewHolder(ViewGroup parent) {
            super(View.inflate(parent.getContext(), R.layout.item_extra_loading, null));
            loading = (TextView) itemView.findViewById(R.id.extra_loading_text);
        }
    }

    private static class ErrorViewHolder extends RecyclerView.ViewHolder {
        final TextView error;

        ErrorViewHolder(ViewGroup parent, final EndlessRecyclerAdapter<?> adapter) {
            super(View.inflate(parent.getContext(), R.layout.item_extra_error, null));
            error = (TextView) itemView.findViewById(R.id.extra_error);
            error.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.reloadNextItemsIfError();
                }
            });
        }
    }

}
