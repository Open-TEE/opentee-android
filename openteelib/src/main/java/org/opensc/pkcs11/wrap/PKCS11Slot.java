/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 * 
 * Copyright (C) 2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 16, 2006
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.DestroyFailedException;

import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.util.DestroyableHolder;

public class PKCS11Slot extends DestroyableHolder
{

	/**
	 * The ID of the token.
	 */
	private long id;
	
	/**
	 * The C handle of the provider.
	 */
	private long pvh;
	
	/**
	 * The C handle of the slot.
	 */
	private long handle;
	
	private native long initSlotNative(long _pvh, long _id) throws PKCS11Exception;
	private native void destroySlotNative(long _pvh, long _handle) throws DestroyFailedException;
	
	/**
	 * This contructor constructs an instance of an individual slot.
	 * The slots a usually label starting with an Id of 0 and onwards.
	 * So, if you have just one device attached to your computer you
	 * should usually done by calling new PKCS11Slot(provider,0).
	 * 
	 * @param id The Id of the slot.
	 * @throws PKCS11Exception Upon errors when retrieving the slot information.
	 */
	public PKCS11Slot(PKCS11Provider provider, long id) throws PKCS11Exception
	{
		super(provider);
		this.id = id;
		this.pvh = provider.getPkcs11ModuleHandle();
		this.handle = initSlotNative(this.pvh,id);
	}
	
	private static native long[] enumerateSlotsNative(long pvh) throws PKCS11Exception;

	/**
	 * Enumeraate all available slots of a given PKCS11 provider.
	 * 
	 * @param provider The PKCS11 provider to retrieve the slots for.
	 * @return A list of all available slots.
	 * @throws PKCS11Exception Upon errors when retrieving the slot information.
	 */
	public static List<PKCS11Slot> enumerateSlots(PKCS11Provider provider) throws PKCS11Exception
	{
		long[] ids = enumerateSlotsNative(provider.getPkcs11ModuleHandle());
		
		List<PKCS11Slot> ret = new ArrayList<PKCS11Slot> (ids.length);
		
		for (int i = 0; i < ids.length; i++)
		{
			ret.add(new PKCS11Slot(provider, ids[i]));
		}
		return ret;
	}
	
	private static native long waitForSlotNative(long pvh) throws PKCS11Exception;

	/**
	 * Enumerate all available slots of a given PKCS11 provider.
	 * 
	 * @param provider The PKCS11 provider to retrieve the slots for.
	 * @return A list of all available slots.
	 * @throws PKCS11Exception Upon errors when retrieving the slot information.
	 */
	public static PKCS11Slot waitForSlot(PKCS11Provider provider)
			throws PKCS11Exception
	{
		long id = -1;

		try
		{
			id = waitForSlotNative(provider.getPkcs11ModuleHandle());
		} catch (PKCS11Exception e)
		{
			if (e.getErrorCode() == PKCS11Exception.CKR_FUNCTION_NOT_SUPPORTED)
				try
				{
					PKCS11Slot ret = null;

					do
					{
						Thread.sleep(1000);

						List<PKCS11Slot> slots = enumerateSlots(provider);

						for (PKCS11Slot slot : slots)
						{
							if (ret == null && slot.isTokenPresent())
								ret = slot;
							else
								try
								{
									slot.destroy();
								} catch (DestroyFailedException e1)
								{
									android.util.Log.w("PKCS11Slot", "destroy error while waiting for slot:" + e1);
								}
						}
					} while (ret == null);
					
					return ret;
					
				} catch (InterruptedException e1)
				{
					throw new PKCS11Exception(
							PKCS11Exception.CKR_FUNCTION_CANCELED,
							"The operation has been interrupted.");
				}
			else
			{
				throw e;
			}
		}

		return new PKCS11Slot(provider, id);
	}
	
	private native boolean isTokenPresentNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return Whether a token is present in this slot.
	 */
	public boolean isTokenPresent() throws PKCS11Exception
	{
		return isTokenPresentNative(this.pvh,this.handle);
	}
	
	private native boolean isRemovableDeviceNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return Whether a token is present in this slot.
	 */
	public boolean isRemovableDevice() throws PKCS11Exception
	{
		return isRemovableDeviceNative(this.pvh,this.handle);
	}
	
	private native boolean isHardwareDeviceNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return Whether a token is present in this slot.
	 */
	public boolean isHardwareDevice() throws PKCS11Exception
	{
		return isHardwareDeviceNative(this.pvh,this.handle);
	}
	
