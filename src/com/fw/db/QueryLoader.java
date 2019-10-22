package com.fw.db;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 参照系クエリ実行クラス
 *
 * @author t.yoshida
 */
public class QueryLoader
{
	/**
	 * 指定されたクエリを基に {@link PreparedStatement} を作成する。
	 * <p>
	 * クエリに指定されたプレースホルダーには対応する値を設定する。
	 * </p>
	 *
	 * @param query クエリ
	 * @param params プレースホルダーに対応する値をプレースホルダーの順に指定
	 * @return {@link PreparedStatement}
	 * @throws SQLException データベース関連例外
	 */
	static PreparedStatement createStatement(String query, Object... params) throws SQLException
	{
		Connection con = DBAgent.getConnection();
		PreparedStatement stmt = con.prepareStatement(query);
		for(int i=0; i<params.length; i++)
		{
			int idxParam = i + 1;
			stmt.setObject(idxParam, params[i]);
		}

		return stmt;
	}

	/**
	 * 整数値をデータベースから取得する。
	 * <p>
	 * 単一の整数型の値を取得する場合に利用する。
	 * </p>
	 *
	 * @param query クエリ
	 * @param params プレースホルダーに対応する値をプレースホルダーの順に指定
	 * @return データベースから取得した整数値（取得できなかった場合、null）
	 * @throws SQLException データベース関連例外
	 */
	public static Integer loadAsInteger(String query, Object... params) throws SQLException
	{
		Integer value = null;
		try
		(
			PreparedStatement stmt = createStatement(query, params);
		)
		{
			ResultSet rs = stmt.executeQuery();
			if(rs.next())
			{
				value = new Integer(rs.getInt(1));
				if(rs.wasNull())
				{
					value = null;
				}
			}
		}

		return value;
	}

	/**
	 * クエリを基にデータベースから値を取得し、指定エンティティクラスに値を設定して返す。
	 * <p>
	 * SELECT文で指定したカラム名（エイリアス名）と一致するセッターをエンティティクラスから探し、
	 * データベースから取得した値をそのセッターを通じてエンティティクラスに設定する。
	 * </p>
	 *
	 * @param entityClass エンティティクラス
	 * @param query クエリ
	 * @param params プレースホルダーに対応する値をプレースホルダーの順に指定
	 * @return データベースから取得した値を設定したエンティティクラス
	 * @throws SQLException データベース関連例外
	 */
	public static <T> T load(Class<T> entityClass, String query, Object... params) throws SQLException
	{
		T entity = null;

		try
		(
			PreparedStatement stmt = createStatement(query, params);
		)
		{
			ResultSet rs = stmt.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int size = meta.getColumnCount();
			if(rs.next())
			{
				entity = entityClass.getDeclaredConstructor().newInstance();
				for(int i=1; i<=size; i++)
				{
					setValue(rs, meta, i, entity);
				}
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}

		return entity;
	}

	/**
	 * クエリを基にデータベースから値を取得し、指定エンティティクラスに値を設定し、リストとして返す。
	 * <p>
	 * SELECT文で指定したカラム名（エイリアス名）と一致するセッターをエンティティクラスから探し、
	 * データベースから取得した値をそのセッターを通じてエンティティクラスに設定し、リストにまとめる。
	 * </p>
	 *
	 * @param entityClass エンティティクラス
	 * @param query クエリ
	 * @param params プレースホルダーに対応する値をプレースホルダーの順に指定
	 * @return データベースから取得した値を設定したエンティティクラスのリスト
	 * @throws SQLException データベース関連例外
	 */
	public static <T> List<T> loadAsList(Class<T> entityClass, String query, Object... params) throws SQLException
	{
		List<T> list = new ArrayList<>();

		try
		(
			PreparedStatement stmt = createStatement(query, params);
		)
		{
			ResultSet rs = stmt.executeQuery();

			ResultSetMetaData meta = rs.getMetaData();
			int size = meta.getColumnCount();
			while(rs.next())
			{
				T entity = entityClass.getDeclaredConstructor().newInstance();
				for(int i=1; i<=size; i++)
				{
					setValue(rs, meta, i, entity);
				}
				list.add(entity);
			}
		}
		catch(Exception ex)
		{
			throw new RuntimeException(ex);
		}

		return list;
	}

	/**
	 * セッターを通じて対応するカラムの値をエンティティクラスに設定する。
	 *
	 * @param rs {@link ResultSet}
	 * @param meta {@link ResultSetMetaData}
	 * @param idxColumn カラムインデックス
	 * @param entity エンティティ
	 * @throws Exception セッターを通じて値を設定する際に発生するエラー
	 */
	private static <T> void setValue(ResultSet rs, ResultSetMetaData meta, int idxColumn, T entity) throws Exception
	{
		String name = meta.getColumnLabel(idxColumn);
		Object value = rs.getObject(name);

		PropertyDescriptor prop = new PropertyDescriptor(name, entity.getClass());
		Method setter = prop.getWriteMethod();
		setter.invoke(entity, value);
	}
}
