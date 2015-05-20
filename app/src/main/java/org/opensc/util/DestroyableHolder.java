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

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;


/**
 * This class holds a map of destroyables, which will be
 * destroyed recursively.
 *
 * @author wglas
 */
public class DestroyableHolder extends DestroyableChild implements DestroyableParent
{
	private Set<Destroyable> children;

	/**
	 * Constructs a holder without a parent.
	 */
	public DestroyableHolder()
	{
		super();
		this.children = null;
	}

	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.util.DestroyableParent#register(javax.security.auth.Destroyable)
	 */
	public void register(Destroyable destroyable)
	{
		if (this.children == null)
			this.children = new HashSet<Destroyable>();
		
		this.children.add(destroyable);
	}
	
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.util.DestroyableParent#deregister(javax.security.auth.Destroyable)
	 */
	public void deregister(Destroyable destroyable)
	{
		if (this.children == null) return;
		
		this.children.remove(destroyable);
	}
	
	/**
	 * Constructs a holder with a parent.
	 */
	public DestroyableHolder(DestroyableParent parent)
	{
		super(parent);
		this.children = null;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.Destroyable#destroy()
	 */
	public void destroy() throws DestroyFailedException
	{
		if (this.children != null)
		{
			for (Destroyable destroyable : this.children)
			{
				if (destroyable.isDestroyed()) continue;
				
				if (destroyable instanceof DestroyableChild)
					((DestroyableChild)destroyable).unlink();
				
				destroyable.destroy();
			}
			
			this.children = null;
		}
		super.destroy();
	}

}
