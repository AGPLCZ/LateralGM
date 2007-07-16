/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.lateralgm.components.ResourceMenu;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.ResId;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.library.LibArgument;

public class Argument
	{
	public static final byte ARG_EXPRESSION = 0;
	public static final byte ARG_STRING = 1;
	public static final byte ARG_BOTH = 2;
	public static final byte ARG_BOOLEAN = 3;
	public static final byte ARG_MENU = 4;
	public static final byte ARG_COLOR = 13;
	@Deprecated
	public static final byte ARG_FONTSTRING = 15;
	public static final byte ARG_SPRITE = 5;
	public static final byte ARG_SOUND = 6;
	public static final byte ARG_BACKGROUND = 7;
	public static final byte ARG_PATH = 8;
	public static final byte ARG_SCRIPT = 9;
	public static final byte ARG_GMOBJECT = 10;
	public static final byte ARG_ROOM = 11;
	public static final byte ARG_FONT = 12;
	public static final byte ARG_TIMELINE = 14;

	public byte kind = ARG_EXPRESSION;
	public String val = "";
	public ResId res = null; // for references to Resources

	private JComponent editor;

	public Argument(byte kind, String val, ResId res)
		{
		this.kind = kind;
		this.val = val;
		this.res = res;
		}

	public Argument()
		{
		}

	public static byte getResourceKind(byte argumentKind)
		{
		switch (argumentKind)
			{
			case ARG_SPRITE: return Resource.SPRITE;
			case ARG_SOUND: return Resource.SOUND;
			case ARG_BACKGROUND: return Resource.BACKGROUND;
			case ARG_PATH: return Resource.PATH;
			case ARG_SCRIPT: return Resource.SCRIPT;
			case ARG_GMOBJECT: return Resource.GMOBJECT;
			case ARG_ROOM: return Resource.ROOM;
			case ARG_FONT: return Resource.FONT;
			case ARG_TIMELINE: return Resource.TIMELINE;
			default : return 0;
			}
		}

	//TODO: Add support for ResourceMenu
	private JComponent makeEditor(LibArgument la)
		{
		if (kind == ARG_BOOLEAN)
			{
			String[] s = { "false","true" };
			JComboBox b = new JComboBox(s);
			b.setSelectedIndex(Integer.parseInt(val));
			return b;
			}
		if (kind == ARG_MENU)
			{
			if (la == null) return new JTextField(val);
			String[] s = la.menu.split("\\|"); //$NON-NLS-1$
			JComboBox b = new JComboBox(s);
			b.setSelectedIndex(Integer.parseInt(val));
			return b;
			}
		if (kind == ARG_COLOR)
			{
			final String s = Messages.getString("Argument.COLOR");
			final JButton b = new JButton(s);
			Color revCol = Color.decode(val);
			b.setBackground(new Color(revCol.getGreen(),revCol.getBlue(),revCol.getRed()));
			b.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
						{
						Color ret = JColorChooser.showDialog(null,s,b.getBackground());
						if (ret != null) b.setBackground(ret);
						}
				});
			return b;
			}
		return new JTextField(val);
		}

	/**
	 * Gets a JComponent editor for this Argument. Defaults to raw JTextField.
	 * @param la - The corresponding LibArgument, used for Menus.
	 * May be null, but then a menu will default to JTextField.
	 * @return One of JButton, JComboBox, JColorChooser, ResourceMenu, or JTextField
	 */
	public JComponent getEditor(LibArgument la)
		{
		if (editor == null) editor = makeEditor(la);
		return editor;
		}

	/** Commits any changes in the JComponent editor to update this Argument. */
	public void commit()
		{
		if (editor instanceof JTextField)
			{
			val = ((JTextField) editor).getText();
			return;
			}
		if (editor instanceof JComboBox)
			{
			val = Integer.toString(((JComboBox) editor).getSelectedIndex());
			return;
			}
		if (editor instanceof JButton)
			{
			val = Integer.toString(Util.getGmColor(((JButton) editor).getBackground()));
			}
		if (editor instanceof ResourceMenu)
			{
			Resource sel = ((ResourceMenu) editor).getSelected();
			if (sel == null)
				val = "-1";
			else
				val = sel.getId().toString();
			return;
			}
		}
	}
