/*
 * Copyright (C) 2006, 2007 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2008 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.file;

import java.util.Hashtable;

import org.lateralgm.resources.Resource;
import org.lateralgm.resources.ResourceReference;
import org.lateralgm.resources.Room;

public class RefList<R extends Resource<R>>
	{
	private Hashtable<Integer,ResRef<R>> rrt = new Hashtable<Integer,ResRef<R>>();
	private Class<R> clazz;
	private GmFile parent;

	public RefList(Class<R> clazz, GmFile parent)
		{
		this.clazz = clazz;
		this.parent = parent;
		}

	public ResourceReference<R> get(int id)
		{
		if (id < 0) return null;
		ResRef<R> rr = rrt.get(id);
		if (rr != null) return rr.reference;
		R r = null;
		try
			{
			if (clazz == Room.class)
				r = clazz.getConstructor(GmFile.class).newInstance(parent);
			else
				r = clazz.newInstance();
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		if (r != null)
			{
			rr = new ResRef<R>(r);
			rrt.put(id,rr);
			r.setId(id);
			return rr.reference;
			}
		return null;
		}

	private static class ResRef<R extends Resource<R>>
		{
		R resource;
		ResourceReference<R> reference;

		public ResRef(R res)
			{
			resource = res;
			reference = res.reference;
			}
		}
	}
