package app.com.cn.album.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.com.cn.album.CommentUtils;
import app.com.cn.album.View.DividerGridItemDecoration;
import app.com.cn.album.adapter.CategoryPhotoListAdapter;
import app.com.cn.album.adapter.EndlessRecyclerAdapter;
import app.com.cn.album.dao.Category;
import app.com.cn.album.dao.Photo;
import app.com.cn.album.net.R;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：ArMn on 2016/3/3
 * 邮箱：859686819@qq.com
 */
public class CategoryFragment extends BaseFragment implements CategoryPhotoListAdapter.OnPhotoListener {
    private static final int PAGE_SIZE = 30;
    private ViewHolder mViewHolder;

    private Map<String, Category> mapInffix = new HashMap<>();
    private CategoryPhotoListAdapter mGridAdapter;

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.category_layout, null, false);
    }

    @Override
    public void initViewIDs(View view) {
        mViewHolder = new ViewHolder(mContext);
    }

    @Override
    public void initData() {
        super.initData();
        initDecorMargins();
        initGrid();

        mapInffix.clear();
        fetchImageUrlFromContentProviderAndFilter();
    }

    private void initDecorMargins() {
        CommentUtils.paddingForNavBar(mViewHolder.grid);
    }

    private void initGrid() {
        // Setting up images grid
        final int cols = getResources().getInteger(R.integer.images_grid_columns);

        mViewHolder.grid.setLayoutManager(new GridLayoutManager(mContext, /*cols*/2));
        mViewHolder.grid.addItemDecoration(new DividerGridItemDecoration(mContext));
        mViewHolder.grid.setItemAnimator(new DefaultItemAnimator());

        mGridAdapter = new CategoryPhotoListAdapter(this);
        mGridAdapter.setLoadingOffset(PAGE_SIZE / 2);
        mGridAdapter.setCallbacks(new EndlessRecyclerAdapter.LoaderCallbacks() {
            @Override
            public boolean canLoadNextItems() {
                return mGridAdapter.canLoadNext();
            }

            @Override
            public void loadNextItems() {
                // We should either load all items that were loaded before state save / restore,
                // or next page if we already loaded all previously shown items
                //---Set data for GridAdapter.

            }
        });
        mViewHolder.grid.setAdapter(mGridAdapter);
    }

    public List fetchImageUrlFromContentProvider() {
        List<Photo> listImage = new ArrayList<>();
        // 扫描外部设备中的照片
        String str[] = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,};

        Cursor cursor = mContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, str,
                null, null, null);

        while (cursor.moveToNext()) {
            // 图片ID   图片文件名   图片绝对路径
            Photo mPhoto = new Photo(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(2), null, cursor.getString(2));
            listImage.add(mPhoto);
        }
        return listImage;
    }

    public void fetchImageUrlFromContentProviderAndFilter() {
        final List<Photo> listImage = new ArrayList<>();
        listImage.clear();
        Action1 onNextAction = new Action1<Photo>() {
            @Override
            public void call(Photo mPhoto) {
                //--
            }
        };

        Action0 onCompletedAction = new Action0() {
            // onCompleted()
            @Override
            public void call() {
                List<Category> list = new ArrayList<>(mapInffix.values());
                mGridAdapter.setPhotos(list, true);
            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            // onError()
            @Override
            public void call(Throwable throwable) {
                // Error handling
            }
        };

        Observable.from(fetchImageUrlFromContentProvider())
                .subscribeOn(Schedulers.io())
                .filter(new Func1<Photo, Boolean>() {
                    @Override
                    public Boolean call(Photo mPhoto) {
                        if (mPhoto == null)
                            return false;

                        String name = new File(mPhoto.getLargeUrl()).getName();
                        int index = name.lastIndexOf(".", name.length() - 1);
                        String suffix = name.substring(index + 1, name.length());
                        if (!mapInffix.containsKey(suffix)) {
                            mapInffix.put(suffix, new Category(suffix, new ArrayList<String>()));
                        }
                        Category category = mapInffix.get(suffix);
                        if (null != category) {
                            category.lists.add(name);
                        }
                        return true;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNextAction, onErrorAction, onCompletedAction);

    }

    @Override
    public void onPhotoClick(Category photo, int position, ImageView image) {

    }

    private class ViewHolder {
        public final RecyclerView grid;
        public final Toolbar pagerToolbar;

        public ViewHolder(Activity activity) {
            grid = (RecyclerView) activity.findViewById(R.id.rbCategory_list);
            pagerToolbar = (Toolbar) activity.findViewById(R.id.flickr_full_toolbar);
        }
    }
}
