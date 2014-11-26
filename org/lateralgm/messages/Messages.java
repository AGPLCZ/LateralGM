/*
 * Copyright (C) 2007, 2008 Quadduc <quadduc@gmail.com>
 * Copyright (C) 2013, 2014 Robert B. Colton
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

package org.lateralgm.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.lateralgm.main.Prefs;

public final class Messages
	{
	private static final String BUNDLE_NAME = "org.lateralgm.messages.messages"; //$NON-NLS-1$
	private static final String KEYBOARD_BUNDLE_NAME = "org.lateralgm.messages.keyboard"; //$NON-NLS-1$
	
	// NOTE: See comments about locale below.
	private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("",""));
	private static ResourceBundle KEYBOARD_BUNDLE = ResourceBundle.getBundle(KEYBOARD_BUNDLE_NAME);
	
	private static boolean prefsApplied = false;
	
	private Messages()
		{

		}

	//TODO: This method is exceedingly verbose, and we also need a way for users to install their own language packages.
	public static void updateLangPack()
		{
		String langbundle = "org.lateralgm.messages.messages";
		if (Prefs.languageName.contains("English"))
			{
			langbundle = "org.lateralgm.messages.messages"; //$NON-NLS-1$
			}
		else if (Prefs.languageName.contains("French"))
			{
			langbundle = "org.lateralgm.messages.messages_fr"; //$NON-NLS-1$
			}
		else if (Prefs.languageName.contains("Turkish"))
			{
			langbundle = "org.lateralgm.messages.messages_tr_TR"; //$NON-NLS-1$
			}
		else if (Prefs.languageName.contains("Danish"))
			{
			langbundle = "org.lateralgm.messages.messages_da"; //$NON-NLS-1$
			}
		// The bogus locale stops the Operating System Locale from overriding the preference the user
		// has selected, this was reported by egofree where his OS had French but he wanted all English translations.
		// Another way to solve this was to rename the default messages bundle with the postfix "_en_US"
		RESOURCE_BUNDLE = ResourceBundle.getBundle(langbundle, new Locale("",""));
		}

	public static String getString(String key)
		{
		// need to apply the language pack from preferences if we have not done so
		// because of static loaded messages like for the resource names on the tree
		//TODO: Find a better way to do this, ask IsmAvatar if we should be loading messages statically.
		if (!prefsApplied) updateLangPack();
		try
			{
			return RESOURCE_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}
	
	public static String getKeyboardString(String key)
		{
		try
			{
			return KEYBOARD_BUNDLE.getString(key);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}

	public static String format(String key, Object...arguments)
		{
		try
			{
			String p = RESOURCE_BUNDLE.getString(key);
			return MessageFormat.format(p,arguments);
			}
		catch (MissingResourceException e)
			{
			return '!' + key + '!';
			}
		}
	}
