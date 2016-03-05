package app.com.cn.album.dao;

/**
 * 作者：ArMn on 2016/3/5
 * 邮箱：859686819@qq.com
 */
public class Photo {

    public String sMediumUrl;
    public String sThumbnailUrl;
    public String sLargeSize;
    public String sLargeUrl;

    public String getLargeUrl() {
        return sLargeUrl;
    }

    public void setLargeUrl(String sLargeUrl) {
        this.sLargeUrl = sLargeUrl;
    }

    public String getMediumUrl() {
        return sMediumUrl;
    }

    public void setMediumUrl(String sMediumUrl) {
        this.sMediumUrl = sMediumUrl;
    }

    public String getThumbnailUrl() {
        return sThumbnailUrl;
    }

    public void setThumbnailUrl(String sThumbnailUrl) {
        this.sThumbnailUrl = sThumbnailUrl;
    }

    public String getLargeSize() {
        return sLargeSize;
    }

    public void setLargeSize(String sLargeSize) {
        this.sLargeSize = sLargeSize;
    }
}
