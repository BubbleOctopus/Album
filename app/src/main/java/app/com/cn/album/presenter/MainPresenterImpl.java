package app.com.cn.album.presenter;

import android.content.Context;
import android.util.Log;
import android.view.View;

import app.com.cn.album.CommentUtils;
import app.com.cn.album.MainActivity;
import app.com.cn.album.MainInteractor;
import app.com.cn.album.factory.AbstractFragmentFactory;
import app.com.cn.album.factory.FragmentObjFactory;
import app.com.cn.album.fragment.BaseFragment;
import app.com.cn.album.net.R;


/**
 * 作者：ArMn on 2016/3/3
 * 邮箱：859686819@qq.com
 */
public class MainPresenterImpl implements MainPresenter {

    private static final String TAG = MainActivity.class.getSimpleName();
    private FragmentObjFactory mFragmentFactory;
    private Context mContext = null;
    private View contentView = null;

    private FragmentObjFactory.FRAG_KEY preFragment = null;

    //--Callback
    private MainInteractor mCallBack = null;

    public MainPresenterImpl(View content, MainInteractor interactor, AbstractFragmentFactory fragment) {
        contentView = content;
        if (null != content)
            mContext = content.getContext();

        this.mCallBack = interactor;
        this.mFragmentFactory = (FragmentObjFactory) fragment;
    }

    @Override
    public void onClick(View view) {
        BaseFragment resultFragment = null;
        FragmentObjFactory.FRAG_KEY nextFragment = FragmentObjFactory.FRAG_KEY.CATEGORY;
        if(null == mFragmentFactory)
            return;
        switch (view.getId()) {
            case R.id.rbCategory:
                //--do work
                nextFragment = FragmentObjFactory.FRAG_KEY.CATEGORY;
                resultFragment = mFragmentFactory.getFragmentInstanceFromFactory(nextFragment);
                break;
            case R.id.rbMySetting:
                //--do work
                nextFragment = FragmentObjFactory.FRAG_KEY.MYFRAG;
                resultFragment = mFragmentFactory.getFragmentInstanceFromFactory(nextFragment);
                break;
            case R.id.rbRecent:
                //--do work
                nextFragment = FragmentObjFactory.FRAG_KEY.RECENT;
                resultFragment = mFragmentFactory.getFragmentInstanceFromFactory(nextFragment);
                break;
            default:
                break;
        }
        onClickResult(resultFragment, nextFragment);
    }

    public void onClickResult(BaseFragment fragment, FragmentObjFactory.FRAG_KEY nextFragment){
        if(preFragment == nextFragment) {
            CommentUtils.d(TAG, "not transition fragment");
            return;
        }
        if(null != mCallBack)
            mCallBack.onClickResult(fragment, nextFragment.toString());

    }
}
