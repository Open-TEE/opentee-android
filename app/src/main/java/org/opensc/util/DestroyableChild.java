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

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;


/**
 * A class, that is a destroyable and a child of a DestroybleParent.
 * 
 * @author wglas
 */
public class DestroyableChild implements Destroyable
{
	DestroyableParent parent;

	/**
	 * Constructs a child with no parent.
	 */
	public DestroyableChild()
	{
		super();
		this.parent = null;
	}

	/**
	 * Constructs a child for a given parent.
	 */
	public DestroyableChild(DestroyableParent parent)
	{
		super();
		this.parent = parent;
		parent.register(this);
	}

	/**
	 * Used internally by DEstroyableHodler.destroy in order tp
	 * avoid double acces to a Colletion.
	 */
	protected final void unlink()
	{
		this.parent = null;
	}
	
	/* (non-Javadoc)
	 * @see javax.security.auth.Destroyable#destroy()
	 */
	public void destroy() throws DestroyFailedException
	{
		if (this.parent==null) return;
			
		this.parent.deregister(this);
		this.parent = null;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.Destroyable#isDestroyed()
	 */
	public boolean isDestroyed()
	{
		return this.parent==null;
	}

	/**
	 * @return Returns the parent.
	 */
	public DestroyableParent getParent()
	{
		return this.parent;
	}

}
