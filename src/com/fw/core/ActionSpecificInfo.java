package com.fw.core;

import com.fw.form.BaseForm;

/**
 * {@link Action} の固有情報を提供するインターフェース定義
 *
 * @author t.yoshida
 */
public interface ActionSpecificInfo
{
	/**
	 * {@link ActionManager#find(String, String)} にて、指定された画面ID、イベントIDに対応する
	 * {@link Action} の実装が無い場合に利用されるデフォルト {@link Action} 実装を生成する。
	 *
	 * @return デフォルト {@link Action} 実装
	 */
	<P extends BaseForm> Action<P> createDefaultAction();

	/**
	 * {@link Action} の実装が存在するパッケージ名を返す。
	 *
	 * @return パッケージ名
	 */
	String getPackageNameWhereActionImplExists();
}