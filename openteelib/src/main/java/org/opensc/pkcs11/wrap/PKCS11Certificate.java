/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jul 18, 2006
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

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import sun.security.util.BigInt;

/**
 * This class manages X509 certificates stored on the card.
 * 
 * @author wglas
 */
public class PKCS11Certificate extends PKCS11Object
{
    public static final int CKC_X_509           = 0x00000000;
    public static final int CKC_X_509_ATTR_CERT = 0x00000001;
    public static final int CKC_VENDOR_DEFINED  = 0x80000000;

    private X500Principal subject;
	private X500Principal issuer;
	private BigInt serial;
	
	/**
	 * @param session The session to which we are associated.
	 * @param handle The object handle as returned by PKCS11Object.enumRawObjects().
	 * @throws PKCS11Exception Upon errors when retrieving the information for
	 *                         this certificate from the token.
	 */
	protected PKCS11Certificate(PKCS11Session session, long handle)
			throws PKCS11Exception
	{
		super(session, handle);
		
		byte[] raw_subject = getRawAttribute(PKCS11Attribute.CKA_SUBJECT);
		this.subject = new X500Principal(raw_subject); 
		
		byte[] raw_issuer = getRawAttribute(PKCS11Attribute.CKA_ISSUER);
		this.issuer = new X500Principal(raw_issuer); 
		
		byte[] raw_serial = getRawAttribute(PKCS11Attribute.CKA_SERIAL_NUMBER);
		this.serial = new BigInt(raw_serial);
	}

	/**
	 * Fetches all certificates stored in the specified slot.
	 * 
	 * @param session The session of which to find the certificates. 
	 * @return The list of all certificates found in this slot.
	 * @throws PKCS11Exception Upon errors from the underlying PKCS11 module.
	 */
	public static List<PKCS11Certificate> getCertificates(PKCS11Session session) throws PKCS11Exception
	{
		long[] handles = enumRawObjects(session,PKCS11Object.CKO_CERTIFICATE);
		
		List<PKCS11Certificate> ret = new ArrayList<PKCS11Certificate>(handles.length);
		
		for (int i = 0; i < handles.length; i++)
		{
			ret.add(new PKCS11Certificate(session,handles[i]));
		}
		return ret;
	}

    /**
     * Store a signed certificate to the token and return a reference to the newly created token
     * object.
     * 
     * Currently, the only supported certificate type is X.509.
     * 
     * @param session The session in which to create the new certificate.
     * @param cert The certificate to be stored. Currently the certificate must
     *             be an extension of {@link X509Certificate}.
     * @param label An optional label for the certificate object on the token.
     * @param trusted The PKCS#11 trusted flag of the certificate.
     * @return A reference to the newly created certificate object on the token.
     * @throws PKCS11Exception Upon errors from the underlying PKCS11 module.
     * @throws CertificateEncodingException If the certificae could not be serialized
     *                 or the certificate in not an X.509 certificate.
     */
    public static PKCS11Certificate storeCertificate(PKCS11Session session, Certificate cert,
                                                     String label, boolean trusted) throws PKCS11Exception, CertificateEncodingException
    {
        if (!(cert instanceof X509Certificate))
            throw new CertificateEncodingException("Only X.509 certificates are supported.");
        
        X509Certificate x509 = (X509Certificate)cert;
        
        try
        {
            int nAttrs = 7;
            if (label != null) ++nAttrs;
            
            PKCS11Attribute[] attrs = new PKCS11Attribute[nAttrs];
            
            attrs[0] = new PKCS11Attribute(PKCS11Attribute.CKA_CLASS,CKO_CERTIFICATE);
            attrs[1] = new PKCS11Attribute(PKCS11Attribute.CKA_CERTIFICATE_TYPE,CKC_X_509);
            attrs[2] = new PKCS11Attribute(PKCS11Attribute.CKA_SUBJECT,
                                           x509.getSubjectX500Principal().getEncoded());
            attrs[3] = new PKCS11Attribute(PKCS11Attribute.CKA_ISSUER,
                                           x509.getIssuerX500Principal().getEncoded());
            attrs[4] = new PKCS11Attribute(PKCS11Attribute.CKA_SERIAL_NUMBER,
                                           x509.getSerialNumber().toByteArray());
            attrs[5] = new PKCS11Attribute(PKCS11Attribute.CKA_VALUE,cert.getEncoded());
            attrs[6] = new PKCS11Attribute(PKCS11Attribute.CKA_TRUSTED,trusted);
             
            if (label != null)
                attrs[7] = new PKCS11Attribute(PKCS11Attribute.CKA_LABEL,label.getBytes("UTF-8"));
            
            return new PKCS11Certificate(session,PKCS11Object.createObject(session, attrs));

        } catch (UnsupportedEncodingException e)
        {
            throw new CertificateEncodingException("Unexpected error during utf-8 encoding",e); 
        }
    }
    
	/**
	 * @return The decoded X509 certificate of this entry.
	 * @throws CertificateException Upon errors when decoding the
	 *                              raw ASN1 encoded certificate from the token.
	 */
	public Certificate getCertificate() throws PKCS11Exception, CertificateException
	{
		byte[] asn1_certificate = getRawAttribute(PKCS11Attribute.CKA_VALUE);
		
		CertificateFactory factory =
			CertificateFactory.getInstance("X.509");
		
		ByteArrayInputStream is = new ByteArrayInputStream(asn1_certificate);

		return factory.generateCertificate(is);
	}

	/**
	 * @return Returns the issuer, which is the value of the CKA_ISSUER attribute.
	 */
	public X500Principal getIssuer()
	{
		return this.issuer;
	}

	/**
	 * @return Returns the serial, which is the value of the CKA_SERIAL_NUMBER attribute.
	 */
	public BigInt getSerial()
	{
		return this.serial;
	}

	/**
	 * @return Returns the subject, which is the value of the CKA_SUBJECT attribute.
	 */
	public X500Principal getSubject()
	{
		return this.subject;
	}
}

