package com.example.ymiyauchi.tundokumanager.tree.filetree;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.example.ymiyauchi.tundokumanager.tree.TreeElement;
import com.example.ymiyauchi.tundokumanager.tree.TreeFragment;

/**
 * Created by ymiyauchi on 2017/02/03.
 */

public class FileElement extends FileTreeElement {

    public FileElement(String[] paths, FileTree fileTree) {
        super(paths, fileTree);
    }

    @Override
    public TreeElement getChild(int index) {
        return null;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean hasChild() {
        return false;
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public void load(TreeFragment fragment) {

    }

    @Override
    public boolean isLoadable() {
        return false;
    }

    public FileElement(Parcel in, FileTree fileTree) {
        super(in, fileTree);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {

    }

    public static final Parcelable.Creator<FileElement> CREATOR = new Parcelable.Creator<FileElement>() {
        @Override
        public FileElement createFromParcel(Parcel in) {
            return new FileElement(in, new FileTree());
        }

        @Override
        public FileElement[] newArray(int size) {
            return new FileElement[size];
        }
    };
}
