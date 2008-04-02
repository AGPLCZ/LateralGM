/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.components.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

public class CustomFileFilter extends FileFilter implements FilenameFilter
	{
	private ArrayList<String> ext = new ArrayList<String>();
	private String desc;

	/**
	 * Gets the extension part of the given filename, including the period
	 * @param filename
	 * @return the extension, including period
	 */
	public static String getExtension(String filename)
		{
		int p = filename.lastIndexOf(".");
		if (p == -1) return null;
		return filename.substring(p).toLowerCase(Locale.ENGLISH);
		}

	public CustomFileFilter(String ext, String desc)
		{
		this.ext.add(ext);
		this.desc = desc;
		}

	public CustomFileFilter(String[] ext, String desc)
		{
		for (String element : ext)
			{
			this.ext.add(element);
			}
		this.desc = desc;
		}

	public boolean accept(File f)
		{
		if (f.isDirectory()) return true;
		return accept(f,f.getPath());
		}

	public String getDescription()
		{
		return desc;
		}

	public boolean accept(File dir, String name)
		{
		if (ext.size() == 0) return true;
		//if (f.isDirectory()) return true;
		String s = getExtension(name);
		if (s == null) return false;
		return ext.contains(s);
		}
	}
