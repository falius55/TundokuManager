package jp.gr.java_conf.falius.tundokumanager.app.tree;

/**
 * Created by ymiyauchi on 2017/02/05.
 *
 * 木構造の各ノードを管理するクラスのインターフェースです。
 */

public interface TreeManager {

    TreeElement findById(long id);
}
