/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.lateralgm.components.IndexButtonGroup;
import org.lateralgm.components.IntegerField;
import org.lateralgm.components.ResNode;
import org.lateralgm.components.SubimagePreview;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sprite;

public class SpriteFrame extends ResourceFrame<Sprite> implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	private static ImageIcon frameIcon = Sprite.ICON[Sprite.SPRITE];
	private static ImageIcon saveIcon = LGM.getIconForKey("SpriteFrame.SAVE"); //$NON-NLS-1$
	private static ImageIcon loadIcon = LGM.getIconForKey("SpriteFrame.LOAD"); //$NON-NLS-1$

	public JButton load;
	public JLabel width;
	public JLabel height;
	public JLabel subCount;
	public JButton subLeft;
	public JButton subRight;
	public JLabel showLab;
	public JLabel show;
	public int currSub;
	public JButton edit;
	public JCheckBox transparent;

	public JCheckBox preciseCC;
	public JCheckBox smooth;
	public JCheckBox preload;
	public IntegerField originX;
	public IntegerField originY;
	public JButton centre;
	public IndexButtonGroup bboxGroup;
	public JRadioButton auto;
	public JRadioButton full;
	public JRadioButton manual;
	public IntegerField bboxLeft;
	public IntegerField bboxRight;
	public IntegerField bboxTop;
	public IntegerField bboxBottom;
	public boolean imageChanged = false;

	public SubimagePreview preview;

	public SpriteFrame(Sprite res, ResNode node)
		{
		super(res,node);

		setSize(560,320);
		setMinimumSize(new Dimension(560,320));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setFrameIcon(frameIcon);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setMinimumSize(new Dimension(180,280));
		side1.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));
		side1.setPreferredSize(new Dimension(180,280));

		// side1.setBackground(Color.RED);
		JLabel lab = new JLabel(Messages.getString("SpriteFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(40,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(120,20));
		side1.add(name);
		load = new JButton(Messages.getString("SpriteFrame.LOAD")); //$NON-NLS-1$
		load.setIcon(loadIcon);
		load.setPreferredSize(new Dimension(130,24));
		load.addActionListener(this);
		side1.add(load);

		width = new JLabel();
		width.setPreferredSize(new Dimension(80,16));
		side1.add(width);
		height = new JLabel();
		height.setPreferredSize(new Dimension(80,16));
		side1.add(height);
		subCount = new JLabel();
		subCount.setPreferredSize(new Dimension(160,16));
		side1.add(subCount);

		addGap(side1,160,10);
		showLab = new JLabel(Messages.getString("SpriteFrame.SHOW")); //$NON-NLS-1$
		showLab.setPreferredSize(new Dimension(200,16));
		showLab.setHorizontalAlignment(JLabel.CENTER);
		side1.add(showLab);
		// TODO Possibly get an icon for the arrows
		subLeft = new JButton("<"); //$NON-NLS-1$
		subLeft.setPreferredSize(new Dimension(45,20));
		subLeft.addActionListener(this);
		side1.add(subLeft);
		currSub = 0;
		show = new JLabel();
		show.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		show.setPreferredSize(new Dimension(30,20));
		show.setHorizontalAlignment(JLabel.CENTER);
		side1.add(show);
		subRight = new JButton(">"); //$NON-NLS-1$
		subRight.setPreferredSize(new Dimension(45,20));
		subRight.addActionListener(this);
		side1.add(subRight);

		addGap(side1,160,10);

		edit = new JButton(Messages.getString("SpriteFrame.EDIT_SPRITE")); //$NON-NLS-1$
		edit.setPreferredSize(new Dimension(130,24));
		side1.add(edit);
		transparent = new JCheckBox(Messages.getString("SpriteFrame.TRANSPARENT")); //$NON-NLS-1$
		transparent.setPreferredSize(new Dimension(160,16));
		transparent.setSelected(res.transparent);
		transparent.addActionListener(this);
		side1.add(transparent);

		addGap(side1,100,20);

		save.setPreferredSize(new Dimension(130,24));
		save.setText(Messages.getString("SpriteFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(saveIcon);
		side1.add(save);

		JPanel side2 = new JPanel(new FlowLayout());
		side2.setMinimumSize(new Dimension(200,280));
		side2.setPreferredSize(new Dimension(200,280));
		side2.setMaximumSize(new Dimension(200,Integer.MAX_VALUE));
		// side2.setPreferredSize(new Dimension(200,280));
		// side2.setBackground(Color.GREEN);

		preciseCC = new JCheckBox(Messages.getString("SpriteFrame.PRECISE_CC")); //$NON-NLS-1$
		preciseCC.setPreferredSize(new Dimension(180,16));
		preciseCC.setSelected(res.preciseCC);
		side2.add(preciseCC);
		smooth = new JCheckBox(Messages.getString("SpriteFrame.SMOOTH")); //$NON-NLS-1$
		smooth.setPreferredSize(new Dimension(180,16));
		smooth.setSelected(res.smoothEdges);
		side2.add(smooth);
		preload = new JCheckBox(Messages.getString("SpriteFrame.PRELOAD")); //$NON-NLS-1$
		preload.setPreferredSize(new Dimension(180,16));
		preload.setSelected(res.preload);
		side2.add(preload);

		JPanel origin = new JPanel(new FlowLayout());
		String t = Messages.getString("SpriteFrame.ORIGIN"); //$NON-NLS-1$
		origin.setBorder(BorderFactory.createTitledBorder(t));
		origin.setPreferredSize(new Dimension(200,80));
		lab = new JLabel(Messages.getString("SpriteFrame.X")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		origin.add(lab);
		originX = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.originX);
		originX.setPreferredSize(new Dimension(40,20));
		originX.addActionListener(this);
		origin.add(originX);
		lab = new JLabel(Messages.getString("SpriteFrame.Y")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(20,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		origin.add(lab);
		originY = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.originY);
		originY.setPreferredSize(new Dimension(40,20));
		originY.addActionListener(this);
		origin.add(originY);
		centre = new JButton(Messages.getString("SpriteFrame.CENTRE")); //$NON-NLS-1$
		centre.setPreferredSize(new Dimension(80,20));
		centre.addActionListener(this);
		origin.add(centre);
		side2.add(origin);

		JPanel bbox = new JPanel(new FlowLayout());
		t = Messages.getString("SpriteFrame.BBOX"); //$NON-NLS-1$
		bbox.setBorder(BorderFactory.createTitledBorder(t));
		bbox.setPreferredSize(new Dimension(200,120));

		bboxGroup = new IndexButtonGroup(3);
		auto = new JRadioButton(Messages.getString("SpriteFrame.AUTO")); //$NON-NLS-1$
		auto.setPreferredSize(new Dimension(85,16));
		auto.addActionListener(this);
		bboxGroup.add(auto,Sprite.BBOX_AUTO);
		full = new JRadioButton(Messages.getString("SpriteFrame.FULL")); //$NON-NLS-1$
		full.setPreferredSize(new Dimension(85,16));
		full.addActionListener(this);
		bboxGroup.add(full,Sprite.BBOX_FULL);
		manual = new JRadioButton(Messages.getString("SpriteFrame.MANUAL")); //$NON-NLS-1$
		manual.setPreferredSize(new Dimension(85,16));
		manual.addActionListener(this);
		bboxGroup.add(manual,Sprite.BBOX_MANUAL);
		bboxGroup.setValue(res.boundingBoxMode);
		bboxGroup.populate(bbox);
		addGap(bbox,85,16);

		lab = new JLabel(Messages.getString("SpriteFrame.LEFT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(25,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxLeft = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxLeft);
		bboxLeft.setPreferredSize(new Dimension(40,20));
		bboxLeft.addActionListener(this);
		bbox.add(bboxLeft);

		lab = new JLabel(Messages.getString("SpriteFrame.RIGHT")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(45,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxRight = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxRight);
		bboxRight.setPreferredSize(new Dimension(40,20));
		bboxRight.addActionListener(this);
		bbox.add(bboxRight);

		lab = new JLabel(Messages.getString("SpriteFrame.TOP")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(25,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxTop = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxTop);
		bboxTop.setPreferredSize(new Dimension(40,20));
		bboxTop.addActionListener(this);
		bbox.add(bboxTop);

		lab = new JLabel(Messages.getString("SpriteFrame.BOTTOM")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(45,16));
		lab.setHorizontalAlignment(SwingConstants.RIGHT);
		bbox.add(lab);
		bboxBottom = new IntegerField(Integer.MIN_VALUE,Integer.MAX_VALUE,res.boundingBoxBottom);
		bboxBottom.setPreferredSize(new Dimension(40,20));
		bboxBottom.addActionListener(this);
		bbox.add(bboxBottom);

		side2.add(bbox);

		preview = new SubimagePreview(this);
		preview.setVerticalAlignment(SwingConstants.TOP);
		JScrollPane scroll = new JScrollPane(preview);

		updateBoundingBox();
		updateImage();
		updateInfo();

		add(side1);
		add(side2);
		add(scroll);
		}

	@Override
	public boolean resourceChanged()
		{
		return !resOriginal.getName().equals(name.getText()) || imageChanged
				|| resOriginal.transparent != transparent.isSelected()
				|| resOriginal.preciseCC != preciseCC.isSelected()
				|| resOriginal.smoothEdges != smooth.isSelected()
				|| resOriginal.preload != preload.isSelected()
				|| resOriginal.originX != originX.getIntValue()
				|| resOriginal.originY != originY.getIntValue()
				|| resOriginal.boundingBoxMode != (byte) bboxGroup.getValue()
				|| resOriginal.boundingBoxLeft != bboxLeft.getIntValue()
				|| resOriginal.boundingBoxRight != bboxRight.getIntValue()
				|| resOriginal.boundingBoxTop != bboxTop.getIntValue()
				|| resOriginal.boundingBoxBottom != bboxBottom.getIntValue();
		}

	public void revertResource()
		{
		LGM.currentFile.sprites.replace(res.getId(),resOriginal);
		}

	public void updateResource()
		{
		res.setName(name.getText());
		res.transparent = transparent.isSelected();
		res.preciseCC = preciseCC.isSelected();
		res.smoothEdges = smooth.isSelected();
		res.preload = preload.isSelected();
		res.originX = originX.getIntValue();
		res.originY = originY.getIntValue();
		res.boundingBoxMode = (byte) bboxGroup.getValue();
		res.boundingBoxLeft = bboxLeft.getIntValue();
		res.boundingBoxRight = bboxRight.getIntValue();
		res.boundingBoxTop = bboxTop.getIntValue();
		res.boundingBoxBottom = bboxBottom.getIntValue();
		resOriginal = res.copy();
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			try
				{
				BufferedImage img = Util.getValidImage();
				if (img != null)
					{
					res.clearSubImages();
					res.addSubImage(img);
					res.width = img.getWidth();
					res.height = img.getHeight();
					imageChanged = true;
					currSub = 0;
					preview.setIcon(new ImageIcon(res.getSubImage(0)));
					updateInfo();
					updateBoundingBox();
					LGM.tree.repaint();
					}
				}
			catch (Throwable t) // Includes out of memory errors
				{
				//t.printStackTrace();
				String msg = Messages.getString("SpriteFrame.ERROR_LOADING"); //$NON-NLS-1$
				JOptionPane.showMessageDialog(LGM.frame,msg + Util.imageFc.getSelectedFile().getPath());
				}
			return;
			}
		if (e.getSource() == subRight)
			{
			if (currSub < res.noSubImages() - 1)
				{
				currSub += 1;
				updateImage();
				}
			return;
			}
		if (e.getSource() == subLeft)
			{
			if (currSub > 0)
				{
				currSub -= 1;
				updateImage();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			return;
			}
		if (e.getSource() == centre)
			{
			originX.setIntValue(res.width / 2);
			originY.setIntValue(res.height / 2);
			return;
			}
		if (e.getSource() == auto || e.getSource() == full || e.getSource() == manual)
			{
			updateBoundingBox();
			return;
			}
		if (e.getSource() == transparent)
			{
			updateBoundingBox();
			res.transparent = transparent.isSelected();
			LGM.tree.repaint();
			return;
			}
		if (e.getSource() == originX || e.getSource() == originY || e.getSource() == bboxLeft
				|| e.getSource() == bboxRight || e.getSource() == bboxTop || e.getSource() == bboxBottom)
			{
			preview.repaint(((JViewport) preview.getParent()).getViewRect());
			return;
			}
		super.actionPerformed(e);
		}

	public void updateInfo()
		{
		width.setText(Messages.getString("SpriteFrame.WIDTH") + res.width); //$NON-NLS-1$
		height.setText(Messages.getString("SpriteFrame.HEIGHT") + res.height); //$NON-NLS-1$
		subCount.setText(Messages.getString("SpriteFrame.NO_OF_SUBIMAGES") //$NON-NLS-1$
				+ res.noSubImages());
		}

	// TODO cache auto bbox
	public void updateBoundingBox()
		{
		int mode = bboxGroup.getValue();
		switch (mode)
			{
			case Sprite.BBOX_AUTO:
				Rectangle r = transparent.isSelected() ? getOverallBounds() : new Rectangle(0,0,
						res.width - 1,res.height - 1);
				res.boundingBoxLeft = r.x;
				res.boundingBoxRight = r.x + r.width;
				res.boundingBoxTop = r.y;
				res.boundingBoxBottom = r.y + r.height;
				break;
			case Sprite.BBOX_FULL:
				res.boundingBoxLeft = 0;
				res.boundingBoxRight = res.width - 1;
				res.boundingBoxTop = 0;
				res.boundingBoxBottom = res.height - 1;
				break;
			default:
				break;
			}
		bboxLeft.setIntValue(res.boundingBoxLeft);
		bboxRight.setIntValue(res.boundingBoxRight);
		bboxTop.setIntValue(res.boundingBoxTop);
		bboxBottom.setIntValue(res.boundingBoxBottom);
		bboxLeft.setEnabled(mode == Sprite.BBOX_MANUAL);
		bboxRight.setEnabled(mode == Sprite.BBOX_MANUAL);
		bboxTop.setEnabled(mode == Sprite.BBOX_MANUAL);
		bboxBottom.setEnabled(mode == Sprite.BBOX_MANUAL);
		}

	public void updateImage()
		{
		BufferedImage img = getSubimage();
		if (img != null)
			{
			preview.setIcon(new ImageIcon(img));
			subLeft.setEnabled(currSub > 0);
			subRight.setEnabled(currSub < res.noSubImages() - 1);
			show.setText(Integer.toString(currSub));
			}
		else
			{
			preview.setIcon(null);
			subLeft.setEnabled(false);
			subRight.setEnabled(false);
			show.setText("");
			}
		}

	public BufferedImage getSubimage()
		{
		return res.getSubImage(currSub);
		}

	public static Rectangle getCropBounds(BufferedImage img)
		{
		int transparent = img.getRGB(0,img.getHeight() - 1);
		int width = img.getWidth();
		int height = img.getHeight();

		int y1 = -1;
		y1loop: for (int j = 0; j < height; j++)
			for (int i = 0; i < width; i++)
				if (img.getRGB(i,j) != transparent)
					{
					y1 = j;
					break y1loop;
					}
		if (y1 == -1) return new Rectangle(0,0,0,0);

		int x1 = 0;
		x1loop: for (int i = 0; i < width; i++)
			for (int j = y1; j < height; j++)
				if (img.getRGB(i,j) != transparent)
					{
					x1 = i;
					break x1loop;
					}

		int y2 = 0;
		y2loop: for (int j = height - 1; j > 0; j--)
			for (int i = x1; i < width; i++)
				if (img.getRGB(i,j) != transparent)
					{
					y2 = j;
					break y2loop;
					}

		int x2 = 0;
		x2loop: for (int i = width - 1; i > 0; i--)
			for (int j = y1; j < y2; j++)
				if (img.getRGB(i,j) != transparent)
					{
					x2 = i;
					break x2loop;
					}
		return new Rectangle(x1,y1,x2 - x1,y2 - y1);
		}

	public Rectangle getOverallBounds()
		{
		Rectangle rects[] = new Rectangle[res.noSubImages()];
		for (int i = 0; i < rects.length; i++)
			rects[i] = getCropBounds(res.getSubImage(i));
		for (int i = 1; i < rects.length; i++)
			rects[0] = rects[0].union(rects[i]);
		return rects.length > 0 ? rects[0] : new Rectangle(0,0,res.width - 1,res.height - 1);
		}
	}
