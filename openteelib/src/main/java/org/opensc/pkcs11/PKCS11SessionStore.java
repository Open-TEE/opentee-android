/***********************************************************
 * $Id$
 * 
 * PKCS11 provider of the OpenSC project http://www.opensc-project.org
 *
 * Copyright (C) 2002-2006 ev-i Informationstechnologie GmbH
 *
 * Created: Jan 25, 2007
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

package org.opensc.pkcs11;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStore.CallbackHandlerProtection;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.util.List;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensc.pkcs11.wrap.PKCS11Exception;
import org.opensc.pkcs11.wrap.PKCS11Session;
import org.opensc.pkcs11.wrap.PKCS11Slot;

/**
 * This class is used to establish a session on the token, wherever
 * a SPI implementation needs to do so.
 * It additionally implements the {@link LoadStoreParameter} interface in order
 * to allow sharing of a session among various Spi instances.
 * 
 * @author wglas
 */
public class PKCS11SessionStore implements LoadStoreParameter
{
    private static Log log = LogFactory.getLog(PKCS11SessionStore.class);
    
    PKCS11Slot slot;
    PKCS11Session session;
    PKCS11EventCallback cb;
    CallbackHandler eventHandler;
    ProtectionParameter protectionParameter;
    
    /**
     * Contruct a PKCS11SessionStore instance, which may be opened afterwards.
     */
    public PKCS11SessionStore()
    {
        this.slot = null;
        this.session = null;
        this.protectionParameter = null;
        this.cb = null;
    }
    
    private void changeEvent(int ev) throws IOException
    {
        this.cb.setEvent(ev);
        if (this.eventHandler==null) return;
        
        try
        {
            this.eventHandler.handle(new Callback[]{this.cb});
        } catch (UnsupportedCallbackException e)
        {
            log.warn("PKCSEventCallback not supported by CallbackHandler ["+this.eventHandler.getClass()+"]",e);
        }
    }
    
    private void eventFailed(Exception e) throws IOException
    {
        int fe;
        
        switch (this.cb.getEvent())
        {
        default:
            fe = PKCS11EventCallback.INITIALIZATION_FAILED;
            break;
            
        case PKCS11EventCallback.WAITING_FOR_CARD:
            fe = PKCS11EventCallback.CARD_WAIT_FAILED;
            break;
            
        case PKCS11EventCallback.WAITING_FOR_SW_PIN:
        case PKCS11EventCallback.WAITING_FOR_SW_SO_PIN:
            // an IOException during software PIN entry is interpreted as an abort of
            // the PIN entry process. This is done so, because there is no standard exception
            // for such a situation defined.
            if ((e instanceof IOException) &&
                    !(e instanceof PKCS11Exception))
            {
                fe = (this.cb.getEvent() == PKCS11EventCallback.WAITING_FOR_SW_SO_PIN) ?
                        PKCS11EventCallback.SO_PIN_ENTRY_ABORTED :
                            PKCS11EventCallback.PIN_ENTRY_ABORTED;
                break;
            }
        
            // A PKCS11Exception with CKR_FUNCTION_CANCELED od CKR_CANCEL
            // is interpreted as a PIN entry abort by the user.
            if (e instanceof PKCS11Exception)
            {
                PKCS11Exception p11e = (PKCS11Exception)e;
                
                if (p11e.getErrorCode() == PKCS11Exception.CKR_FUNCTION_CANCELED ||
                        p11e.getErrorCode() == PKCS11Exception.CKR_CANCEL)
                {
                    fe = (this.cb.getEvent() == PKCS11EventCallback.WAITING_FOR_SW_SO_PIN) ?
                            PKCS11EventCallback.SO_PIN_ENTRY_ABORTED :
                                PKCS11EventCallback.PIN_ENTRY_ABORTED;
                    break;
                }
            }
            
            fe = (this.cb.getEvent() == PKCS11EventCallback.WAITING_FOR_SW_SO_PIN) ?
                    PKCS11EventCallback.SO_PIN_ENTRY_FAILED :
                        PKCS11EventCallback.PIN_ENTRY_FAILED;
            break;
            
        case PKCS11EventCallback.HW_AUTHENTICATION_IN_PROGRESS:
        case PKCS11EventCallback.SO_HW_AUTHENTICATION_IN_PROGRESS:
            // A PKCS11Exception with CKR_FUNCTION_CANCELED od CKR_CANCEL
            // is interpreted as an authentication abort by the user.
            if (e instanceof PKCS11Exception)
            {
                PKCS11Exception p11e = (PKCS11Exception)e;
                
                if (p11e.getErrorCode() == PKCS11Exception.CKR_FUNCTION_CANCELED ||
                        p11e.getErrorCode() == PKCS11Exception.CKR_CANCEL)
                {
                    fe = (this.cb.getEvent() == PKCS11EventCallback.SO_HW_AUTHENTICATION_IN_PROGRESS) ?
                            PKCS11EventCallback.SO_AUHENTICATION_ABORTED :
                                PKCS11EventCallback.AUHENTICATION_ABORTED;
                    break;
                }
            }

            fe = (this.cb.getEvent() == PKCS11EventCallback.SO_HW_AUTHENTICATION_IN_PROGRESS) ?
                    PKCS11EventCallback.SO_AUHENTICATION_FAILED :
                        PKCS11EventCallback.AUHENTICATION_FAILED;
            break;
            
        case PKCS11EventCallback.PIN_AUTHENTICATION_IN_PROGRESS:
            fe = PKCS11EventCallback.AUHENTICATION_FAILED;
            break;
            
        case PKCS11EventCallback.SO_PIN_AUTHENTICATION_IN_PROGRESS:
            fe = PKCS11EventCallback.SO_AUHENTICATION_FAILED;
            break;
        }
        
        changeEvent(fe);
        close();
    }
    
