/*
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.main;

import java.awt.Font;
import java.awt.event.KeyEvent;

public class Prefs
	{
	public static boolean protectRoot = true;
	public static boolean groupKind = true;
	public static boolean iconizeGroup = false;
	public static Font codeFont = new Font("Monospaced",Font.PLAIN,12);
	public static int tabSize = 4;
	public static String[] prefixes = { "","obj_","spr_","snd_","rm_","","bk_","scr_","path_",
			"font_","","","time_" };
	public static String defaultLibraryPath = "org/lateralgm/resources/library/lib/";
	public static String[] defaultLibNames = { "01_move","02_main1","03_main2","04_control",
			"05_score","06_extra","07_draw" };
	public static String userLibraryPath = null;
	public static int eventKeyInputAddKey = KeyEvent.VK_BACK_SLASH;
	}
