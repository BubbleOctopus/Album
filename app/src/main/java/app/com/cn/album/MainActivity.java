package app.com.cn.album;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import app.com.cn.album.factory.FragmentObjFactory;
import app.com.cn.album.fragment.BaseFragment;
import app.com.cn.album.net.R;
import app.com.cn.album.presenter.MainPresenterImpl;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends Activity implements MainInteractor, View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_PICK_IMAGE = 101;

    private FrameLayout content;
    private RadioButton rbCateGory;
    private RadioButton rbMySetting;
    private RadioButton rbRecent;

    private MainPresenterImpl mMainPresenterImpl;
    private FragmentObjFactory mFragmentFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        content = (FrameLayout) findViewById(R.id.content);
        rbCateGory = (RadioButton) findViewById(R.id.rbCategory);
        rbMySetting = (RadioButton) findViewById(R.id.rbMySetting);
        rbRecent = (RadioButton) findViewById(R.id.rbRecent);


        rbCateGory.setOnClickListener(this);
        rbMySetting.setOnClickListener(this);
        rbRecent.setOnClickListener(this);

        mFragmentFactory = new FragmentObjFactory();

        mMainPresenterImpl = new MainPresenterImpl(content, this, mFragmentFactory);

        defaultFragment();
        //--init factory

       /* new Thread(){
            @Override
            public void run() {
                fetchImageUrlFromContentProviderAndFilter(".jpg");
            }
        }.start();*/
    }


    public List fetchImageUrlFromContentProvider() {
        List<String> listImage = new ArrayList<String>();
        // 扫描外部设备中的照片
        String str[] = {MediaStore.Images.Media._ID,
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

    public List fetchImageUrlFromContentProviderAndFilter(final String filter) {
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

    public void defaultFragment(){
        BaseFragment fragment = mFragmentFactory.getFragmentInstanceFromFactory(FragmentObjFactory.FRAG_KEY.CATEGORY);
        String tag = FragmentObjFactory.FRAG_KEY.CATEGORY.toString();
        onClickResult(fragment, tag);
    }

    @Override
    public void onClickResult(BaseFragment fragment, String tag) {
        CommentUtils.e(TAG,"onClickResult fragment = " + fragment +" : " + tag);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content, fragment, tag);
//        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        if (null != mMainPresenterImpl)
            mMainPresenterImpl.onClick(v);
    }
}
