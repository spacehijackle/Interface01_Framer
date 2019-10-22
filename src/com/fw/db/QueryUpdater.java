package com.fw.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 更新系クエリ実行クラス
 *
 * @author t.yoshida
 */
public class QueryUpdater
{
	/**
	 * 指定クエリを実行し、テーブル更新する。
	 *
	 * @param query クエリ
	 * @param params プレースホルダーに対応する値をプレースホルダーの順に指定
	 * @return 更新レコード数
	 * @throws SQLException データベース関連例外
	 */
	public static int update(String query, Object... params) throws SQLException
	{
		int cntUpdated = 0;

		try
		(
			PreparedStatement stmt = QueryLoader.createStatement(query, params);
		)
		{
			cntUpdated = stmt.executeUpdate();
		}

		return cntUpdated;
	}
}
