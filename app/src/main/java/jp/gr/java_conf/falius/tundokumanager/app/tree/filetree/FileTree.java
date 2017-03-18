package jp.gr.java_conf.falius.tundokumanager.app.tree.filetree;

import android.os.Parcel;

import java.util.HashSet;
import java.util.Set;

import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeElement;
import jp.gr.java_conf.falius.tundokumanager.app.tree.TreeManager;

/**
 * Created by ymiyauchi on 2017/02/03.
 */

public class FileTree implements TreeManager {
    private final Set<FileTreeElement> mElements = new HashSet<>();
    private long mNextId = 0;

    @Override
    public FileTreeElement findById(long id) {
        for (FileTreeElement element : mElements) {
            if (element.getId() == id) {
                return element;
            }
        }
        return null;
    }

    public FileTreeElement find(TreeElement.Predicate func) {
        for (FileTreeElement element : mElements) {
            if (func.test(element)) {
                return element;
            }
        }
        return null;
    }

    public void forEach(TreeElement.Each each) {
        for (FileTreeElement element : mElements) {
            each.each(element);
        }
    }

    public void add(FileTreeElement element) {
        if (mElements.contains(element)) {
            return;
        }
        mElements.add(element);
        element.setId(mNextId++);
    }

    public void recoveryFrom(Parcel in, FileTreeElement element, long id) {
        if (mElements.contains(element)) {
            return;
        }
        mElements.add(element);
        if (id >= mNextId) {
            mNextId = id + 1;
        }
    }
}
