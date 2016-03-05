package app.com.cn.album.factory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import app.com.cn.album.fragment.BaseFragment;
import app.com.cn.album.fragment.CategoryFragment;
import app.com.cn.album.fragment.MyFragment;
import app.com.cn.album.fragment.RecentFragment;

/**
 * 作者：ArMn on 2016/3/5
 * 邮箱：859686819@qq.com
 */
public class FragmentObjFactory extends AbstractFragmentFactory {

    public enum FRAG_KEY {
        CATEGORY, MYFRAG, RECENT
    }

    public Map<FRAG_KEY, Class> map = new HashMap<>();

    {
        map.put(FRAG_KEY.CATEGORY, CategoryFragment.class);
        map.put(FRAG_KEY.MYFRAG, MyFragment.class);
        map.put(FRAG_KEY.RECENT, RecentFragment.class);
    }

    public Map<FRAG_KEY, BaseFragment> memeoryFragment = new HashMap<>();

    public BaseFragment getFragmentInstanceFromFactory(FRAG_KEY key) {
        //--去缓存中读，如果没有在创建
        if (memeoryFragment.containsKey(key)) {
            BaseFragment baseFragment = memeoryFragment.get(key);
            if (null != baseFragment)
                return baseFragment;
        }
        Class clzz = map.get(key);
        try {
            Constructor con = clzz.getConstructor();
            BaseFragment baseFragment = (BaseFragment) con.newInstance();
            //---缓存fragment
            if (null != baseFragment)
                memeoryFragment.put(key, baseFragment);
            return baseFragment;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
