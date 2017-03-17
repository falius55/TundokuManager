package com.example.ymiyauchi.app.tree.filetree;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.ymiyauchi.app.tree.TreeElement;

import java.io.File;
import java.util.Arrays;

/**
 * Created by ymiyauchi on 2017/02/03.
 *
 * ファイルシステムの各ファイル、ディレクトリを表すクラスの基底クラスです。
 */

public abstract class FileTreeElement implements TreeElement {
    private static final String TAG = "FILE_TREE_ELEMENT";
    private static final String CREATE_TAG = "FILE_TREE_CREATE";

    public enum Type {
        FILE, DIRECTORY,
    }

    private long mId = -1;
    private final FileTree mFileTree;
    private FileTreeElement mParent = null;
    private final String[] mPath;
    private final String mName;


    protected FileTreeElement(String[] paths, @NonNull FileTree fileTree) {
        // Linuxのルートは空文字のみにすること
        Log.d(CREATE_TAG, "paths : " + Arrays.toString(paths));
        if (paths.length == 0) {
            paths = new String[]{""};
        }

        mPath = new String[paths.length - 1]; // empty if it is root:
        System.arraycopy(paths, 0, mPath, 0, mPath.length);
        if (paths.length == 1 && !paths[0].endsWith(File.separator)) {
            // root -> "C:\" or "\"
            mName = paths[0] + File.separator;
        } else {
            mName = paths[paths.length - 1];
        }
        mFileTree = fileTree;
        fileTree.add(this);
    }

    public void setId(long id) {
        mId = id;
    }

    public String getPath() {
        // root : ""
        // directory : "C:\aaa\" "C:\" "\"
        // file : "C:\aaa\" "C:\"

        if (mPath.length == 0) {
            return "";
        }
        return TextUtils.join(File.separator, mPath) + File.separator;
    }

    public String getName() {
        // root : "C:\" or "\"
        // directory : "xxx"
        // file : "xxx"

        return mName;
    }

    public String getAbsoluteName() {
        // root: "C:\" or "\"
        // directory: "C:\xxx\aaa"
        // file : "C:\xxx\aaa.txt"

        if (mPath.length == 0) {
            // root
            return getName();
        }
        return getPath() + getName();
    }

    @NonNull
    @Override
    public FileTree getManager() {
        return mFileTree;
    }

    public void addParent(DirectoryElement parent) {
        mParent = parent;
    }

    @Override
    public FileTreeElement getParent() {
        return mParent;
    }

    public FileTreeElement root() {
        FileTreeElement tmp = this;
        while (true) {
            FileTreeElement ret = tmp;
            tmp = ret.getParent();
            if (tmp == null) {
                return ret;
            }
        }
    }

    @Override
    public long getId() {
        return mId;
    }

    public FileTree getFileTree() {
        return mFileTree;
    }

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof FileTreeElement)) {
            return false;
        }
        return getAbsoluteName().equals(((FileTreeElement) another).getAbsoluteName());
    }

    @Override
    public int hashCode() {
        return getAbsoluteName().hashCode();
    }

    @Override
    public String toString() {
        return getAbsoluteName();
    }

    public String toTreeString(String separator) {
        return toTreeString(separator, "");
    }

    private String toTreeString(String separator, String acumulator) {
        String LINE_SEPARATOR = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append(acumulator).append(toString()).append(LINE_SEPARATOR);

        String childLine = acumulator + separator;
        for (int i = 0; i < getChildCount(); i++) {
            FileTreeElement child = getChild(i);
            sb.append(child.toTreeString(separator, childLine)).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    public FileTreeElement(Parcel in, FileTree fileTree) {
        mId = in.readLong();
        mPath = new String[in.readInt()];
        in.readStringArray(mPath);
        mName = in.readString();

        mFileTree = fileTree;
        fileTree.recoveryFrom(in, this, mId);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mId);
        out.writeInt(mPath.length);
        out.writeStringArray(mPath);
        out.writeString(mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public abstract FileTreeElement getChild(int index);
}
