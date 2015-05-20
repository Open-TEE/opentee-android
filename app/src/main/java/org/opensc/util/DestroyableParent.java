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

import javax.security.auth.Destroyable;

/**
 * This interface is omplemented by all classes, which act as a parent
 * for Destroyables.
 * 
 * @author wglas
 */
public interface DestroyableParent
{

	/**
	 * Register a Destroyable for recursive destruction.
	 * 
	 * @param destroyable The child to be registered.
	 */
	public void register(Destroyable destroyable);

	/**
	 * Deregister a Destroyable from recursive destruction.
	 * 
	 * @param destroyable The child to be deregistered.
	 */
	public void deregister(Destroyable destroyable);

}