    /**
     * Open a session by using the supplied LoadStoreParameter settings.
     * You should preferrably use the implementation {@link PKCS11LoadStoreParameter}
     * for opening the session. PKCS11LoadStoreParameter provides you with a full
     * set of PKCS#11 related properties.
     *
     * @param provider The PKCS#1 provider on which to open a session.
     * @param param The LoadStoreParamater set used to open a session. 
     * 
     * @see KeyStore#store(LoadStoreParameter)
     */
    public void open(PKCS11Provider provider, LoadStoreParameter param) throws IOException
    {
        if (this.slot != null) close();
        
        this.cb = new PKCS11EventCallback(PKCS11EventCallback.NO_EVENT);

        this.eventHandler = null;
        if (param instanceof PKCS11LoadStoreParameter)
            this.eventHandler = ((PKCS11LoadStoreParameter)param).getEventHandler();
        
        try
        {

            PKCS11LoadStoreParameter p11_param = null;
            if (param instanceof PKCS11LoadStoreParameter)
                p11_param = (PKCS11LoadStoreParameter) param;
            
            // get the new slot.
            PKCS11Slot s = null;

            // OK, the user knows, which slot is desired.
            if (p11_param != null && p11_param.getSlotId() != null)
            {
                s = new PKCS11Slot(provider, p11_param.getSlotId());
                
                // is there a token ?
                // no token, but user wants to wait.
                if (!s.isTokenPresent() && p11_param.isWaitForSlot())
                {
                    s.destroy();

                    changeEvent(PKCS11EventCallback.WAITING_FOR_CARD);

                    // OK, someone might argue, that we could intrduce a loop
                    // here in order to wait for the right token.
                    // For the moment, I prefer to throw an exception, if the
                    // user
                    // inserts a token into the wrong slot.
                    s = PKCS11Slot.waitForSlot(provider);

                    if (s.getId() != p11_param.getSlotId().longValue())
                    {
                        s.destroy();
                        throw new PKCS11Exception(
                                "A token has been inserted in slot number "
                                        + s.getId()
                                        + " instead of slot number "
                                        + p11_param.getSlotId());
                    }
                }

            }
            // The user does not know, which slot is desired, so go and find
            // one.
            else
            {
                List<PKCS11Slot> slots = PKCS11Slot
                        .enumerateSlots(provider);

                for (PKCS11Slot checkSlot : slots)
                {
                    if (s == null && checkSlot.isTokenPresent())
                        s = checkSlot;
                    else
                        checkSlot.destroy();
                }

                // not a single token found and user wants to wait.
                if (s == null && p11_param != null && p11_param.isWaitForSlot())
                {
                    changeEvent(PKCS11EventCallback.WAITING_FOR_CARD);
                    s = PKCS11Slot.waitForSlot(provider);
                }
            }

            // So, did we finally find a slot ?
            if (s == null)
            {
                throw new PKCS11Exception(
                        "Could not find a valid slot with a present token.");
            } else if (!s.isTokenPresent())
            {
                long slotId = s.getId();
                s.destroy();
                throw new PKCS11Exception(
                        "No token is present in the given slot number "
                                + slotId);
            }

            this.slot = s;

            int open_mode = PKCS11Session.OPEN_MODE_READ_ONLY;
            
            if (p11_param != null && p11_param.isWriteEnabled())
                open_mode = PKCS11Session.OPEN_MODE_READ_WRITE;
            
            // open the session.
            this.session = PKCS11Session.open(this.slot,open_mode);
            
            if (p11_param != null)
            {
                this.authenticateSO(p11_param.getSOProtectionParameter());
            }

            this.authenticate(param.getProtectionParameter());
            
        } catch (IOException e)
        {
            eventFailed(e);
            throw e;    
        } catch (DestroyFailedException e)
        {
            eventFailed(e);
            throw new PKCS11Exception("destroy exception caught: ",e);
        }
    }
        
    /**
     * Closes the session of a successful call to {@link #open(LoadStoreParameter)}
     * and log any error, that might occur.
     */
    public void close()
    {
        if (this.slot != null)
        {
            try
            {
                this.slot.destroy();
            } catch (DestroyFailedException e)
            {
                log.warn("Cannot destroy slot:",e);
            }
        }        
        
        this.slot = null;
        this.session = null;
        this.protectionParameter = null;
        this.cb = null;
        this.eventHandler = null;
    }
    
