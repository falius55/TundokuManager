package com.example.ymiyauchi.tundokumanager.input;

import android.util.SparseArray;

/**
 * Created by ymiyauchi on 2017/01/07.
 * <p>
 * 入力画面からメイン画面に戻った際に行う操作を示す
 */

public enum Result {
    REGISTER(0),
    UPDATE(1),
    DELETE(2),
    NON(3);

    static final SparseArray<Result> codeToEnum = new SparseArray<>();

    static {
        for (Result result : values()) {
            codeToEnum.append(result.getCode(), result);
        }
    }

    public static Result fromCode(int code) {
        return codeToEnum.get(code);
    }

    private final int code;

    Result(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    enum WhereFrom {
        CONTEXT_MENU(UPDATE, DELETE), NEW_INPUT(REGISTER, NON);

        private final Result mEntryResult;
        private final Result mDeleteResult;

        WhereFrom(Result entryResult, Result deleteResult) {
            mEntryResult = entryResult;
            mDeleteResult = deleteResult;
        }

        Result getEntryResult() {
            return mEntryResult;
        }

        Result getDeleteResult() {
            return mDeleteResult;
        }
    }
}
