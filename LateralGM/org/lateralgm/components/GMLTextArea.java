/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.PlainDocument;

import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.file.ResourceList;
import org.lateralgm.jedit.CompletionMenu;
import org.lateralgm.jedit.DefaultInputHandler;
import org.lateralgm.jedit.GMLKeywords;
import org.lateralgm.jedit.GMLTokenMarker;
import org.lateralgm.jedit.InputHandler;
import org.lateralgm.jedit.JEditTextArea;
import org.lateralgm.jedit.KeywordMap;
import org.lateralgm.jedit.SyntaxDocument;
import org.lateralgm.jedit.Token;
import org.lateralgm.jedit.CompletionMenu.Completion;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Resource;

public class GMLTextArea extends JEditTextArea implements ChangeListener
	{
	private static final long serialVersionUID = 1L;

	private static final int[] KM_RESOURCES = { Resource.BACKGROUND,Resource.FONT,Resource.GMOBJECT,
			Resource.PATH,Resource.ROOM,Resource.SCRIPT,Resource.SOUND,Resource.SPRITE,Resource.TIMELINE };
	private static final GMLKeywords.Keyword[][] GML_KEYWORDS = { GMLKeywords.CONSTRUCTS,
			GMLKeywords.FUNCTIONS,GMLKeywords.VARIABLES,GMLKeywords.OPERATORS,GMLKeywords.CONSTANTS };

	private final GMLTokenMarker gmlTokenMarker = new GMLTokenMarker();
	private final DocumentUndoManager undoManager = new DocumentUndoManager();
	protected static Timer timer;
	protected Integer lastUpdateTaskID = 0;
	private String[][] resourceKeywords = new String[KM_RESOURCES.length][];
	protected Completion[] completions;

	public GMLTextArea(String text)
		{
		super();
		setDocument(new SyntaxDocument());
		getDocument().getDocumentProperties().put(PlainDocument.tabSizeAttribute,Prefs.tabSize);
		updateResourceKeywords();
		setTokenMarker(gmlTokenMarker);
		painter.setFont(Prefs.codeFont);
		painter.setStyles(PrefsStore.getSyntaxStyles());
		painter.setBracketHighlightColor(Color.gray);
		inputHandler = new DefaultInputHandler();
		inputHandler.addDefaultKeyBindings();
		putClientProperty(InputHandler.KEEP_INDENT_PROPERTY,Boolean.TRUE);
		putClientProperty(InputHandler.TAB_TO_INDENT_PROPERTY,Boolean.TRUE);
		putClientProperty(InputHandler.CONVERT_TABS_PROPERTY,Boolean.TRUE);
		text = text.replace("\r\n","\n"); //$NON-NLS-1$ //$NON-NLS-2$
		setText(text);
		setCaretPosition(0);
		LGM.currentFile.addChangeListener(this);
		addCaretListener(undoManager);
		document.addUndoableEditListener(undoManager);
		inputHandler.addKeyBinding("C+Z",undoManager.getUndoAction()); //$NON-NLS-1$
		inputHandler.addKeyBinding("C+Y",undoManager.getRedoAction()); //$NON-NLS-1$
		inputHandler.addKeyBinding("C+SPACE",new CompletionAction());
		}

	private static JButton makeToolbarButton(Action a)
		{
		JButton b = new JButton(a);
		b.setToolTipText(b.getText());
		b.setText(null);
		b.setRequestFocusEnabled(false);
		return b;
		}

	private JButton makeInputHandlerToolbarButton(final ActionListener l, String key)
		{
		final GMLTextArea source = this;
		Action a = new AbstractAction(Messages.getString(key),LGM.getIconForKey(key))
			{
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e)
					{
					getInputHandler().executeAction(l,source,null);
					}
			};
		return makeToolbarButton(a);
		}

	public void addEditorButtons(JToolBar tb)
		{
		tb.add(makeToolbarButton(getUndoManager().getUndoAction()));
		tb.add(makeToolbarButton(getUndoManager().getRedoAction()));
		tb.addSeparator();
		tb.add(makeInputHandlerToolbarButton(InputHandler.CUT,"GMLTextArea.CUT")); //$NON-NLS-1$
		tb.add(makeInputHandlerToolbarButton(InputHandler.COPY,"GMLTextArea.COPY")); //$NON-NLS-1$
		tb.add(makeInputHandlerToolbarButton(InputHandler.PASTE,"GMLTextArea.PASTE")); //$NON-NLS-1$
		}

	public DocumentUndoManager getUndoManager()
		{
		return undoManager;
		}

	public String getTextCompat()
		{
		String s = getText();
		s = s.replaceAll("\r?\n","\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
		return s;
		}

	public void updateResourceKeywords()
		{
		for (int j = 0; j < resourceKeywords.length; j++)
			{
			ResourceList<?> rl = LGM.currentFile.getList(KM_RESOURCES[j]);
			int l = rl.size();
			String[] a = new String[l];
			for (int i = 0; i < l; i++)
				{
				a[i] = rl.get(i).getName();
				}
			resourceKeywords[j] = a;
			}
		completions = null;
		updateTokenMarker();
		}

	private void updateTokenMarker()
		{
		KeywordMap km = new KeywordMap(false);
		for (String[] a : resourceKeywords)
			{
			for (String s : a)
				{
				if (s.length() > 0) km.add(s,Token.KEYWORD3);
				}
			}
		gmlTokenMarker.setCustomKeywords(km);
		}

	protected void updateCompletions()
		{
		int l = 0;
		for (String[] a : resourceKeywords)
			{
			l += a.length;
			}
		for (GMLKeywords.Keyword[] a : GML_KEYWORDS)
			{
			l += a.length;
			}
		completions = new Completion[l];
		int i = 0;
		for (String[] a : resourceKeywords)
			{
			for (String s : a)
				{
				completions[i] = new CompletionMenu.WordCompletion(s);
				i++;
				}
			}
		for (GMLKeywords.Keyword[] a : GML_KEYWORDS)
			{
			for (GMLKeywords.Keyword k : a)
				{
				if (k instanceof GMLKeywords.Function)
					completions[i] = new FunctionCompletion((GMLKeywords.Function) k);
				else if (k instanceof GMLKeywords.Variable)
					completions[i] = new VariableCompletion((GMLKeywords.Variable) k);
				else
					completions[i] = new CompletionMenu.WordCompletion(k.getName());
				i++;
				}
			}
		}

	public class VariableCompletion extends CompletionMenu.Completion
		{
		private final GMLKeywords.Variable variable;

		public VariableCompletion(GMLKeywords.Variable v)
			{
			variable = v;
			name = v.getName();
			}

		public boolean apply(JEditTextArea a, char input, int offset, int pos, int length)
			{
			String s = name;
			int l = input != '\0' ? pos : length;
			int p = s.length();
			if (variable.arraySize > 0)
				{
				s += "[]";
				boolean ci = true;
				switch (input)
					{
					case '\0':
					case '[':
						break;
					case ']':
						ci = false;
						break;
					default:
						s += String.valueOf(input);
					}
				if (ci)
					p = s.length() - 1;
				else
					p = s.length();
				}
			SyntaxDocument d = a.getDocument();
			if (!replace(d,offset,l,s)) return false;
			a.setCaretPosition(offset + p);
			return true;
			}

		public String toString()
			{
			String s = name;
			if (variable.arraySize > 0) s += "[0.." + String.valueOf(variable.arraySize - 1) + "]";
			if (variable.readOnly) s += "*";
			return s;
			}
		}

	public class FunctionCompletion extends CompletionMenu.Completion
		{
		private final GMLKeywords.Function function;

		public FunctionCompletion(GMLKeywords.Function f)
			{
			function = f;
			name = f.getName();
			}

		public boolean apply(JEditTextArea a, char input, int offset, int pos, int length)
			{
			String s = name + "(" + getArguments() + ")";
			int l = input != '\0' ? pos : length;
			int p1, p2;
			boolean argSel = true;
			switch (input)
				{
				case '\0':
				case '(':
					break;
				case ')':
					argSel = false;
					break;
				default:
					s += String.valueOf(input);
				}
			if (argSel && function.arguments.length > 0)
				{
				p1 = name.length() + 1;
				p2 = p1 + getArgument(0).length();
				}
			else
				{
				p1 = s.length();
				p2 = p1;
				}
			SyntaxDocument d = a.getDocument();
			if (!replace(d,offset,l,s)) return false;
			a.setSelectionStart(offset + p1);
			a.setSelectionEnd(offset + p2);
			return true;
			}

		public String getArgument(int i)
			{
			if (i >= function.arguments.length) return null;
			return function.arguments[i] + (i == function.dynArgIndex ? "..." : "");
			}

		public String getArguments()
			{
			String s = "";
			for (int i = 0; i < function.arguments.length; i++)
				s += (i > 0 ? "," : "") + getArgument(i);
			return s;
			}

		public String toString()
			{
			return name + "(" + getArguments() + ")";
			}
		}

	private class CompletionAction implements ActionListener
		{
		public CompletionAction()
			{
			super();
			}

		private String find(String input, String regex)
			{
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(input);
			if (m.find()) return m.group();
			return "";
			}

		public void actionPerformed(ActionEvent e)
			{
			if (isEditable())
				{
				int s = getSelectionStart();
				int sl = getSelectionStartLine();
				int ls = s - getLineStartOffset(sl);
				String lt = getLineText(sl);
				int l1 = find(lt.substring(0,ls),"\\w+$").length();
				int l2 = find(lt.substring(ls),"^\\w+").length();
				if (completions == null) updateCompletions();
				new CompletionMenu(LGM.frame,GMLTextArea.this,s - l1,l1,l1 + l2,completions);
				}
			}
		}

	public void stateChanged(ChangeEvent e)
		{
		if (timer == null) timer = new Timer();
		timer.schedule(new UpdateTask(),500);
		}

	private class UpdateTask extends TimerTask
		{
		private int id;

		public UpdateTask()
			{
			synchronized (lastUpdateTaskID)
				{
				id = ++lastUpdateTaskID;
				}
			}

		public void run()
			{
			synchronized (lastUpdateTaskID)
				{
				if (id != lastUpdateTaskID) return;
				}
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
						{
						updateResourceKeywords();
						int fl = getFirstLine();
						painter.invalidateLineRange(fl,fl + getVisibleLines());
						}
				});
			}
		}
	}
