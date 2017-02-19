package com.example.ymiyauchi.tundokumanager.tree.filetree;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.example.ymiyauchi.tundokumanager.tree.TreeElement;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ymiyauchi on 2017/02/03.
 *
 * ファイルシステムの各ファイル、ディレクトリを表すクラスの基底クラスです。
 */

public abstract class FileTreeElement implements TreeElement {
    private static final String TAG = "FILE_TREE_ELEMENT";
    private static final String CREATE_TAG = "FILE_TREE_CREATE";
    private static final String FILENAME_SEPARATOR = "\\";

    public enum Type {
        FILE, DIRECTORY,
    }

    private long mId = -1;
    final FileTree mFileTree;
    private FileTreeElement mParent = null;
    private final String mPath;
    private final String mName;

    protected FileTreeElement(String absoluteName, @NonNull FileTree fileTree) {
        // rootディレクトリは空文字で表される
//        Log.d(CREATE_TAG, "absolute name:" + absoluteName);
        int sepIndex = absoluteName.lastIndexOf(FILENAME_SEPARATOR);
//        Log.d(CREATE_TAG, "sepIndex" + sepIndex);
        if (checkRoot(absoluteName)) {
            mPath = "";
            mName = "";
            Log.d(TAG, "root");
        } else {
            if (absoluteName.endsWith(FILENAME_SEPARATOR)) {
                absoluteName = absoluteName.substring(0, absoluteName.length() - 1);
            }
            int rootIndex = absoluteName.indexOf(FILENAME_SEPARATOR);
//            Log.d(CREATE_TAG, "rootIndex:" + rootIndex);
            mPath = absoluteName.substring(rootIndex, sepIndex);
            mName = absoluteName.substring(sepIndex + 1, absoluteName.length());
//            Log.d(CREATE_TAG, "path:" + mPath);
//            Log.d(CREATE_TAG, "name:" + mName);
        }
        mFileTree = fileTree;
        fileTree.add(this);
    }

    private boolean checkRoot(String absoluteName) {
        Pattern p = Pattern.compile("^[A-Z]?:\\\\$");
        Matcher matcher = p.matcher(absoluteName);
        return matcher.find() || !absoluteName.contains(FILENAME_SEPARATOR);
    }

    public void setId(long id) {
        mId = id;
    }

    public String getPath() {
        // root : "\"
        // directory : "\xxx"
        // file : "\xxx"
        if (mPath.equals("")) {
            return File.separator;
        }
        return mPath.replaceAll("[/\\\\]", File.separator);
    }

    public String getName() {
        if (mName.equals("") && mPath.equals("")) {
            return "root";
        }
        return mName;
    }

    public String getAbsoluteName() {
        // root: "\"
        // directory: "\xxx\aaa"
        // file : "\xxx\aaa.txt"
        String absoluteName = TextUtils.join(File.separator, new String[]{mPath, mName});
        return absoluteName.replaceAll("[\\\\/]", File.separator);
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
            FileTreeElement child = (FileTreeElement) getChild(i);
            sb.append(child.toTreeString(separator, childLine)).append(LINE_SEPARATOR);
        }
        return sb.toString();
    }

    public FileTreeElement(Parcel in, FileTree fileTree) {
        mId = in.readLong();
        mPath = in.readString();
        mName = in.readString();

        mFileTree = fileTree;
        fileTree.recoveryFrom(in, this, mId);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mId);
        out.writeString(mPath);
        out.writeString(mName);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
