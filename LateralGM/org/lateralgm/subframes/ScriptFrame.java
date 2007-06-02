/*
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Script;

public class ScriptFrame extends ResourceFrame<Script>
	{
	private static final long serialVersionUID = 1L;
	private static ImageIcon frameIcon = LGM.getIconForKey("ScriptFrame.SCRIPT"); //$NON-NLS-1$
	private static ImageIcon saveIcon = LGM.getIconForKey("ScriptFrame.SAVE"); //$NON-NLS-1$
	public GMLTextArea code;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		setFrameIcon(frameIcon);
		// Setup the toolbar
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add("North",tool); //$NON-NLS-1$
		// Setup the buttons
		save.setIcon(saveIcon);
		tool.add(save);
		tool.addSeparator();
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(name);
		// the code text area
		code = new GMLTextArea(res.scriptStr);
		getContentPane().add(code);
		addInternalFrameListener(new ScriptFrameListener());
		}

	public void revertResource()
		{
		LGM.currentFile.scripts.replace(res.getId(),resOriginal);
		}

	public void updateResource()
		{
		res.scriptStr = code.getText().replaceAll("\r?\n","\r\n");
		res.setName(name.getText());
		resOriginal = (Script) res.copy(false,null);
		}

	public boolean resourceChanged()
		{
		return (!code.getText().equals(res.scriptStr.replace("\r\n","\n")))
				|| (!res.getName().equals(resOriginal.getName()));
		}

	private class ScriptFrameListener implements InternalFrameListener
		{
		public void internalFrameActivated(InternalFrameEvent e)
			{
			code.grabFocus();
			}

		public void internalFrameClosed(InternalFrameEvent e)
			{
			LGM.currentFile.removeChangeListener(code.rcl);
			}

		public void internalFrameClosing(InternalFrameEvent e)
			{
			}

		public void internalFrameDeactivated(InternalFrameEvent e)
			{
			}

		public void internalFrameDeiconified(InternalFrameEvent e)
			{
			}

		public void internalFrameIconified(InternalFrameEvent e)
			{
			}

		public void internalFrameOpened(InternalFrameEvent e)
			{
			}
		}
	}
