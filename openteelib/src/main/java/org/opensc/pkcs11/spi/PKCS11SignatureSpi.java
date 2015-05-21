/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 22, 2006
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

import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.wrap.PKCS11Exception;
import org.opensc.pkcs11.wrap.PKCS11Mechanism;
import org.opensc.pkcs11.wrap.PKCS11SessionChild;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.SignatureSpi;

/**
 * The signature service of the OpenSC PKCS#11 provider.
 *
 * @author wglas
 */
public class PKCS11SignatureSpi extends SignatureSpi
{
	PKCS11Provider provider;
	String algorithm;
	PKCS11SessionChild worker;
	PrivateKey privateKey;
	PublicKey publicKey;
	
	private native void initSignNative(long pvh, long shandle, long hsession, long hkey, int algo) throws PKCS11Exception;
	private native void updateSignNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
	private native void updateSignNative1(long pvh, long shandle, long hsession, byte data) throws PKCS11Exception;
	private native byte[] signNative(long pvh, long shandle, long hsession) throws PKCS11Exception;
	
	private native void initVerifyNative(long pvh, long shandle, long hsession, long hkey, int algo) throws PKCS11Exception;
	private native void updateVerifyNative(long pvh, long shandle, long hsession, byte[] data, int off, int len) throws PKCS11Exception;
	private native void updateVerifyNative1(long pvh, long shandle, long hsession, byte data) throws PKCS11Exception;
	private native boolean verifyNative(long pvh, long shandle, long hsession, byte[] data) throws PKCS11Exception;
	
	/**
	 * Contructs an instance of PKCS11SignatureSpi using the given provider
	 * and algorithm. Usually, you will not have to call this contructor,
	 * This class is implicitly instantiated using <tt>Signature.getInstance()</tt>.
	 * 
	 * @see java.security.Signature#getInstance(String, java.security.Provider)
	 */
	public PKCS11SignatureSpi(PKCS11Provider provider, String algorithm)
	{
		super();
		this.provider = provider;
		this.algorithm = algorithm;
	}

