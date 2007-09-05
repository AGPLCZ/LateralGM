/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import static org.lateralgm.resources.Ref.deRef;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.impl.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Listener;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.Ref;
import org.lateralgm.resources.Resource;

public class ResourceMenu<R extends Resource<R>> extends JPanel implements MouseListener,
		ActionListener
	{
	private static final long serialVersionUID = 1L;
	private JLabel label;
	private JButton button;
	//Direct reference possible, because the menu updates itself
	private R selected;
	private JPopupMenu pm;
	private JMenuItem noResource;
	private ActionEvent actionEvent;
	private byte kind;
	private final ArrayList<NodeListener> nodeListeners = new ArrayList<NodeListener>();
	private final RMUpdatable updatable = new RMUpdatable();

	public static interface Updatable
		{
		void update();
		}

	public class ResourceMenuItem extends JMenuItem implements Updatable
		{
		private static final long serialVersionUID = 1L;
		public ResNode node;

		public ResourceMenuItem(ResNode node, int kind)
			{
			super(node.getUserObject().toString());
			this.node = node;
			new NodeListener(node,this);
			setIcon(node.getIcon());
			}

		public void update()
			{
			setIcon(node.getIcon());
			setText(node.getUserObject().toString());
			ResourceMenu.this.setSelected(selected); //update text
			}
		}

	private static class NodeListener implements ChangeListener
		{
		private static ReferenceQueue<Updatable> refQueue;
		private static Cleaner cleaner;
		private WeakReference<Updatable> updatable;
		private ResNode node;

		public NodeListener(ResNode n, Updatable u)
			{
			if (refQueue == null) refQueue = new ReferenceQueue<Updatable>();
			if (cleaner == null) cleaner = new Cleaner(refQueue);
			node = n;
			updatable = new WeakReference<Updatable>(u,refQueue);
			node.addChangeListener(this);
			cleaner.add(this);
			}

		public void stateChanged(ChangeEvent e)
			{
			Updatable u = updatable.get();
			if (u == null)
				{
				dispose();
				return;
				}
			u.update();
			}

		public void dispose()
			{
			synchronized (node)
				{
				node.removeChangeListener(this);
				node = null;
				}
			}

		private static class Cleaner
			{
			private ReferenceQueue<Updatable> rq;
			private Hashtable<WeakReference<Updatable>,NodeListener> listeners;
			private Timer timer;
			private CleanerTask task;

			public Cleaner(ReferenceQueue<Updatable> q)
				{
				rq = q;
				}

			public void add(NodeListener l)
				{
				if (listeners == null)
					{
					listeners = new Hashtable<WeakReference<Updatable>,NodeListener>();
					task = new CleanerTask();
					if (timer == null)
						timer = new Timer();
					else
						timer.purge();
					timer.schedule(task,60000,60000);
					}
				listeners.put(l.updatable,l);
				}

			private class CleanerTask extends TimerTask
				{
				public void run()
					{
					Reference<? extends Updatable> r;
					while ((r = rq.poll()) != null)
						{
						NodeListener l = listeners.remove(r);
						if (l != null) l.dispose();
						}
					if (listeners.size() == 0)
						{
						cancel();
						listeners = null;
						}
					}
				}
			}
		}

	/**
	 * Creates a Resource Menu of given Resource kind.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected
	 * @param showDef - Whether to display the default value as a selectable menu option
	 * @param width - The component width desired
	 */
	public ResourceMenu(byte kind, String def, boolean showDef, int width)
		{
		this.kind = kind;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		label = new JLabel(def);
		label.setBorder(BorderFactory.createEtchedBorder());
		label.addMouseListener(this);
		label.setPreferredSize(new Dimension(width - 20,20));
		add(label,gbc);
		gbc = new GridBagConstraints();
		button = new JButton(Resource.ICON[kind]);
		button.addMouseListener(this);
		button.setPreferredSize(new Dimension(20,19));
		button.setMaximumSize(button.getPreferredSize());
		add(button,gbc);

		pm = new JPopupMenu();
		if (showDef)
			{
			noResource = pm.add(new JMenuItem(def));
			noResource.addActionListener(this);
			}
		populate(kind);
		}

	/**
	 * Convenience method for creating a Resource Menu that does display the default value
	 * as a selectable menu option.
	 * @param kind - One of the kind constants defined in Resource (eg Resource.SPRITE)
	 * @param def - The default value to display if no resource is selected (selectable in menu)
	 * @param width - The component width desired
	 */
	public ResourceMenu(byte kind, String def, int width)
		{
		this(kind,def,true,width);
		}

	private void populate(byte kind)
		{
		if (Prefs.groupKind)
			{
			for (int m = 0; m < LGM.root.getChildCount(); m++)
				{
				ResNode group = (ResNode) LGM.root.getChildAt(m);
				if (group.kind == kind)
					{
					populate(pm,group,kind);
					return;
					} //found group
				} //root loop
			} //group kind
		populate(pm,LGM.root,kind);
		return;
		}

	private void populate(JComponent parent, ResNode group, int kind)
		{
		for (int i = 0; i < group.getChildCount(); i++)
			{
			ResNode child = (ResNode) group.getChildAt(i);
			if (child.status != ResNode.STATUS_SECONDARY)
				{
				ImageIcon groupIco = LGM.getIconForKey("GmTreeGraphics.GROUP"); //$NON-NLS-1$
				JMenuItem newParent;
				if (child.getChildCount() == 0)
					newParent = new JMenuItem((String) child.getUserObject());
				else
					newParent = new JMenu((String) child.getUserObject());
				newParent.setIcon(groupIco);
				nodeListeners.add(new NodeListener(LGM.root,updatable));
				parent.add(newParent);
				populate(newParent,child,kind);
				continue;
				}
			if (child.kind != kind) continue;
			ResourceMenuItem newParent = new ResourceMenuItem(child,kind);
			newParent.addActionListener(this);
			parent.add(newParent);
			}
		}

	public void addActionListener(ActionListener il)
		{
		listenerList.add(ActionListener.class,il);
		}

	public void removeActionListener(ActionListener il)
		{
		listenerList.remove(ActionListener.class,il);
		}

	protected void fireActionPerformed()
		{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			{
			if (listeners[i] == ActionListener.class)
				{
				if (actionEvent == null)
					actionEvent = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,""); //$NON-NLS-1$
				((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
				}
			}
		}

	public void showPopup(Component c, int x, int y)
		{

		if (pm.getComponentCount() == 0) return;
		pm.show(c,x,y);
		}

	public R getSelected()
		{
		return selected;
		}

	public Ref<R> getSelectedRef()
		{
		return selected == null ? null : selected.getRef();
		}

	public void setSelected(R res)
		{
		selected = res;
		label.setText((res == null) ? (noResource != null ? noResource.getText() : "") : res.getName()); //$NON-NLS-1$
		}

	public void setRefSelected(Ref<R> ref)
		{
		setSelected(deRef(ref));
		}

	public void setEnabled(boolean enabled)
		{
		label.setEnabled(enabled);
		button.setEnabled(enabled);
		super.setEnabled(enabled);
		}

	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e)
		{
		JMenuItem source = (JMenuItem) e.getSource();
		if (source instanceof ResourceMenu.ResourceMenuItem)
			setSelected((R) ((ResourceMenuItem) source).node.res);
		else
			setSelected(null);
		fireActionPerformed();
		}

	public void mouseClicked(MouseEvent e)
		{
		if (!isEnabled()) return;
		if (pm.getComponentCount() == 0) return;
		showPopup(e.getComponent(),e.getX(),e.getY());
		}

	//Unused
	public void mouseEntered(MouseEvent arg0)
		{
		}

	public void mouseExited(MouseEvent arg0)
		{
		}

	public void mousePressed(MouseEvent arg0)
		{
		}

	public void mouseReleased(MouseEvent arg0)
		{
		}

	private class RMUpdatable implements Updatable
		{
		public void update()
			{
			pm.removeAll();
			for (NodeListener nl : nodeListeners)
				nl.dispose();
			nodeListeners.clear();
			if (noResource != null) pm.add(noResource);
			populate(kind);
			if (selected == null || !Listener.getPrimaryParent(kind).contains(selected))
				setSelected(null);
			setSelected(selected);
			}
		}
	}
