package com.fw.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Action} 実装クラスにてサポートする画面ID、イベントIDを指定するためのアノテーション
 *
 * @author t.yoshida
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Page
{
	/**
	 * 画面IDを返す。
	 *
	 * @return 画面ID
	 */
	String pageId();

	/**
	 * イベントIDを返す。
	 *
	 * @return イベントID
	 */
	String eventId();
}
