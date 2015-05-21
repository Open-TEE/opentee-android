/*****************************************************************************
** Copyright (C) 2013 Intel Corporation.                                    **
**                                                                          **
** Licensed under the Apache License, Version 2.0 (the "License");          **
** you may not use this file except in compliance with the License.         **
** You may obtain a copy of the License at                                  **
**                                                                          **
**      http://www.apache.org/licenses/LICENSE-2.0                          **
**                                                                          **
** Unless required by applicable law or agreed to in writing, software      **
** distributed under the License is distributed on an "AS IS" BASIS,        **
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. **
** See the License for the specific language governing permissions and      **
** limitations under the License.                                           **
*****************************************************************************/

#ifndef __TEE_CLIENT_API_H__
#define __TEE_CLIENT_API_H__

#include <inttypes.h>
#include <stddef.h>

#include "tee_shared_data_types.h"

/* clang-format off */
/* Shared Memory Control Flags */
#define TEEC_MEM_INPUT			0x00000001
#define TEEC_MEM_OUTPUT			0x00000002

/* Paramater Types */
#define TEEC_NONE			0x00000000
#define TEEC_VALUE_INPUT		0x00000001
#define TEEC_VALUE_OUTPUT		0x00000002
#define TEEC_VALUE_INOUT		0x00000003
#define TEEC_MEMREF_TEMP_INPUT		0x00000005
#define TEEC_MEMREF_TEMP_OUTPUT		0x00000006
#define TEEC_MEMREF_TEMP_INOUT		0x00000007
#define TEEC_MEMREF_WHOLE		0x0000000C
#define TEEC_MEMREF_PARTIAL_INPUT	0x0000000D
#define TEEC_MEMREF_PARTIAL_OUTPUT	0x0000000E
#define TEEC_MEMREF_PARTIAL_INOUT	0x0000000F

/* Session Login Methods (client api) */
#define TEEC_LOGIN_PUBLIC		0x00000000
#define TEEC_LOGIN_USER			0x00000001
#define TEEC_LOGIN_GROUP		0x00000002
#define TEEC_LOGIN_APPLICATION		0x00000004
#define TEEC_LOGIN_USER_APPLICATION	0x00000005
#define TEEC_LOGIN_GROUP_APPLICATION	0x00000006

/* clang-format on */

/*!
 * \brief TEEC_Context Logical container linking the Client Application to a particular TEE
 */
typedef struct {
	void *imp;
} TEEC_Context;

/*!
 * \brief TEEC_Session Container linking a Client Application to a particular Trusted Application
 */
typedef struct {
	void *imp;
} TEEC_Session;

/*!
  * \brief TEEC_SharedMemory A shared memory block that has been registered or allocated
  */
typedef struct {
	void *buffer;   /*!< pointer to a memory buffer that is shared with TEE */
	size_t size;    /*!< The size of the memory buffer in bytes */
	uint32_t flags; /*!< bit vector that can contain TEEC_MEM_INPUT or TEEC_MEM_OUTPUT */
	void *imp;
} TEEC_SharedMemory;

/*!
 * \brief TEEC_TempMemoryReference A Temporary memorry Reference as used by \sa TEEC_Operation
 */
typedef struct {
	void *buffer; /*!< Pointer to the first byte of a buffer that needs to be referenced */
	size_t size;  /*!< Size of the referenced memory region */
} TEEC_TempMemoryReference;

/*!
 * \brief TEEC_RegisteredMemoryReference Uses a pre-registered memory or pre-allocated memory block
 */
typedef struct {
	TEEC_SharedMemory *parent; /*!< Either a whole or partial memory reference */
	size_t size;		   /*!< The size of the referenced memory region, in bytes */
	size_t offset;		   /*!< The offset in bytes of the referenced memory region */
} TEEC_RegisteredMemoryReference;

/*!
 * \brief TEEC_Value Defines a paramater that is not referencing shared memory
 */
typedef struct {
	uint32_t a; /*!< Paramater meaning is defined by the protocol between TA and Client */
	uint32_t b; /*!< Paramater meaning is defined by the protocol between TA and Client */
} TEEC_Value;

/*!
 * \brief TEEC_Parameter Defines a parameter of a \sa TEEC_Operation
 */
typedef union {
	TEEC_TempMemoryReference tmpref;
	TEEC_RegisteredMemoryReference memref;
	TEEC_Value value;
} TEEC_Parameter;

/*!
 * \brief TEEC_Operation Defines the payload of either an open session or invoke command
 */
typedef struct {
	uint32_t started;    /*!< Must set to zero if the client may try to cancel the operation */
	uint32_t paramTypes; /*!< Encodes the type of each paramater that is being transfered */
	TEEC_Parameter params[4]; /*!< an array of 4 possible paramaters to share with TA */
	/* TODO what should be done about the opaque type <implementation defined> section */
	void *imp;
} TEEC_Operation;

/*
 * 4GB default 32 bit- TODO this should be checked on platform basis, so should be possible
 * to define it it at compile time "-DTEEC_CONFIG_SHAREDMEM_MAX_SIZE=XXX"
 */
