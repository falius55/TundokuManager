package com.example.ymiyauchi.tundokumanager.data;

import com.example.ymiyauchi.tundokumanager.R;
import com.example.ymiyauchi.tundokumanager.Type;
import com.example.ymiyauchi.mylibrary.DateTime;
import com.example.ymiyauchi.mylibrary.view.manager.ContainerManager;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * リストビューの特定位置にあるアイテムをデータソースとして変換するDataConverterです
 * <p>
 * 不変でないことに注意
 */

public class ListItemDataConverter extends AbstractDataConverter {
    private final ContainerManager mContainerManager;

    public ListItemDataConverter(Type type, ContainerManager containerManager, int position) {
        super(type, position);
        mContainerManager = containerManager;
    }

    @Override
    public String getName() {
        return mContainerManager.getString(getPosition(), R.id.name, "");
    }

    @Override
    public String getDate() {
        String defaultValue = DateTime.now().format();
        return mContainerManager.getString(getPosition(), R.id.date, defaultValue);
    }

    @Override
    public int getPrice() {
        return mContainerManager.getInt(getPosition(), R.id.value, 0);
    }

    @Override
    public String getPlayedText() {
        return mContainerManager.getString(getPosition(), R.id.played, getType().playedText(false));
    }

    @Override
    public boolean isPlayed() {
        return getType().isPlayed(getPlayedText());
    }

    @Override
    public int getCurrent() {
        return mContainerManager.getInt(getPosition(), R.id.current, 0);
    }

    @Override
    public int getCapacity() {
        return mContainerManager.getInt(getPosition(), R.id.max, 0);
    }

    @Override
    public String getMemo() {
        return mContainerManager.getString(getPosition(), R.id.memo, "");
    }

    @Override
    public long getId() {
        return mContainerManager.getItemId(getPosition());
    }
}
