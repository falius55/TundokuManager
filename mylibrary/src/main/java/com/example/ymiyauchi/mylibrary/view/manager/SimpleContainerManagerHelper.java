package com.example.ymiyauchi.mylibrary.view.manager;

import android.widget.SimpleAdapter;

import com.example.ymiyauchi.mylibrary.listener.ObservableList;
import com.example.ymiyauchi.mylibrary.listener.ObservableMap;
import com.example.ymiyauchi.mylibrary.listener.OnListChangedListener;
import com.example.ymiyauchi.mylibrary.listener.OnMapChangedListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by ymiyauchi on 2016/11/11.
 * <p>
 * SimpleContainerManagerをサポートするクラスです
 * originalDataを変更すると自動的にリストビューの内容が変更されるよう、リスナーを管理およびadapterDataの操作をします
 */

class SimpleContainerManagerHelper implements OnListChangedListener<ObservableMap<Integer, String>>, OnMapChangedListener<Integer, String> {
    private final ObservableList<ObservableMap<Integer, String>> originalData; // このクラスではあくまで参照用。データの変更はしない
    /*
     * アダプターに設置するデータ。レイアウトIDを文字列化してキーにする
     */
    private final List<Map<String, String>> adapterData;
    private final SimpleAdapter adapter;

    SimpleContainerManagerHelper(ObservableList<ObservableMap<Integer, String>> originalData, List<Map<String, String>> adapterData, SimpleAdapter adapter) {
        this.originalData = originalData;
        this.adapterData = adapterData;
        this.adapter = adapter;

        originalData.addEventListener(this);
        for (ObservableMap<Integer, String> map : originalData) {
            map.addEventListener(this);
        }
    }

    @Override
    public void onRemoveAll(ObservableList<ObservableMap<Integer, String>> oldList, Collection<?> collection) {
        for (Object elem : collection) {
            int index = originalData.indexOf(elem);
            if (index >= 0)
                adapterData.remove(index);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRetainAll(ObservableList<ObservableMap<Integer, String>> oldList, Collection<?> collection) {

    }

    @Override
    public void onSet(ObservableList<ObservableMap<Integer, String>> oldList, int index, ObservableMap<Integer, String> element) {

    }

    @Override
    public void onAddAll(ObservableList<ObservableMap<Integer, String>> oldList, int index, Collection<? extends ObservableMap<Integer, String>> collection) {
        List<Map<String, String>> newData = new ArrayList<>();
        for (ObservableMap<Integer, String> map : collection) {
            newData.add(SimpleContainerManager.toStringKeyMap(map));
            map.addEventListener(this);
        }
        adapterData.addAll(newData);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAddedAll(ObservableList<ObservableMap<Integer, String>> oldList, int index, Collection<? extends ObservableMap<Integer, String>> collection) {

    }

    @Override
    public void onAdd(ObservableList<ObservableMap<Integer, String>> oldList, int index, ObservableMap<Integer, String> element) {

        Map<String, String> newMap = SimpleContainerManager.toStringKeyMap(element);
        element.addEventListener(this);
        adapterData.add(newMap);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onAdded(ObservableList<ObservableMap<Integer, String>> newList, int index) {
    }

    @Override
    public void onRemove(ObservableList<ObservableMap<Integer, String>> oldList, int index) {
        adapterData.remove(index);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClear(ObservableList<ObservableMap<Integer, String>> oldList) {
        adapterData.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onPut(ObservableMap<Integer, String> oldMap, Integer key, String value) {
        int index = originalData.indexOf(oldMap);
        adapterData.get(index).put(Integer.toString(key), value);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void onPuted(ObservableMap<Integer, String> newMap, Integer key, String value) {

    }

    @Override
    public void onPutAll(ObservableMap<Integer, String> oldMap, Map<? extends Integer, ? extends String> putMap) {
        int index = originalData.indexOf(oldMap);
        adapterData.get(index).putAll(SimpleContainerManager.toStringKeyMap(putMap));
        adapter.notifyDataSetChanged();
    }
}
