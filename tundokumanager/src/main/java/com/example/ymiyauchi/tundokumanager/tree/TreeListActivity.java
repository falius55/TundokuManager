package com.example.ymiyauchi.tundokumanager.tree;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.ymiyauchi.tundokumanager.R;

public abstract class TreeListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tree_list);
        TreeElement root = getRoot();
        replaceFragment(root);

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final TextView textView = (TextView) findViewById(R.id.text_tree_title);
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    finish();
                    return;
                }
                FragmentManager.BackStackEntry backStackEntry
                        = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
                textView.setText(backStackEntry.getName());
            }
        });
    }

    public void replaceFragment(TreeElement node) {
        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();
        TreeFragment fragment = getFragment(node);
        fragmentTransaction.addToBackStack(fragment.getTitle());
        fragmentTransaction.replace(R.id.tree_fragment_frame, fragment);
        fragmentTransaction.commit();
    }

    protected abstract TreeElement getRoot();

    protected abstract TreeFragment getFragment(TreeElement node);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close) {
            finish();
        }
        return true;
    }
}
