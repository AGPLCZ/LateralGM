/*
 * Copyright (C) 2007, 2011 IsmAvatar <IsmAvatar@gmail.com>
 * Copyright (C) 2007, 2008, 2009 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2014, Robert B. Colton
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.subframes;

import static java.lang.Integer.MAX_VALUE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lateralgm.components.CustomFileChooser;
import org.lateralgm.components.impl.CustomFileFilter;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.FileChangeMonitor;
import org.lateralgm.file.FileChangeMonitor.FileUpdateEvent;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.Util;
import org.lateralgm.messages.Messages;
import org.lateralgm.resources.Sound;
import org.lateralgm.resources.Sound.PSound;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.ComboBoxConversion;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.DefaultComboBoxConversion;
import org.lateralgm.ui.swing.propertylink.ComboBoxLink.IndexComboBoxConversion;
import org.lateralgm.ui.swing.util.SwingExecutor;

public class SoundFrame extends InstantiableResourceFrame<Sound,PSound>
	{
	private static final long serialVersionUID = 1L;
	private static final ImageIcon LOAD_ICON = LGM.getIconForKey("SoundFrame.LOAD"); //$NON-NLS-1$
	private static final ImageIcon PLAY_ICON = LGM.getIconForKey("SoundFrame.PLAY"); //$NON-NLS-1$
	private static final ImageIcon STOP_ICON = LGM.getIconForKey("SoundFrame.STOP"); //$NON-NLS-1$
	private static final ImageIcon STORE_ICON = LGM.getIconForKey("SoundFrame.STORE"); //$NON-NLS-1$
	private static final ImageIcon EDIT_ICON = LGM.getIconForKey("SoundFrame.EDIT"); //$NON-NLS-1$

	public JButton load;
	public JButton play;
	public JButton stop;
	public JButton store;
	public JSlider volume;
	public JSlider pan;
	public JButton center;
	public JCheckBox preload;
	public JButton edit;
	public byte[] data;
	public boolean modified = false;
	private CustomFileChooser fc = new CustomFileChooser("/org/lateralgm","LAST_SOUND_DIR");
	private SoundEditor editor;
	private Clip clip;
	private JLabel statusLabel;
	private JPanel statusBar;
	//private JSlider pitch;
	private JSlider position;

	public String formatTime(long duration) {
		String formated = String.format("%dm%ds", 
	    TimeUnit.MICROSECONDS.toMinutes(duration),
	    TimeUnit.MICROSECONDS.toSeconds(duration) - 
	    TimeUnit.MINUTES.toSeconds(TimeUnit.MICROSECONDS.toMinutes(duration))
		);
		return formated;
	}
	
	public SoundFrame(Sound res, ResNode node)
		{
		super(res,node);
		setLayout(new BorderLayout());

		statusBar = makeStatusBar();
		add(statusBar, BorderLayout.SOUTH);
		add(makeToolBar(),BorderLayout.NORTH);
		JPanel content = new JPanel();
		add(content,BorderLayout.CENTER);

		GroupLayout layout = new GroupLayout(content);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		content.setLayout(layout);

		String s[] = { ".ogg",".wav",".mid",".mp3",".mod",".xm",".s3m",".it",".nfs",".gfs",".minigfs",
				".flac" };
		String[] d = { Messages.getString("SoundFrame.FORMAT_SOUND"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_OGG"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_WAV"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_MID"), //$NON-NLS-1$
				Messages.getString("SoundFrame.FORMAT_MP3") }; //$NON-NLS-1$

		CustomFileFilter soundsFilter = new CustomFileFilter(d[0],s);
		fc.addChoosableFileFilter(soundsFilter);
		fc.addChoosableFileFilter(new CustomFileFilter(d[1],s[0]));
		fc.addChoosableFileFilter(new CustomFileFilter(d[2],s[1]));
		fc.addChoosableFileFilter(new CustomFileFilter(d[3],s[2]));
		fc.addChoosableFileFilter(new CustomFileFilter(d[4],s[3]));
		fc.setFileFilter(soundsFilter);

		edit = new JButton(Messages.getString("SoundFrame.EDIT"),EDIT_ICON); //$NON-NLS-1$
		edit.addActionListener(this);
		play = new JButton(PLAY_ICON);
		play.addActionListener(this);
		play.setEnabled(false);
		stop = new JButton(STOP_ICON);
		stop.addActionListener(this);
		stop.setEnabled(false);

		JPanel pKind = makeAttributesPane();
		JPanel pEffects = makeEffectsPane();
		JPanel pAttr = makeFormatPane();

		final JLabel lVolume = new JLabel(Messages.getString("SoundFrame.VOLUME") + ": 100"); //$NON-NLS-1$
		volume = new JSlider(0,100,100);
		//volume.setPaintLabels(true);
		volume.setMajorTickSpacing(10);
		volume.setPaintTicks(true);
		volume.setSize(new Dimension(50, 50));
		volume.addChangeListener(new ChangeListener() {

		public void stateChanged(ChangeEvent ev)
			{
			lVolume.setText(Messages.getString("SoundFrame.VOLUME") + ": " + volume.getValue());
			}
	
		});
		plf.make(volume.getModel(),PSound.VOLUME,100.0);

		final JLabel lPan = new JLabel(Messages.getString("SoundFrame.PAN") + ": 0"); //$NON-NLS-1$
		pan = new JSlider(-100,100,0);
		//pan.setPaintLabels(true);
		pan.setMajorTickSpacing(20);
		pan.setPaintTicks(true);
		pan.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent ev)
				{
				lPan.setText(Messages.getString("SoundFrame.PAN") + ": " + pan.getValue());
				}
		
		});
		plf.make(pan.getModel(),PSound.PAN,100.0);

		/* TODO: Implement Pitch
		JLabel lPitch = new JLabel(Messages.getString("SoundFrame.PITCH") + ": 0"); //$NON-NLS-1$
		pitch = new JSlider(-100,100,0);
		//pan.setPaintLabels(true);
		pitch.setMajorTickSpacing(20);
		pitch.setPaintTicks(true);
		*/
		
		/*
		String positiontxt = "";
		if (clip != null) { 
			positiontxt = Messages.getString("SoundFrame.POSITION") + ": " + formatTime(clip.getMicrosecondPosition()) + " | " +
					Messages.getString("SoundFrame.DURATION") + ": " + formatTime(clip.getMicrosecondLength());
		} else {
			positiontxt = Messages.getString("SoundFrame.POSITION") + ": 0m0s | " +
				Messages.getString("SoundFrame.DURATION") + ": 0m0s";
		}
		*/
		final JLabel lPosition = new JLabel(Messages.getString("SoundFrame.POSITION") + ": 0m0s | " + 
		Messages.getString("SoundFrame.DURATION") + ": 0m0s"); //$NON-NLS-1$
		position = new JSlider(0,100,0);
		//pan.setPaintLabels(true);
		position.setMajorTickSpacing(10);
		position.setMinorTickSpacing(2);
		position.setPaintTicks(true);
		position.addChangeListener(new ChangeListener() {

		public void stateChanged(ChangeEvent ev)
			{
			if (clip == null) { return; }
			lPosition.setText(Messages.getString("SoundFrame.POSITION") + ": " + formatTime(clip.getMicrosecondPosition()) + " | " +
					Messages.getString("SoundFrame.DURATION") + ": " + formatTime(clip.getMicrosecondLength()) );
			}
	
		});
		
		/* TODO: Not sure if this button is needed since I added the label listener
		 * and you can tell when you set it to 0.
		 */
		/*
		center = new JButton(Messages.getString("SoundFrame.PAN_CENTER")); //$NON-NLS-1$
		center.addActionListener(this);
		center.setPreferredSize(edit.getSize());
		*/
		
		data = res.data;
		loadClip();
		layout.setHorizontalGroup(layout.createParallelGroup()
		/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(pKind,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*	*/.addComponent(pEffects,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE)
		/*	*/.addComponent(pAttr,DEFAULT_SIZE,DEFAULT_SIZE,MAX_VALUE))
				/**/.addGroup(layout.createSequentialGroup()
		/*	*/.addComponent(edit)
		/*	*/.addComponent(stop)
		/*	*/.addComponent(play))
		.addGroup(layout.createSequentialGroup()
		/**/.addComponent(lPosition))
		/**/.addComponent(position)
		.addGroup(layout.createSequentialGroup()
		/**/.addComponent(lVolume))
		/**/.addComponent(volume)
		.addGroup(layout.createSequentialGroup()
		/**/.addComponent(lPan))
		/**/.addComponent(pan)
		.addGroup(layout.createSequentialGroup()));
		
		
		layout.setVerticalGroup(layout.createSequentialGroup()
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(pKind)
		/*	*/.addComponent(pEffects)
		/*	*/.addComponent(pAttr))
		/**/.addGroup(layout.createParallelGroup()
		/*	*/.addComponent(edit)
		/*	*/.addComponent(stop)
		/*	*/.addComponent(play))
		.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/**/.addComponent(lPosition).addGap(0))
		/**/.addComponent(position)
		.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/**/.addComponent(lVolume).addGap(0))
		/**/.addComponent(volume)
		.addGroup(layout.createParallelGroup(Alignment.BASELINE)
		/**/.addComponent(lPan).addGap(0))
		/**/.addComponent(pan));
		
		pack();
		}

	private JToolBar makeToolBar()
		{
		JToolBar tool = new JToolBar();
		tool.setFloatable(false);
		tool.setAlignmentX(0);

		tool.add(save);

		load = new JButton(LOAD_ICON);
		load.setToolTipText(Messages.getString("SoundFrame.LOAD")); //$NON-NLS-1$
		load.addActionListener(this);
		tool.add(load);

		store = new JButton(STORE_ICON);
		store.setToolTipText(Messages.getString("SoundFrame.STORE")); //$NON-NLS-1$
		store.addActionListener(this);
		tool.add(store);

		tool.addSeparator();

		name.setColumns(13);
		name.setMaximumSize(name.getPreferredSize());
		tool.add(new JLabel(Messages.getString("SoundFrame.NAME"))); //$NON-NLS-1$
		tool.add(name);

		tool.addSeparator();
		preload = new JCheckBox(Messages.getString("SoundFrame.PRELOAD")); //$NON-NLS-1$
		plf.make(preload,PSound.PRELOAD);
		tool.add(preload);
		
		return tool;
		}

	private JPanel makeAttributesPane()
		{
		JPanel pAttr = new JPanel();
		// The options must be added in the order corresponding to Sound.SoundKind
		final String kindOptions[] = { 
				Messages.getString("SoundFrame.NORMAL"),
				Messages.getString("SoundFrame.BACKGROUND"),
				Messages.getString("SoundFrame.THREE"),
				Messages.getString("SoundFrame.MULT")
		};
		
		JComboBox kindCombo = new JComboBox(kindOptions);
		plf.make(kindCombo,PSound.KIND,new IndexComboBoxConversion() {
		public Object convertItem(int ind, Object o)
			{
			return ind;
			}
		});
		JOptionPane.showMessageDialog(null,this.res.properties.get(PSound.KIND));
		JLabel kindLabel = new JLabel(Messages.getString("SoundFrame.KIND") + ":");
		
		JCheckBox compressedCB = new JCheckBox(Messages.getString("SoundFrame.COMPRESSED"));
		plf.make(compressedCB,PSound.COMPRESSED);
		JCheckBox streamedCB = new JCheckBox(Messages.getString("SoundFrame.STREAMED"));
		plf.make(streamedCB,PSound.STREAMED);
		JCheckBox decompressCB = new JCheckBox(Messages.getString("SoundFrame.DECOMPRESS"));
		plf.make(decompressCB,PSound.DECOMPRESS_ON_LOAD);
		
		GroupLayout aLayout = new GroupLayout(pAttr);
		pAttr.setLayout(aLayout);
		aLayout.setHorizontalGroup(aLayout.createParallelGroup()
		/**/.addGroup(aLayout.createSequentialGroup()
		/*  */.addComponent(kindLabel)
		/*  */.addComponent(kindCombo))
		/**/.addComponent(compressedCB)
		/**/.addComponent(streamedCB)
		/**/.addComponent(decompressCB));
		aLayout.setVerticalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
				/*  */.addComponent(kindLabel)
				/*  */.addComponent(kindCombo))
		/**/.addComponent(compressedCB)
		/**/.addComponent(streamedCB)
		/**/.addComponent(decompressCB));
		return pAttr;
		}
	
	private JPanel makeFormatPane() {
		JPanel pFormat = new JPanel();
		
		final String typeOptions[] = { 
				Messages.getString("SoundFrame.MONO"),
				Messages.getString("SoundFrame.STEREO"),
				Messages.getString("SoundFrame.THREE")
		};
		JComboBox<String> typeCombo = new JComboBox<String>(typeOptions);
		plf.make(typeCombo,PSound.TYPE,new ComboBoxConversion() {
		public Object convertItem(int index, Object o)
			{
			return index;
			}
		});
		
		final String depthOptions[] = { "8 bit", "16 bit" };
		JComboBox<String> depthCombo = new JComboBox<String>(depthOptions);
		plf.make(depthCombo,PSound.BIT_DEPTH,new DefaultComboBoxConversion() {
			public Object convertItem(int index, Object o)
			{
				return ((String)o).substring(0, ((String)o).indexOf(' '));
			}
			public void select(JComboBox<Object> b, Object o)
			{
				b.setSelectedItem(o + " bit");
			}
		});
		
		final Integer sampleOptions[] = { 5512, 11025, 22050, 32000, 44100, 48000 };
		JComboBox<Integer> sampleCombo = new JComboBox<Integer>(sampleOptions);
		plf.make(sampleCombo,PSound.SAMPLE_RATE,new ComboBoxConversion() {
			public Object convertItem(int index, Object o)
				{
				return o;
				}
		});
		JLabel sampleLabel = new JLabel(Messages.getString("SoundFrame.SAMPLERATE"));
		
		ArrayList<Integer> bitOptions = new ArrayList<Integer>();
    for (int i = 8; i <= 512; i += 8 * Math.floor(Math.log(i)/Math.log(8))) {
			bitOptions.add(i);
		}
		JComboBox<Integer> bitCombo = new JComboBox(bitOptions.toArray());
		plf.make(bitCombo,PSound.BIT_RATE,new ComboBoxConversion() {
		public Object convertItem(int index, Object o)
			{
			return o;
			}
		});
		JLabel bitLabel = new JLabel(Messages.getString("SoundFrame.BITRATE"));
		
		GroupLayout aLayout = new GroupLayout(pFormat);
		aLayout.setAutoCreateGaps(true);
		pFormat.setLayout(aLayout);
		pFormat.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.FORMAT")));
		aLayout.setHorizontalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup()
		/*  */.addComponent(typeCombo)
		/*  */.addComponent(depthCombo))
		/**/.addGroup(aLayout.createParallelGroup()
		/*  */.addComponent(sampleLabel)
		/*  */.addComponent(bitLabel))
		/**/.addGroup(aLayout.createParallelGroup()
		/*  */.addComponent(sampleCombo)
		/*  */.addComponent(bitCombo)));
		aLayout.setVerticalGroup(aLayout.createSequentialGroup()
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(typeCombo)
		/*  */.addComponent(sampleLabel)
		/*  */.addComponent(sampleCombo))
		/**/.addGroup(aLayout.createParallelGroup(Alignment.BASELINE)
		/*  */.addComponent(depthCombo)
		/*  */.addComponent(bitLabel)
		/*  */.addComponent(bitCombo)));
		
		return pFormat;
	}

	private JPanel makeEffectsPane()
		{
		// these are in bit order as appears in a GM6 file, not the same as GM shows them
		//effects = new IndexButtonGroup(5,false);
		AbstractButton eChorus = new JCheckBox(Messages.getString("SoundFrame.CHORUS")); //$NON-NLS-1$
		plf.make(eChorus,PSound.CHORUS);
		AbstractButton eEcho = new JCheckBox(Messages.getString("SoundFrame.ECHO")); //$NON-NLS-1$
		plf.make(eEcho,PSound.ECHO);
		AbstractButton eFlanger = new JCheckBox(Messages.getString("SoundFrame.FLANGER")); //$NON-NLS-1$
		plf.make(eFlanger,PSound.FLANGER);
		AbstractButton eGargle = new JCheckBox(Messages.getString("SoundFrame.GARGLE")); //$NON-NLS-1$
		plf.make(eGargle,PSound.GARGLE);
		AbstractButton eReverb = new JCheckBox(Messages.getString("SoundFrame.REVERB")); //$NON-NLS-1$
		plf.make(eReverb,PSound.REVERB);
		JPanel pEffects = new JPanel();
		GroupLayout eLayout = new GroupLayout(pEffects);
		pEffects.setLayout(eLayout);
		pEffects.setBorder(BorderFactory.createTitledBorder(Messages.getString("SoundFrame.EFFECTS")));
		eLayout.setHorizontalGroup(eLayout.createSequentialGroup()
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eChorus)
		/*		*/.addComponent(eFlanger)
		/*		*/.addComponent(eReverb))
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eEcho)
		/*		*/.addComponent(eGargle)));
		eLayout.setVerticalGroup(eLayout.createSequentialGroup()
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eChorus)
		/*		*/.addComponent(eEcho))
		/**/.addGroup(eLayout.createParallelGroup()
		/*		*/.addComponent(eFlanger)
		/*		*/.addComponent(eGargle))
		/**/.addComponent(eReverb));
		return pEffects;
		}

	protected boolean areResourceFieldsEqual()
		{
		return !modified;
		}

	public void commitChanges()
		{
		res.setName(name.getText());
		res.data = data;
		}

	public void updateResource()
		{
		super.updateResource();
		modified = false;
		}

	public void actionPerformed(ActionEvent e)
		{
		if (e.getSource() == load)
			{
			File f;
			while (true)
				{
				if (fc.showOpenDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
				f = fc.getSelectedFile();
				if (f.exists()) break;
				JOptionPane.showMessageDialog(null,
						f.getName() + Messages.getString("SoundFrame.FILE_MISSING"), //$NON-NLS-1$
						Messages.getString("SoundFrame.FILE_OPEN"),JOptionPane.WARNING_MESSAGE); //$NON-NLS-1$
				}
			try
				{
				data = fileToBytes(f);
				//loadClip();
				String fn = f.getName();
				String extension = "";

				int i = fn.lastIndexOf('.');
				if (i > 0) {
				    extension = fn.substring(i+1);
				}
				// Set multi-media player for mp3's like Game Maker 8.1 one did for DirectSound
				// is ignored in OpenAL anyway so it don't matter.
				if (extension.toLowerCase().equals("mp3")) {
					res.put(PSound.KIND,Sound.SoundKind.MULTIMEDIA);
				}
				res.put(PSound.FILE_NAME,fn);
				String ft = CustomFileFilter.getExtension(fn);
				if (ft == null) ft = "";
				res.put(PSound.FILE_TYPE,ft);
				updateStatusLabel();
				play.setEnabled(false);
				if (clip != null) {
					clip.stop();
					clip.close();
					clip.flush();
					clip = null;
				}
				loadClip();
				}
			catch (Exception ex)
				{
				ex.printStackTrace();
				}
			modified = true;
			cleanup();
			return;
			}
		if (e.getSource() == play)
			{
			if (data == null || data.length == 0) return;
			if (clip == null) {
				loadClip();
			}
			play.setEnabled(false);
			stop.setEnabled(true);
			clip.setFramePosition((int)(((float)position.getValue()/100) * clip.getFrameLength()));
			//FloatControl gainControl = 
			    //(FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			//gainControl.setValue(-10);
			clip.start();
			new Thread()
			{
				public void run()
				{
					while (clip.isActive()) {
						//NOTE: Fuck you Java for actually making me have to cast like this.
						float pos = (float)clip.getLongFramePosition() / (float)clip.getFrameLength();

						position.setValue((int)(pos * position.getMaximum()));
					}

					clip.setFramePosition(0);
					position.setValue(0);
					play.setEnabled(true);
					stop.setEnabled(false);
				}
			}.start();
		return;
		}
		if (e.getSource() == stop)
			{
			if (clip != null) clip.stop(); stop.setEnabled(false);
			return;
			}
		if (e.getSource() == store)
			{
			if (fc.showSaveDialog(LGM.frame) != JFileChooser.APPROVE_OPTION) return;
			try
				{
				BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
						fc.getSelectedFile()));
				out.write(data);
				out.close();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			return;
			}
		if (e.getSource() == edit)
			{
			try
				{
				if (editor == null)
					new SoundEditor();
				else
					editor.start();
				}
			catch (IOException ex)
				{
				ex.printStackTrace();
				}
			return;
			}
		if (e.getSource() == center)
			{
			pan.setValue(0);
			return;
			}
		super.actionPerformed(e);
		}

	public static byte[] fileToBytes(File f) throws IOException
		{
		InputStream in = null;
		try
			{
			return Util.readFully(in = new FileInputStream(f)).toByteArray();
			}
		finally
			{
			if (in != null) in.close();
			}
		}
	
	private JPanel makeStatusBar() {
		JPanel status = new JPanel(new FlowLayout());
		BoxLayout layout = new BoxLayout(status,BoxLayout.X_AXIS);
		status.setLayout(layout);
		status.setMaximumSize(new Dimension(Integer.MAX_VALUE,11));

		statusLabel = new JLabel();
		status.add(statusLabel);
		
		updateStatusLabel();

		return status;
	}
	
	public static String formatData(long bytes) {
		if (bytes <= 0) return "0 B";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digits = (int) (Math.log(bytes)/Math.log(1024));
		return new DecimalFormat("#,##0.##").format(bytes/Math.pow(1024, digits)) + " " + 
			units[digits];
	}
	
	public void loadClip () {
		if (data == null || data.length <= 0) { return; }
		try {
			InputStream source = new ByteArrayInputStream(data);
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(source));
			AudioFormat fmt = ais.getFormat();
			//Forcibly convert to PCM Signed because non-pulse can't play unsigned (Java bug)
			if (fmt.getEncoding() != AudioFormat.Encoding.PCM_SIGNED)
			{
					fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,fmt.getSampleRate(),
					fmt.getSampleSizeInBits() * 2,fmt.getChannels(),fmt.getFrameSize() * 2,
					fmt.getFrameRate(),true);
					ais = AudioSystem.getAudioInputStream(fmt,ais);
			}
			//Clip c = AudioSystem.getClip() generates a bogus format instead of using ais.getFormat.
		  clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class,fmt));
		 
			clip.open(ais);
			play.setEnabled(true);
		} catch (IOException e) {
				e.printStackTrace();
		}
		catch (LineUnavailableException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (UnsupportedAudioFileException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateStatusLabel() {
		String stat = " " + Messages.getString("SoundFrame.FILENAME") + ": " +  res.get(PSound.FILE_NAME) + " | " + 
				Messages.getString("SoundFrame.MEMORY") + ": ";
		
		if (res.data.length != 0) {
			stat += formatData(res.data.length);
		} else if (data != null) {
			stat += formatData(data.length);
		} else {
			stat += formatData(0);
		}

		statusLabel.setText(stat);
	}

	private class SoundEditor implements UpdateListener
		{
		public final FileChangeMonitor monitor;

		public SoundEditor() throws IOException,UnsupportedOperationException
			{
			File f = File.createTempFile(res.getName(),new File((String) res.get(PSound.FILE_NAME)).getName(),
					LGM.tempDir);
			f.deleteOnExit();
			FileOutputStream out = new FileOutputStream(f);
			out.write(data);
			out.close();
			monitor = new FileChangeMonitor(f,SwingExecutor.INSTANCE);
			monitor.updateSource.addListener(this);
			editor = this;
			start();
			}

		public void start() throws IOException
			{
			if (!Prefs.useExternalSoundEditor || Prefs.externalSoundEditorCommand == null)
				try
					{
						Desktop.getDesktop().edit(monitor.file);
					}
				catch (UnsupportedOperationException e)
					{
						throw new UnsupportedOperationException("no internal or system sound editor",e);
					}
				catch (IOException e) {
						e.printStackTrace();
					}
			else
				Runtime.getRuntime().exec(
						String.format(Prefs.externalSoundEditorCommand,monitor.file.getAbsolutePath()));
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
					try
						{
						data = fileToBytes(monitor.file);
						updateStatusLabel();
						}
					catch (IOException ioe)
						{
						ioe.printStackTrace();
						return;
						}
					modified = true;
					break;
				case DELETED:
					editor = null;
				}
			}
		}

	public void dispose()
		{
		cleanup();
		super.dispose();
		}

	protected void cleanup()
		{
		if (editor != null) editor.stop();
		if (clip != null) {
			clip.stop();
			clip.close();
			clip = null;
		}
		}
	}
