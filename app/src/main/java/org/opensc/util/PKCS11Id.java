/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jan 28, 2007
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

import java.io.Serializable;

/**
 * This class represents a PKCS11 Id of an object, which is stored on the token.
 * Basically, it encapsulates a byte array and supports additional implementations
 * of equals and hasCode suitable for using the object as a key in hash maps etc.
 * 
 * @author wglas
 */
public final class PKCS11Id implements Serializable
{
    private static final long serialVersionUID = -8611698105484679940L;

    private static char[] digits =
    { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
    
    private byte[] data;
    
    /**
     * Contruct a null PKCS11Id object.
     */
    public PKCS11Id()
    {
        this.data = null;
    }
    
    /**
     * Contruct a null PKCS11Id object from a given byte array.
     */
    public PKCS11Id(byte[] data)
    {
        this.data = data;
    }

    /**
     * @return Returns the data.
     */
    public byte[] getData()
    {
        return this.data;
    }

    /**
     * @param data The data to set.
     */
    public void setData(byte[] data)
    {
        this.data = data;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PKCS11Id))
            return false;
        
        PKCS11Id a = (PKCS11Id)obj; 
        
        byte[] adata = a.getData();
        
        if (adata == null)
            return this.data == null;
        
        if (this.data == null)
            return false;
        
        if (this.data.length != adata.length) return false;
        
        for (int i=0; i<this.data.length;++i)
        {
            if (this.data[i] != adata[i]) return false;
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        if (this.data == null) return -1;
        
        int ret = 0;
        
        for (int i = 0; i<this.data.length; i++)
        {
            int x = (((int)this.data[i]) & 0xff) << ((i%4)*8);
            
            ret ^= x;
        }
        
        return ret;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        if (this.data == null) return null;
        
        StringBuffer sb = new StringBuffer();
        
        for (int i = 0; i<this.data.length; i++)
        {
            int b = (int) this.data[i];
            char c = digits[(b >> 4) & 0xf];
            sb.append(c);
            c = digits[b & 0xf];
            sb.append(c);
        }
        
        return sb.toString();
    }
    
    
    
    
}
