package com.example.ymiyauchi.tundokumanager.tree.filetree;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.example.ymiyauchi.mylibrary.remote.receiver.Receiver;
import com.example.ymiyauchi.tundokumanager.remote.DirectoryLoadTask;
import com.example.ymiyauchi.tundokumanager.tree.TreeElement;
import com.example.ymiyauchi.tundokumanager.tree.TreeFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ymiyauchi on 2017/02/03.
 *
 * ディレクトリを表すクラスです。
 */

public class DirectoryElement extends FileTreeElement {
    private static final String TAG = "DirectoryElement";
    private final List<FileTreeElement> mChildren = new ArrayList<>();

    public DirectoryElement(String[] paths, FileTree fileTree) {
        super(paths, fileTree);
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    public void addDirectory(String[] paths) throws FileNotFoundException {
        FileTreeElement child = new DirectoryElement(paths, getFileTree());
        child.addParent(this);
        if (!checkChild(child)) {
            return;
        }
        mChildren.add(child);
    }

    public void addFile(String[] paths) throws FileNotFoundException {
        FileTreeElement child = new FileElement(paths, getFileTree());
        child.addParent(this);
        if (!checkChild(child)) {
            return;
        }
        mChildren.add(child);
    }

    public void addParent(String[] paths) throws FileNotFoundException {
        DirectoryElement parent = new DirectoryElement(paths, getFileTree());

        // rootは最後に\がつくが、それ以外はつかない(getAbsoluteName())
        // getPath()で得られる文字列は最後に\がつく
        // そのため、\をすべてにつけてから比較する
        String absoluteName;
        if (parent.getAbsoluteName().endsWith(File.separator)) {
            absoluteName = parent.getAbsoluteName();
        } else {
            absoluteName = parent.getAbsoluteName() + File.separator;
        }
        if (!absoluteName.equals(getPath())) {
            throw new FileNotFoundException("parent sbsolutename " + parent.getAbsoluteName() + " is " +
                    "not " + getPath());
        }

        super.addParent(parent);
        parent.mChildren.add(this);
    }

    private boolean checkChild(FileTreeElement newChild) throws FileNotFoundException {
        Log.d(TAG, "checkChild newChild:" + newChild);
        // rootは最後に\がつくが、それ以外はつかない(getAbsoluteName())
        // getPath()で得られる文字列は最後に\がつく
        // そのため、\をすべてにつけてから比較する
        String absoluteName;
        if (getAbsoluteName().endsWith(File.separator)) {
            absoluteName = getAbsoluteName();
        } else {
            absoluteName = getAbsoluteName() + File.separator;
        }
        if (!newChild.getPath().equals(absoluteName)) {
            throw new FileNotFoundException("invalid file name: \"" + newChild +
                    "\" is not child of \"" + getAbsoluteName() + "\" : \"" + newChild.getPath() + "\" != " + absoluteName + "\"");
        }

        // already add
        for (FileTreeElement child : mChildren) {
            if (newChild.getAbsoluteName().equals(child.getAbsoluteName())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TreeElement getChild(int index) {
        return mChildren.get(index);
    }

    @Override
    public boolean hasChild() {
//        Log.d(TAG, "has child:" + getName());
        return mChildren.size() > 0;
    }

    @Override
    public int getChildCount() {
//        Log.d("CHILD_COUNT", "get child count:" + getAbsoluteName() + ":" + mChildren.size());
        return mChildren.size();
    }

    @Override
    public void load(TreeFragment treeFragment) {
        Log.d(TAG, "load:" + toString());
        AsyncTask<DirectoryElement, Integer, Receiver> task
                = new DirectoryLoadTask(treeFragment);
        task.execute(this);
    }

    @Override
    public boolean isLoadable() {
        return !hasChild();
    }

    public DirectoryElement(Parcel in, FileTree fileTree) {
        super(in, fileTree);
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String type = in.readString();
            FileTreeElement child;
            if (type.equals("directory")) {
                child = new DirectoryElement(in, fileTree);
            } else if (type.equals("file")) {
                child = new FileElement(in, fileTree);
            } else {
                continue;
            }
            mChildren.add(child);
        }
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(mChildren.size());
        for (FileTreeElement child : mChildren) {
            if (child instanceof DirectoryElement) {
                out.writeString("directory");
            } else if (child instanceof FileElement) {
                out.writeString("file");
            }
            child.writeToParcel(out, flags);
        }
    }

    public static final Parcelable.Creator<DirectoryElement> CREATOR = new Parcelable.Creator<DirectoryElement>() {
        @Override
        public DirectoryElement createFromParcel(Parcel source) {
            return new DirectoryElement(source, new FileTree());
        }

        @Override
        public DirectoryElement[] newArray(int size) {
            return new DirectoryElement[size];
        }
    };
}
