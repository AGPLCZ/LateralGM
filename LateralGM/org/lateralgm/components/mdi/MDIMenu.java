/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.mdi;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.WeakHashMap;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GmMenu;
import org.lateralgm.messages.Messages;
import org.lateralgm.subframes.ResourceFrame;

public class MDIMenu extends GmMenu implements ActionListener,ContainerListener
	{
	private static final long serialVersionUID = 1L;
	private MDIPane pane;
	public static ButtonGroup group = new ButtonGroup();
	private final WeakHashMap<MDIFrame,FrameButton> frameButtons;

	public MDIMenu(MDIPane pane)
		{
		super(Messages.getString("MDIMenu.WINDOW"));
		this.pane = pane;
		frameButtons = new WeakHashMap<MDIFrame,FrameButton>();
		pane.addContainerListener(this);
		addItem("MDIMenu.CASCADE",this);
		addItem("MDIMenu.ARRANGE_ICONS",this);
		addItem("MDIMenu.CLOSE_ALL",this);
		addItem("MDIMenu.MINIMIZE_ALL",this);
		addSeparator();
		addItem("MDIMenu.CLOSE",this);
		addItem("MDIMenu.CLOSE_OTHERS",this);
		addSeparator();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getActionCommand().endsWith("CASCADE"))
			{
			pane.cascadeFrames();
			return;
			}
		if (e.getActionCommand().endsWith("ARRANGE_ICONS"))
			{
			pane.arrangeDesktopIcons();
			return;
			}
		if (e.getActionCommand().endsWith("CLOSE_ALL"))
			{
			pane.closeAll();
			return;
			}
		if (e.getActionCommand().endsWith("MINIMIZE_ALL"))
			{
			pane.iconizeAll();
			return;
			}
		if (e.getActionCommand().endsWith("CLOSE") && pane.getSelectedFrame() != null)
			{
			if (pane.getSelectedFrame() instanceof ResourceFrame)
				try
					{
					pane.getSelectedFrame().setClosed(true);
					}
				catch (PropertyVetoException e1)
					{
					e1.printStackTrace();
					}
			else
				pane.getSelectedFrame().setVisible(false);
			return;
			}
		if (e.getActionCommand().endsWith("CLOSE_OTHERS"))
			{
			pane.closeOthers();
			return;
			}
		}

	private void addRadio(FrameButton item)
		{
		group.add(item);
		add(item);
		}

	private void removeRadio(FrameButton item)
		{
		group.remove(item);
		remove(item);
		}

	public void componentAdded(ContainerEvent e)
		{
		Component c = e.getChild();
		if (c instanceof MDIFrame) new FrameButton((MDIFrame) c);
		}

	public void componentRemoved(ContainerEvent e)
		{
		Component c = e.getChild();
		FrameButton b = frameButtons.get(c);
		if (b != null) b.dispose();
		}

	private class FrameButton extends JRadioButtonMenuItem
		{
		private static final long serialVersionUID = 1L;
		private IFListener ifl = new IFListener();
		private PCListener pcl = new PCListener();
		private CListener cl = new CListener();
		private MDIFrame mdif;

		public FrameButton(MDIFrame f)
			{
			mdif = f;
			frameButtons.put(f,this);
			MDIMenu.this.addRadio(this);
			f.addInternalFrameListener(ifl);
			f.addPropertyChangeListener(pcl);
			f.addComponentListener(cl);
			update();
			setVisible(mdif.isVisible());
			addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
						{
						mdif.toTop();
						}
				});
			}

		public void dispose()
			{
			frameButtons.remove(mdif);
			MDIMenu.this.removeRadio(this);
			mdif.removeInternalFrameListener(ifl);
			mdif.removePropertyChangeListener(pcl);
			mdif.removeComponentListener(cl);
			}

		private void update()
			{
			setText(mdif.getTitle());
			setIcon(mdif.getFrameIcon());
			}

		private class IFListener extends InternalFrameAdapter
			{
			public void internalFrameActivated(InternalFrameEvent e)
				{
				setSelected(true);
				}
			}

		private class PCListener implements PropertyChangeListener
			{
			public void propertyChange(PropertyChangeEvent evt)
				{
				update();
				}
			}

		private class CListener extends ComponentAdapter
			{
			public void componentHidden(ComponentEvent e)
				{
				setVisible(false);
				}

			public void componentShown(ComponentEvent e)
				{
				setVisible(true);
				}
			}
		}
	}