#ifndef TEEC_CONFIG_SHAREDMEM_MAX_SIZE
/*!
 * \brief TEEC_CONFIG_SHAREDMEM_MAX_SIZE The maximum size of a shared
 */
#define TEEC_CONFIG_SHAREDMEM_MAX_SIZE 0xFFFFFFFF
#endif

/*!
 * \brief TEEC_InitializeContext
 * \param name The name of the TEE to connect to
 * \param context The context taht will be initialized by this function call
 * \return TEEC_SUCCESS on success, or another Return Code on failure
 */
TEEC_Result TEEC_InitializeContext(const char *name, TEEC_Context *context);

/*!
 * \brief TEEC_FinalizeContext Finalizes an initialized context
 * \param context The initialized TEEC_Context that is to be finalized
 */
void TEEC_FinalizeContext(TEEC_Context *context);

/*!
 * \brief TEEC_RegisterSharedMemory
 * Register a block of existing Client Memory as a block of Shared Memory within the scope of
 * the specified TEEC_Context
 * \param context Must point to an initialized TEE_Context
 * \param sharedMem Must point to the shared memory region to be registered
 * \return TEEC_SUCCESS on success, TEEC_ERROR_OUT_OF_MEMORY when no memory or another Return Code
 */
TEEC_Result TEEC_RegisterSharedMemory(TEEC_Context *context, TEEC_SharedMemory *shared_mem);

/*!
 * \brief TEEC_AllocateSharedMemory
 * Allocate a new block of memory as a block of shared memory within the scope of the specified
 * TEEC_Context
 * \param context Must point to an initialized TEEC_Context
 * \param sharedMem Must point to the shared memory region definition to to be populated
 * The size field must be set to define the rquired size of the memory region and the flags field
 * must also be set to indicate the direction of flow for the memory.
 * \return TEEC_SUCCESS on success, TEEC_ERROR_OUT_OF_MEMORY when no memory or another Return Code
 */
TEEC_Result TEEC_AllocateSharedMemory(TEEC_Context *context, TEEC_SharedMemory *shared_mem);

/*!
 * \brief TEEC_ReleaseSharedMemory
 * Deregister or deallocate a previously initialized block of Shared Memory
 * \param sharedMem A pointer to a valid shared memory region
 */
void TEEC_ReleaseSharedMemory(TEEC_SharedMemory *shared_mem);

/*!
 * \brief TEEC_OpenSession
 * Open a new session between the client application and the specified Trusted Application
 * \param context A pointer to an initialized context
 * \param session A pointer to a Session structure to be populated
 * \param destination The UUID of the destination Trusted Application
 * \param connectionMethod The method used to connect as defined in Session Connecton Methods
 * \param connectionData Any data necessary to support the connection method choosen
 * \param operation An operation containing a set of parameters to exchange with the TA
 * \param returnOrigin The origin of the returned result
 * \return TEEC_SUCCEESS or another Return Code on error
 */
TEEC_Result TEEC_OpenSession(TEEC_Context *context, TEEC_Session *session,
			     const TEEC_UUID *destination, uint32_t connection_method,
			     void *connection_data, TEEC_Operation *operation,
			     uint32_t *return_origin);

/*!
 * \brief TEEC_CloseSession
 * Close an existing session to a Trusted Application
 * \param session A valid session to close
 */
void TEEC_CloseSession(TEEC_Session *session);

/*!
 * \brief TEEC_InvokeCommand
 * Run a specfic command within the session to the trusted application
 * \param session Must point to a valid open session
 * \param commandID Indicates the command which should be run within the trusted Application
 * \param operation Optional data to be sent with the command invocation
 * \param returnOrigin The origin of the returned result
 * \return TEEC_SUCCEESS or another Return Code on error
 */
TEEC_Result TEEC_InvokeCommand(TEEC_Session *session, uint32_t command_id,
			       TEEC_Operation *operation, uint32_t *return_origin);

/*!
 * \brief TEEC_RequestCancellation
 * Request the cancellation of a pending Open Session or Comand invocation operation
 * \param operation A pointer to the operation to be canceled
 */
void TEEC_RequestCancellation(TEEC_Operation *operation);

/*!
 * \brief TEEC_PARAM_TYPES Create a Paramater type that can be used with an operation
 * \param param0Type Type of parameter 0
 * \param param1Type Type of parameter 1
 * \param param2Type Type of parameter 2
 * \param param3Type Type of parameter 3
 * \return a uint32_t value that can be used in the operation to define the param types
 */
#define TEEC_PARAM_TYPES(param0Type, param1Type, param2Type, param3Type)                           \
	((param0Type) | ((param1Type) << 4) | ((param2Type) << 8) | ((param3Type) << 12))

/*!
 * \brief TEEC_PARAM_TYPE_GET retrieve a paramater type at a given index
 * \param paramsType The paramaters value that is retrieved from the operation
 * \param index The index of the parameter who's type is required.
 * \return The paramater type stored at that index
 */
#define TEEC_PARAM_TYPE_GET(paramsType, index) (((paramsType) >> (index * 4)) & 0xF)

#endif
