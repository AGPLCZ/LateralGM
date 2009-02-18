/*
 * Copyright (C) 2007, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2007, 2008 Clam <clamisgood@gmail.com>
 * Copyright (C) 2006 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2006, 2007 TGMG <thegamemakerguru@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.event.InternalFrameEvent;

import org.lateralgm.components.GMLTextArea;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.impl.TextAreaFocusTraversalPolicy;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Script;
import org.lateralgm.resources.Script.PScript;
import org.lateralgm.ui.swing.util.SwingExecutor;

public class ScriptFrame extends ResourceFrame<Script,PScript> implements ActionListener
	{
	private static final long serialVersionUID = 1L;
	public JToolBar tool;
	public GMLTextArea code;
	public JButton edit;

	private ScriptEditor editor;

	public ScriptFrame(Script res, ResNode node)
		{
		super(res,node);
		setSize(600,400);
		setLayout(new BorderLayout());

		// Setup the toolbar
		tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);
		add(tool,BorderLayout.NORTH);

		tool.add(save);
		tool.addSeparator();

		code = new GMLTextArea((String) res.get(PScript.CODE));
		add(code,BorderLayout.CENTER);

		if (!Prefs.useExternalScriptEditor)
			code.addEditorButtons(tool);
		else
			{
			code.editable = false;
			edit = new JButton(Messages.getString("ScriptFrame.EDIT")); //$NON-NLS-1$
			edit.addActionListener(this);
			tool.add(edit);
			}

		tool.addSeparator();
		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("ScriptFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		setFocusTraversalPolicy(new TextAreaFocusTraversalPolicy(code));
		}

	public void revertResource()
		{
		resOriginal.updateReference();
		}

	public void commitChanges()
		{
		res.put(PScript.CODE,code.getTextCompat());
		res.setName(name.getText());
		}

	public boolean resourceChanged()
		{
		return (Prefs.useExternalScriptEditor ? !res.get(PScript.CODE).equals(
				resOriginal.get(PScript.CODE)) : code.getUndoManager().isModified())
				|| !resOriginal.getName().equals(name.getText());
		//return !code.getTextCompat().equals(resOriginal.scriptStr)
		//		|| !resOriginal.getName().equals(name.getText());
		}

	public void fireInternalFrameEvent(int id)
		{
		if (id == InternalFrameEvent.INTERNAL_FRAME_CLOSED)
			LGM.currentFile.updateSource.removeListener(code);
		super.fireInternalFrameEvent(id);
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == edit)
			{
			if (Prefs.useExternalScriptEditor)
				{
				try
					{
					if (editor == null)
						new ScriptEditor();
					else
						editor.start();
					}
				catch (Exception ex)
					{
					ex.printStackTrace();
					}
				}
			return;
			}
		super.actionPerformed(e);
		}

	private class ScriptEditor implements UpdateListener
		{
		public final FileChangeMonitor monitor;

		public ScriptEditor() throws IOException
			{
			File f = File.createTempFile(res.getName(),".gml",LGM.tempDir);
			f.deleteOnExit();
			FileWriter out = new FileWriter(f);
			out.write((String) res.get(PScript.CODE));
			out.close();
			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this,true);
			editor = this;
			start();
			}

		public void start() throws IOException
			{
			Runtime.getRuntime().exec(
					String.format(Prefs.externalScriptEditorCommand,monitor.file.getAbsolutePath()));
			}

		public void stop()
			{
			monitor.stop();
			monitor.file.delete();
			editor = null;
			}

		public void updated(UpdateEvent e)
			{
			if (!(e instanceof FileUpdateEvent)) return;
			switch (((FileUpdateEvent) e).flag)
				{
				case CHANGED:
					StringBuffer sb = new StringBuffer(1024);
					try
						{
						BufferedReader reader = new BufferedReader(new FileReader(monitor.file));
						char[] chars = new char[1024];
						int len = 0;
						while ((len = reader.read(chars)) > -1)
							sb.append(chars,0,len);
						reader.close();
						}
					catch (IOException ioe)
						{
						ioe.printStackTrace();
						return;
						}
					String s = sb.toString();
					res.put(PScript.CODE,s);
					code.setText(s);
					break;
				case DELETED:
					editor = null;
				}
			}
		}

	public void dispose()
		{
		if (editor != null) editor.stop();
		super.dispose();
		}
	}
