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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ymiyauchi.mylibrary.AndroidDatabase;
import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.database.BasicDatabase;
import com.example.ymiyauchi.tundokumanager.remote.RemoteSaveFileTask;

import java.util.Objects;

/**
 * Created by ymiyauchi on 2017/02/05.
 */

public class TreeFragment extends ListFragment {
    private static final String TAG = "TREE_FRAGMENT";

    private static final String ARG_ROOT_ELEMENT = "arg tree root element";
    private static final String ARG_PRING_NODE_ID = "arg print node id";

    private TreeElement mDisplayNode = null;

    public static TreeFragment newInstance(TreeElement node) {
        TreeFragment fragment = new TreeFragment();
        Bundle bundle = new Bundle();

        TreeElement root = node.root();
        bundle.putParcelable(ARG_ROOT_ELEMENT, root);

        long id = node.getId();
        bundle.putLong(ARG_PRING_NODE_ID, id);

        fragment.setArguments(bundle);
        return fragment;
    }

    public TreeFragment() {
        // empty
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        TreeElement root = bundle.getParcelable(ARG_ROOT_ELEMENT);
        Objects.requireNonNull(root);

        long dirId = bundle.getLong(ARG_PRING_NODE_ID);
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
        registerForContextMenu(getListView());
    }

    public void createList(TreeElement node) {
        if (node == null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, new String[0]);
            setListAdapter(adapter);
            return;
        }
        String[] data = new String[node.getChildCount()];
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeElement child = node.getChild(i);
            data[i] = child.toString();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, data);
        setListAdapter(adapter);
    }

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
