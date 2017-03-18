package jp.gr.java_conf.falius.tundokumanager.app.input;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.ymiyauchi.app.R;

import jp.gr.java_conf.falius.tundokumanager.app.data.DataConverter;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * 情報入力画面
 */

public class InputActivity extends AppCompatActivity {
    private Conductor mConductor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        int position = getIntent().getIntExtra(DataConverter.POSITION, -1);
        View layout = getWindow().getDecorView();
        Result.WhereFrom whereFrom =
                position == -1 ? Result.WhereFrom.NEW_INPUT : Result.WhereFrom.CONTEXT_MENU;
        mConductor = new Conductor(this, layout, whereFrom);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_input, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_del) {
            // [削除]選択処理
            mConductor.onDeleteOptionSelected();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mConductor.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mConductor.onRestoreInstanceState(savedInstanceState);
    }
}
