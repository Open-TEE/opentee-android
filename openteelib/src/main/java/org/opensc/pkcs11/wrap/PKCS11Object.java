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

package org.opensc.pkcs11.wrap;

import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.util.DestroyableChild;
import org.opensc.util.PKCS11Id;

import javax.security.auth.DestroyFailedException;

/**
 * @author wglas
 *
 * This class manages Objects like certificates or keys
 * stored on a PKCS11 device in a specific slot.
 */
public class PKCS11Object extends DestroyableChild implements PKCS11SessionChild
{
	/**
	 * The C handle of the provider.
	 */
	protected long pvh;
	
	/**
	 * The C handle of the slot.
	 */
	protected long shandle;

	/**
	 * The C handle of the session.
	 */
	protected long hsession;

	/**
	 * The C handle of the object.
	 */
	protected long handle;

	/**
	 * The Id of the object, i.e. the CKA_ID attribute value.
	 */
	private PKCS11Id id;
	
	/**
	 * The Id of the object, i.e. the CKA_ID attribute value.
	 */
	private String label;
	
	/*
	 * PKCS11 Class constants used for enumeration imported from pkcs11t.h
	 */
	static public final int CKO_CERTIFICATE   =    0x00000001;
	static public final int CKO_PUBLIC_KEY    =    0x00000002;
	static public final int CKO_PRIVATE_KEY   =    0x00000003;
	static public final int CKO_SECRET_KEY    =    0x00000004;
	
	/* internal native interface */
    private static native long[] enumObjectsNative(long pvh, long slot_handle, long hsession, PKCS11Attribute[] attrs) throws PKCS11Exception;
	private static native byte[] getAttributeNative(long pvh, long slot_handle, long hsession, long handle, int att) throws PKCS11Exception;
	private static native int getULongAttributeNative(long pvh, long slot_handle, long hsession, long handle, int att) throws PKCS11Exception;
	private static native boolean getBooleanAttributeNative(long pvh, long slot_handle, long hsession, long handle, int att) throws PKCS11Exception;
	private static native PKCS11Mechanism[] getAllowedMechanismsNative(long pvh, long slot_handle, long hsession, long handle) throws PKCS11Exception;
    private static native long createObjectNative(long pvh, long slot_handle, long hsession, PKCS11Attribute[] attrs) throws PKCS11Exception;
    
	/**
	 * Just a small wrapper around the native function.
	 * @param att The attribute type to receive.
	 * @return The raw value of the attribute.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	protected byte[] getRawAttribute(int att) throws PKCS11Exception
	{
		return  getAttributeNative(this.pvh,this.shandle,this.hsession,this.handle,att);
	}
	
	/**
	 * Just a small wrapper around the native function.
	 * @param att The attribute type to receive.
	 * @return The 4-byte integer value of the attribute.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	protected int getULongAttribute(int att) throws PKCS11Exception
	{
		return  getULongAttributeNative(this.pvh,this.shandle,this.hsession,this.handle,att);
	}
	
	/**
	 * Just a small wrapper around the native function.
	 * @param att The attribute type to receive.
	 * @return The 4-byte integer value of the attribute.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	protected static int getULongAttribute(PKCS11Session session, long handle, int att) throws PKCS11Exception
	{
		return  getULongAttributeNative(session.getPvh(),session.getSlotHandle(),session.getHandle(),handle,att);
	}
	
	/**
	 * Just a small wrapper around the native function.
	 * @param att The attribute type to receive.
	 * @return The boolean value of the attribute.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	protected boolean getBooleanAttribute(int att) throws PKCS11Exception
	{
		return  getBooleanAttributeNative(this.pvh,this.shandle,this.hsession,this.handle,att);
	}
	
	/**
	 * Just a small wrapper around the native function.
	 * @param att The attribute type to receive.
	 * @return The 4-byte integer value of the attribute.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	protected static boolean getBooleanAttribute(PKCS11Session session, long handle, int att) throws PKCS11Exception
	{
		return  getBooleanAttributeNative(session.getPvh(),session.getSlotHandle(),session.getHandle(),handle,att);
	}
	
    /**
     * Just a small wrapper around the native function.
     * @param session The session for which to enumerate the objects.
     * @param pkcs11_cls The object class to be seeked.
     *        Should be one of the CKO_* constants
     * @return The object handles of the retrieved objects,
     *         which have to be passed to the constructor.
     * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
     */
    protected static long[] enumRawObjects(PKCS11Session session, int pkcs11_cls) throws PKCS11Exception
    {
        PKCS11Attribute attrs[] = new PKCS11Attribute[1];
        attrs[0] = new PKCS11Attribute(PKCS11Attribute.CKA_CLASS,pkcs11_cls);
        
        return enumObjectsNative(session.getPvh(),session.getSlotHandle(),session.getHandle(),
                                 attrs);
    }
    
    /**
     * Just a small wrapper around the native function. Finds all objects in a session.
     * @param session The session for which to enumerate the objects.
     * @return The object handles of the retrieved objects,
     *         which have to be passed to the constructor.
     * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
     */
    protected static long[] enumRawObjects(PKCS11Session session) throws PKCS11Exception
    {
        PKCS11Attribute attrs[] = new PKCS11Attribute[0];
        
        return enumObjectsNative(session.getPvh(),session.getSlotHandle(),session.getHandle(),
                                 attrs);
    }
    
