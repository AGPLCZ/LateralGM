/*
 * Copyright (C) 2013 Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.GroupLayout.Alignment;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.DocumentUndoManager;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.components.mdi.MDIFrame;
import org.lateralgm.main.LGM;
import org.lateralgm.main.PrefsStore;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.GameInformation;

public class PreferencesFrame extends JFrame implements ActionListener
{
	private static final long serialVersionUID = 1L;
	protected JTabbedPane tabs;
	protected JSpinner sSizes;
	protected DocumentUndoManager undoManager = new DocumentUndoManager();
	protected Color fgColor;
	
	JComboBox themeCombo, iconCombo;

	private JPanel makeGeneralPrefs()
	{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		String key;
		
		key = "PreferencesFrame.APPLY_CHANGES";
    JButton applyBut = new JButton(Messages.getString(key));
    applyBut.addActionListener(this);
		applyBut.setActionCommand(key);

		JLabel themeLabel = new JLabel(Messages.getString("PreferencesFrame.THEME") + ":");
    String[] themeOptions = { "Swing", "Native", "Motif", "GTK", "Custom"};
    themeCombo = new JComboBox(themeOptions);
    themeCombo.setSelectedItem(LGM.themename);
		JLabel iconLabel = new JLabel(Messages.getString("PreferencesFrame.ICONS") + ":");
    String[] iconOptions = { "Swing", "Standard", "Custom" };
    iconCombo = new JComboBox(iconOptions);
    iconCombo.setSelectedItem(LGM.iconspack);
    JCheckBox dndEnable = new JCheckBox(Messages.getString("PreferencesFrame.ENABLE_DND"));

    p.add(dndEnable);
    p.add(applyBut);
    p.add(themeLabel);
		p.add(themeCombo);
		p.add(iconLabel);
		p.add(iconCombo);
		
		return p;
	}
	
	private JPanel makeExternalEditorPrefs()
	{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		//p.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		JLabel imageEditorLabel = new JLabel(Messages.getString("PreferencesFrame.IMAGE_EDITOR") + ":");
    String[] imageEditorOptions = { Messages.getString("PreferencesFrame.DEFAULT"), 
    		Messages.getString("PreferencesFrame.SYSTEM"), 
    		Messages.getString("PreferencesFrame.CUSTOM") };
    JComboBox imageEditorCombo = new JComboBox(imageEditorOptions);
    imageEditorCombo.setSelectedItem("Default");
		JTextField imageEditorPath = new JTextField();
		JButton imageEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		JLabel codeEditorLabel = new JLabel(Messages.getString("PreferencesFrame.CODE_EDITOR") + ":");
    String[] codeEditorOptions = { Messages.getString("PreferencesFrame.DEFAULT"), 
    		Messages.getString("PreferencesFrame.SYSTEM"), 
    		Messages.getString("PreferencesFrame.CUSTOM") };
    JComboBox codeEditorCombo = new JComboBox(imageEditorOptions);
    imageEditorCombo.setSelectedItem("Default");
		JTextField codeEditorPath = new JTextField();
		JButton codeEditorButton = new JButton(Messages.getString("PreferencesFrame.FIND"));
		
		p.add(imageEditorLabel);
		p.add(imageEditorCombo);
		p.add(imageEditorPath);
		p.add(imageEditorButton);
		
		p.add(codeEditorLabel);
		p.add(codeEditorCombo);
		p.add(codeEditorPath);
		p.add(codeEditorButton);
		
		
		/*gl.setVerticalGroup(
			   gl.createSequentialGroup()
			      .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
			           //.addComponent(codeEditorLabel)
			           //.addComponent(codeEditorCombo)
			           .addComponent(codeEditorPath))
			      //.addComponent(codeEditorButton)
			);*/
		
		 // Create a sequential group for the vertical axis.
	  // GroupLayout.SequentialGroup vGroup = gl.createSequentialGroup();
	 
	   // The sequential group contains two parallel groups that align
	   // the contents along the baseline. The first parallel group contains
	   // the first label and text field, and the second parallel group contains
	   // the second label and text field. By using a sequential group
	   // the labels and text fields are positioned vertically after one another.
	   //vGroup.addGroup(gl.createParallelGroup(Alignment.BASELINE).
	            //addComponent(codeEditorButton).addComponent(codeEditorPath));
	   //vGroup.addGroup(gl.createParallelGroup(Alignment.BASELINE).
	            //addComponent(label2).addComponent(tf2));
	  // gl.setVerticalGroup(vGroup);
		
		return p;
	}
	
	private JPanel makeCodeEditorPrefs()
	{
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);

		return p;
	}

	public PreferencesFrame(GameInformation res)
	{
		this(res,null);
	}

	public PreferencesFrame(GameInformation res, ResNode node)
	{
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		setSize(600,400);
		setTitle(Messages.getString("PreferencesFrame.TITLE"));
		setResizable(true);

		tabs = new JTabbedPane();
		add(tabs,BorderLayout.CENTER);

		tabs.addTab(Messages.getString("PreferencesFrame.TAB_GENERAL"), //$NON-NLS-1$
				/**/null,makeGeneralPrefs(),Messages.getString("PreferencesFrame.HINT_GENERAL")); //$NON-NLS-1$ 
		tabs.addTab(Messages.getString("PreferencesFrame.TAB_EXTERNAL_EDITOR"), //$NON-NLS-1$
				/**/null,makeExternalEditorPrefs(),Messages.getString("PreferencesFrame.HINT_EXTERNAL_EDITOR")); //$NON-NLS-1$ 
		tabs.addTab(Messages.getString("PreferencesFrame.TAB_CODE_EDITOR"), //$NON-NLS-1$
				/**/null,makeCodeEditorPrefs(),Messages.getString("PreferencesFrame.HINT_CODE_EDITOR")); //$NON-NLS-1$ 
	}
	
	public void SavePreferences()
  {
    LGM.iconspack = (String)iconCombo.getSelectedItem();
    LGM.themename = (String)themeCombo.getSelectedItem();
	  PrefsStore.setSwingTheme(LGM.themename);
    PrefsStore.setIconPack(LGM.iconspack);
	}
	
	public void ResetPreferences()
	{
		
	}
	
	public void actionPerformed(ActionEvent ev)
	{
			String com = ev.getActionCommand();
			if (com.equals("PreferencesFrame.APPLY_CHANGES")) //$NON-NLS-1$
			{
			  JOptionPane.showMessageDialog(this, Messages.getString("PreferencesFrame.APPLY_CHANGES_NOTICE"));
			  SavePreferences();
			  LGM.SetLookAndFeel(LGM.themename);
			  LGM.UpdateLookAndFeel();
			}
	}
}