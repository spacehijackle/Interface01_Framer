package com.fw.form;

/**
 * フォーム値を保持する基底クラス
 *
 * @author t.yoshida
 */
public class BaseForm
{
	// 画面ID
	private String pageId;

	// イベントID
	private String eventId;

	/**
	 * 画面IDを返す。
	 *
	 * @return 画面ID
	 */
	public String getPageId()
	{
		return pageId;
	}

	/**
	 * 画面IDを設定する。
	 *
	 * @param pageId 画面ID
	 */
	public void setPageId(String pageId)
	{
		this.pageId = pageId;
	}

	/**
	 * イベントIDを返す。
	 *
	 * @return イベントID
	 */
	public String getEventId()
	{
		return eventId;
	}

	/**
	 * イベントIDを設定する。
	 *
	 * @param eventId イベントID
	 */
	public void setEventId(String eventId)
	{
		this.eventId = eventId;
	}
}
