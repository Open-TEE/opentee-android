/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 17, 2006
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 * 
 ***********************************************************/

package org.opensc.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * Utilitiy functions for implementing PKCS11 wrappers.
 *
 * @author wglas
 */
public class Util
{
	/**
	 * private constructor, because we only define static public methods.
	 */
	private Util()
	{
		super();
	}
	
	/**
	 * Translate a character array to an utf-8 encoded byte array.
	 * 
	 * @param pin The chracter array.
	 * @return The UTF-8 encoded byte-equivalent.
	 */
	public static byte[] translatePin(char[] pin)
	{
		if (pin == null) return null;
		Charset charset = Charset.forName("UTF-8");
		CharBuffer cb = CharBuffer.wrap(pin);
		ByteBuffer bb = charset.encode(cb);
		return bb.array();
	}

}
