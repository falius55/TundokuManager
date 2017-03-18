package jp.gr.java_conf.falius.tundokumanager.app;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import java.util.Objects;

import jp.gr.java_conf.falius.tundokumanager.app.mainfragment.MainFragment;
import jp.gr.java_conf.falius.tundokumanager.lib.view.pageradapter.PagerType;

/**
 * Created by ymiyauchi on 2016/11/15.
 * <p>
 * 記録する媒体の種類を表す
 */

public enum Type implements PagerType {
    ANIMATION(0, "アニメ", "視聴済", "未視聴", "本", "何話まで読んだ？", "視聴済話数", "話"),
    BOOK(1, "書籍", "既読", "未読", "冊", "どこまで読んだ？", "既読ページ数", "ページ"),
    GAME(2, "ゲーム", "プレイ済", "未プレイ", "本", null, null, null),
    CD(3, "CD", "視聴済", "未視聴", "枚", null, null, null);

    private static final SparseArray<Type> codeToEnum;

    static {
        codeToEnum = new SparseArray<>();
        for (Type type : values()) {
            codeToEnum.put(type.getCode(), type);
        }
    }

    public static Type fromCode(int code) {
        return codeToEnum.get(code);
    }

    private final int code;
    private final String name;
    private final String played;
    private final String unPlayed;
    private final String unit;
    private final String dialogMessage;
    private final String dialogTag;
    private final String countUnit;

    Type(int code, String name, String played, String unPlayed, String unit,
         String dialogMessage, String dialogTag, String countUnit) {
        this.code = code;
        this.name = name;
        this.played = played;
        this.unPlayed = unPlayed;
        this.unit = unit;
        this.dialogMessage = dialogMessage;
        this.dialogTag = dialogTag;  // dialogTagがnullであればダイアログは表示されず、ページ数等も表示されない
        this.countUnit = countUnit;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String playedText(boolean played) {
        if (played) {
            return this.played;
        } else {
            return this.unPlayed;
        }
    }

    public String getUnit() {
        return unit;
    }

    public String getCountUnit() {
        return countUnit;
    }

    @Nullable
    public String getDialogMessage() {
        return dialogMessage;
    }

    @Nullable
    public String getDialogTag() {
        return dialogTag;
    }

    public boolean isPlayed(String text) {
        if (Objects.equals(text, played)) {
            return true;
        } else if (Objects.equals(text, unPlayed)) {
            return false;
        }
        throw new IllegalArgumentException(this
                + " need be passed " + played + " or " + unPlayed + " (arg : " + text + ")");
    }

    public String table() {
        return toString();
    }

    public String historyTable() {
        return table() + "_history";
    }

    @Override
    public Fragment getFragment() {
        return MainFragment.newInstance(this);
    }

    @Override
    public String getPageTitle() {
        return getName();
    }

    public boolean hasProgress() {
        return getDialogTag() != null;
    }
}
