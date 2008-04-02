/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * 
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.subframes;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.impl.NameDocument;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

/** Provides common functionality and structure to Resource editing frames */
public abstract class ResourceFrame<R extends Resource<R>> extends MDIFrame implements
		DocumentListener,ActionListener
	{
	private static final long serialVersionUID = 1L;
	/**
	 * The Resource's name - setup automatically to update the title of the frame and
	 * the ResNode's text
	 */
	public final JTextField name = new JTextField();
	/** Automatically set up to save and close the frame */
	public final JButton save = new JButton();
	/** The resource this frame is editing (feel free to change it as you wish) */
	public R res;
	/** Backup of res as it was before changes were made */
	public R resOriginal;
	/** The ResNode this frame is linked to */
	public final ResNode node;

	public String titlePrefix = ""; //$NON-NLS-1$
	public String titleSuffix = ""; //$NON-NLS-1$

	/**
	 * Note for inheriting classes. Be sure to call this parent instantiation for proper setup.
	 * The res and node parameters are only needed in the instantiation to assign globals;
	 * That is, once you call this, they will immediately gain global scope and may be treated thusly.
	 */
	public ResourceFrame(R res, ResNode node)
		{
		super("",true,true,true,true); //$NON-NLS-1$
		this.res = res;
		this.node = node;
		resOriginal = res.copy();
		setTitle(res.getName());
		setFrameIcon(Resource.ICON[res.getKind()]);
		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		name.setDocument(new NameDocument());
		name.setText(res.getName());
		name.getDocument().addDocumentListener(this);
		name.setCaretPosition(0);
		save.setToolTipText(Messages.getString("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(LGM.getIconForKey("ResourceFrame.SAVE")); //$NON-NLS-1$
		save.addActionListener(this);
		}

	public void updateResource()
		{
		commitChanges();
		resOriginal = res.copy();
		}

	/**
	 * Simply calls:
	 * <pre>LGM.currentFile.&ltappropriate list&gt.replace(res,resOriginal);</pre>
	 */
	public abstract void revertResource();

	public abstract boolean resourceChanged();

	public abstract void commitChanges();

	public void addGap(int w, int h)
		{
		addGap(this,w,h);
		}

	public static void addGap(Container c, int w, int h)
		{
		JLabel l = new JLabel();
		l.setPreferredSize(new Dimension(w,h));
		c.add(l);
		}

	public void changedUpdate(DocumentEvent e)
		{
		// Not used
		}

	public void insertUpdate(DocumentEvent e)
		{
		if (e.getDocument() == name.getDocument())
			{
			res.setName(name.getText());
			setTitle(name.getText());
			node.setUserObject(name.getText());
			LGM.tree.updateUI();
			}
		}

	public void removeUpdate(DocumentEvent e)
		{
		if (e.getDocument() == name.getDocument())
			{
			res.setName(name.getText());
			setTitle(name.getText());
			node.setUserObject(name.getText());
			LGM.tree.updateUI();
			}
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == save)
			{
			updateResource();
			dispose();
			}
		}

	public void setTitle(String title)
		{
		super.setTitle(titlePrefix + title + titleSuffix);
		}

	public void dispose()
		{
		super.dispose();
		node.frame = null; // allows a new frame to open
		name.getDocument().removeDocumentListener(this);
		save.removeActionListener(this);
		}

	protected void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSING)
			{
			if (resourceChanged())
				{
				int ret = JOptionPane.showConfirmDialog(LGM.frame,Messages.format(
						"ResourceFrame.KEEPCHANGES",res.getName()), //$NON-NLS-1$
						Messages.getString("ResourceFrame.KEEPCHANGES_TITLE"),JOptionPane.YES_NO_CANCEL_OPTION); //$NON-NLS-1$
				if (ret == JOptionPane.YES_OPTION)
					{
					updateResource();
					node.setUserObject(res.getName());
					node.updateIcon();
					dispose();
					}
				else if (ret == JOptionPane.NO_OPTION)
					{
					revertResource();
					node.setRes(new WeakReference<R>(resOriginal));
					node.setUserObject(resOriginal.getName());
					node.updateIcon();
					dispose();
					}
				}
			else
				{
				updateResource();
				node.updateIcon();
				dispose();
				}
			}
		super.fireInternalFrameEvent(id);
		}
	}
