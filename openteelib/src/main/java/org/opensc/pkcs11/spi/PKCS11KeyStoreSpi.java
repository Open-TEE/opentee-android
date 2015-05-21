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

package org.opensc.pkcs11.spi;

import android.util.Log;

import org.opensc.pkcs11.PKCS11LoadStoreParameter;
import org.opensc.pkcs11.PKCS11Provider;
import org.opensc.pkcs11.PKCS11SessionStore;
import org.opensc.pkcs11.wrap.PKCS11Certificate;
import org.opensc.pkcs11.wrap.PKCS11Exception;
import org.opensc.pkcs11.wrap.PKCS11PrivateKey;
import org.opensc.util.PKCS11Id;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore.Entry;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.ProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

/**
 * This is a JAVA KeyStore, which accesses a slot on a PKCS#11 cryptographic token.
 *
 * @author wglas
 */
public class PKCS11KeyStoreSpi extends KeyStoreSpi
{
	private static final String TAG = "PKCS11KeyStoreSpi";
	static private final int MAX_SIMILAR_CERTIFICATES = 32;
	
	private class PKCS11KSEntry implements Entry
	{
		public Date creationDate;
		public PKCS11Certificate certificate;
		private Certificate decodedCertificate;
		public PKCS11PrivateKey privateKey;
		
		PKCS11KSEntry(PKCS11PrivateKey privateKey)
		{
			this.creationDate = new Date();
			this.privateKey = privateKey;
		}
		
		PKCS11KSEntry(PKCS11Certificate certificate)
		{
			this.creationDate = new Date();
			this.certificate = certificate;
		}
		
		public Certificate getDecodedCertificate() throws PKCS11Exception, CertificateException
		{
			if (this.decodedCertificate == null && this.certificate != null)
				this.decodedCertificate = this.certificate.getCertificate();
			
			return this.decodedCertificate;
		}
	}
	
	private final PKCS11Provider provider;
	private PKCS11SessionStore sessionStore;
    private boolean needToCloseSessionStore;
	private Map<String,PKCS11KSEntry> entries;
	
	/**
	 * Contruct a PKCS11 KeyStore.
	 */
	public PKCS11KeyStoreSpi(PKCS11Provider provider, String algorithm)
	{
		super();
		this.provider = provider;
        this.sessionStore = null;
        this.entries = null;
		this.needToCloseSessionStore = false;
        
		if (algorithm != "PKCS11")
			throw new ProviderException("Algorithm for PKCS11 KeyStore can only be \"PKCS11\".");
	}
	
	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetKey(java.lang.String, char[])
	 */
	@Override
	public Key engineGetKey(String name, char[] pin)
			throws NoSuchAlgorithmException, UnrecoverableKeyException
	{
		PKCS11KSEntry entry = this.entries.get(name);
		if (entry == null) return null;
		return entry.privateKey;
	}
	
	/**
	 * Returns all certificates for the given X500Principal.
	 * 
	 * @param subject The subject to search for.
	 * @return All certificates, which match this subject.
	 */
	private Map<String,PKCS11KSEntry> getAllCertificatesForSubject(X500Principal subject)
	{
		Map<String,PKCS11KSEntry> ret = new HashMap<String,PKCS11KSEntry>();
		
		String subj = subject.toString();
		
		PKCS11KSEntry entry = this.entries.get(subj);
		
		if (entry != null)
		{
			ret.put(subj,entry);
			
			int i = 1;
			
			do
			{
				++i;
				String name = String.format("%s_%02X",subj,i);
				
				entry = this.entries.get(name);
				if (entry != null) ret.put(name,entry);
			}
			while (entry != null && i < MAX_SIMILAR_CERTIFICATES);
		}
		
		
		return ret;
	}

	private static boolean isRootCA(X509Certificate cert) throws InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException
	{
		if (!cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal()))
			return false;
		
