package app.com.cn.album;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import app.com.cn.album.net.R;
import app.com.cn.album.presenter.MainPresenter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements MainInteractor{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PICK_IMAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




       /* new Thread(){
            @Override
            public void run() {
                fetchImageUrlFromContentProviderAndFilter(".jpg");
            }
        }.start();*/
    }

    public List fetchImageUrlFromContentProvider(){
        List<String> listImage = new ArrayList<String>();
        // 扫描外部设备中的照片
        String str[] = { MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA};
        Cursor cursor = MainActivity.this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, str,
                null, null, null);

        while (cursor.moveToNext()) {
//            System.out.println(TAG + " fetchImageUrlFromContentProvider : " + cursor.getString(0)); // 图片ID
//            System.out.println(TAG + " fetchImageUrlFromContentProvider : " +  cursor.getString(1)); // 图片文件名
            System.out.println(TAG + " fetchImageUrlFromContentProvider : " + cursor.getString(2)); // 图片绝对路径
            listImage.add(cursor.getString(2));
        }

        return listImage;
    }

    public List fetchImageUrlFromContentProviderAndFilter(final String filter){
        final List<String> listImage = new ArrayList<String>();
        listImage.clear();
        Observable.from(fetchImageUrlFromContentProvider())
                .subscribeOn(Schedulers.io())
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String str) {
                        return str.endsWith(filter);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String pic_path) {
                        listImage.add(pic_path);
                    }
                });

        return listImage;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClickResult(View view) {

    }
}
