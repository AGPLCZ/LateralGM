/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources.sub;

import java.lang.ref.WeakReference;

import org.lateralgm.resources.GmObject;

public class View
	{
	public boolean visible = false;
	public int viewX = 0;
	public int viewY = 0;
	public int viewW = 640;
	public int viewH = 480;
	public int portX = 0;
	public int portY = 0;
	public int portW = 640;
	public int portH = 480;
	public int hbor = 32;
	public int vbor = 32;
	public int hspeed = -1;
	public int vspeed = -1;
	public WeakReference<GmObject> objectFollowing = null;
	}
