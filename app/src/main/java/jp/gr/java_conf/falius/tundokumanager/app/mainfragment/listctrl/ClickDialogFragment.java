package jp.gr.java_conf.falius.tundokumanager.app.mainfragment.listctrl;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import jp.gr.java_conf.falius.tundokumanager.app.ApplicationManager;
import jp.gr.java_conf.falius.tundokumanager.app.MainActivity;
import jp.gr.java_conf.falius.tundokumanager.app.R;
import jp.gr.java_conf.falius.tundokumanager.app.Type;
import jp.gr.java_conf.falius.tundokumanager.app.data.BundleDataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.data.DataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.data.ListItemDataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.data.MutableDataConverter;
import jp.gr.java_conf.falius.tundokumanager.app.database.ItemColumns;
import jp.gr.java_conf.falius.tundokumanager.lib.view.manager.ContainerManager;


/**
 * Created by ymiyauchi on 2015/12/28.
 * <p>
 * ダイアログ表示のフラグメント
 */

public class ClickDialogFragment extends DialogFragment {

    public static ClickDialogFragment newInstance(Type type, int position, ContainerManager containerManager) {
        DataConverter data = new ListItemDataConverter(type, containerManager, position);
        ClickDialogFragment fragment = new ClickDialogFragment();
        fragment.setArguments(data.toBundle());
        return fragment;
    }

    /**
     * ダイアログの作成処理
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final DataConverter data = new BundleDataConverter(getArguments());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_listclick, null);

        builder.setView(layout);

        // シークバーの初期設定
        final Type type = data.getType();
        final SeekBar seekbar = (SeekBar) layout.findViewById(R.id.seekbar_page);
        final int capacity = data.getCapacity();
        boolean played = data.isPlayed();
        final int current = played ? capacity : data.getCurrent();

        // 現在値と最大値
        seekbar.setMax(capacity); // 最大値
        seekbar.setProgress(current); // 現在値
        // 既読ページ数をテキストビューに設定
        final TextView txtSetValue = (TextView) layout.findViewById(R.id.txtsetvalue);
        setCurrentTag(txtSetValue, data, current);

        // シークバーのリスナー
        seekbar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // ツマミをドラッグしたときの処理
                        // 既読ページ数を表示させる
                        setCurrentTag(txtSetValue, data, seekbar.getProgress());
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                }
        );

        // UP/DOWNボタン押下処理
        Button btnUp = (Button) layout.findViewById(R.id.btn_up);
        Button btnDown = (Button) layout.findViewById(R.id.btn_down);
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = seekbar.getProgress();

                int newValue = value + 1;
                setCurrentTag(txtSetValue, data, newValue);
                seekbar.setProgress(newValue);
            }
        });
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int value = seekbar.getProgress();

                int newValue = value - 1;
                setCurrentTag(txtSetValue, data, newValue);
                seekbar.setProgress(newValue);
            }
        });

        // タイトル表示
        // 保存・閉じるボタン押下処理
        builder.setMessage(type.getDialogMessage())
                .setPositiveButton(
                        ApplicationManager.getContext().getString(R.string.dialog_save),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 保存ボタンが押されたときの処理
                                // シークバーの値を取得
                                int numCurrentPage = seekbar.getProgress();
                                boolean isPlayed = numCurrentPage == capacity;
                                DataConverter newData = new MutableDataConverter(data)
                                        .putCurrent(numCurrentPage).putPlayed(isPlayed);
                                MainActivity callingActivity = (MainActivity) getActivity();
                                callingActivity.onDialogLeaved(newData);

                            }
                        })
                .setNegativeButton(
                        ApplicationManager.getContext().getString(R.string.dialog_close),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

        return builder.create();
    }

    private void setCurrentTag(TextView textView, DataConverter data, int num) {
        if (num < 0) {
            num = 0;
        } else if (num >= data.getCapacity()) {
            num = data.getCapacity();
        }
        String text = data.getType().getDialogTag() + " : " + num;
        textView.setText(text);
        getArguments().putInt(ItemColumns.CURRENT.getName(), num);
    }
}

