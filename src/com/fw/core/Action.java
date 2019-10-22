package com.fw.core;

import java.sql.SQLException;

import com.fw.form.BaseForm;

/**
 * クライアントからのリクエストを受け、処理を行うインターフェース定義
 *
 * @author t.yoshida
 *
 * @param <P> フォームデータ
 */
public interface Action<P extends BaseForm>
{
	/**
	 * データベースアクセスが参照のみか否かを返す。
	 *
	 * @return 参照のみの場合: true, 更新がある場合: false
	 */
	boolean isReadOnly();

	/**
	 * クライアントから受信したフォーム値を基にレスポンス処理を行う。
	 *
	 * @param form フォーム
	 * @param container データ保持
	 * @throws SQLException データベース関連例外
	 */
	void execute(P form, DataContainer container) throws SQLException;

	/**
	 * 遷移先ページを返す。
	 *
	 * @return 遷移先ページ
	 */
	String moveTo();

	/**
	 * コミット完了通知
	 *
	 * @param container データ保持
	 */
	default void onCommitCompleted(DataContainer container) { }
}
