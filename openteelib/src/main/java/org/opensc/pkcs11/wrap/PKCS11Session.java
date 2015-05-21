/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 19, 2006
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

import javax.security.auth.DestroyFailedException;

import org.opensc.util.DestroyableHolder;
import org.opensc.util.Util;

/**
 * @author wglas
 *
 * This class represents an open session on a token.
 */
public class PKCS11Session extends DestroyableHolder
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
	protected long handle;

	private boolean userLoggedIn;
	
	private boolean SOLoggedIn;
	
	public static final int OPEN_MODE_READ_ONLY = 0;
	public static final int OPEN_MODE_READ_WRITE = 1;

	/**
	 * The counterpart of SKU_SO in pkcs11t.h, used to present the
	 * security officer PIN to the card.
	 */
	private static int LOGIN_TYPE_SO = 0;
	/**
	 * The counterpart of SKU_USER in pkcs11t.h, used to present the
	 * user PIN to the card.
	 */
	private static int LOGIN_TYPE_USER = 1;

	/**
	 * Contruct a session from a given handle-
	 */
	protected PKCS11Session(PKCS11Slot slot, long handle)
	{
		super(slot);
		this.pvh = slot.getPvh();
		this.shandle = slot.getHandle();
		this.handle = handle;
		this.userLoggedIn = false;
		this.SOLoggedIn = false;
	}

	private static native long openNative(long pvh, long shandle, int mode) throws PKCS11Exception;
	private static native void closeNative(long pvh, long shandle, long handle);

    private static native void signInitNative(long pvh, long shandle, long hsession, long hkey, int mech, byte[] param) throws PKCS11Exception;
    private static native void signUpdateNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
    private static native void signUpdateByteNative(long pvh, long shandle, long hsession, byte data) throws PKCS11Exception;
    private static native byte[] signFinalNative(long pvh, long shandle, long hsession) throws PKCS11Exception;
    private static native byte[] signNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
	
    private static native void verifyInitNative(long pvh, long shandle, long hsession, long hkey, int mech, byte[] param) throws PKCS11Exception;
    private static native void verifyUpdateNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
    private static native void verifyUpdateByteNative(long pvh, long shandle, long hsession, byte data) throws PKCS11Exception;
    private static native boolean verifyFinalNative(long pvh, long shandle, long hsession, byte[] signature) throws PKCS11Exception;
    private static native boolean verifyNative(long pvh, long shandle, long hsession, byte[] data, int off, int len, byte[] signature) throws PKCS11Exception;

    private static native void encryptInitNative(long pvh, long shandle, long hsession, long hkey, int pkcs11_alg, byte[] param) throws PKCS11Exception;
    private static native byte[] encryptUpdateNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
    private static native int encryptUpdateOffNative(long pvh, long shandle, long hsession, 
            byte[] input, int off, int len, byte[] output, int output_off) throws PKCS11Exception;
    private static native byte[] encryptFinalNative(long pvh, long shandle, long hsession) throws PKCS11Exception;
    private static native byte[] encryptNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;

    private static native void decryptInitNative(long pvh, long shandle, long hsession, long hkey, int pkcs11_alg, byte[] param) throws PKCS11Exception;
    private static native byte[] decryptUpdateNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
    private static native int decryptUpdateOffNative(long pvh, long shandle, long hsession, 
            byte[] input, int off, int len, byte[] output, int output_off) throws PKCS11Exception;
    private static native byte[] decryptFinalNative(long pvh, long shandle, long hsession) throws PKCS11Exception;
    private static native byte[] decryptNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
    
    /**
	 * Opens a session on the given slot.
	 * 
	 * @param slot The slot on which we open the session.
	 * @param mode Either OPEN_MODE_READ_ONLY or OPEN_MODE_READ_WRITE
	 * @return The open session.
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 module.
	 */
	public static PKCS11Session open(PKCS11Slot slot, int mode) throws PKCS11Exception
	{
		long handle = openNative(slot.getPvh(),slot.getHandle(),mode);
		return new PKCS11Session(slot,handle);
	}

	private native void loginNative(long _pvh, long _shandle, long _handle, int type, byte[] pin) throws PKCS11Exception;
	
	/**
	 * Presents the user PIN to the token. Should only be called after open().
	 * 
	 * @param pin The user pin. This paremeter may be <code>null</code>, if the
	 *            token has a protected authentication path.
	 *            
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 engine.
	 *            
	 * @see PKCS11Slot#hasTokenProtectedAuthPath()
	 */
	public void loginUser(char[] pin) throws PKCS11Exception
	{
		if (this.userLoggedIn)
			throw new PKCS11Exception("The user is already logged in.");

		loginNative(this.pvh,this.shandle,this.handle,LOGIN_TYPE_USER,Util.translatePin(pin));
		
        this.userLoggedIn = true;
	}
	
	/**
	 * Presents the security officer PIN to the token. Should only be called after open().
	 * 
	 * @param pin The SO pin. This paremeter may be <code>null</code>, if the
	 *            token has a protected authentication path.
	 *            
	 * @throws PKCS11Exception Upon errors of the underlying PKCS#11 engine.
	 *            
	 * @see PKCS11Slot#hasTokenProtectedAuthPath()
	 */
	public void loginSO(char[] pin) throws PKCS11Exception
	{
		if (this.SOLoggedIn)
			throw new PKCS11Exception("The security officer is already logged in.");
		
		loginNative(this.pvh,this.shandle,this.handle,LOGIN_TYPE_SO,Util.translatePin(pin));
		
        this.SOLoggedIn = true;
	}

	/**
	 * @return Returns, whether the security officer has successfully logged in
	 *         through loginSO().
	 */
	public boolean isSOLoggedIn()
	{
		return this.SOLoggedIn;
	}

	/**
	 * @return Returns, whether the user has successfully logged in
	 *         through loginUser().
	 */
	public boolean isUserLoggedIn()
	{
		return this.userLoggedIn;
	}
	
	private native void logoutNative(long _pvh, long _shandle, long _handle) throws PKCS11Exception;
	
	/**
	 * Logs out from the token.
	 */
	public void logout() throws PKCS11Exception
	{
		if (!this.userLoggedIn && ! this.SOLoggedIn) return;
		
		logoutNative(this.pvh,this.shandle,this.handle);
		
        this.userLoggedIn = false;
        this.SOLoggedIn = false;
	}

	/* (non-Javadoc)
	 * @see org.opensc.util.DestroyableChild#destroy()
	 */
	@Override
	public void destroy() throws DestroyFailedException
	{
		closeNative(this.pvh,this.shandle,this.handle);
        this.handle = 0;
        this.shandle = 0;
        this.pvh = 0;
        this.userLoggedIn = false;
        this.SOLoggedIn = false;
		super.destroy();
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
	protected long getSlotHandle()
	{
		return this.shandle;
	}
	
	/**
	 * @return Returns the C handle of the session.
	 */
	protected long getHandle()
	{
		return this.handle;
	}

    /**
     * Initialize signing operation (C_SignInit)
     * 
     * @param key   PKCS#11 key handle
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void signInit(int key, int mech, byte[] param) throws PKCS11Exception {
        signInitNative(this.pvh, this.shandle, this.handle, (long)key, mech, param);
    }

    /**
     * Initialize signing operation (C_SignInit)
     * 
     * @param key   PKCS#11 object representing a key
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void signInit(PKCS11Object key, int mech, byte[] param) throws PKCS11Exception {
        signInitNative(this.pvh, this.shandle, this.handle, key.getHandle(), mech, param);
    }

    /**
     * Update internal signing buffer (C_SignUpdate)
     * 
     * @param data  Partial data to be signed
     * @param off   Offset in data buffer
     * @param len   Length of data in buffer
     * @throws PKCS11Exception
     */
    public void signUpdate(byte[] data, int off, int len) throws PKCS11Exception {
        signUpdateNative(this.pvh, this.shandle, this.handle, data, off, len);
    }

    /**
     * Update internal signing buffer with single byte (C_SignUpdate)
     * 
     * @param data  Byte to add
     * @throws PKCS11Exception
     */
    public void signUpdate(byte data) throws PKCS11Exception {
        signUpdateByteNative(this.pvh, this.shandle, this.handle, data);
    }

    /**
     * End signing operation and generate signature (C_SignFinal)
     * @return Signature
     * @throws PKCS11Exception
     */
    public byte[] signFinal() throws PKCS11Exception {
        return signFinalNative(this.pvh, this.shandle, this.handle);
    }

    /**
     * Single step signing operation (C_Sign)
     * 
     * @param data  Data to be signed
     * @param off   Offset in data buffer
     * @param len   Length of data in buffer, starting at offset
     * @return      Signature
     * @throws PKCS11Exception
     */
    public byte[] sign(byte[] data, int off, int len) throws PKCS11Exception {
        return signNative(this.pvh, this.shandle, this.handle, data, off, len);
    }

    /**
     * Single step signing operation (C_Sign)
     * 
     * @param data  Data to be signed
     * @return Signature
     * @throws PKCS11Exception
     */
    public byte[] sign(byte[] data) throws PKCS11Exception {
        return sign(data, 0, data.length);
    }

    /**
     * Initialize verifying operation (C_VerifyInit)
     * 
     * @param key   PKCS#11 key handle
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void verifyInit(int key, int mech, byte[] param) throws PKCS11Exception {
        verifyInitNative(this.pvh, this.shandle, this.handle, (long)key, mech, param);
    }

    /**
     * Initialize verifying operation (C_VerifyInit)
     * 
     * @param key   PKCS#11 object representing a key
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void verifyInit(PKCS11Object key, int mech, byte[] param) throws PKCS11Exception {
        verifyInitNative(this.pvh, this.shandle, this.handle, key.getHandle(), mech, param);
    }

    /**
     * Update internal verifying buffer (C_VerifyUpdate)
     * 
     * @param data  Partial data to be signed
     * @param off   Offset in data buffer
     * @param len   Length of data in buffer
     * @throws PKCS11Exception
     */
    public void verifyUpdate(byte[] data, int off, int len) throws PKCS11Exception {
        verifyUpdateNative(this.pvh, this.shandle, this.handle, data, off, len);
    }

    /**
     * Update internal verifying buffer with single byte (C_VerifyUpdate)
     * 
     * @param data  Byte to add
     * @throws PKCS11Exception
     */
    public void verifyUpdate(byte data) throws PKCS11Exception {
        verifyUpdateByteNative(this.pvh, this.shandle, this.handle, data);
    }

    /**
     * End verifying operation and validate signature (C_VerifyFinal)
     * @param signature Signature to be verified
     * @return True if signature is valid
     * @throws PKCS11Exception
     */
    public boolean verifyFinal(byte[] signature) throws PKCS11Exception {
        return verifyFinalNative(this.pvh, this.shandle, this.handle, signature);
    }

    /**
     * Single step verifying operation (C_Verify)
     * 
     * @param data  Data to be verified
     * @param off   Offset in data buffer
     * @param len   Length of data in buffer, starting at offset
     * @param signature Signature to be verified
     * @return      True if signature is valid
     * @throws PKCS11Exception
     */
    public boolean verify(byte[] data, int off, int len, byte[] signature) throws PKCS11Exception {
        return verifyNative(this.pvh, this.shandle, this.handle, data, off, len, signature);
    }
    
    /**
     * Single step verifying operation (C_Verify)
     * 
     * @param data      Message to be verified
     * @param signature Signature to be verified
     * @return true if the signature is valid
     * @throws PKCS11Exception
     */
    public boolean verify(byte[] data, byte[] signature) throws PKCS11Exception {
        return verify(data, 0, data.length, signature);
    }

    /**
     * Initialize encryption operation (C_EncryptInit)
     * 
     * @param key   PKCS#11 key handle
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void encryptInit(int key, int mech, byte[] param) throws PKCS11Exception {
        encryptInitNative(this.pvh, this.shandle, this.handle, (long)key, mech, param);
    }
    
    /**
     * Initialize encryption operation (C_EncryptInit)
     * 
     * @param key   PKCS#11 object representing a key
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void encryptInit(PKCS11Object key, int mech, byte[] param) throws PKCS11Exception {
        encryptInitNative(this.pvh, this.shandle, this.handle, key.getHandle(), mech, param);
    }

    /**
     * Process next block in encryption operation (C_EncryptUpdate)
     * 
     * @param data  Data to encrypt
     * @param off   Offset in data
     * @param len   Length of region from offset
     * @return      Encrypted data
     * @throws PKCS11Exception
     */
    public byte[] encryptUpdate(byte[] data, int off, int len) throws PKCS11Exception {
        return encryptUpdateNative(this.pvh, this.shandle, this.handle, data, off, len);
    }
    
    /**
     * Process next block in encryption operation (C_EncryptUpdate)
     * 
     * @param input         Data to encrypt
     * @param off           Offset in data
     * @param len           Length of region from offset
     * @param output        Output buffer
     * @param output_off    Offset in output buffer
     * @return              Length of data in output
     * @throws PKCS11Exception
     */
    public int encryptUpdateOff(byte[] input, int off, int len, byte[] output, int output_off) throws PKCS11Exception {
        return encryptUpdateOffNative(this.pvh, this.shandle, this.handle, input, off, len, output, output_off);
    }

    /**
     * Perform final encryption step (C_EncryptFinal)
     * 
     * @return          Final encrypted block or null if none
     * @throws PKCS11Exception
     */
    public byte[] encryptFinal() throws PKCS11Exception {
        return encryptFinalNative(this.pvh, this.shandle, this.handle);
    }
    
    /**
     * Single step encryption operation (C_Encrypt)
     * 
     * @param data  Data to encrypt
     * @param off   Offset in data
     * @param len   Length of region from offset
     * @return      Encrypted data
     * @throws PKCS11Exception
     */
    public byte[] encrypt(byte[] data, int off, int len) throws PKCS11Exception {
        return encryptNative(this.pvh, this.shandle, this.handle, data, off, len);
    }

    /**
     * Initialize decryption operation (C_DecryptInit)
     * 
     * @param key   PKCS#11 key handle
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void decryptInit(int key, int mech, byte[] param) throws PKCS11Exception {
        decryptInitNative(this.pvh, this.shandle, this.handle, (long)key, mech, param);
    }
    
    /**
     * Initialize decryption operation (C_DecryptInit)
     * 
     * @param key   PKCS#11 object representing a key
     * @param mech  CKM_ mechanism
     * @param param Parameter for mechanims as plain byte array in machine dependent structure
     * @throws PKCS11Exception
     */
    public void decryptInit(PKCS11Object key, int mech, byte[] param) throws PKCS11Exception {
        decryptInitNative(this.pvh, this.shandle, this.handle, key.getHandle(), mech, param);
    }

    /**
     * Process next block in decryption operation (C_DecryptUpdate)
     * 
     * @param data  Data to decrypt
     * @param off   Offset in data
     * @param len   Length of region from offset
     * @return      Decrypted data
     * @throws PKCS11Exception
     */
    public byte[] decryptUpdate(byte[] data, int off, int len) throws PKCS11Exception {
        return decryptUpdateNative(this.pvh, this.shandle, this.handle, data, off, len);
    }
    
    /**
     * Process next block in decryption operation (C_DecryptUpdate)
     * 
     * @param input         Data to decrypt
     * @param off           Offset in data
     * @param len           Length of region from offset
     * @param output        Output buffer
     * @param output_off    Offset in output buffer
     * @return              Length of data in output
     * @throws PKCS11Exception
     */
    public int decryptUpdateOff(byte[] input, int off, int len, byte[] output, int output_off) throws PKCS11Exception {
        return decryptUpdateOffNative(this.pvh, this.shandle, this.handle, input, off, len, output, output_off);
    }

    /**
     * Perform final decryption step (C_DecryptFinal)
     * 
     * @return          Final decrypted block or null if none
     * @throws PKCS11Exception
     */
    public byte[] decryptFinal() throws PKCS11Exception {
        return decryptFinalNative(this.pvh, this.shandle, this.handle);
    }
    
    /**
     * Single step decryption operation (C_Decrypt)
     * 
     * @param data  Data to decrypt
     * @param off   Offset in data
     * @param len   Length of region from offset
     * @return      Decrypted data
     * @throws PKCS11Exception
     */
    public byte[] decrypt(byte[] data, int off, int len) throws PKCS11Exception {
        return decryptNative(this.pvh, this.shandle, this.handle, data, off, len);
    }
}
