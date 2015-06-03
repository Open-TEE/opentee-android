package org.opensc.pkcs11;

import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;

import javax.security.auth.callback.CallbackHandler;

/**
 * An instance of this class should be passed to the function <tt>KeyStore.load()</tt>
 * in order to configure PKCS11 store loading with parameters appropriate
 * for cyrptographic tokens.
 * 
 * @see java.security.KeyStore#load(java.security.KeyStore.LoadStoreParameter)
 * @author wglas
 */
public class PKCS11LoadStoreParameter implements LoadStoreParameter
{
	ProtectionParameter protectionParameter;
	ProtectionParameter SOProtectionParameter;
	CallbackHandler eventHandler;
	boolean waitForSlot;
	Long slotId;
	boolean writeEnabled;
		
	/**
	 * Constructs a PKCS11LoadStoreParameter instance using default settings.
	 * 
	 * No protection parameters are set, the slot ID ist set to null and
	 * <tt>KeyStore.load()</tt>
	 * does not not wait for a token insertion, if no token is present.
	 */
	public PKCS11LoadStoreParameter()
	{
		this.protectionParameter   = null;
		this.SOProtectionParameter = null;
		this.eventHandler          = null;
		this.waitForSlot           = false;
		this.slotId                = null;
		this.writeEnabled          = false;
	}
	
	/* (non-Javadoc)
	 * @see java.security.KeyStore$LoadStoreParameter#getProtectionParameter()
	 */
	public ProtectionParameter getProtectionParameter()
	{
		return this.protectionParameter;
	}
	
	/**
	 * @param protectionParameter The protectionParameter for the normal user to set
	 *                            A protection parameter for a normal user
	 *                            is needed for signing as well as for listing 
	 *                            private keys on the token.
	 *                            
	 * @see java.security.KeyStore.PasswordProtection
	 * @see java.security.KeyStore.CallbackHandlerProtection
	 * @see javax.security.auth.callback.PasswordCallback
	 */
	public void setProtectionParameter(ProtectionParameter protectionParameter)
	{
		this.protectionParameter = protectionParameter;
	}
	
	/**
	 * This is a convenience function for setting a password protection
	 * to the protection parameter.
	 * 
	 * Equivalent to calling
	 * <code>this.setProtectionParameter(new PasswordProtection(pin))</code>.
	 * 
	 * @param pin The pin to present to the token.
	 * 
	 * @see PKCS11LoadStoreParameter#setProtectionParameter(ProtectionParameter)
	 */
	public void setProtectionPIN(char[] pin)
	{
		this.setProtectionParameter(new PasswordProtection(pin));
	}
	
	/**
	 * This is a convenience function for setting a callback protection
	 * to the protection parameter. The callback handler will receive
	 * callbacks of type <code>PasswordCallback</code>.
	 * 
	 * Equivalent to calling
	 * <code>this.setProtectionParameter(new CallbackHandlerProtection(handler))</code>.
	 * 
	 * @param handler The callback handler for querying the PIN from the user by means
	 *                of a <code>PasswordCallback</code>. 
	 * 
	 * @see PKCS11LoadStoreParameter#setProtectionParameter(ProtectionParameter)
	 * @see javax.security.auth.callback.PasswordCallback
	 * @see java.security.KeyStore.CallbackHandlerProtection
	 */
	public void setProtectionCallback(CallbackHandler handler)
	{
		this.setProtectionParameter(new CallbackHandlerProtection(handler));
	}
	
	/**
	 * @return The protection parameter of the security officer,
	 *         which might be used in order to store a certificate on the
	 *         token.
	 */
	public ProtectionParameter getSOProtectionParameter()
	{
		return this.SOProtectionParameter;
	}

