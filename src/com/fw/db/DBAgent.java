package com.fw.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * データベースとの処理のやり取りを担当
 * <p>
 * コネクションはスレッド毎に設定される。
 * <p>
 * ※コネクションプール非対応
 *
 * @author t.yoshida
 */
public class DBAgent
{
	// スレッド単位でコネクションを保持するスレッドローカル
	private static final ThreadLocal<Connection> _conHolder = new ThreadLocal<>();

	private DBAgent()
	{

	}

	/**
	 * トランザクションの開始
	 */
	public static void beginTransaction(ServletContext context) throws SQLException
	{
		Connection con = _conHolder.get();
		if(con != null)
		{
			// 既にコネクションが取得されていた場合、そのコネクションは破棄
			dispose();
		}

		// スレッドローカルにコネクションを登録
		_conHolder.set(con = createConnection(context));
	}

	/**
	 * トランザクションのコミット
	 *
	 * @throws SQLException データベース関連例外
	 */
	public static void commit() throws SQLException
	{
		Connection con = getConnection();
		if(con != null)
		{
			con.commit();
		}
	}

	/**
	 * トランザクションのロールバック
	 *
	 * @throws SQLException データベース関連例外
	 */
	public static void rollback() throws SQLException
	{
		Connection con = getConnection();
		if(con != null)
		{
			con.rollback();
		}
	}

	/**
	 * コネクションのクローズやリソースの開放等の最終処理
	 */
	public static void dispose() throws SQLException
	{
		Connection con = getConnection();
		if(con != null)
		{
			// コネクションのクローズ
			con.close();

			// スレッドローカルが保持していたコネクションを開放
			_conHolder.remove();
		}
	}

	/**
	 * スレッドに登録されたコネクションを取得する。
	 *
	 * @return コネクション
	 * @throws SQLException データベース関連例外
	 */
	static Connection getConnection() throws SQLException
	{
		Connection con = _conHolder.get();
		if(con == null)
		{
			throw new SQLException("You have to get a connection first through beginTransaction().");
		}

		return _conHolder.get();
	}

	/**
	 * コネクションを生成する。
	 *
	 * @return コネクション
	 * @throws SQLException データベース関連例外
	 */
	private static Connection createConnection(ServletContext context) throws SQLException
	{
		// コネクションの生成
		Connection con = null;
		try
		{
			Context ctx = new InitialContext();
			DataSource ds = (DataSource)ctx.lookup("java:comp/env/jdbc/datasource");
			con = ds.getConnection();
		}
		catch(NamingException ex)
		{
			throw new SQLException(ex);
		}

		// オート・コミットOFF
		con.setAutoCommit(false);

		// トランザクション分離レベルの設定
		DatabaseMetaData meta = con.getMetaData();
		if(meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
		{
			// ※SQLite は未サポート
			con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		}
		else
		{
			con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		}

		return con;
	}
}