    /**
     * Just a small wrapper around the native function.
     * @param session The session for which to find an object.
     * @param pkcs11_cls The object class to be seeked.
     *        Should be one of the CKO_* constants.
     * @param id The object id to be searched.
     * @return The object handles of the retrieved objects,
     *         which have to be passed to the constructor.
     * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
     */
    protected static long findRawObject(PKCS11Session session, int pkcs11_cls, PKCS11Id id) throws PKCS11Exception
    {
        PKCS11Attribute attrs[] = new PKCS11Attribute[1];
        attrs[0] = new PKCS11Attribute(PKCS11Attribute.CKA_CLASS,pkcs11_cls);
//        attrs[1] = new PKCS11Attribute(PKCS11Attribute.CKA_ID,id);
        
//        String idString = "";
        
//        try {
//			idString = new String(attrs[1].getData(), "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        System.out.println("Id not specified " + idString);
        
        long[] handles = enumObjectsNative(session.getPvh(),session.getSlotHandle(),session.getHandle(),
                                           attrs);
        
        if (handles == null || handles.length < 1)
            throw new PKCS11Exception("The requested object with id "+id+" of class "+pkcs11_cls+" was not found.");

        if (handles.length > 1)
            throw new PKCS11Exception("There are more than one objects with id "+id+" of class "+pkcs11_cls+".");
        
        return handles[0];
    }
    
    /**
     * Just a small wrapper around the native function.
     * @param session The session in which to create the new objects.
     * @param attrs The attributes of the new object to save.
     * @return The object handle, which may be used by subclasses in order to construct
     *         a new specialized object like PKCS11Certificate.
     * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
     */
    protected static long createObject(PKCS11Session session, PKCS11Attribute[] attrs) throws PKCS11Exception
    {
        return createObjectNative(session.getPvh(),session.getSlotHandle(),session.getHandle(), attrs);
    }
    
    /**
	 * Protected contructor used by subclasses.
	 */
	protected PKCS11Object(PKCS11Session session, long handle) throws PKCS11Exception
	{
		super(session);
		this.pvh = session.getPvh();
		this.shandle = session.getSlotHandle();
		this.hsession = session.getHandle();
		this.handle = handle;
		
        // TODO code should move to derived class, as not all objects habe CKA_ID and CKA_LABEL
		try
		{
            this.id = new PKCS11Id(getRawAttribute(PKCS11Attribute.CKA_ID));
        }
        catch (PKCS11Exception pe) {
            if (pe.getErrorCode() == PKCS11Exception.CKR_ATTRIBUTE_TYPE_INVALID) {
                this.id = null;
            } else {
                throw pe;
            }
        }

        try {
			byte[] utf8_label = getRawAttribute(PKCS11Attribute.CKA_LABEL);
			this.label = new String(utf8_label,"UTF-8");
			
		} 
        catch (PKCS11Exception pe) {
            if (pe.getErrorCode() == PKCS11Exception.CKR_ATTRIBUTE_TYPE_INVALID) {
                this.label = null;
            } else {
                throw pe;
            }
        }
        catch (UnsupportedEncodingException e)
		{
			throw new PKCS11Exception("Invalid encoding:",e);
		}
	}

	/**
	 * Just a small wrapper atround the native function.
	 * @return The allowed mechanism for this object.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	public PKCS11Mechanism[] getAllowedMechanisms() throws PKCS11Exception
	{
		return getAllowedMechanismsNative(this.pvh,this.shandle,this.hsession,this.handle);
	}
	
	/**
	 * @return The Id of this object.
	 */
	public PKCS11Id getId()
	{
		return this.id;
	}
	
	/**
	 * @return The label of this object.
	 */
	public String getLabel()
	{
		return this.label;
	}
	
	/**
	 * @return The underlying PKCS11 security provider.
	 *         This function throws a runtime exception, if destroy()
	 *         has been called before.
	 */
	public PKCS11Provider getProvider()
	{
		DestroyableChild session = (DestroyableChild)getParent();
		DestroyableChild slot = (DestroyableChild)session.getParent();
		return (PKCS11Provider)slot.getParent();
	}
	
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.wrap.PKCS11SessionChild#getPvh()
	 */
	public long getPvh()
	{
		return this.pvh;
	}
	
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.wrap.PKCS11SessionChild#getSlotHandle()
	 */
	public long getSlotHandle()
	{
		return this.shandle;
	}
	
	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.wrap.PKCS11SessionChild#getSessionHandle()
	 */
	public long getSessionHandle()
	{
		return this.hsession;
	}

	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.wrap.PKCS11SessionChild#getHandle()
	 */
	public long getHandle()
	{
		return this.handle;
	}

	/* (non-Javadoc)
	 * @see org.opensc.util.DestroyableChild#destroy()
	 */
	@Override
	public void destroy() throws DestroyFailedException
	{
		// just invalidate the handles.
        this.pvh = 0;
        this.shandle = 0;
        this.hsession = 0;
        this.handle = 0;
		super.destroy();
	}

}
