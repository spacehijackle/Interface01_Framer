package com.fw.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * データ保持クラス
 *
 * @author t.yoshida
 */
public class DataContainer
{
	// リクエスト変数
	private HttpServletRequest request;

	/**
	 * リクエスト変数を指定して {@link DataContainer} を生成する。
	 *
	 * @param request リクエスト変数
	 */
	DataContainer(HttpServletRequest request)
	{
		this.request = request;
	}

	/**
	 * リクエスト変数に設定した値を取得する。
	 *
	 * @param key キー
	 * @return 値
	 */
	public Object getAttr(String key)
	{
		return request.getAttribute(key);
	}

	/**
	 * リクエスト変数に値を設定する。
	 *
	 * @param key キー
	 * @param value 値
	 */
	public void setAttr(String key, Object value)
	{
		request.setAttribute(key, value);
	}

	/**
	 * リクエスト変数に値を設定する。
	 * キーは値のクラス名が自動的に指定される。
	 *
	 * @param value 値
	 */
	public void setAttr(Object value)
	{
		String key = value.getClass().getSimpleName();
		setAttr(key, value);
	}

	/**
	 * セッションから設定した値を取得する。
	 *
	 * @param key セッションキー
	 * @return 値
	 */
	public Object getAttrFromSession(String key)
	{
		HttpSession session = request.getSession(true);
		return session.getAttribute(key);
	}

	/**
	 * セッションに値を設定する。
	 *
	 * @param key セッションキー
	 * @param value 値
	 */
	public void setAttrAsSession(String key, Object value)
	{
		HttpSession session = request.getSession(true);
		session.setAttribute(key, value);
	}

	/**
	 * セッションに値を設定する。
	 * キーは値のクラス名が自動的に指定される。
	 *
	 * @param value 値
	 */
	public void setAttrAsSession(Object value)
	{
		String key = value.getClass().getSimpleName();
		setAttrAsSession(key, value);
	}

	/**
	 * 指定したセッションキーの値を削除する。
	 *
	 * @param key セッションキー
	 */
	public void removeSession(String key)
	{
		HttpSession session = request.getSession();
		session.removeAttribute(key);
	}
}
