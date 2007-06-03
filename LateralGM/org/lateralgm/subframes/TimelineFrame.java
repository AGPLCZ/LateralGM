/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.ResNode;
import org.lateralgm.main.LGM;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Timeline;
import org.lateralgm.resources.library.LibAction;
import org.lateralgm.resources.library.LibManager;
import org.lateralgm.resources.library.Library;
import org.lateralgm.resources.sub.Action;
import org.lateralgm.resources.sub.Moment;

public class TimelineFrame extends ResourceFrame<Timeline> implements ActionListener,
		ListSelectionListener
	{
	private static final long serialVersionUID = 1L;
	private static ImageIcon frameIcon = LGM.getIconForKey("TimelineFrame.TIMELINE"); //$NON-NLS-1$$
	private static ImageIcon saveIcon = LGM.getIconForKey("TimelineFrame.SAVE"); //$NON-NLS-1$

	public JButton add;
	public JButton change;
	public JButton delete;
	public JButton duplicate;
	public JButton shift;
	public JButton merge;
	public JButton clear;

	public JList moments;
	public JList actions;
	public GMLTextArea code;

	public TimelineFrame(Timeline res, ResNode node)
		{
		super(res,node);

		setSize(560,385);
		setMinimumSize(new Dimension(560,385));
		setLayout(new BoxLayout(getContentPane(),BoxLayout.X_AXIS));
		setFrameIcon(frameIcon);

		JPanel side1 = new JPanel(new FlowLayout());
		side1.setPreferredSize(new Dimension(180,280));
		//		side1.setMaximumSize(new Dimension(180,Integer.MAX_VALUE));

		JLabel lab = new JLabel(Messages.getString("TimelineFrame.NAME")); //$NON-NLS-1$
		lab.setPreferredSize(new Dimension(160,14));
		side1.add(lab);
		name.setPreferredSize(new Dimension(160,20));
		side1.add(name);

		addGap(side1,180,20);

		add = new JButton(Messages.getString("TimelineFrame.ADD")); //$NON-NLS-1$
		add.setPreferredSize(new Dimension(80,20));
		add.addActionListener(this);
		side1.add(add);
		change = new JButton(Messages.getString("TimelineFrame.CHANGE")); //$NON-NLS-1$
		change.setPreferredSize(new Dimension(80,20));
		change.addActionListener(this);
		side1.add(change);
		delete = new JButton(Messages.getString("TimelineFrame.DELETE")); //$NON-NLS-1$
		delete.setPreferredSize(new Dimension(80,20));
		delete.addActionListener(this);
		side1.add(delete);
		duplicate = new JButton(Messages.getString("TimelineFrame.DUPLICATE")); //$NON-NLS-1$
		duplicate.setPreferredSize(new Dimension(90,20));
		duplicate.addActionListener(this);
		side1.add(duplicate);

		addGap(side1,180,20);

		shift = new JButton(Messages.getString("TimelineFrame.SHIFT")); //$NON-NLS-1$
		shift.setPreferredSize(new Dimension(80,20));
		shift.addActionListener(this);
		side1.add(shift);
		merge = new JButton(Messages.getString("TimelineFrame.MERGE")); //$NON-NLS-1$
		merge.setPreferredSize(new Dimension(80,20));
		merge.addActionListener(this);
		side1.add(merge);
		clear = new JButton(Messages.getString("TimelineFrame.CLEAR")); //$NON-NLS-1$
		clear.setPreferredSize(new Dimension(80,20));
		clear.addActionListener(this);
		side1.add(clear);

		addGap(side1,180,50);

		save.setPreferredSize(new Dimension(130,24));
		save.setText(Messages.getString("TimelineFrame.SAVE")); //$NON-NLS-1$
		save.setIcon(saveIcon);
		side1.add(save);

		JPanel side2 = new JPanel(new BorderLayout());
		side2.setMaximumSize(new Dimension(90,Integer.MAX_VALUE));
		lab = new JLabel(Messages.getString("TimelineFrame.MOMENTS")); //$NON-NLS-1$
		side2.add(lab,"North");
		moments = new JList(res.moments.toArray());
		moments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		moments.addListSelectionListener(this);
		JScrollPane scroll = new JScrollPane(moments);
		scroll.setPreferredSize(new Dimension(90,260));
		side2.add(scroll,"Center");

		add(side1);
		add(side2);

		if (false)
			{
			code = new GMLTextArea("");
			JScrollPane codePane = new JScrollPane(code);
			add(codePane);
			}
		else
			{
			JPanel side3 = new JPanel(new BorderLayout());
			side3.setMinimumSize(new Dimension(0,0));
			side3.setPreferredSize(new Dimension(100,260));
			lab = new JLabel(Messages.getString("TimelineFrame.ACTIONS")); //$NON-NLS-1$
			side3.add(lab,"North");
			actions = new JList();
			scroll = new JScrollPane(actions);
			side3.add(scroll,"Center");

			JTabbedPane side4 = getLibraryTabs();
			side4.setPreferredSize(new Dimension(165,260)); //319

			add(side3);
			add(side4);
			}
		}

	//possibly extract to some place like resources.library.LibManager
	public JTabbedPane getLibraryTabs()
		{
		JTabbedPane tp = new JTabbedPane(JTabbedPane.RIGHT);
		for (Library l : LibManager.libs)
			{
			//if (l.advanced) continue;
			JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			for (LibAction la : l.libActions)
				{
				if (la.hidden || la.actionKind == Action.ACT_SEPARATOR) continue;
				if (la.advanced && !la.advanced) continue;
				JLabel b;
				if (la.actionKind == Action.ACT_LABEL)
					{
					b = new JLabel();
					b.setBorder(BorderFactory.createTitledBorder(la.name));
					b.setPreferredSize(new Dimension(90,14));
					p.add(b);
					continue;
					}
				if (la.actionKind == Action.ACT_PLACEHOLDER)
					b = new JLabel();
				else
					b = new JLabel(new ImageIcon(org.lateralgm.main.Util.getTransparentIcon(la.actImage)));
				b.setHorizontalAlignment(JLabel.LEFT);
				b.setVerticalAlignment(JLabel.TOP);
				b.setPreferredSize(new Dimension(32,32));
				p.add(b);
				}
			tp.add(l.tabCaption,p);
			}
		return tp;
		}

	@Override
	public boolean resourceChanged()
		{
		return true;
		}

	@Override
	public void revertResource()
		{
		LGM.currentFile.timelines.replace(res.getId(),resOriginal);
		}

	@Override
	public void updateResource()
		{
		res.setName(name.getText());

		resOriginal = (Timeline) res.copy(false,null);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == add)
			{
			return;
			}
		super.actionPerformed(e);
		}

	//Moments selection changed
	public void valueChanged(ListSelectionEvent e)
		{
		if (e.getValueIsAdjusting()) return;
		Moment m = (Moment) moments.getSelectedValue();
		if (m == null) return;
		actions.setListData(m.actions.toArray());
		}
	}