	private int getPKCS11MechanismType() throws InvalidKeyException
	{
		int pkcs11_alg;
		
		if (this.algorithm.equals("NONEwithRSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_RSA_PKCS;
		else if (this.algorithm.equals("MD5withRSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_MD5_RSA_PKCS;
		else if (this.algorithm.equals("SHA1withRSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_SHA1_RSA_PKCS;
		else if (this.algorithm.equals("SHA256withRSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_SHA256_RSA_PKCS;
		else if (this.algorithm.equals("SHA384withRSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_SHA384_RSA_PKCS;
		else if (this.algorithm.equals("SHA512withRSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_SHA512_RSA_PKCS;
		else if (this.algorithm.equals("SHA1withDSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_DSA_SHA1;
		else if (this.algorithm.equals("NONEwithDSA")) 
			pkcs11_alg = PKCS11Mechanism.CKM_DSA;
		else
			throw new InvalidKeyException("Signature algorithm ["+
                    this.algorithm+"] is unsupported.");
	
		return pkcs11_alg;
	}
	
	
	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineInitVerify(java.security.PublicKey)
	 */
	@Override
	protected void engineInitVerify(PublicKey pubKey) throws InvalidKeyException
	{
		if (! (pubKey instanceof PKCS11SessionChild))
			throw new InvalidKeyException("PKCS11 signature engine expects a valid PKCS11 object.");
		
		if (!this.algorithm.endsWith(pubKey.getAlgorithm()))
			throw new InvalidKeyException("PKCS11 key algorithm ["+
					pubKey.getAlgorithm()+
					"] is incompatible with signature algorithm ["+
                    this.algorithm+"].");

		int pkcs11_alg = getPKCS11MechanismType();
		
		this.worker = (PKCS11SessionChild)pubKey;
		this.publicKey = pubKey;
		this.privateKey = null;
		
		try
		{
			initVerifyNative(this.worker.getPvh(),
                    this.worker.getSlotHandle(),this.worker.getSessionHandle(),
                    this.worker.getHandle(),pkcs11_alg);
			
		} catch (PKCS11Exception e)
		{
			throw new InvalidKeyException("PKCS11 exception",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineInitSign(java.security.PrivateKey, java.security.SecureRandom)
	 */
	@Override
	protected void engineInitSign(PrivateKey privKey, SecureRandom random) throws InvalidKeyException
	{
		this.engineInitSign(privKey);
	}
	
	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineInitSign(java.security.PrivateKey)
	 */
	@Override
	protected void engineInitSign(PrivateKey privKey) throws InvalidKeyException
	{
		if (! (privKey instanceof PKCS11SessionChild))
			throw new InvalidKeyException("PKCS11 signature engine expects a valid PKCS11 object.");
			
		if (!this.algorithm.endsWith(privKey.getAlgorithm()))
			throw new InvalidKeyException("PKCS11 key algorithm ["+
					privKey.getAlgorithm()+
					"] is incompatible with signature algorithm ["+
                    this.algorithm+"].");

		int pkcs11_alg = getPKCS11MechanismType();
		this.worker = (PKCS11SessionChild)privKey;
		this.publicKey = null;
		this.privateKey = privKey;
		
		try
		{
			initSignNative(this.worker.getPvh(),
                    this.worker.getSlotHandle(),this.worker.getSessionHandle(),
                    this.worker.getHandle(),pkcs11_alg);
			
		} catch (PKCS11Exception e)
		{
			throw new InvalidKeyException("PKCS11 exception",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineUpdate(byte)
	 */
	@Override
	protected void engineUpdate(byte b) throws SignatureException
	{
		if (this.worker == null)
			throw new SignatureException("Signature not initialized through initSign() or initVerify().");
		
		try
		{
			if (this.privateKey != null)
				updateSignNative1(this.worker.getPvh(),
                        this.worker.getSlotHandle(),this.worker.getSessionHandle(),b);
			else
				updateVerifyNative1(this.worker.getPvh(),
                        this.worker.getSlotHandle(),this.worker.getSessionHandle(),b);
				
		} catch (PKCS11Exception e)
		{
			throw new SignatureException("PKCS11 exception",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineUpdate(byte[], int, int)
	 */
	@Override
	protected void engineUpdate(byte[] data, int off, int len)
			throws SignatureException
	{
		if (this.worker == null)
			throw new SignatureException("Signature not initialized through initSign() or initVerify().");

		try
		{
			if (this.privateKey != null)
				updateSignNative(this.worker.getPvh(),
                        this.worker.getSlotHandle(),this.worker.getSessionHandle(),data,off,len);
			else
				updateVerifyNative(this.worker.getPvh(),
                        this.worker.getSlotHandle(),this.worker.getSessionHandle(),data,off,len);
				
		} catch (PKCS11Exception e)
		{
			throw new SignatureException("PKCS11 exception",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineSign()
	 */
	@Override
	protected byte[] engineSign() throws SignatureException
	{
		if (this.worker == null)
			throw new SignatureException("Signature not initialized through initSign() or initVerify().");

		if (this.privateKey == null)
			throw new SignatureException("Signature not initialized through initSign().");
		
		try
		{
			
			return signNative(this.worker.getPvh(),
                    this.worker.getSlotHandle(),this.worker.getSessionHandle());
				
		} catch (PKCS11Exception e)
		{
			throw new SignatureException("PKCS11 exception",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineVerify(byte[])
	 */
	@Override
	protected boolean engineVerify(byte[] signature) throws SignatureException
	{
		if (this.worker == null)
			throw new SignatureException("Signature not initialized through initSign() or initVerify().");

		if (this.publicKey == null)
			throw new SignatureException("Signature not initialized through initVerify().");
		
		try
		{
			return verifyNative(this.worker.getPvh(),
                    this.worker.getSlotHandle(),this.worker.getSessionHandle(),signature);
				
		} catch (PKCS11Exception e)
		{
			throw new SignatureException("PKCS11 exception",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineSetParameter(java.lang.String, java.lang.Object)
	 */
	@Override
	protected void engineSetParameter(String key, Object value)
			throws InvalidParameterException
	{
		throw new InvalidParameterException("Parameter ["+key+"] is not recognized by the PKCS11 signature engine.");
	}

	/* (non-Javadoc)
	 * @see java.security.SignatureSpi#engineGetParameter(java.lang.String)
	 */
	@Override
	protected Object engineGetParameter(String key)
			throws InvalidParameterException
	{
		throw new InvalidParameterException("Parameter ["+key+"] is not recognized by the PKCS11 signature engine.");
	}

}
