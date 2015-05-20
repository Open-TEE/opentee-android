package org.opensc.pkcs11.wrap;

import javax.security.auth.Destroyable;

/**
 * @author wglas
 *
 * This interface is used by any signature/decryption service, which
 * calls PKCS#11 operations on objects, which life in the PKCS#11
 * session context.
 */
public interface PKCS11SessionChild extends Destroyable
{

	/**
	 * @return Returns the C handle of the underlying provider.
	 */
	public long getPvh();

	/**
	 * @return Returns the C handle of the slot.
	 */
	public long getSlotHandle();

	/**
	 * @return Returns the C handle of the session.
	 */
	public long getSessionHandle();

	/**
	 * @return Returns the C handle of the object.
	 */
	public long getHandle();

}