/*
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * 
 * Lateral GM is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Lateral GM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Lateral GM; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.lateralgm.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.lateralgm.components.FramePrefsHandler;
import org.lateralgm.components.GmMenuBar;
import org.lateralgm.components.GmTreeEditor;
import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.Listener;
import org.lateralgm.components.MDIPane;
import org.lateralgm.components.ResNode;
import org.lateralgm.file.Gm6File;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.subframes.EventFrame;
import org.lateralgm.subframes.GameInformationFrame;
import org.lateralgm.subframes.GameSettingFrame;

public class LGM extends JPanel
	{
	private static final long serialVersionUID = 1L;
	public static JFrame frame = new JFrame("Lateral GM 6.1: <new game>"); //$NON-NLS-1$
	public static Listener listener = new Listener();
	public static JToolBar tool;
	public static JTree tree;
	public static ResNode root;
	public static Gm6File currentFile = new Gm6File();
	public static MDIPane mdi;
	public static GameInformationFrame gameInfo;
	public static GameSettingFrame gameSet;
	public static EventFrame eventSelect;

	public LGM()
		{
		super(new BorderLayout());
		}

	public static ImageIcon findIcon(String filename)
		{
		String location = "org/lateralgm/icons/" + filename; //$NON-NLS-1$
		ImageIcon ico = new ImageIcon(location);
		if (ico.getIconWidth() == -1)
			{
			URL url = LGM.class.getClassLoader().getResource(location);
			if (url != null)
				{
				ico = new ImageIcon(url);
				}
			}
		return ico;
		}

	public static ImageIcon getIconForKey(String key)
		{
		Properties iconProps = new Properties();
		InputStream is = LGM.class.getClassLoader().getResourceAsStream(
				"org/lateralgm/main/icons.properties"); //$NON-NLS-1$
		try
			{
			iconProps.load(is);
			}
		catch (IOException e)
			{
			System.err.println("Unable to read icons.properties");
			}
		String filename = iconProps.getProperty(key,""); //$NON-NLS-1$
		if (filename != "") //$NON-NLS-1$
			return findIcon(filename);
		return null;
		}

	public JButton makeButton(String key)
		{
		JButton but = new JButton();
		makeButton(but,key);
		return but;
		}

	public AbstractButton makeButton(AbstractButton but, String key)
		{
		Icon ico = LGM.getIconForKey(key);
		if (ico != null)
			but.setIcon(ico);
		else
			but.setIcon(GmTreeGraphics.getBlankIcon());
		but.setActionCommand(key);
		but.setToolTipText(Messages.getString(key));
		but.addActionListener(listener);
		return but;
		}

	public void createToolBar()
		{
		tool = new JToolBar();
		tool.setFloatable(false);
		add("North",tool); //$NON-NLS-1$
		tool.add(makeButton("LGM.NEW")); //$NON-NLS-1$
		tool.add(makeButton("LGM.OPEN")); //$NON-NLS-1$
		tool.add(makeButton("LGM.SAVE")); //$NON-NLS-1$
		tool.add(new JToolBar.Separator());
		tool.add(makeButton("LGM.SAVEAS")); //$NON-NLS-1$
		}

	public void createTree(boolean populate)
		{
		createTree(new ResNode("Root",(byte) 0,(byte) 0,null),populate); //$NON-NLS-1$
		}

	public void createTree(ResNode newroot, boolean populate)
		{
		root = newroot;
		tree = new JTree(new DefaultTreeModel(root));
		GmTreeGraphics renderer = new GmTreeGraphics();
		GmTreeEditor editor = new GmTreeEditor(tree,renderer);
		editor.addCellEditorListener(listener);
		tree.setEditable(true);
		tree.addMouseListener(listener);
		tree.setTransferHandler(listener);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setCellEditor(editor);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		if (populate)
			{
			root.addChild(Messages.getString("LGM.SPRITES"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.SPRITE);
			root.addChild(Messages.getString("LGM.SOUNDS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.SOUND);
			root.addChild(Messages.getString("LGM.BACKGROUNDS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.BACKGROUND);
			root.addChild(Messages.getString("LGM.PATHS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.PATH);
			root.addChild(Messages.getString("LGM.SCRIPTS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.SCRIPT);
			root.addChild(Messages.getString("LGM.FONTS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.FONT);
			root.addChild(Messages.getString("LGM.TIMELINES"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.TIMELINE);
			root.addChild(Messages.getString("LGM.OBJECTS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.GMOBJECT);
			root.addChild(Messages.getString("LGM.ROOMS"), //$NON-NLS-1$
					ResNode.STATUS_PRIMARY,Resource.ROOM);
			root.addChild(Messages.getString("LGM.GAMEINFO"), //$NON-NLS-1$
					ResNode.STATUS_SECONDARY,Resource.GAMEINFO);
			root.addChild(Messages.getString("LGM.GAMESETTINGS"), //$NON-NLS-1$
					ResNode.STATUS_SECONDARY,Resource.GAMESETTINGS);
			tree.setSelectionPath(new TreePath(root).pathByAddingChild(root.getChildAt(0)));
			}
		else
			{
			tree.setSelectionRow(0);
			}

		/*
		 * Setup the rest of the main window
		 */

		JScrollPane scroll = new JScrollPane(tree);
		scroll.setPreferredSize(new Dimension(200,100));
		mdi = new MDIPane();
		JScrollPane scroll2 = new JScrollPane(mdi);
		mdi.setScrollPane(scroll2);
		scroll2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,scroll,scroll2);
		split.setDividerLocation(170);
		add(split);
		// .setDefaultCloseOperation(GameInformationFrame.HIDE_ON_CLOSE);
		mdi.setBackground(Color.BLACK);
		mdi.add(gameSet);
		mdi.add(gameInfo);
		//TODO: Get an icon for event selector
		JToggleButton toggle = (JToggleButton) makeButton(new JToggleButton(),"LGM.TOGGLE_EVENT");
		tool.addSeparator();
		tool.add(toggle);
		eventSelect = new EventFrame(toggle);
		mdi.add(eventSelect);
		// gameInfo.setVisible(true);
		}

	static
		{
		try
			{
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			// TODO At some point, add LAF as an option
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		gameInfo = new GameInformationFrame();
		gameSet = new GameSettingFrame();
		}

	public static void main(String[] args)
		{
		LibManager.autoLoad();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		LGM f = new LGM();
		f.createToolBar();
		f.createTree(true);
		frame.setJMenuBar(new GmMenuBar());
		f.setOpaque(true);
		frame.setContentPane(f);
		new FramePrefsHandler(frame);
		frame.setVisible(true);
		}
	}
