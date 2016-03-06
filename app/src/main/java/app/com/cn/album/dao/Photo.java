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
    public String ids;
    public String sFolderName;
    public Photo(String ids, String sFolderName, String sMediumUrl, String sThumbnailUrl, String sLargeSize, String sLargeUrl){
        this.ids = ids;
        this.sFolderName = sFolderName;
        this.sMediumUrl = sMediumUrl;
        this.sThumbnailUrl = sThumbnailUrl;
        this.sLargeSize = sLargeSize;
        this.sLargeUrl = sLargeUrl;
    }

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
