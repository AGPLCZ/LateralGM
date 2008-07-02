/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * 
 * This file is part of LateralGM.
 * 
 * LateralGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * LateralGM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License (COPYING) for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.lateralgm.resources;

import javax.swing.ImageIcon;

import org.lateralgm.components.GmTreeGraphics;
import org.lateralgm.components.impl.ResNode;
import org.lateralgm.file.ResourceList;
import org.lateralgm.main.LGM;
import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateTrigger;

public abstract class Resource<R extends Resource<R>> implements Comparable<Resource<R>>
	{
	public static final byte SPRITE = 2;
	public static final byte SOUND = 3;
	public static final byte BACKGROUND = 6;
	public static final byte PATH = 8;
	public static final byte SCRIPT = 7;
	public static final byte FONT = 9;
	public static final byte TIMELINE = 12;
	public static final byte GMOBJECT = 1;
	public static final byte ROOM = 4;

	public static final byte GAMEINFO = 10;
	public static final byte GAMESETTINGS = 11;
	public static final byte EXTENSIONS = 13;

	public static final ImageIcon[] ICON = new ImageIcon[14];
	static
		{
		ICON[SPRITE] = LGM.getIconForKey("Resource.SPRITE"); //$NON-NLS-1$
		ICON[SOUND] = LGM.getIconForKey("Resource.SOUND"); //$NON-NLS-1$
		ICON[BACKGROUND] = LGM.getIconForKey("Resource.BACKGROUND"); //$NON-NLS-1$
		ICON[PATH] = LGM.getIconForKey("Resource.PATH"); //$NON-NLS-1$
		ICON[SCRIPT] = LGM.getIconForKey("Resource.SCRIPT"); //$NON-NLS-1$
		ICON[FONT] = LGM.getIconForKey("Resource.FONT"); //$NON-NLS-1$
		ICON[TIMELINE] = LGM.getIconForKey("Resource.TIMELINE"); //$NON-NLS-1$
		ICON[GMOBJECT] = LGM.getIconForKey("Resource.GMOBJECT"); //$NON-NLS-1$
		ICON[ROOM] = LGM.getIconForKey("Resource.ROOM"); //$NON-NLS-1$
		ICON[GAMEINFO] = LGM.getIconForKey("Resource.GAMEINFO"); //$NON-NLS-1$
		ICON[GAMESETTINGS] = LGM.getIconForKey("Resource.GAMESETTINGS"); //$NON-NLS-1$
		ICON[EXTENSIONS] = GmTreeGraphics.getBlankIcon();
		}

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	private ResNode node;
	private String name = "";
	private int id;

	public void setId(int id)
		{
		this.id = id;
		fireUpdate();
		}

	public int getId()
		{
		return id;
		}

	public int compareTo(Resource<R> res)
		{
		return res.id == id ? 0 : (res.id < id ? -1 : 1);
		}

	protected void fireUpdate()
		{
		updateTrigger.fire();
		}

	public String getName()
		{
		return name;
		}

	public void setName(String name)
		{
		this.name = name;
		fireUpdate();
		}

	public ResNode getNode()
		{
		return node;
		}

	public void setNode(ResNode node)
		{
		this.node = node;
		}

	public abstract R copy(ResourceList<R> src);

	public abstract R copy();

	public abstract byte getKind();

	public String toString()
		{
		return name;
		}
	}
