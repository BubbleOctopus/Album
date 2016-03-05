package app.com.cn.album.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.transition.SimpleViewsTracker;
import com.alexvasilkov.gestures.transition.ViewsCoordinator;
import com.alexvasilkov.gestures.transition.ViewsTransitionAnimator;
import com.alexvasilkov.gestures.transition.ViewsTransitionBuilder;

import app.com.cn.album.CommentUtils;
import app.com.cn.album.adapter.EndlessRecyclerAdapter;
import app.com.cn.album.adapter.FlickrPhotoListAdapter;
import app.com.cn.album.adapter.FlickrPhotoPagerAdapter;
import app.com.cn.album.dao.Photo;
import app.com.cn.album.net.R;

/**
 * 作者：ArMn on 2016/3/3
 * 邮箱：859686819@qq.com
 */
public class RecentFragment extends BaseFragment implements ViewPositionAnimator.PositionUpdateListener, FlickrPhotoListAdapter.OnPhotoListener {

    private static final int PAGE_SIZE = 30;
    private static final int NO_POSITION = -1;

    private ViewHolder mViewHolder;
    private ViewsTransitionAnimator<Integer> mAnimator;
    private FlickrPhotoPagerAdapter mPagerAdapter;
    private ViewPager.SimpleOnPageChangeListener mPagerListener;
    private FlickrPhotoListAdapter mGridAdapter;

    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.recent_flickr_layout, null, false);
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
        initPager();
        initAnimator();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initDecorMargins() {
        // Adjusting margins and paddings to fit translucent decor
        CommentUtils.paddingForStatusBar(mViewHolder.toolbar, true);
        CommentUtils.paddingForStatusBar(mViewHolder.toolbarBack, true);
        CommentUtils.paddingForStatusBar(mViewHolder.pagerToolbar, true);
        CommentUtils.marginForStatusBar(mViewHolder.grid);
        CommentUtils.paddingForNavBar(mViewHolder.grid);
        CommentUtils.marginForNavBar(mViewHolder.pagerTitle);
    }


    private void initGrid() {
        // Setting up images grid
        final int cols = getResources().getInteger(R.integer.images_grid_columns);

        mViewHolder.grid.setLayoutManager(new GridLayoutManager(mContext, cols));
        mViewHolder.grid.setItemAnimator(new DefaultItemAnimator());

        mGridAdapter = new FlickrPhotoListAdapter(this);
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

    @SuppressLint("PrivateResource")
    private void initPager() {
        // Setting up pager views
        mPagerAdapter = new FlickrPhotoPagerAdapter(mViewHolder.pager);

        mPagerListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                onPhotoInPagerSelected(position);
            }
        };

        mViewHolder.pager.setAdapter(mPagerAdapter);
        mViewHolder.pager.addOnPageChangeListener(mPagerListener);
        mViewHolder.pager.setPageTransformer(true, new DepthPageTransformer());

        mViewHolder.pagerToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mViewHolder.pagerToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                onBackPressed();
            }
        });

        onCreateOptionsMenuFullMode(mViewHolder.pagerToolbar.getMenu());

        mViewHolder.pagerToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelectedFullMode(item);
            }
        });
    }

    private void initAnimator() {
        mAnimator = new ViewsTransitionBuilder<Integer>()
                .fromRecyclerView(mViewHolder.grid, new SimpleViewsTracker() {
                    @Override
                    public View getViewForPosition(int position) {
                        RecyclerView.ViewHolder holder =
                                mViewHolder.grid.findViewHolderForLayoutPosition(position);
                        return holder == null ? null : FlickrPhotoListAdapter.getImage(holder);
                    }
                })
                .intoViewPager(mViewHolder.pager, new SimpleViewsTracker() {
                    @Override
                    public View getViewForPosition(int position) {
                        RecyclePagerAdapter.ViewHolder holder = mPagerAdapter.getViewHolder(
                                position);
                        return holder == null ? null : FlickrPhotoPagerAdapter.getImage(holder);
                    }
                })
                .build();
        mAnimator.addPositionUpdateListener(this);
        mAnimator.setReadyListener(new ViewsCoordinator.OnViewsReadyListener<Integer>() {
            @Override
            public void onViewsReady(@NonNull Integer id) {
                // Setting image drawable from 'from' view to 'to' to prevent flickering
                ImageView from = (ImageView) mAnimator.getFromView();
                ImageView to = (ImageView) mAnimator.getToView();
                if (to.getDrawable() == null) {
                    to.setImageDrawable(from.getDrawable());
                }
            }
        });
    }

    public void onPhotoInPagerSelected(int position) {
        //--do work

    }

    @Override
    public void onPositionUpdate(float state, boolean isLeaving) {
        //--do work  pager位置更新回调

    }

    public boolean onOptionsItemSelectedFullMode(MenuItem item){
        //---ViewPager 界面点击右上角菜单切割按钮的回调
        return false;
    }

    private void onCreateOptionsMenuFullMode(Menu menu) {
        //--ViewPager 中toolbar的左上角menu.
        MenuItem crop = menu.add(Menu.NONE, R.id.menu_crop, 0, R.string.button_crop);
        crop.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        crop.setIcon(R.drawable.ic_crop_white_24dp);
    }

    public void onBackPressed(){
        //--toolbar左上角点击回调
    }

    @Override
    public void onPhotoClick(Photo photo, int position, ImageView image) {
        //--GridView item onClick callback.
        mPagerAdapter.setActivated(true);
        mAnimator.enter(position, true);
    }

    private class ViewHolder {
        public final Toolbar toolbar;
        public final View toolbarBack;
        public final RecyclerView grid;

        public final ViewPager pager;
        public final Toolbar pagerToolbar;
        public final TextView pagerTitle;
        public final View pagerBackground;

        public ViewHolder(Activity activity) {
            toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
            toolbarBack = activity.findViewById(R.id.flickr_toolbar_back);
            grid = (RecyclerView) activity.findViewById(R.id.flickr_list);

            pager = (ViewPager) activity.findViewById(R.id.flickr_pager);
            pagerToolbar = (Toolbar) activity.findViewById(R.id.flickr_full_toolbar);
            pagerTitle = (TextView) activity.findViewById(R.id.flickr_full_title);
            pagerBackground = activity.findViewById(R.id.flickr_full_background);
        }
    }
}
