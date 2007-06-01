package org.lateralgm.components;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JLabel;

import org.lateralgm.subframes.SpriteFrame;

public class SubimagePreview extends JLabel
	{
	private static final long serialVersionUID = 1L;

	private SpriteFrame frame;

	private static final int ORIGIN_SIZE = 20;

	public SubimagePreview(SpriteFrame frame)
		{
		this.frame = frame;
		enableEvents(MouseEvent.MOUSE_PRESSED);
		setOpaque(true);
		if (frame.res.noSubImages() > 0)
			{
			BufferedImage img = frame.res.getSubImage(frame.currSub);
			setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
			}
		else
			setPreferredSize(new Dimension(0,0));
		}

	public void paintComponent(Graphics g)
		{
		super.paintComponent(g);
		BufferedImage img = frame.getSubimage();
		if (img != null)
			{
			setPreferredSize(new Dimension(img.getWidth(),img.getHeight()));
			int originX = frame.originX.getIntValue();
			int originY = frame.originY.getIntValue();
			int bboxLeft = frame.bboxLeft.getIntValue();
			int bboxRight = frame.bboxRight.getIntValue();
			int bboxTop = frame.bboxTop.getIntValue();
			int bboxBottom = frame.bboxBottom.getIntValue();

			drawInvertedHorizontalLine(g,img,originX - ORIGIN_SIZE,originY,2 * ORIGIN_SIZE);
			drawInvertedVerticalLine(g,img,originX,originY - ORIGIN_SIZE,2 * ORIGIN_SIZE);
			drawInvertedRectangle(g,img,bboxLeft,bboxTop,bboxRight,bboxBottom);
			}
		else
			setPreferredSize(new Dimension(0,0));
		}

	protected void processMouseEvent(MouseEvent e)
		{
		if (e.getID() == MouseEvent.MOUSE_PRESSED && e.getButton() == MouseEvent.BUTTON1
				&& e.getX() < getPreferredSize().width && e.getY() < getPreferredSize().height)
			{
			frame.originX.setIntValue(e.getX());
			frame.originY.setIntValue(e.getY());
			}
		super.processMouseEvent(e);
		}

	public void setIcon(Icon ico)
		{
		super.setIcon(ico);
		if (ico != null)
			setPreferredSize(new Dimension(ico.getIconWidth(),ico.getIconHeight()));
		else
			setPreferredSize(new Dimension(0,0));
		}

	private void drawInvertedHorizontalLine(Graphics g, BufferedImage src, int x, int y, int length)
		{
		Rectangle r = g.getClipBounds().intersection(new Rectangle(x,y,length,1));
		if (!r.isEmpty() && r.x < src.getWidth() && r.y < src.getHeight())
			{
			r.width = Math.min(r.width,src.getWidth() - r.x);
			r.height = Math.min(r.height,src.getHeight() - r.y);
			BufferedImage dest = new BufferedImage(r.width,1,BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < r.width; i++)
				dest.setRGB(i,0,(~src.getRGB(r.x + i,r.y)) | 0xFF000000);
			g.drawImage(dest,r.x,r.y,null);
			}
		}

	private void drawInvertedVerticalLine(Graphics g, BufferedImage src, int x, int y, int length)
		{
		Rectangle r = g.getClipBounds().intersection(new Rectangle(x,y,1,length));
		if (!r.isEmpty() && r.x < src.getWidth() && r.y < src.getHeight())
			{
			r.width = Math.min(r.width,src.getWidth() - r.x);
			r.height = Math.min(r.height,src.getHeight() - r.y);
			BufferedImage dest = new BufferedImage(1,r.height,BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < r.height; i++)
				dest.setRGB(0,i,(~src.getRGB(r.x,r.y + i)) | 0xFF000000);
			g.drawImage(dest,r.x,r.y,null);
			}
		}

	private void drawInvertedRectangle(Graphics g, BufferedImage src, int x1, int y1, int x2, int y2)
		{
		int left = Math.min(x1,x2);
		int top = Math.min(y1,y2);
		int width = Math.abs(x1 - x2);
		int height = Math.abs(y1 - y2);
		drawInvertedHorizontalLine(g,src,left,top,width);
		drawInvertedHorizontalLine(g,src,left,Math.max(y1,y2),width);
		drawInvertedVerticalLine(g,src,left,top,height);
		drawInvertedVerticalLine(g,src,Math.max(x1,x2),top,height);
		}
	}
