/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 21, 2006
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

package org.opensc.pkcs11.wrap;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import org.opensc.util.PKCS11Id;

/**
 * @author wglas
 *
 * This class represents a private key stored on a hardware token.
 */
public class PKCS11PrivateKey extends PKCS11Key implements PrivateKey
{
	/**
	 * To be changed upon class layout change.
	 */
	private static final long serialVersionUID = 930054758668115904L;

	boolean extractable;
	boolean sensitive;

	/**
	 * @param session The session to which this key belongs.
	 * @param handle The handle as returned by @see PKCS11Object#enumRawObjects(PKCS11Session, int).
	 * @throws PKCS11Exception
	 */
	protected PKCS11PrivateKey(PKCS11Session session, int type, boolean extractable, long handle)
			throws PKCS11Exception
	{
		super(session,type,handle);
		
		this.extractable = extractable;		
//		this.sensitive = super.getBooleanAttribute(PKCS11Attribute.CKA_SENSITIVE);
	}

    private static PKCS11PrivateKey makePrivateKey(PKCS11Session session, long handle, int keyType, boolean extractable) throws PKCS11Exception
    {
        //
        // Well the rationale behind all this code below is, that we
        // have to export non-extractable keys as a plain PrivateKey
        // implementation, because the interfaces {RSA,DSA}PrivateKey
        // grant access to all private information of the private key.
        //
        // Moreover, the delayed provider selection described in
        // http://java.sun.com/j2se/1.5.0/docs/guide/security/p11guide.html
        // come to the end, that implementations of {RSA,DSA}PrivateKey
        // are supported by the SunRSA provider, which causes the signature to
        // fail lateron, because the the private informations are null.
        // (e.g. RSAPrivateKey.getPrivateExponent() == null).
        //
        // This would render the private keys unusable for SSL peer
        // authentication, so this is another argument for exporting a
        // non-extractable key as an implementation of the plain PrivateKey
        // interface.
        //
        switch (keyType)
        {
        case CKK_RSA:
            if (extractable)
                return new PKCS11RSAPrivateKey(session,handle);
            else
                return new PKCS11NeRSAPrivateKey(session,handle);
                
        case CKK_DSA:
            if (extractable)
                return new PKCS11DSAPrivateKey(session,handle);
            else
                return new PKCS11NeDSAPrivateKey(session,handle);
            
        default:
            return new PKCS11PrivateKey(session,keyType,extractable, handle);
        }
        
    }
    
	/**
	 * Fetches all private keys stored in the specified slot.
	 * 
	 * @param session The session of which to find the certificates. 
	 * @return The list of all private keys found in this slot.
	 * @throws PKCS11Exception Upon errors from the underlying PKCS11 module.
	 */
	public static List<PKCS11PrivateKey> getPrivateKeys(PKCS11Session session) throws PKCS11Exception
	{
		long[] handles = enumRawObjects(session,PKCS11Object.CKO_PRIVATE_KEY);
		
		List<PKCS11PrivateKey> ret = new ArrayList<PKCS11PrivateKey>(handles.length);
		
		for (int i = 0; i < handles.length; i++)
		{
			int keyType =
				PKCS11Object.getULongAttribute(session,handles[i],PKCS11Attribute.CKA_KEY_TYPE);
			
			boolean extractable=
				PKCS11Object.getBooleanAttribute(session,handles[i],PKCS11Attribute.CKA_EXTRACTABLE);

			PKCS11PrivateKey key= makePrivateKey(session, handles[i], keyType, extractable);
			ret.add(key);
		}
		return ret;
	}

    /**
     * Get the private key with the given id from the session.
     * 
     * @param session The session of which to find a private key.
     * @param id The Id of the key to be searched.
     * @return The private key with the given id.
     * @throws PKCS11Exception Upon error on the underlying PKCS11 module or
     *                         when the key could not be found. 
     */
    public static PKCS11PrivateKey findPrivateKey(PKCS11Session session, PKCS11Id id) throws PKCS11Exception
    {
        long handle = findRawObject(session, PKCS11Object.CKO_PRIVATE_KEY, id);
        
        int keyType = PKCS11Object.getULongAttribute(session,handle,PKCS11Attribute.CKA_KEY_TYPE);
        
        boolean extractable= false;
//            PKCS11Object.getBooleanAttribute(session,handle,PKCS11Attribute.CKA_EXTRACTABLE);

        return makePrivateKey(session,handle,keyType,extractable);
    }
    
    /**
     * @return The matching public key, if it is stored on the token.
     * @throws PKCS11Exception upon errors of the underlying PKCS#11 module or when
     *              the crresponding public key could not be found on the token.
     */
    public PKCS11PublicKey getPublicKey() throws PKCS11Exception
    {
        return PKCS11PublicKey.findPublicKey((PKCS11Session)this.getParent(), this.getId());
    }
    
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.wrap.PKCS11Key#getFormat()
	 */
	@Override
	public String getFormat()
	{
		return null;
	}

	/**
	 * @return Returns whether the key is extractable.
	 */
	public boolean isExtractable()
	{
		return this.extractable;
	}

	/**
	 * @return Returns whether the key is extractable.
	 */
	public boolean isSensitive()
	{
		return this.sensitive;
	}
	
	
}
