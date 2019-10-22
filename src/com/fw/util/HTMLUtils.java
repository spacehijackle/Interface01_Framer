package com.fw.util;

/**
 * HTML関連ユーティリティ
 *
 * @author t.yoshida
 */
public class HTMLUtils
{
	/**
	 * サニタイズを行う。
	 *
	 * @param text 対象文字列
	 * @return サニタイズ後文字列
	 */
	public static String sanitize(String text)
	{
		text = text.replaceAll("&",  "&amp;");
		text = text.replaceAll("<",  "&lt;");
		text = text.replaceAll(">",  "&gt;");
		text = text.replaceAll("\"", "&quot;");
		text = text.replaceAll("'" , "&#39;");

		return text;
	}

	/**
	 * 改行コードを改行タグに変換する。
	 *
	 * @param text 対象文字列
	 * @return 変換後文字列
	 */
	public static String convRetCodeToTag(String text)
	{
		text = text.replaceAll("\r\n", "<br/>");
		text = text.replaceAll("\n", "<br/>");
		text = text.replaceAll("\r", "<br/>");

		return text;
	}
}
