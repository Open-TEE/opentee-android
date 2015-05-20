/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 23, 2006
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

package org.opensc.pkcs11.spi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.wrap.PKCS11Exception;
import org.opensc.pkcs11.wrap.PKCS11Mechanism;
import org.opensc.pkcs11.wrap.PKCS11PrivateKey;
import org.opensc.pkcs11.wrap.PKCS11PublicKey;
import org.opensc.pkcs11.wrap.PKCS11SessionChild;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * This class is the implementation of the cryptographic Cipher service
 * for the OpenSC PKCS#11 provider.
 * 
 * @author wglas
 */
public class PKCS11CipherSpi extends CipherSpi
{
	static Log log = LogFactory.getLog(PKCS11CipherSpi.class);
	
	PKCS11Provider provider;
	String algorithm;
	PKCS11SessionChild worker;
	PrivateKey privateKey;
	PublicKey publicKey;
	int mode;
	long count;

	/**
	 * Contructs an instance of PKCS11CipherSpi using the given provider
	 * and algorithm. Usually, you will not have to call this contructor,
	 * This class is implicitly instantiated using <tt>Cipher.getInstance</tt>
	 * 
	 * @see Cipher#getInstance(String, java.security.Provider)
	 * 
	 */
	public PKCS11CipherSpi(PKCS11Provider provider, String algorithm)
	{
		super();
		this.provider = provider;
		this.algorithm = algorithm;
		this.mode = 0;
		this.count = 0;
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineSetMode(java.lang.String)
	 */
	@Override
	protected void engineSetMode(String engineMode) throws NoSuchAlgorithmException
	{
		if (!engineMode.equals("ECB"))
			throw new NoSuchAlgorithmException("Only ECB mode is supported.");
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineSetPadding(java.lang.String)
	 */
	@Override
	protected void engineSetPadding(String padding) throws NoSuchPaddingException
	{
		if (!padding.equals("PKCS1Padding"))
			throw new NoSuchPaddingException("Only PKCS1Padding is supported.");
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineGetKeySize(java.security.Key)
	 */
	@Override
	protected int engineGetKeySize(Key key) throws InvalidKeyException
	{
		if (key instanceof PKCS11PrivateKey)
		{
			return ((PKCS11PrivateKey)key).getKeyBits();
		}
		
		if (key instanceof PKCS11PublicKey)
		{
			return ((PKCS11PublicKey)key).getKeyBits();
		}

		throw new InvalidKeyException("Invalid key class "+key.getClass());
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineGetBlockSize()
	 */
	@Override
	protected int engineGetBlockSize()
	{
		if (this.privateKey != null)
		{
			return ((PKCS11PrivateKey)this.privateKey).getKeyBits() / 8;
		}
		
		if (this.publicKey != null && this.publicKey instanceof PKCS11PublicKey)
		{
			return ((PKCS11PublicKey)this.publicKey).getKeyBits() / 8;
		}
		
		return 1;
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineGetOutputSize(int)
	 */
	@Override
	protected int engineGetOutputSize(int sz)
	{
		int block_sz = engineGetBlockSize();
		
		return (sz + block_sz - 1) / block_sz;
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineGetIV()
	 */
	@Override
	protected byte[] engineGetIV()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineGetParameters()
	 */
	@Override
	protected AlgorithmParameters engineGetParameters()
	{
		return null;
	}

	private int getPKCS11MechanismType() throws InvalidKeyException
	{
		int pkcs11_alg;
		
		if (this.algorithm.equals("RSA/ECB/PKCS1Padding")) 
			pkcs11_alg = PKCS11Mechanism.CKM_RSA_PKCS;
		else
			throw new InvalidKeyException("Signature algorithm ["+
                    this.algorithm+"] is unsupported.");
	
		return pkcs11_alg;
	}

	private native void initEncryptNative(long pvh, long shandle, long hsession, long handle, int pkcs11_alg) throws PKCS11Exception;
	private native void initDecryptNative(long pvh, long shandle, long hsession, long handle, int pkcs11_alg) throws PKCS11Exception;

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineInit(int, java.security.Key, java.security.SecureRandom)
	 */
	@Override
	protected void engineInit(int opmode, Key key, SecureRandom random)
			throws InvalidKeyException
	{
		if (opmode == Cipher.ENCRYPT_MODE)
		{
			if (! (key instanceof PKCS11SessionChild))
				throw new InvalidKeyException("PKCS11 signature engine expects a valid PKCS11 object.");

			if (!this.algorithm.startsWith(key.getAlgorithm()))
				throw new InvalidKeyException("PKCS11 key algorithm ["+
						key.getAlgorithm()+
						"] is incompatible with signature algorithm ["+
                        this.algorithm+"].");
			
			int pkcs11_alg = getPKCS11MechanismType();
			
			this.worker = (PKCS11SessionChild)key;
			
			if (key instanceof PublicKey)
			{
				this.publicKey = (PublicKey)key;
				this.privateKey = null;
			}
			else if (key instanceof PrivateKey)
			{
				this.publicKey = null;
				this.privateKey = (PrivateKey)key;
			}
			else
				throw new InvalidKeyException("PKCS11 signature engine expects a public or private key for encryption mode.");
	
			this.mode = opmode;
			
			try
			{
				initEncryptNative(this.worker.getPvh(),
                        this.worker.getSlotHandle(),this.worker.getSessionHandle(),
                        this.worker.getHandle(),pkcs11_alg);
				
			} catch (PKCS11Exception e)
			{
				throw new InvalidKeyException("PKCS11 exception initializing encryption:",e);
			}
		}
		else if (opmode == Cipher.DECRYPT_MODE)
		{
			if (! (key instanceof PKCS11SessionChild))
				throw new InvalidKeyException("PKCS11 signature engine expects a valid PKCS11 object.");
			
			if (!this.algorithm.startsWith(key.getAlgorithm()))
				throw new InvalidKeyException("PKCS11 key algorithm ["+
						key.getAlgorithm()+
						"] is incompatible with signature algorithm ["+
                        this.algorithm+"].");
			
			int pkcs11_alg = getPKCS11MechanismType();
			
			this.worker = (PKCS11SessionChild)key;
			if (key instanceof PublicKey)
			{
				this.publicKey = (PublicKey)key;
				this.privateKey = null;
			}
			else if (key instanceof PrivateKey)
			{
				this.publicKey = null;
				this.privateKey = (PrivateKey)key;
			}
			else
				throw new InvalidKeyException("PKCS11 signature engine expects a public or private key for decryption mode.");
			
			this.mode = opmode;

			try
			{
				initDecryptNative(this.worker.getPvh(),
                        this.worker.getSlotHandle(),this.worker.getSessionHandle(),
                        this.worker.getHandle(),pkcs11_alg);
				
			} catch (PKCS11Exception e)
			{
				throw new InvalidKeyException("PKCS11 exception initializing decryption:",e);
			}			
		}
		else
			throw new InvalidKeyException("Invalid operation mode ["+opmode+"] in PKCS11CipherSpi.engineInit().");
		
		this.count = 0;
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineInit(int, java.security.Key, java.security.spec.AlgorithmParameterSpec, java.security.SecureRandom)
	 */
	@Override
	protected void engineInit(int opmode, Key key, AlgorithmParameterSpec param,
			SecureRandom random) throws InvalidKeyException,
			InvalidAlgorithmParameterException
	{
		engineInit(opmode,key,random);
	}

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineInit(int, java.security.Key, java.security.AlgorithmParameters, java.security.SecureRandom)
	 */
	@Override
	protected void engineInit(int opmode, Key key, AlgorithmParameters param,
			SecureRandom random) throws InvalidKeyException,
			InvalidAlgorithmParameterException
	{
		engineInit(opmode,key,random);
	}

	private native byte[] updateDecryptNative(long pvh, long shandle, long hsession, long handle, byte[] data, int off, int len) throws PKCS11Exception;
	private native byte[] updateEncryptNative(long pvh, long shandle, long hsession, long handle, byte[] data, int off, int len) throws PKCS11Exception;
	
	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineUpdate(byte[], int, int)
	 */
	@Override
	protected byte[] engineUpdate(byte[] data, int off, int len)
	{
		try
		{
			this.count += len;
			
			if (this.mode == Cipher.DECRYPT_MODE)
				return updateDecryptNative(this.worker.getPvh(),this.worker.getSlotHandle(),
						this.worker.getSessionHandle(),this.worker.getHandle(),data,off,len);
			else
				return updateEncryptNative(this.worker.getPvh(),this.worker.getSlotHandle(),
						this.worker.getSessionHandle(),this.worker.getHandle(),data,off,len);
			
		} catch (PKCS11Exception e)
		{
			log.error("PKCS11Exception caught:",e);
		}
		
		return null;
	}

	private native int updateDecryptNativeOff(long pvh, long shandle, long hsession, long handle,
			byte[] input, int off, int len, byte[] output, int ouput_off) throws PKCS11Exception;
	private native int updateEncryptNativeOff(long pvh, long shandle, long hsession, long handle,
			byte[] input, int off, int len, byte[] output, int ouput_off) throws PKCS11Exception;

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineUpdate(byte[], int, int, byte[], int)
	 */
	@Override
	protected int engineUpdate(byte[] input, int off, int len, byte[] output,
			int output_off) throws ShortBufferException
	{
		try
		{
			this.count += len;
			
			if (this.mode == Cipher.DECRYPT_MODE)
				return updateDecryptNativeOff(this.worker.getPvh(),this.worker.getSlotHandle(),
						this.worker.getSessionHandle(),this.worker.getHandle(),
						input,off,len,output,output_off);
			else
				return updateEncryptNativeOff(this.worker.getPvh(),this.worker.getSlotHandle(),
						this.worker.getSessionHandle(),this.worker.getHandle(),
						input,off,len,output,output_off);
			
		} catch (PKCS11Exception e)
		{
			log.error("PKCS11Exception caught:",e);
			throw new ShortBufferException("PKCS11 exception:"+e);
		}
	}

	private native byte[] doFinalDecryptNative(long pvh, long shandle, long hsession, long handle, byte[] data, int off, int len) throws PKCS11Exception;
	private native byte[] doFinalEncryptNative(long pvh, long shandle, long hsession, long handle, byte[] data, int off, int len) throws PKCS11Exception;

	private native byte[] doDecryptNative(long pvh, long shandle, long hsession, long handle, byte[] data, int off, int len) throws PKCS11Exception;
	private native byte[] doEncryptNative(long pvh, long shandle, long hsession, long handle, byte[] data, int off, int len) throws PKCS11Exception;

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineDoFinal(byte[], int, int)
	 */
	@Override
	protected byte[] engineDoFinal(byte[] input, int off, int len)
			throws IllegalBlockSizeException, BadPaddingException
	{
		byte[] ret;
		
		try
		{
			if (this.mode == Cipher.DECRYPT_MODE)
				if (this.count == 0)
					ret = doDecryptNative(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),input,off,len);
				else
					ret = doFinalDecryptNative(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),input,off,len);
			else
				if (this.count == 0)
					ret = doEncryptNative(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),input,off,len);
				else
					ret = doFinalEncryptNative(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),input,off,len);
			
		} catch (PKCS11Exception e)
		{
			log.error("PKCS11Exception caught:",e);
			throw new IllegalBlockSizeException("PKCS11Exception caught:"+e);
		}
		this.count = 0;
		return ret;
	}

	private native int doFinalDecryptNativeOff(long pvh, long shandle, long hsession, long handle,
			byte[] input, int off, int len, byte[] output, int ouput_off) throws PKCS11Exception;
	private native int doFinalEncryptNativeOff(long pvh, long shandle, long hsession, long handle,
			byte[] input, int off, int len, byte[] output, int ouput_off) throws PKCS11Exception;

	private native int doDecryptNativeOff(long pvh, long shandle, long hsession, long handle,
			byte[] input, int off, int len, byte[] output, int ouput_off) throws PKCS11Exception;
	private native int doEncryptNativeOff(long pvh, long shandle, long hsession, long handle,
			byte[] input, int off, int len, byte[] output, int ouput_off) throws PKCS11Exception;

	/* (non-Javadoc)
	 * @see javax.crypto.CipherSpi#engineDoFinal(byte[], int, int, byte[], int)
	 */
	@Override
	protected int engineDoFinal(byte[] input, int off, int len, byte[] output,
			int output_off) throws ShortBufferException, IllegalBlockSizeException,
			BadPaddingException
	{
		int ret;
		
		try
		{
			if (this.mode == Cipher.DECRYPT_MODE)
				if (this.count == 0)
					ret = doDecryptNativeOff(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),
							input,off,len,output,output_off);
				else
					ret = doFinalDecryptNativeOff(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),
							input,off,len,output,output_off);
			else
				if (this.count == 0)
					ret = doEncryptNativeOff(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),
							input,off,len,output,output_off);
				else
					ret = doFinalEncryptNativeOff(this.worker.getPvh(),this.worker.getSlotHandle(),
							this.worker.getSessionHandle(),this.worker.getHandle(),
							input,off,len,output,output_off);
			
		} catch (PKCS11Exception e)
		{
			log.error("PKCS11Exception caught:",e);
			throw new ShortBufferException("PKCS11 exception:"+e);
		}
		
		this.count = 0;
		return ret;
	}

}
