/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free
 * software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.visual;

import static org.lateralgm.main.Util.deRef;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.ref.WeakReference;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.main.Util;
import org.lateralgm.resources.Background;
import org.lateralgm.resources.GmObject;
import org.lateralgm.resources.Room;
import org.lateralgm.resources.Sprite;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.subframes.RoomFrame;

public class RoomEditor extends JPanel implements ImageObserver
	{
	private static final long serialVersionUID = 1L;
	public static final int EDIT_NONE = 0;
	public static final int EDIT_INSTANCES = 1;
	public static final int EDIT_TILES = 2;

	private Room room;
	private RoomFrame frame;
	private int editMode = EDIT_NONE;
	private WeakReference<GmObject> editInstancesObject;
	private boolean editInstancesDeleteUnderlying;
	private Instance cursorInstance;
	private Hashtable<Instance,InstanceComponent> instances;

	public RoomEditor(Room r, RoomFrame frame)
		{
		setOpaque(false);
		room = r;
		this.frame = frame;
		refresh();
		enableEvents(MouseEvent.MOUSE_EVENT_MASK | MouseEvent.MOUSE_MOTION_EVENT_MASK);
		instances = new Hashtable<Instance,InstanceComponent>(room.instances.size());
		for (Instance i : room.instances)
			{
			InstanceComponent ic = new RoomEditor.InstanceComponent(i);
			add(ic);
			instances.put(i,ic);
			}
		}

	public void refresh()
		{
		Dimension s = new Dimension(frame.sWidth.getIntValue(),frame.sHeight.getIntValue());
		setPreferredSize(s);
		revalidate();
		repaint();
		}

	public int getEditMode()
		{
		return editMode;
		}

	public void setEditMode(int editMode)
		{
		this.editMode = editMode;
		}

	public void setEditInstancesParams(WeakReference<GmObject> object, boolean deleteUnderlying)
		{
		editInstancesObject = object;
		editInstancesDeleteUnderlying = deleteUnderlying;
		}

	//	public void updateInstance(Instance i)
	//		{
	//		InstanceComponent ic = instances.get(i);
	//		if(ic != null)
	//			ic.
	//		}

	//TODO: Handle mouse for adding/deleting instances/tiles
	protected void processMouseEvent(MouseEvent e)
		{
		super.processMouseEvent(e);
		mouseEdit(e);
		}

	protected void processMouseMotionEvent(MouseEvent e)
		{
		super.processMouseMotionEvent(e);
		mouseEdit(e);
		}

	protected void mouseEdit(MouseEvent e)
		{
		int x = e.getX();
		int y = e.getY();
		Point p = e.getPoint();
		InstanceComponent mc = null;
		for (Component c : getComponents())
			{
			if (c instanceof InstanceComponent)
				{
				InstanceComponent ic = (InstanceComponent) c;
				if (new Rectangle(ic.x,ic.y,ic.width,ic.height).contains(p)) mc = ic;
				}
			}
		int button = e.getButton();
		int type = e.getID();
		int modifiers = e.getModifiersEx();
		if (editMode == EDIT_INSTANCES)
			{

			if ((modifiers & MouseEvent.BUTTON1_DOWN_MASK) != 0)
				{
				if (type == MouseEvent.MOUSE_PRESSED)
					{
					if ((modifiers & MouseEvent.CTRL_DOWN_MASK) != 0)
						{
						if (mc != null) cursorInstance = mc.instance;
						}
					else if (editInstancesObject != null && cursorInstance == null)
						{
						cursorInstance = room.addInstance();
						cursorInstance.gmObjectId = editInstancesObject;
						add(new InstanceComponent(cursorInstance));
						}
					}
				if (cursorInstance != null)
					{
					if ((modifiers & MouseEvent.ALT_DOWN_MASK) != 0)
						{
						cursorInstance.setX(x);
						cursorInstance.setY(y);
						}
					else
						{
						cursorInstance.setX((x + room.snapX / 2) / room.snapX * room.snapX);
						cursorInstance.setY((y + room.snapY / 2) / room.snapY * room.snapY);
						}
					}
				repaint();
				}
			else
				{
				cursorInstance = null;
				}
			if ((modifiers & MouseEvent.BUTTON2_DOWN_MASK) != 0)
				{
				if (mc != null)
					{
					if ((modifiers & (MouseEvent.ALT_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK)) == 0)
						{
						remove(mc);
						room.instances.remove(mc.instance);
						}
					}
				repaint();
				}
			}
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1)
			{
			return;
			//check delete underlying
			//add object/tile
			}
		}

	@Override
	public void paintComponent(Graphics g)
		{
		Graphics g2 = g.create();
		int width = frame.sWidth.getIntValue();
		int height = frame.sHeight.getIntValue();
		g2.clipRect(0,0,width,height);
		g2.setColor(frame.bDrawColor.isSelected() ? frame.bColor.getSelectedColor() : Color.BLACK);
		g2.fillRect(0,0,width,height);
		if (frame.sSBack.isSelected())
			{
			for (int i = 0; i < 8; i++)
				{
				BackgroundDef bd = frame.res.backgroundDefs[i];
				if (!bd.visible || bd.foreground || deRef(bd.backgroundId) == null) continue;
				BufferedImage bi = bd.backgroundId.get().backgroundImage;
				if (bd.tileHoriz || bd.tileVert)
					{
					int x = bd.x;
					int y = bd.y;
					int ncol = 1;
					int nrow = 1;
					int w = bd.stretch ? width : bi.getWidth();
					int h = bd.stretch ? height : bi.getHeight();
					if (bd.tileHoriz)
						{
						x = 1 + ((bd.x + w - 1) % w) - w;
						ncol = 1 + (width - x - 1) / w;
						}
					if (bd.tileVert)
						{
						y = 1 + ((bd.y + h - 1) % h) - h;
						nrow = 1 + (height - y - 1) / h;
						}
					for (int row = 0; row < nrow; row++)
						for (int col = 0; col < ncol; col++)
							if (bd.stretch)
								g2.drawImage(bi,x + w * col,y + h * row,w,h,this);
							else
								g2.drawImage(bi,x + w * col,y + h * row,this);
					}
				else if (bd.stretch)
					g2.drawImage(bi,bd.x,bd.y,width,height,this);
				else
					g2.drawImage(bi,bd.x,bd.y,this);
				}
			}
		//TODO: Extract to internal TileComponent class
		if (frame.sSTile.isSelected())
			{
			WeakReference<Background> bg = null;
			BufferedImage bi = null;
			for (Tile t : frame.res.tiles)
				{
				if (bg != t.backgroundId)
					{
					bg = t.backgroundId;
					bi = bg.get().backgroundImage;
					if (t.backgroundId.get().transparent) bi = Util.getTransparentIcon(bi);
					}
				g2.drawImage(bi.getSubimage(t.tileX,t.tileY,t.width,t.height),t.x,t.y,this);
				}
			}
		if (frame.sGridVis.isSelected())
			{
			int w = frame.sSnapX.getIntValue();
			int h = frame.sSnapY.getIntValue();
			if (w > 3)
				{
				g2.setXORMode(Color.BLACK);
				g2.setColor(Color.WHITE);
				for (int x = 0; x < width; x += w)
					g2.drawLine(x,0,x,height - 1);
				}
			if (h > 3)
				{
				g2.setXORMode(Color.BLACK);
				g2.setColor(Color.WHITE);
				for (int y = 0; y < height; y += h)
					g2.drawLine(0,y,width - 1,y);
				}
			}
		g2.dispose();
		}

	//TODO: Make invisible when Show Objects is unselected
	public static class InstanceComponent extends JComponent
		{
		private static final long serialVersionUID = 1L;
		private static final BufferedImage EMPTY_IMAGE = new BufferedImage(16,16,
				BufferedImage.TYPE_INT_ARGB);
		private final Instance instance;
		private final GmObject object;
		private Sprite sprite;
		private BufferedImage image;
		private final ResourceChangeListener rcl;
		private int x, y, width, height;
		private boolean doListen;

		public InstanceComponent(Instance i)
			{
			instance = i;
			object = deRef(i.gmObjectId);
			rcl = new ResourceChangeListener();
			if (object == null) image = EMPTY_IMAGE;
			}

		private void setListen(boolean l)
			{
			if (l == doListen) return;
			if (l)
				{
				if (sprite != null) sprite.addChangeListener(rcl);
				if (object != null) object.addChangeListener(rcl);
				instance.addChangeListener(rcl);
				}
			else
				{
				if (sprite != null) sprite.removeChangeListener(rcl);
				if (object != null) object.removeChangeListener(rcl);
				instance.removeChangeListener(rcl);
				}
			doListen = l;
			}

		private void updateSprite()
			{
			Sprite s = deRef(object.sprite);
			if (s != sprite)
				{
				if (sprite != null) sprite.removeChangeListener(rcl);
				if (doListen && s != null) s.addChangeListener(rcl);
				image = null;
				sprite = s;
				}
			}

		private void updateBounds()
			{
			x = instance.getX() - (sprite == null ? 0 : sprite.originX);
			y = instance.getY() - (sprite == null ? 0 : sprite.originY);
			if (sprite == null)
				{
				width = EMPTY_IMAGE.getWidth();
				height = EMPTY_IMAGE.getHeight();
				}
			else
				{
				width = sprite.width;
				height = sprite.height;
				}
			invalidate();
			}

		private void updateImage()
			{
			image = sprite == null ? null : sprite.getDisplayImage();
			if (image == null)
				{
				image = EMPTY_IMAGE;
				setOpaque(false);
				}
			else
				{
				setOpaque(!sprite.transparent);
				}
			}

		public void paintComponent(Graphics g)
			{
			if (object == null)
				{
				getParent().remove(this);
				return;
				}
			if (image == null) updateImage();
			g.drawImage(image,0,0,null);
			}

		@Override
		public int getHeight()
			{
			return height;
			}

		@Override
		public int getWidth()
			{
			return width;
			}

		@Override
		public int getX()
			{
			return x;
			}

		@Override
		public int getY()
			{
			return y;
			}

		@Override
		public void addNotify()
			{
			super.addNotify();
			updateSprite();
			updateBounds();
			setListen(true);
			}

		@Override
		public void removeNotify()
			{
			super.removeNotify();
			setListen(false);
			}

		private class ResourceChangeListener implements ChangeListener
			{
			public void stateChanged(ChangeEvent e)
				{
				updateSprite();
				updateBounds();
				repaint();
				}
			}
		}
	}