	private native byte[] getManufacturerNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The manufacturer of the slot.
	 */
	public String getManufacturer() throws PKCS11Exception
	{
		try
		{
			return new String(getManufacturerNative(this.pvh,this.handle),"UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	private native byte[] getDescriptionNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return A description of the slot.
	 */
	public String getDescription() throws PKCS11Exception
	{
		try
		{
			return new String(getDescriptionNative(this.pvh,this.handle),"UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	private native double getHardwareVersionNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The hardware verion of the slot.
	 */
	public double getHardwareVersion() throws PKCS11Exception
	{
		return getHardwareVersionNative(this.pvh,this.handle);
	}
	
	private native double getFirmwareVersionNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The Firmware verion of the slot.
	 */
	public double getFirmwareVersion() throws PKCS11Exception
	{
		return getFirmwareVersionNative(this.pvh,this.handle);
	}
	
	private native PKCS11Mechanism[] getMechanismsNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return A list of mechanisms supported by this slot.
	 * @throws PKCS11Exception
	 */
	public PKCS11Mechanism[] getMechanisms() throws PKCS11Exception
	{
		return getMechanismsNative(this.pvh,this.handle);
	}
	
	private native byte[] getTokenLabelNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The label of the token.
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()                         
	 */
	public String getTokenLabel() throws PKCS11Exception
	{
		try
		{
			return new String(getTokenLabelNative(this.pvh,this.handle),"UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	private native byte[] getTokenManufacturerNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The manufacturer of the token.
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()                         
	 */
	public String getTokenManufacturer() throws PKCS11Exception
	{
		try
		{
			return new String(getTokenManufacturerNative(this.pvh,this.handle),"UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
	
	private native byte[] getTokenModelNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The model of the token.
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()                         
	 */
	public String getTokenModel() throws PKCS11Exception
	{
		try
		{
			return new String(getTokenModelNative(this.pvh,this.handle),"UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
		
	private native byte[] getTokenSerialNumberNative(long _pvh, long _handle) throws PKCS11Exception;
	
	/**
	 * @return The serial number of the token.
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()                         
	 */
	public String getTokenSerialNumber() throws PKCS11Exception
	{
		try
		{
			return new String(getTokenSerialNumberNative(this.pvh,this.handle),"UTF-8");
		} catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}
			
	private native int getTokenMinPinLenNative(long _pvh, long _handle) throws PKCS11Exception;

	/**
	 * @return The minimal PIN length of the token.
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()                         
	 */
	public int getTokenMinPinLen() throws PKCS11Exception
	{
		
		return getTokenMinPinLenNative(this.pvh,this.handle);
	}
			
	private native int getTokenMaxPinLenNative(long _pvh, long _handle) throws PKCS11Exception;

	/**
	 * @return The maximal PIN length of the token.
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()                         
	 */
	public int getTokenMaxPinLen() throws PKCS11Exception
	{
		
		return getTokenMaxPinLenNative(this.pvh,this.handle);
	}
			
	private native boolean hasTokenProtectedAuthPathNative(long _pvh, long _handle) throws PKCS11Exception;

	/**
	 * Checks, if the token has an protected authentication path via a PINpad
	 * or another hardware authentication method.
	 * 
	 * @return Whether the token has a protected authentication path.
	 *         If <code>true</code>, PIN parameters passed to the login
	 *         functions of an associated session may be null.
	 *         
	 * @throws PKCS11Exception When no token is in the slot or another
	 *                         error of the underlying PKCS#11 engine occurrs.
	 *                         
	 * @see PKCS11Slot#isTokenPresent()       
	 * @see PKCS11Session#loginUser(char[])
	 * @see PKCS11Session#loginSO(char[])               
	 */
	public boolean hasTokenProtectedAuthPath() throws PKCS11Exception
	{
		return hasTokenProtectedAuthPathNative(this.pvh,this.handle);
	}
			
	/**
	 * @return Returns the id of this slot.
	 */
	public long getId()
	{
		return this.id;
	}

	/* (non-Javadoc)
	 * @see org.opensc.pkcs11.util.DestroyableChild#destroy()
	 */
	@Override
	public void destroy() throws DestroyFailedException
	{
		super.destroy();

		if (this.handle != 0)
		{
			destroySlotNative(this.pvh,this.handle);
            this.handle = 0;
		}
	}
	
	/**
	 * @return Returns the C handle of the underlying provider.
	 */
	protected long getPvh()
	{
		return this.pvh;
	}
	
	/**
	 * @return Returns the C handle of the slot.
	 */
	protected long getHandle()
	{
		return this.handle;
	}
	
}
