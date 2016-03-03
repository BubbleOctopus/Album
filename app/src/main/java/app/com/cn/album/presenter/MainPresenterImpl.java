package app.com.cn.album.presenter;

import android.content.Context;
import android.view.View;


/**
 * 作者：ArMn on 2016/3/3
 * 邮箱：859686819@qq.com
 */
public class MainPresenterImpl implements MainPresenter{
    private Context mContext = null;
    private View contentView = null;

    public MainPresenterImpl(View content) {
        contentView = content;
        if (null != content)
            mContext = content.getContext();
    }

    @Override
    public void onClick(View view) {

    }
}
