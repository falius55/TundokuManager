package jp.gr.java_conf.falius.tundokumanager.app.tree.filetree;

import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.example.ymiyauchi.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeElement;
import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeFragment;
import jp.gr.java_conf.falius.tundokumanager.lib.view.containeradapter.CustomAdapter;

/**
 * Created by ymiyauchi on 2017/02/20.
 */

public class FileTreeFragment extends TreeFragment {
    private static final String ARG_TITLE = "arg_title";

    public static FileTreeFragment newInstance(FileTreeElement node) {
        FileTreeFragment fragment = new FileTreeFragment();
        Bundle bundle = new Bundle();

        TreeElement root = node.root();
        bundle.putParcelable(TreeFragment.ARG_ROOT_ELEMENT, root);

        long id = node.getId();
        bundle.putLong(TreeFragment.ARG_PRINT_NODE_ID, id);

        bundle.putString(ARG_TITLE, node.getAbsoluteName());

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public ListAdapter createAdapter(TreeElement node) {
        if (node == null || !(node instanceof FileTreeElement)) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_list_item_1, new String[0]);
            return adapter;
        }

        FileTreeElement treeNode = (FileTreeElement) node;

        List<SparseArray<Object>> data = new ArrayList<>(node.getChildCount());
        for (int i = 0; i < node.getChildCount(); i++) {
            FileTreeElement child = treeNode.getChild(i);
            SparseArray<Object> childData = new SparseArray<>(3);
            childData.append(CustomAdapter.ID_KEY, child.getId());
            if (child.isDirectory()) {
                childData.append(R.id.file_type, "Directory");
                childData.append(R.id.file_name, child.getName() + File.separator);
            } else {
                childData.append(R.id.file_type, "File");
                childData.append(R.id.file_name, child.getName());
            }
            data.add(childData);
        }
        ListAdapter adapter = new CustomAdapter(getActivity(), R.layout.file_list_item, new int[]{R.id.file_name, R.id.file_type}, data);
        return adapter;
    }

    @Override
    public String getTitle() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            return bundle.getString(ARG_TITLE);
        }
        return null;
    }
}
