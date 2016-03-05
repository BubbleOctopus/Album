package app.com.cn.album.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.com.cn.album.net.R;

/**
 * 作者：ArMn on 2016/3/3
 * 邮箱：859686819@qq.com
 */
public class MyFragment  extends BaseFragment{
    @Override
    public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.mysetting_layout,null,false);
    }

    @Override
    public void initViewIDs(View view) {

    }
}
