package com.fw.core;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fw.db.DBAgent;
import com.fw.form.BaseForm;

/**
 * サーブレット共通化
 *
 * @author T.Yoshida
 */
public abstract class MainServlet<P extends BaseForm> extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	// ActionManager の実装クラス
	private ActionManager<P> _actionManager;

	@Override
	public void init() throws ServletException
	{
		ServletConfig config = getServletConfig();
		ServletContext context = config.getServletContext();

		/*
		 * web.xml から ActionSpecificInfo の実装クラス名を取得し、
		 * インスタンス化した後、ActionManager コンストラクタ引数として指定。
		 */
		String infoClassName = context.getInitParameter("class.manager.action.info");
		try
		{
			Class<?> infoClass = Class.forName(infoClassName);
			ActionSpecificInfo info = (ActionSpecificInfo)infoClass.getDeclaredConstructor().newInstance();
			_actionManager = new ActionManager<P>(info);
		}
		catch(Exception ex)
		{
			log("ActionManager instantiation error", ex);
			throw new ServletException(ex);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doMain(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doMain(request, response);
	}

	/**
	 * サーバーメイン処理
	 *
	 * @param request リクエスト
	 * @param response レスポンス
	 */
	private void doMain(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		// 文字コード指定
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");

		/*
		 * 画面ID、イベントIDに対応する Action を取得
		 */
		String pageId = request.getParameter("pageId");
		String eventId = request.getParameter("eventId");
		Action<P> action = _actionManager.find(pageId, eventId);

		/*
		 * Action 実装クラスで扱うフォームクラスのインスタンス化
		 */
		ParameterizedType type = (ParameterizedType)action.getClass().getGenericInterfaces()[0];

		@SuppressWarnings("unchecked")
		Class<P> formClass = (Class<P>)type.getActualTypeArguments()[0];

		P form;
		try
		{
			form = formClass.getDeclaredConstructor().newInstance();
		}
		catch(Exception ex)
		{
			log("Form class instantiation error", ex);
			throw new ServletException(ex);
		}

		/*
		 * フォームクラスにクライアントのパラメータ値を設定
		 * ※パラメータ名を基にリフレクション機能により対応するセッターメソッドを実行
		 * ※SELECTタグの複数選択には未対応
		 */
		Enumeration<String> names = request.getParameterNames();
		while(names.hasMoreElements())
		{
			String name = names.nextElement();
			try
			{
				PropertyDescriptor prop = new PropertyDescriptor(name, formClass);
				Method method = prop.getWriteMethod();
				method.invoke(form, request.getParameter(name));
			}
			catch(Exception ex)
			{
				// フォームクラスへの値の設定中に例外が発生しても、次の値の設定に進む
				log("Wrong parameter [" + name + "]", ex);
			}
		}

		// トランザクション処理
		doTransaction(request, response, action, form);
	}

	/**
	 * トランザクション処理
	 *
	 * @param request リクエスト
	 * @param response レスポンス
	 * @param action アクション
	 * @param form フォーム
	 */
	protected void doTransaction
	(
		HttpServletRequest request, HttpServletResponse response, Action<P> action, P form
	)
		throws ServletException
	{
		/*
		 * Action を実行し、処理結果を遷移先ページに反映させる。
		 */
		DataContainer container = new DataContainer(request);
		try
		{
			// トランザクションの開始
			DBAgent.beginTransaction(getServletContext());

			// 処理実行
			action.execute(form, container);

			if(!action.isReadOnly())
			{
				// コミット
				DBAgent.commit();

				// コミット完了通知
				action.onCommitCompleted(container);
			}

			// 指定されたページへ遷移
			String path = action.moveTo();
			moveTo(path, request, response);
		}
		catch(Exception ex)
		{
			if(!action.isReadOnly())
			{
				// ロールバック
				try
				{
					DBAgent.rollback();
				}
				catch(SQLException ex2) { }
			}

			log("Transaction error", ex);
			container.setAttr("error", ex);

			// エラー時ページ遷移
			try
			{
				moveToInCaseOfError(ex, request, response);
			}
			catch(Exception ex3)
			{
				log("Error page moving error", ex);
			}
		}
		finally
		{
			// コネクションの破棄
			try
			{
				DBAgent.dispose();
			}
			catch(SQLException ex) { }
		}
	}

	/*
	 * ページ遷移
	 */
	protected void moveTo(String path, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		RequestDispatcher dispatcher = request.getRequestDispatcher(path);
		dispatcher.forward(request, response);
	}

	/**
	 * エラー（例外）が発生した場合のページ遷移
	 *
	 * @param ex 発生例外
	 * @param request リクエスト変数
	 * @param response レスポンス変数
	 */
	protected abstract void moveToInCaseOfError(Exception ex, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