		cert.verify(cert.getPublicKey());
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCertificateChain(java.lang.String)
	 */
	@Override
	public Certificate[] engineGetCertificateChain(String name)
	{
		Certificate endEntity = engineGetCertificate(name);
		
		if (endEntity == null) return null;
		
		if (!(endEntity instanceof X509Certificate))
		{
			Log.e(TAG, "engineGetCertificateChain: Only X.509 certificates are supported.");
			return null;
		}
		
		List<Certificate> ret = new ArrayList<Certificate>();
		
		ret.add(endEntity);
		
		X509Certificate x509Certificate = (X509Certificate)endEntity;
		
		try
		{
			// OK ,this is acrude form of certificate chain evaluation.
			// Assuming, that the upper layer does a more detailed anlysis of the
			// validity period and key extensions, we only search the chain by
			// finding the issuing certificate on the token using the issuer DN
			// and trying to check the Signature on the certificate using the
			// public key on the next certificate.
			while (!isRootCA(x509Certificate))
			{
				Map<String,PKCS11KSEntry> centries =
					getAllCertificatesForSubject(x509Certificate.getIssuerX500Principal());
				
				X509Certificate x509NextCert = null;
				
				for (PKCS11KSEntry entry : centries.values())
				{
					Certificate next = entry.getDecodedCertificate();
									
					X509Certificate x509Next = (X509Certificate)next;
				
					if (!x509Next.getSubjectX500Principal().equals(x509Certificate.getIssuerX500Principal()))
						continue;
						
					try {
						x509Certificate.verify(x509Next.getPublicKey());
						x509NextCert = x509Next;
						break;
					}
					catch (Exception e) {
						Log.w(TAG, "Exception during evaluation of certificate chain:" + e);
					}
				}
				
				if (x509NextCert == null)
				{
					throw new CertificateException("Cannot find the issuing CA for certificate ["+x509Certificate+"].");
				}
				
				x509Certificate = x509NextCert;
				ret.add(x509Certificate);
			}
			
			return ret.toArray(new Certificate[0]);
			
		} catch (Exception e)
		{
			Log.e(TAG, "Exception caught during analysis of the certificate chain:" + e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCertificate(java.lang.String)
	 */
	@Override
	public Certificate engineGetCertificate(String name)
	{
		PKCS11KSEntry entry = this.entries.get(name);
		if (entry == null) return null;
		try
		{
			return entry.getDecodedCertificate();
		} catch (PKCS11Exception e)
		{
			Log.e(TAG, "PKCS11 Error decoding Certificate for entry " + name + ":" + e);
		} catch (CertificateException e)
		{
			Log.e(TAG, "Certificate Error decoding Certificate for entry " + name + ":" + e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCreationDate(java.lang.String)
	 */
	@Override
	public Date engineGetCreationDate(String name)
	{
		PKCS11KSEntry entry = this.entries.get(name);
		if (entry == null) return null;
		return entry.creationDate;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSetKeyEntry(java.lang.String, java.security.Key, char[], java.security.cert.Certificate[])
	 */
	@Override
	public void engineSetKeyEntry(String name, Key key, char[] pin,
			Certificate[] certificateChain) throws KeyStoreException
	{
		throw new KeyStoreException("setKeyEntry is unimplmented.");
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSetKeyEntry(java.lang.String, byte[], java.security.cert.Certificate[])
	 */
	@Override
	public void engineSetKeyEntry(String name, byte[] pin, Certificate[] certificateChain)
			throws KeyStoreException
	{
		throw new KeyStoreException("setKeyEntry is unimplmented.");
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSetCertificateEntry(java.lang.String, java.security.cert.Certificate)
	 */
	@Override
	public void engineSetCertificateEntry(String name, Certificate certificate)
			throws KeyStoreException
	{
	    try
        {
	        PKCS11Certificate cert =
                PKCS11Certificate.storeCertificate(this.sessionStore.getSession(),
                                                   certificate, name, true);
            
            PKCS11KSEntry entry = new PKCS11KSEntry(cert);

            String keyName = "ID_" + cert.getId();

            PKCS11KSEntry pk_entry = this.entries.get(keyName);
                
            if (pk_entry != null)
            {
                entry.privateKey = pk_entry.privateKey;
                this.entries.remove(keyName);
            }
            
            if (name == null)
                this.entries.put(cert.getSubject().toString(),entry);
            else
                this.entries.put(name,entry);
           
        } catch (CertificateEncodingException e)
        {
            throw new KeyStoreException("Error encoding certificate",e);
        } catch (PKCS11Exception e)
        {
            throw new KeyStoreException("Error storing certificate on the token",e);
        }
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineDeleteEntry(java.lang.String)
	 */
	@Override
	public void engineDeleteEntry(String name) throws KeyStoreException
	{
		throw new KeyStoreException("deleteEntry is unimplemented.");
	}

	
	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineAliases()
	 */
	@Override
	public Enumeration<String> engineAliases()
	{
		// Enumeration is efinitely a misconception, as you can see
		// by the code below...
		Set<String> keys = this.entries.keySet();
		Vector<String> sv = new Vector<String>(keys.size());
		sv.addAll(keys);
		
		return sv.elements();
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineContainsAlias(java.lang.String)
	 */
	@Override
	public boolean engineContainsAlias(String name)
	{
		return this.entries.containsKey(name);
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineSize()
	 */
	@Override
	public int engineSize()
	{
		return this.entries.size();
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineIsKeyEntry(java.lang.String)
	 */
	@Override
	public boolean engineIsKeyEntry(String name)
	{
		PKCS11KSEntry entry = this.entries.get(name);
		if (entry == null) return false;
	
		return entry.privateKey != null;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineIsCertificateEntry(java.lang.String)
	 */
	@Override
	public boolean engineIsCertificateEntry(String name)
	{
		PKCS11KSEntry entry = this.entries.get(name);
		if (entry == null) return false;
	
		return entry.certificate != null;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineGetCertificateAlias(java.security.cert.Certificate)
	 */
	@Override
	public String engineGetCertificateAlias(Certificate certificate)
	{
		if (! (certificate instanceof X509Certificate))
		{
			Log.e(TAG, "engineGetCertificateAlias: Only X.509 certificates are supported.");
		}
		
		X509Certificate x509Certificate = (X509Certificate)certificate;
		
		X500Principal subject = x509Certificate.getSubjectX500Principal();
		
		Map<String,PKCS11KSEntry> centries = getAllCertificatesForSubject(subject);
		
		for (String name : centries.keySet())
		{
			try
			{
				PKCS11KSEntry entry = centries.get(name);
				
				if (entry.certificate != null &&
					entry.getDecodedCertificate().equals(certificate))
					return name;
				
			} catch (PKCS11Exception e)
			{
				Log.e(TAG, "PKCS11 Error decoding Certificate for entry " + name + ":" + e);
			} catch (CertificateException e)
			{
				Log.e(TAG, "Certificate Error decoding Certificate for entry " + name + ":" + e);
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineStore(java.io.OutputStream, char[])
	 */
	@Override
	public void engineStore(OutputStream arg0, char[] arg1) throws IOException,
			NoSuchAlgorithmException, CertificateException
	{
		throw new NoSuchAlgorithmException("PKCS11 key store does not support a store operation.");
	}
	
	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineLoad(java.io.InputStream, char[])
	 */
	@Override
	public void engineLoad(InputStream file, char[] pin) throws IOException,
			NoSuchAlgorithmException, CertificateException
	{
		if (file != null)
			throw new IOException ("PKCS11 Key Store requires a null InputStream a the first argument.");
	
		PKCS11LoadStoreParameter param = new PKCS11LoadStoreParameter();
		
		param.setProtectionParameter(new PasswordProtection(pin));
		
		engineLoad(param);
	}

	/* (non-Javadoc)
	 * @see java.security.KeyStoreSpi#engineLoad(java.security.KeyStore.LoadStoreParameter)
	 */
	@Override
	public void engineLoad(LoadStoreParameter param) throws IOException,
			NoSuchAlgorithmException, CertificateException
	{
	    if (this.sessionStore != null)
	    {
	        if (this.needToCloseSessionStore)
	            this.sessionStore.close();
	    }
            
	    if (param instanceof PKCS11SessionStore)
	    {
	        this.sessionStore = (PKCS11SessionStore)param;
	        this.needToCloseSessionStore = false;
	    }
	    else
	    {
	        this.sessionStore = new PKCS11SessionStore();
	        this.needToCloseSessionStore = true;
	        this.sessionStore.open(this.provider, param);
	    }
	    
	    // OK, the session is up and running, now get the certificates
	    // and keys.
	    this.entries = new HashMap<String,PKCS11KSEntry>();
			
	    List<PKCS11PrivateKey> privKeys =
	        PKCS11PrivateKey.getPrivateKeys(this.sessionStore.getSession());
			
	    Map<PKCS11Id,PKCS11KSEntry> privKeysById =
	        new HashMap<PKCS11Id,PKCS11KSEntry>();
			
	    for (PKCS11PrivateKey privKey : privKeys)
	    {
	        privKeysById.put(privKey.getId(),new PKCS11KSEntry(privKey));
	    }
			
	    List<PKCS11Certificate> certificates =
	        PKCS11Certificate.getCertificates(this.sessionStore.getSession());
			
	    for (PKCS11Certificate certificate : certificates)
	    {
	        // contruct a unique name for certificate entries.
	        String subj = certificate.getSubject().toString();
	        String name = subj;
	        
	        name = subj;
				
	        int i = 1;
					
	        while (this.entries.containsKey(name) && i < MAX_SIMILAR_CERTIFICATES)
	        {
	            ++i;
	            name = String.format("%s_%02X",subj,i);
	        }
				
	        if (i >= MAX_SIMILAR_CERTIFICATES) {
	            throw new CertificateException("More than "+MAX_SIMILAR_CERTIFICATES+
	                                           " instances of the same certificate subject ["+subj+
	                                           "]found on the token.");
	        }
				
	        PKCS11KSEntry entry = new PKCS11KSEntry(certificate);
	        PKCS11KSEntry pk_entry = privKeysById.get(certificate.getId());
				
	        if (pk_entry != null)
	        {
	            entry.privateKey = pk_entry.privateKey;
	            pk_entry.certificate = certificate;
	        }
				
	        this.entries.put(name,entry);
	    }
	    
	    for (PKCS11Id id : privKeysById.keySet())
	    {
	        PKCS11KSEntry entry = privKeysById.get(id);
				
	        if (entry.certificate != null) continue;
				
	        String name = "ID_"+id;
				
	        this.entries.put(name,entry);
	    }
	}
}