    /**
     * This method allows you to authenticate you against the token, if the initial call to
     * {@link #open(LoadStoreParameter)} did not contain a
     * ProtectionParameter. This may be use in order to search for a certificate on a token
     * without entering a PIN.
     * 
     * @param param The protection parameters used to do normal (user) authentication.
     * 
     * @see PKCS11LoadStoreParameter#getProtectionParameter()
     */
    public void authenticate(ProtectionParameter param) throws IOException
    {
        this.protectionParameter = param;
        
        try
        {
            if (this.protectionParameter instanceof PasswordProtection)
            {
                changeEvent(PKCS11EventCallback.PIN_AUTHENTICATION_IN_PROGRESS);
                PasswordProtection pp =
                    (PasswordProtection)this.protectionParameter;
            
                this.session.loginUser(pp.getPassword());
                changeEvent(PKCS11EventCallback.AUHENTICATION_SUCEEDED);
            }
            else if (this.protectionParameter instanceof CallbackHandlerProtection)
            {
                CallbackHandlerProtection cbhp =
                    (CallbackHandlerProtection)this.protectionParameter;
            
                char [] pin = null;
                // do authenticate with the protected auth method of the token,
                // if this is possible, otherwise use the callback to authenticate. 
                if (this.slot.hasTokenProtectedAuthPath())
                {
                    changeEvent(PKCS11EventCallback.HW_AUTHENTICATION_IN_PROGRESS);
                }
                else
                {
                    changeEvent(PKCS11EventCallback.WAITING_FOR_SW_PIN);
                    
                    CallbackHandler cbh = cbhp.getCallbackHandler();
            
                    PasswordCallback pcb = new PasswordCallback("Please enter the user pin:",false);
                    cbh.handle(new Callback[] { pcb });
                
                    pin = pcb.getPassword();
                    changeEvent(PKCS11EventCallback.PIN_AUTHENTICATION_IN_PROGRESS);
                }

                this.session.loginUser(pin);
                changeEvent(PKCS11EventCallback.AUHENTICATION_SUCEEDED);
            }
        }
        catch (UnsupportedCallbackException e)
        {
            throw new PKCS11Exception("PasswordCallback is not supported",e);
        }
    }
    
    /**
     * This method allows you to authenticate you against the token as the security officer
     * for read/write access to the token, if the initial call to
     * {@link #open(LoadStoreParameter)} did not contain a SO
     * ProtectionParameter.
     * 
     * @param param The protection parameters used to do SO (security officer) authentication.
     * 
     * @see PKCS11LoadStoreParameter#getSOProtectionParameter()
     */
    public void authenticateSO(ProtectionParameter param) throws IOException
    {
        try
        {
            if (param instanceof PasswordProtection)
            {
                PasswordProtection pp=(PasswordProtection)param;
            
                changeEvent(PKCS11EventCallback.SO_PIN_AUTHENTICATION_IN_PROGRESS);
                this.session.loginSO(pp.getPassword());
                changeEvent(PKCS11EventCallback.SO_AUHENTICATION_SUCEEDED);
            }
            else if (param instanceof CallbackHandlerProtection)
            {
                CallbackHandlerProtection cbhp=(CallbackHandlerProtection)param;

                char [] pin = null;
                // do authenticate with the protected auth method of the token,
                // if this is possible, otherwise use the callback to authenticate.
                if (this.slot.hasTokenProtectedAuthPath())
                {
                    changeEvent(PKCS11EventCallback.SO_HW_AUTHENTICATION_IN_PROGRESS);
                }
                else
                {
                    changeEvent(PKCS11EventCallback.WAITING_FOR_SW_SO_PIN);

                    CallbackHandler cbh = cbhp.getCallbackHandler();
            
                    PasswordCallback pcb = new PasswordCallback("Please enter the SO pin:",false);
                    cbh.handle(new Callback[] { pcb });
                    pin = pcb.getPassword();
                    changeEvent(PKCS11EventCallback.SO_PIN_AUTHENTICATION_IN_PROGRESS);
                }
            
                this.session.loginSO(pin);
                changeEvent(PKCS11EventCallback.SO_AUHENTICATION_SUCEEDED);
            }
        }
        catch(UnsupportedCallbackException e)
        {
            throw new PKCS11Exception("PasswordCallback is not supported",e);
        }
    }
    
    
    /**
     * @return The session opened by a successful call to
     *         {@link #open(LoadStoreParameter)}
     */
    public PKCS11Session getSession()
    {
        return this.session;
    }

    /**
     * @return The slot for which a successful call to
     *         {@link #open(LoadStoreParameter)} opened a session.
     */
    public PKCS11Slot getSlot()
    {
        return this.slot;
    }

    /* (non-Javadoc)
     * @see java.security.KeyStore$LoadStoreParameter#getProtectionParameter()
     */
    public ProtectionParameter getProtectionParameter()
    {
        return this.getProtectionParameter();
    }

}
