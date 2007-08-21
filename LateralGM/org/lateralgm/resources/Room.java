/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of Lateral GM.
 * Lateral GM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.ArrayList;

import org.lateralgm.file.Gm6File;
import org.lateralgm.file.ResourceList;
import org.lateralgm.main.LGM;
import org.lateralgm.main.Prefs;
import org.lateralgm.resources.sub.BackgroundDef;
import org.lateralgm.resources.sub.Instance;
import org.lateralgm.resources.sub.Tile;
import org.lateralgm.resources.sub.View;

public class Room extends Resource
	{
	public static final byte TAB_OBJECTS = 0;
	public static final byte TAB_SETTINGS = 1;
	public static final byte TAB_TILES = 2;
	public static final byte TAB_BACKGROUNDS = 3;
	public static final byte TAB_VIEWS = 4;
	public String caption = "";
	public int width = 640;
	public int height = 480;
	public int snapX = 16;
	public int snapY = 16;
	public boolean isometricGrid = false;
	public int speed = 30;
	public boolean persistent = false;
	public int backgroundColor = 0x000000;
	public boolean drawBackgroundColor = true;
	public String creationCode = "";
	public boolean rememberWindowSize = true;
	// ** may not be relevant to swing, or may not produce the same effect in the LGM GUI
	public int editorWidth = 200; // **
	public int editorHeight = 200; // **
	public boolean showGrid = true;
	public boolean showObjects = true;
	public boolean showTiles = true;
	public boolean showBackgrounds = true;
	public boolean showForegrounds = true;
	public boolean showViews = false;
	public boolean deleteUnderlyingObjects = true;
	public boolean deleteUnderlyingTiles = true;
	public int currentTab = TAB_OBJECTS;
	public int scrollBarX = 0; // **
	public int scrollBarY = 0; // **
	public BackgroundDef[] backgroundDefs = new BackgroundDef[8];
	public View[] views = new View[8];
	public boolean enableViews = false;
	public ArrayList<Instance> instances = new ArrayList<Instance>();
	public ArrayList<Tile> tiles = new ArrayList<Tile>();
	private Gm6File parent;

	public Room()
		{
		setName(Prefs.prefixes[Resource.ROOM]);
		parent = LGM.currentFile;
		for (int j = 0; j < 8; j++)
			{
			views[j] = new View();
			backgroundDefs[j] = new BackgroundDef();
			}
		}

	public Room(Gm6File parent) // Rooms are special - they need to know what file they belong to
		{
		this.parent = parent;
		for (int j = 0; j < 8; j++)
			{
			views[j] = new View();
			backgroundDefs[j] = new BackgroundDef();
			}
		}

	public Instance addInstance()
		{
		Instance inst = new Instance();
		inst.instanceId = ++parent.lastInstanceId;
		instances.add(inst);
		return inst;
		}

	public Tile addTile()
		{
		Tile tile = new Tile();
		tile.tileId = ++parent.lastTileId;
		tiles.add(tile);
		return tile;
		}

	@SuppressWarnings("unchecked")
	private Room copy(boolean update, ResourceList src)
		{
		Room rm = new Room();
		rm = new Room();
		rm.caption = caption;
		rm.width = width;
		rm.height = height;
		rm.snapX = snapX;
		rm.snapY = snapY;
		rm.isometricGrid = isometricGrid;
		rm.speed = speed;
		rm.persistent = persistent;
		rm.backgroundColor = backgroundColor;
		rm.drawBackgroundColor = drawBackgroundColor;
		rm.creationCode = creationCode;
		rm.rememberWindowSize = rememberWindowSize;
		rm.editorWidth = editorWidth;
		rm.editorHeight = editorHeight;
		rm.showGrid = showGrid;
		rm.showObjects = showObjects;
		rm.showTiles = showTiles;
		rm.showBackgrounds = showBackgrounds;
		rm.showForegrounds = showForegrounds;
		rm.showViews = showViews;
		rm.deleteUnderlyingObjects = deleteUnderlyingObjects;
		rm.deleteUnderlyingTiles = deleteUnderlyingTiles;
		rm.currentTab = currentTab;
		rm.scrollBarX = scrollBarX;
		rm.scrollBarY = scrollBarY;
		rm.enableViews = enableViews;
		for (Instance inst : instances)
			{
			Instance inst2 = rm.addInstance();
			inst2.creationCode = inst.creationCode;
			inst2.locked = inst.locked;
			inst2.gmObjectId = inst.gmObjectId;
			inst2.x = inst.x;
			inst2.y = inst.y;
			}
		for (Tile tile : tiles)
			{
			Tile tile2 = rm.addTile();
			tile2.backgroundId = tile.backgroundId;
			tile2.depth = tile.depth;
			tile2.height = tile.height;
			tile2.locked = tile.locked;
			tile2.tileX = tile.tileX;
			tile2.tileY = tile.tileY;
			tile2.width = tile.width;
			tile2.x = tile.x;
			tile2.y = tile.y;
			}
		for (int i = 0; i < 8; i++)
			{
			View view = views[i];
			View view2 = rm.views[i];
			view2.enabled = view.enabled;
			view2.viewX = view.viewX;
			view2.viewY = view.viewY;
			view2.viewW = view.viewW;
			view2.viewH = view.viewH;
			view2.portX = view.portX;
			view2.portY = view.portY;
			view2.portW = view.portW;
			view2.portH = view.portH;
			view2.hbor = view.hbor;
			view2.vbor = view.vbor;
			view2.hspeed = view.hspeed;
			view2.vspeed = view.vspeed;
			view2.objectFollowing = view.objectFollowing;
			}
		for (int i = 0; i < 8; i++)
			{
			BackgroundDef back = backgroundDefs[i];
			BackgroundDef back2 = rm.backgroundDefs[i];
			back2.visible = back.visible;
			back2.foreground = back.foreground;
			back2.backgroundId = back.backgroundId;
			back2.x = back.x;
			back2.y = back.y;
			back2.tileHoriz = back.tileHoriz;
			back2.tileVert = back.tileVert;
			back2.horizSpeed = back.horizSpeed;
			back2.vertSpeed = back.vertSpeed;
			back2.stretch = back.stretch;
			}
		if (update)
			{
			rm.setId(new ResId(++src.lastId));
			rm.setName(Prefs.prefixes[Resource.ROOM] + src.lastId);
			src.add(rm);
			}
		else
			{
			rm.setId(getId());
			rm.setName(getName());
			}
		return rm;
		}

	public byte getKind()
		{
		return ROOM;
		}

	@SuppressWarnings("unchecked")
	public Room copy(ResourceList src)
		{
		return copy(true,src);
		}

	public Room copy()
		{
		return copy(false,null);
		}
	}
