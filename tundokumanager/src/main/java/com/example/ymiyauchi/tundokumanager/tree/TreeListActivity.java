package com.example.ymiyauchi.tundokumanager.tree;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public abstract class TreeListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TreeElement root = getRoot();
        replaceFragment(root);
    }

    public void replaceFragment(TreeElement node) {
        FragmentTransaction fragmentTransaction
                = getSupportFragmentManager().beginTransaction();
        TreeFragment fragment = getFragment(node);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(android.R.id.content, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            finish();
        }
    }

    protected abstract TreeElement getRoot();

    protected abstract TreeFragment getFragment(TreeElement node);
}
