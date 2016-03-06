package app.com.cn.album.dao;

import java.util.List;

/**
 * 作者：ArMn on 2016/3/6
 * 邮箱：859686819@qq.com
 */
public class Category {
    public String suffix;
    public List<String> lists;

    public Category(String suffix, List<String> lists) {
        this.suffix = suffix;
        this.lists = lists;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<String> getLists() {
        return lists;
    }

    public void setLists(List<String> lists) {
        this.lists = lists;
    }
}
