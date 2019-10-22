package com.fw.core;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.fw.form.BaseForm;

/**
 * アクション管理クラス
 *
 * @author t.yoshida
 */
public class ActionManager<P extends BaseForm>
{
	// アクションリスト
	private List<Action<P>> _actions;

	// 固有情報
	private ActionSpecificInfo _info;

	/**
	 * {@link ActionManager} を生成する。
	 *
	 * @param info 固有情報
	 */
	ActionManager(ActionSpecificInfo info)
	{
		_actions = new ArrayList<>();
		_info = info;
		searchActions(info.getPackageNameWhereActionImplExists());
	}

	/**
	 * 指定されたパッケージ直下に存在する {@link Action} 実装クラスを検索し、
	 * 取得した実装クラスを保持する。
	 * <p>
	 * Action 実装クラスは必ず {@link Page} アノテーションを付加すること。
	 * </p>
	 *
	 * @param packageName 検索対象パッケージ名
	 */
	private void searchActions(String packageName)
	{
		String resourceName = packageName.replace('.', '/');
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL root = classLoader.getResource(resourceName);

		/**
		 * 検索対象のクラスファイルが JAR で圧縮されていない場合
		 */
		if("file".equals(root.getProtocol()))
		{
			File[] files = new File(root.getFile()).listFiles((dir, name) -> name.endsWith(".class"));
			for(File file : files)
			{
				String name = file.getName();
				name = name.replaceAll(".class$", "");
				String fullName = packageName + "." + name;

				try
				{
					Class<?> clazz = Class.forName(fullName);
					Class<?>[] interfaces = clazz.getInterfaces();
					for(Class<?> inf : interfaces)
					{
						if(Action.class.getName().equals(inf.getName()))
						{
							if(clazz.getAnnotation(Page.class) != null)
							{
								// Action 実装クラスをインスタンス化し、リストに追加
								@SuppressWarnings("unchecked")
								Action<P> instance = (Action<P>)clazz.getDeclaredConstructor().newInstance();
								_actions.add(instance);
							}
						}
					}
				}
				catch(Exception ex)
				{
					// Action 実装クラスをインスタンス化する過程で例外が発生した場合
					throw new RuntimeException("Action instantiation error", ex);
				}
			}
		}

		/*
		 * 検索対象のクラスファイルが JAR で圧縮されている場合
		 */
		if("jar".equals(root.getProtocol()))
		{
			try
			(
				JarFile jarFile = ((JarURLConnection)root.openConnection()).getJarFile()
			)
			{
				Enumeration<JarEntry> e = jarFile.entries();
				while(e.hasMoreElements())
				{
					JarEntry entry = e.nextElement();
					String name = entry.getName();
					if(!name.startsWith(resourceName)) continue;
					if(!name.endsWith(".class")) continue;

					try
					{
						String fullName = name.replace('/', '.').replaceAll(".class$", "");
						Class<?> clazz = classLoader.loadClass(fullName);
						if(Action.class.isAssignableFrom(clazz))
						{
							if(clazz.getAnnotation(Page.class) != null)
							{
								// Action 実装クラスをインスタンス化し、リストに追加
								@SuppressWarnings("unchecked")
								Action<P> instance = (Action<P>)clazz.getDeclaredConstructor().newInstance();
								_actions.add(instance);
							}
						}
					}
					catch(Exception ex)
					{
						// Action 実装クラスをインスタンス化する過程で例外が発生した場合
						throw new RuntimeException("Action instantiation error", ex);
					}
				}
			}
			catch(IOException ex) { }
		}
	}

	/**
	 * 指定されたページID、イベントIDに対応する {@link Action} の実装を返す。
	 * <p>
	 * ※前提として、両IDは実装クラスのクラスアノテーションとして定義されていること。
	 * </p>
	 *
	 * @param pageId ページID
	 * @param eventId イベントID
	 * @return 対応する {@link Action} の実装。存在しない場合、{@link ActionSpecificInfo#getDefaultAction()} の返り値。
	 */
	Action<P> find(String pageId, String eventId)
	{
		if(pageId == null) return _info.getDefaultAction();

		for(Action<P> action : _actions)
		{
			Page page = action.getClass().getAnnotation(Page.class);
			if(pageId.equals(page.pageId()))
			{
				if(eventId == null)
				{
					// イベントIDの指定が無い場合でも、アノテーションのイベントIDが"*"であればＯＫ
					if(page.eventId().equals("*"))
					{
						return action;
					}
				}
				else
				{
					if(eventId.equals(page.eventId()))
					{
						return action;
					}
				}
			}
		}

		return _info.getDefaultAction();
	}
}
