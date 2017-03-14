package com.example.ymiyauchi.tundokumanager;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import com.example.ymiyauchi.mylibrary.AndroidDatabase;

import communication.receiver.Receiver;
import com.example.ymiyauchi.tundokumanager.data.DataConverter;
import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.log.LogActivity;
import com.example.ymiyauchi.tundokumanager.log.LogFile;
import com.example.ymiyauchi.tundokumanager.mainfragment.MainFragment;
import com.example.ymiyauchi.tundokumanager.input.InputActivity;
import com.example.ymiyauchi.mylibrary.view.pageradapter.SimplePagerAdapter;
import com.example.ymiyauchi.tundokumanager.pref.PrefActivity;
import com.example.ymiyauchi.tundokumanager.remote.RemoteSaveFileTask;
import com.example.ymiyauchi.tundokumanager.tree.filetree.FileTreeListActivity;


/**
 * Created by ymiyauchi on 2015/12/09.
 * <p>
 * メイン画面
 */

public class MainActivity extends AppCompatActivity {

    private SimplePagerAdapter mPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LogFile logFile = new LogFile(getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(logFile);

        mPagerAdapter = new SimplePagerAdapter(this);
        mPagerAdapter.addAll(Type.values());

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(1, false);
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_input) {
            int curPage = mViewPager.getCurrentItem();
            MainFragment fragment = (MainFragment) mPagerAdapter.findFragmentByPosition(mViewPager, curPage);
            Intent intent = new Intent(MainActivity.this, InputActivity.class);
            intent.putExtra(DataConverter.TYPE, fragment.type().getCode());
            startActivityForResult(intent, mViewPager.getCurrentItem());
        }

        if (item.getItemId() == R.id.action_pref) {
            Intent intent = new Intent(this, PrefActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.action_remote) {
            AsyncTask<String, String, Receiver> task = new RemoteSaveFileTask(this);
            try (AndroidDatabase db = new BasicDatabase(this)) {
                SQLiteDatabase sdb = db.getReadableDatabase();
                String path = sdb.getPath();
                task.execute("TundokuManager.sql", path);  // FIXME: 別の変更をコミットしたら削除する。引数が異なる。このままではエラー
            }

        }

        if (item.getItemId() == R.id.action_pcfile) {
            Intent intent = new Intent(this, FileTreeListActivity.class);
            startActivity(intent);
        }

        if (item.getItemId() == R.id.action_log) {
            Intent intent = new Intent(this, LogActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * InputActivityが破棄された際に呼ばれる
     * フラグメントからstartActivityForResultしてもここを経由する
     *
     * @param requestCode フラグメントのポジション
     * @param resultCode  更新の種類を表すコード。消去、追加、既存アイテムの更新など
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MainFragment fragment = (MainFragment) mPagerAdapter.findFragmentByPosition(mViewPager, requestCode);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    // ダイアログ内で設定したクリックリスナー内で呼び出している
    // 情報経路：Dialog ⇒ MainActivity ⇒ MainFragment => mCallbackTransmitter
    public void onDialogLeaved(DataConverter newData) {

        int curPage = mViewPager.getCurrentItem();
        MainFragment fragment = (MainFragment) mPagerAdapter.findFragmentByPosition(mViewPager, curPage);
        fragment.onDialogLeaved(newData);
    }

}
