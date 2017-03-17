package com.example.ymiyauchi.tundokumanager.tree;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.example.ymiyauchi.mylibrary.AndroidDatabase;

import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.remote.RemoteSaveFileTask;
import com.example.ymiyauchi.tundokumanager.tree.filetree.FileTreeElement;

import jp.gr.java_conf.falius.communication.receiver.Receiver;

import java.util.Objects;

/**
 * Created by ymiyauchi on 2017/02/05.
 */

public abstract class TreeFragment extends ListFragment {
    private static final String TAG = "TREE_FRAGMENT";

    public static final String ARG_ROOT_ELEMENT = "arg tree root element";
    public static final String ARG_PRINT_NODE_ID = "arg print node id";

    private TreeElement mDisplayNode = null;


    public TreeFragment() {
        // empty
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        TreeElement root = bundle.getParcelable(ARG_ROOT_ELEMENT);
        Objects.requireNonNull(root);

        long dirId = bundle.getLong(ARG_PRINT_NODE_ID);
        TreeManager treeManager = root.getManager();
        TreeElement displayNode = treeManager.findById(dirId);
        mDisplayNode = displayNode;

        if (displayNode.isLoadable()) {
            displayNode.load(this);
        } else {
            createList(displayNode);
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("empty directory");
        registerForContextMenu(getListView());
    }

    public void createList(TreeElement node) {
        ListAdapter adapter = createAdapter(node);
        setListAdapter(adapter);
    }

    public abstract ListAdapter createAdapter(TreeElement node);

    public abstract String getTitle();

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.file_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int pos = info.position;
        TreeElement selectedElement = mDisplayNode.getChild(pos);
        if (((FileTreeElement) selectedElement).isFile()) {
            return false;
        }

        if (item.getItemId() == R.id.action_save_db) {
            try (AndroidDatabase db = new BasicDatabase(getActivity())) {
                SQLiteDatabase sdb = db.getReadableDatabase();
                String fileName = "TundokuManager.sql";
                String pcPath = selectedElement.toString();
                String dbPath = sdb.getPath();

                Log.d(TAG, "db path:" + dbPath);

                AsyncTask<String, String, Receiver> task
                        = new RemoteSaveFileTask(getActivity());
                task.execute(fileName, pcPath, dbPath);
            }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        TreeElement selectedElement = mDisplayNode.getChild(position);
        if (selectedElement.hasChild() || selectedElement.isLoadable()) {
            ((TreeListActivity) getActivity()).replaceFragment(selectedElement);
        }
    }
}
