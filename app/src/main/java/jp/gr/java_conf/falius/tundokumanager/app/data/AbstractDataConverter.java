package jp.gr.java_conf.falius.tundokumanager.app.data;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import com.example.ymiyauchi.app.R;

import jp.gr.java_conf.falius.tundokumanager.app.ApplicationManager;
import jp.gr.java_conf.falius.tundokumanager.app.Type;
import jp.gr.java_conf.falius.tundokumanager.app.database.ItemColumns;
import jp.gr.java_conf.falius.util.datetime.DateTime;

/**
 * Created by ymiyauchi on 2017/01/19.
 */

abstract class AbstractDataConverter implements DataConverter {
    private final Type mType;
    private final int mPosition;

    AbstractDataConverter(Type type, int position) {
        mType = type;
        mPosition = position;
    }

    /**
     * @param intent
     * @throws IllegalArgumentException intentにType情報あるいはposition情報が正しく含まれていない場合
     */
    AbstractDataConverter(Intent intent) {
        mType = typeFrom(intent);
        mPosition = positionFrom(intent);
    }

    private Type typeFrom(Intent intent) {
        if (!intent.hasExtra(TYPE))
            throw new IllegalArgumentException("not found TYPE Extra in intent");
        return Type.fromCode(intent.getIntExtra(TYPE, 0));
    }

    private int positionFrom(Intent intent) {
        return intent.getIntExtra(POSITION, -1);
    }

    @Override
    public Intent toIntent() {
        Intent intent = new Intent();
        return stuffInto(intent);
    }

    @Override
    public Intent stuffInto(Intent intent) {

        intent.putExtra(POSITION, getPosition());
        intent.putExtra(TYPE, mType.getCode());
        intent.putExtra(ItemColumns.ID.getName(), getId());
        intent.putExtra(ItemColumns.DATE.getName(), getDate());
        intent.putExtra(ItemColumns.NAME.getName(), getName());
        intent.putExtra(ItemColumns.PRICE.getName(), getPrice());
        intent.putExtra(ItemColumns.PLAYED.getName(), isPlayed());
        intent.putExtra(ItemColumns.MEMO.getName(), getMemo());

        if (mType.hasProgress()) {
            intent.putExtra(ItemColumns.CAPACITY.getName(), getCapacity());
            intent.putExtra(ItemColumns.CURRENT.getName(), getCurrent());
        }

        return intent;
    }

    @Override
    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        return stuffInto(bundle);
    }

    @Override
    public Bundle stuffInto(Bundle bundle) {
        bundle.putInt(POSITION, getPosition());
        bundle.putInt(TYPE, getType().getCode());
        bundle.putLong(ItemColumns.ID.getName(), getId());
        bundle.putString(ItemColumns.DATE.getName(), getDate());
        bundle.putString(ItemColumns.NAME.getName(), getName());
        bundle.putInt(ItemColumns.PRICE.getName(), getPrice());
        bundle.putBoolean(ItemColumns.PLAYED.getName(), isPlayed());
        bundle.putString(ItemColumns.MEMO.getName(), getMemo());

        if (mType.hasProgress()) {
            bundle.putInt(ItemColumns.CAPACITY.getName(), getCapacity());
            bundle.putInt(ItemColumns.CURRENT.getName(), getCurrent());
        }

        return bundle;
    }

    @Override
    public ContentValues toContentValuesForDB() {
        ContentValues values = new ContentValues();

        values.put(ItemColumns.NAME.getName(), getName());
        values.put(ItemColumns.DATE.getName(), getDateForDB());
        values.put(ItemColumns.PRICE.getName(), getPrice());
        values.put(ItemColumns.PLAYED.getName(), getPlayedText());
        values.put(ItemColumns.MEMO.getName(), getMemo());

        if (mType.hasProgress()) {
            values.put(ItemColumns.CURRENT.getName(), getCurrent());
            values.put(ItemColumns.CAPACITY.getName(), getCapacity());
        }
        return values;
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public int getPosition() {
        return mPosition;
    }

    @Override
    public String getDateForDB() {
        return DateTime.newInstance(getDate()).formatTo(DateTime.SQLITE_DATE_FORMAT);
    }

    @Override
    public String getDays() {
        int days = DateTime.newInstance(getDate()).computeElapsedDays(DateTime.now());
        String strDays = Integer.toString(days);
        return ApplicationManager.getContext().getString(R.string.days, strDays);
    }

    @Override
    public String getPlayedText() {
        return getType().playedText(isPlayed());
    }

    @Override
    public String toString() {
        final String SEPARATOR = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("type : ").append(getType()).append(SEPARATOR);
        sb.append("position : ").append(getPosition()).append(SEPARATOR);
        sb.append("name : ").append(getName()).append(SEPARATOR);
        sb.append("date : ").append(getDate()).append(SEPARATOR);
        sb.append("price : ").append(getPrice()).append(SEPARATOR);
        sb.append("played : ").append(getPlayedText()).append(SEPARATOR);
        return sb.toString();
    }
}