	/**
	 * @param protectionParameter The security officer protection parameter to
	 *                            be used. A SO protection parameter is used,
	 *                            when the token is opened in read/write mode.
	 *                            
	 * @see java.security.KeyStore.PasswordProtection
	 * @see java.security.KeyStore.CallbackHandlerProtection
	 * @see javax.security.auth.callback.PasswordCallback
	 */
	public void setSOProtectionParameter(ProtectionParameter protectionParameter)
	{
        this.SOProtectionParameter = protectionParameter;
	}

	/**
	 * This is a convenience function for setting a password protection
	 * to the SO protection parameter.
	 * 
	 * Equivalent to calling
	 * <code>this.setSOProtectionParameter(new PasswordProtection(pin))</code>.
	 * 
	 * @param pin The SO pin to present to the token.
	 * 
	 * @see PKCS11LoadStoreParameter#setSOProtectionParameter(ProtectionParameter)
	 */
	public void setSOProtectionPIN(char[] pin)
	{
		this.setSOProtectionParameter(new PasswordProtection(pin));
	}
	
	/**
	 * This is a convenience function for setting a callback protection
	 * to the SO protection parameter. The callback handler will receive
	 * callbacks of type <code>PasswordCallback</code>.
	 * 
	 * Equivalent to calling
	 * <code>this.setSOProtectionParameter(new CallbackHandlerProtection(handler))</code>.
	 * 
	 * @param handler The callback handler for querying the SO PIN from the user by means
	 *                of a <code>PasswordCallback</code>. 
	 * 
	 * @see PKCS11LoadStoreParameter#setSOProtectionParameter(ProtectionParameter)
	 * @see javax.security.auth.callback.PasswordCallback
	 * @see java.security.KeyStore.CallbackHandlerProtection
	 */
	public void setSOProtectionCallback(CallbackHandler handler)
	{
		this.setSOProtectionParameter(new CallbackHandlerProtection(handler));
	}
	/**
	 * @return Returns the <code>CallbackHandler</code>, which receives
	 * callbacks of type <code>PKCS11EventCallback</code>. This handler my be used
	 * in order to display some helpful information to the user while the
	 * KeyStore is performing the authentication against the token.
	 * 
	 * @see PKCS11EventCallback
	 */
	public CallbackHandler getEventHandler()
	{
		return this.eventHandler;
	}

	/**
	 * Sets the <code>CallbackHandler</code>, which receives
	 * callbacks of type <code>PKCS11EventCallback</code>.
	 * 
	 * @param eventHandler The CallbackHandler to set.
	 * 
	 * @see PKCS11LoadStoreParameter#getEventHandler()
	 * @see PKCS11EventCallback
	 */
	public void setEventHandler(CallbackHandler eventHandler)
	{
		this.eventHandler = eventHandler;
	}

	/**
	 * @return Returns the ID of the slot to be opened.
	 */
	public Long getSlotId()
	{
		return this.slotId;
	}

	/**
	 * @param slotId Set the ID of the slot to be opened.
	 *               If set to null, the KeyStore opens the first slot
	 *               with a present token.
	 */
	public void setSlotId(Long slotId)
	{
		this.slotId = slotId;
	}

	/**
	 * @return Returns, whether the KeyStore should wait for a token to be inserted
	 *         if no token is found.
	 */
	public boolean isWaitForSlot()
	{
		return this.waitForSlot;
	}

	/**
	 * @param waitForSlot Set, whether the KeyStore should wait for a token
	 *                    to be inserted if no token is found.
	 */
	public void setWaitForSlot(boolean waitForSlot)
	{
		this.waitForSlot = waitForSlot;
	}

	/**
	 * @return Returns, whether the token should be opened in read/write mode instead
	 *         of read-only mode.
	 */
	public boolean isWriteEnabled()
	{
		return this.writeEnabled;
	}

	/**
	 * @param writeEnabled Set, whether the token should be opened in read/write mode
	 *                     instead of read-only mode.
	 */
	public void setWriteEnabled(boolean writeEnabled)
	{
		this.writeEnabled = writeEnabled;
	}
}
