package com.example.ymiyauchi.app.tree.filetree;

import com.example.ymiyauchi.app.tree.TreeElement;
import com.example.ymiyauchi.app.tree.TreeFragment;
import com.example.ymiyauchi.app.tree.TreeListActivity;

/**
 * Created by ymiyauchi on 2017/02/05.
 */

public class FileTreeListActivity extends TreeListActivity {
    @Override
    protected TreeFragment getFragment(TreeElement node) {
        return FileTreeFragment.newInstance((FileTreeElement) node);
    }

    @Override
    protected TreeElement getRoot() {
        return new DirectoryElement(new String[]{"C:"}, new FileTree());
    }
}
