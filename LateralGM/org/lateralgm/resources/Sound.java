/*
 * Copyright (C) 2006 Clam <ebordin@aapt.net.au>
 * Copyright (C) 2007 IsmAvatar <cmagicj@nni.com>
 * Copyright (C) 2008, 2009 Quadduc <quadduc@gmail.com>
 * 
 * This file is part of LateralGM.
 * LateralGM is free software and comes with ABSOLUTELY NO WARRANTY.
 * See LICENSE for details.
 */

package org.lateralgm.resources;

import java.util.EnumMap;

import org.lateralgm.file.ResourceList;
import org.lateralgm.main.Prefs;
import org.lateralgm.util.PropertyMap;

public class Sound extends Resource<Sound,Sound.PSound>
	{
	public byte[] data = new byte[0];

	public enum SoundKind
		{
		NORMAL,BACKGROUND,SPATIAL,MULTIMEDIA
		}

	public enum PSound
		{
		KIND,FILE_TYPE,FILE_NAME,CHORUS,ECHO,FLANGER,GARGLE,REVERB,VOLUME,PAN,PRELOAD
		}

	private static final EnumMap<PSound,Object> DEFS = PropertyMap.makeDefaultMap(PSound.class,
			SoundKind.NORMAL,"","",false,false,false,false,false,1.0,0.0,true);

	public Sound()
		{
		this(null,true);
		}

	public Sound(ResourceReference<Sound> r, boolean update)
		{
		super(r,update);
		setName(Prefs.prefixes.get(Kind.SOUND));
		}

	@Override
	protected Sound copy(ResourceList<Sound> src, ResourceReference<Sound> ref, boolean update)
		{
		Sound s = new Sound(ref,update);
		copy(src,s);
		s.data = new byte[data.length];
		System.arraycopy(data,0,s.data,0,data.length);
		return s;
		}

	public Kind getKind()
		{
		return Kind.SOUND;
		}

	@Override
	protected PropertyMap<PSound> makePropertyMap()
		{
		return new PropertyMap<PSound>(PSound.class,this,DEFS);
		}
	}
