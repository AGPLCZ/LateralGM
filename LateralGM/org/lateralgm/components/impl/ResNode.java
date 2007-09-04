/*
 * Copyright (C) 2006, 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.Font;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Path;
import org.lateralgm.resources.Ref;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.Timeline;
import org.lateralgm.subframes.BackgroundFrame;
import org.lateralgm.subframes.FontFrame;
import org.lateralgm.subframes.GmObjectFrame;
import org.lateralgm.subframes.PathFrame;
import org.lateralgm.subframes.ResourceFrame;
import org.lateralgm.subframes.RoomFrame;
import org.lateralgm.subframes.ScriptFrame;
import org.lateralgm.subframes.SoundFrame;
import org.lateralgm.subframes.SpriteFrame;
import org.lateralgm.subframes.TimelineFrame;

public class ResNode extends DefaultMutableTreeNode implements Transferable
	{
	private static final long serialVersionUID = 1L;
	public static final DataFlavor NODE_FLAVOR = new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType,"Node");
	private DataFlavor[] flavors = { NODE_FLAVOR };
	public static final byte STATUS_PRIMARY = 1;
	public static final byte STATUS_GROUP = 2;
	public static final byte STATUS_SECONDARY = 3;
	/** One of PRIMARY, GROUP, or SECONDARY*/
	public byte status;
	/** What kind of Resource this is */
	public byte kind;
	/**
	 * The <code>Resource</code> this node represents.
	 * Note that we can directly reference here,
	 * because the deletion of the Resource corresponds to
	 * the deletion of this node.
	 */
	public Resource<?> res;
	public ResourceFrame<?> frame = null;
	private EventListenerList listenerList;
	private ChangeEvent changeEvent;
	private Icon icon;

	public Icon getIcon()
		{
		if (icon == null) updateIcon();
		return icon;
		}

	public void updateIcon()
		{
		switch (kind)
			{
			case Resource.SPRITE:
				icon = GmTreeGraphics.getSpriteIcon(((Sprite) res).getRef());
				break;
			case Resource.BACKGROUND:
				icon = GmTreeGraphics.getBackgroundIcon((Background) res);
				break;
			case Resource.GMOBJECT:
			Ref<Sprite> r = ((GmObject) res).sprite;	
			icon = GmTreeGraphics.getSpriteIcon(r);
				break;
			}
		fireStateChanged(null);
		LGM.tree.repaint();
		}

	public ResNode(String name, byte status, byte kind, Resource<?> res)
		{
		super(name);
		this.status = status;
		this.kind = kind;
		this.res = res;
		}

	public ResNode(String name, int status, int kind, Resource<?> res)
		{
		this(name,(byte) status,(byte) kind,res);
		}

	public ResNode(String name, int status, int kind)
		{
		this(name,status,kind,null);
		}

	public ResNode addChild(String name, byte stat, byte type)
		{
		ResNode b = new ResNode(name,stat,type);
		add(b);
		return b;
		}

	public ResNode addChild(String name, int stat, int type)
		{
		return addChild(name,(byte) stat,(byte) type);
		}

	public boolean getAllowsChildren()
		{
		if (status == STATUS_SECONDARY) return false;
		if (Prefs.protectRoot && this == LGM.root) return false;
		return true;
		}

	public DataFlavor[] getTransferDataFlavors()
		{
		return flavors;
		}

	public boolean isDataFlavorSupported(DataFlavor flavor)
		{
		return flavor == NODE_FLAVOR;
		}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
		{
		if (flavor != NODE_FLAVOR) throw new UnsupportedFlavorException(flavor);
		return this;
		}

	public void openFrame()
		{
		if (frame == null)
			{
			ResourceFrame<?> rf = null;
			switch (kind)
				{
				case Resource.SPRITE:
					rf = new SpriteFrame((Sprite) res,this);
					break;
				case Resource.SOUND:
					rf = new SoundFrame((Sound) res,this);
					break;
				case Resource.BACKGROUND:
					rf = new BackgroundFrame((Background) res,this);
					break;
				case Resource.PATH:
					rf = new PathFrame((Path) res,this);
					break;
				case Resource.SCRIPT:
					rf = new ScriptFrame((Script) res,this);
					break;
				case Resource.FONT:
					rf = new FontFrame((Font) res,this);
					break;
				case Resource.TIMELINE:
					rf = new TimelineFrame((Timeline) res,this);
					break;
				case Resource.GMOBJECT:
					rf = new GmObjectFrame((GmObject) res,this);
					break;
				case Resource.ROOM:
					rf = new RoomFrame((Room) res,this);
					break;
				default:
					rf = null;
					break;
				}
			if (rf != null)
				{
				frame = rf;
				LGM.mdi.add(rf);
				}
			}
		if (frame != null)
			{
			frame.setVisible(true);
			frame.toTop();
			}
		}

	public void updateFrame()
		{
		if (status == STATUS_SECONDARY)
			{
			String txt = (String) getUserObject();
			res.setName(txt);
			if (frame != null)
				{
				frame.setTitle(txt);
				frame.name.setText(txt);
				}
			}
		}

	public void insert(MutableTreeNode newChild, int childIndex)
		{
		super.insert(newChild,childIndex);
		fireStateChanged();
		}

	public void remove(int childIndex)
		{
		super.remove(childIndex);
		fireStateChanged();
		}

	public void setUserObject(Object obj)
		{
		super.setUserObject(obj);
		fireStateChanged(null);
		}

	private EventListenerList getListenerList()
		{
		if (listenerList == null) listenerList = new EventListenerList();
		return listenerList;
		}

	/**
	 * Adds the specified ChangeListener.
	 * Note that if a null ChangeEvent is provided to <code>l</code>,
	 * this denotes a change in the userObject or icon.
	 * Otherwise, there has been a change in the structure of the tree. 
	 * @param l The ChangeListener to add
	 */
	public void addChangeListener(ChangeListener l)
		{
		getListenerList().add(ChangeListener.class,l);
		}

	public void removeChangeListener(ChangeListener l)
		{
		getListenerList().remove(ChangeListener.class,l);
		}

	protected void fireStateChanged()
		{
		// Lazily create the event:
		if (changeEvent == null) changeEvent = new ChangeEvent(this);
		fireStateChanged(changeEvent);
		}

	protected void fireStateChanged(ChangeEvent e)
		{
		if (listenerList != null)
			{
			Object[] list = listenerList.getListenerList();
			for (int i = list.length - 2; i >= 0; i -= 2)
				if (list[i] == ChangeListener.class) ((ChangeListener) list[i + 1]).stateChanged(e);
			}
		// Propogate structure changes up the tree
		if (e != null && parent != null && parent instanceof ResNode)
			((ResNode) parent).fireStateChanged(changeEvent);
		}

	/**
	 * Recursively checks (from this node down) for a node with a res field
	 * referring to the same instance as res.
	 * @param res The resource to look for
	 * @return Whether the resource was found
	 */
	public boolean contains(Resource<?> res)
		{
		if (this.res == res) return true; //Just in case
		if (children != null) for (Object obj : children)
			if (obj instanceof ResNode)
				{
				ResNode node = (ResNode) obj;
				if (node.isLeaf())
					{
					if (node.res == res) return true;
					}
				else
					{
					if (node.contains(res)) return true;
					}
				}
		return false;
		}
	}
