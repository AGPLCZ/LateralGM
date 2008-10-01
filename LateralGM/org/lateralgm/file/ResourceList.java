/*
 * Copyright (C) 2007 Clam <ebordin@aapt.net.au>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.util.ArrayList;
import java.util.Collections;

import org.lateralgm.main.UpdateSource;
import org.lateralgm.main.UpdateSource.UpdateEvent;
import org.lateralgm.main.UpdateSource.UpdateListener;
import org.lateralgm.main.UpdateSource.UpdateTrigger;
import org.lateralgm.resources.Resource;
import org.lateralgm.resources.Room;

public class ResourceList<R extends Resource<R>> extends ArrayList<R> implements UpdateListener
	{
	private static final long serialVersionUID = 1L;

	private Class<R> type; // used as a workaround for add()
	private GmFile parent; // used for rooms

	private final UpdateTrigger updateTrigger = new UpdateTrigger();
	public final UpdateSource updateSource = new UpdateSource(this,updateTrigger);

	ResourceList(Class<R> type, GmFile parent)
		{
		this.type = type;
		this.parent = parent;
		}

	public int lastId = -1;

	public boolean add(R res)
		{
		super.add(res);
		res.reference.updateSource.addListener(this);
		updateTrigger.fire();
		res.setId(++lastId);
		return true;
		}

	public R add()
		{
		R res = null;
		try
			{
			if (type == Room.class)
				res = type.getConstructor(GmFile.class).newInstance(parent);
			else
				res = type.newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		if (res != null)
			{
			res.setName(res.getName() + (lastId + 1));
			add(res);
			}
		return res;
		}

	public R duplicate(R res)
		{
		int ind = indexOf(res);
		if (ind == -1) return null;
		R res2 = res.copy(this);
		return res2;
		}

	public R getUnsafe(int id)
		{
		for (R res : this)
			if (res.getId() == id) return res;
		return null;
		}

	/** May return null */
	public R get(String name)
		{
		for (R res : this)
			if (res.getName().equals(name)) return res;
		return null;
		}

	public R remove(int index)
		{
		R res = get(index);
		super.remove(index);
		res.reference.updateSource.removeListener(this);
		updateTrigger.fire();
		return res;
		}

	public void clear()
		{
		if (size() == 0) return;
		for (R r : this)
			{
			r.reference.updateSource.removeListener(this);
			}
		super.clear();
		updateTrigger.fire();
		}

	public void sort()
		{
		Collections.sort(this);
		}

	public void replace(R old, R replacement)
		{
		int ind = indexOf(old);
		if (ind == -1) return;
		set(ind,replacement);
		}

	public void defragIds()
		{
		sort();
		for (int i = 0; i < size(); i++)
			get(i).setId(i);
		lastId = size() - 1;
		}

	public void updated(UpdateEvent e)
		{
		updateTrigger.fire(e);
		}

	/**
	 * Replaces the Resource at the given position with the given Resource.
	 * The old Ref is transferred to the new Resource.
	 * @param index The list index to replace at
	 * @param res The new Resource
	 */
	public R set(int index, R res)
		{
		R old = super.set(index,res);
		old.reference.updateSource.removeListener(this);
		res.reference.updateSource.addListener(this);
		updateTrigger.fire();
		return old;
		}

	public int indexOf(R res)
		{
		int ind = -1;
		for (int i = 0; i < size(); i++)
			if (get(i) == res)
				{
				ind = i;
				break;
				}
		return ind;
		}
	}
