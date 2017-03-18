package jp.gr.java_conf.falius.tundokumanager.app.tree.filetree;

import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeElement;
import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeFragment;
import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeListActivity;

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
