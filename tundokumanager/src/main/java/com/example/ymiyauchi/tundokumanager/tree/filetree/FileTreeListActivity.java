package com.example.ymiyauchi.tundokumanager.tree.filetree;

import com.example.ymiyauchi.tundokumanager.tree.TreeElement;
import com.example.ymiyauchi.tundokumanager.tree.TreeFragment;
import com.example.ymiyauchi.tundokumanager.tree.TreeListActivity;

/**
 * Created by ymiyauchi on 2017/02/05.
 */

public class FileTreeListActivity extends TreeListActivity {
    @Override
    protected TreeFragment getFragment(TreeElement node) {
        return TreeFragment.newInstance(node);
    }

    @Override
    protected TreeElement getRoot() {
        return new DirectoryElement(new String[]{"C:"}, new FileTree());
    }
}
