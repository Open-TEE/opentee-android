/**
 *Java API Version: V 1.0 beta<br>
 *<p>
 *This is the main entrance of public APIs. In order to help explaining the APIs,
 * there are several essential key words defined in the following:<br>
 *     1. CA: Client Application that the developer is creating;<br>
 *     2. TA: Trusted Application which is already deployed in TEE;<br>
 *     3. TEE: Trusted Execution Environment in target Android device within which TAs are running;<br>
 *     4. TEE (Proxy) Service Manager: Android service layer abstraction for TEE, which is responsible for handling
 *     incoming connections from CAs and for communicating with the TEE with the help of Native Libtee;<br>
 *     5. Native Libtee: A library which enables the communication between TEE and TEE Service Manager;<br>
 *     6. Underlying library: A library which resides in the CA and communicates with the remote TEE service on the behalf of CAs.<br>
 *<p>
 *<STRONG>Introduction</STRONG><br>
 *This public API documentation defines the Java APIs corresponding to the C APIs defined in
 *the GlobalPlatform Device Technology TEE Client API specification V 1.0. It describes how
 *the CA should communicate with a remote TEE service manager.
 *<p>
 *<STRONG>Target audience</STRONG><br>
 *This document suits for software developers implementing:<br>
 *     1. Android version of CAs running within the rich operating environment, which needs to
 *     utilizing the functions of TAs;<br>
 *     2. TAs running inside the TEE which need to expose its internal functions
 *     to CAs.
 *<p>
 *<STRONG>Background information</STRONG><br>
 *    1. what is TEE?<br>
 *    TEE stands for Trusted Execution Environment. There is another notation called Rich Execution
 *    Environment (REE). These two are often brought together to help explain both of them by comparisons.
 *    Before taking a look at TEE, it is better to start explaining from REE since it is more closer to our daily sense.
 *    REE represents the common operating system along with its hardware, such as devices running Windows, Mac OSX,
 *    Linux, Android or iOS. It abstracts the underlying hardware and provides resources for the
 *    applications to run with. As such, it has rich features for applications to utilize. However, the
 *    REE frequently suffer from different kinds of attacks, such as malware, worm, trojan and ransomware.
 *    In order to protect very sensitive and private information such as encryption private
 *    keys against these attacks, it is good to keep these private information safely in a separate container in case
 *    the REE is compromised. It is the similar notion as the safe deposit box. For instance, if bad guys broke
 *    into a home, it is still impossible for them to get all your money in the safe deposit box without
 *    the right password to open it. So, with such a thought, TEE showed up to meet such needs.
 *    Currently, the TEE shipped within devices is physically separated with REE by hardware boundaries.
 *    CAs run in the REE and TAs runs in the TEE. Compared with the rich features of REE, TEE mostly comes with very limited hardware capabilities.
 *
 *    <p>
 *    2. GP Specification for TEE Client API Specification.<br>
 *    GP is short for GlobalPlatform. It is a non-profit organization that publishes specifications to promote
 *    security and interoperability of secure devices. One of the specifications it published, named
 *    "GlobalPlatform Device Technology TEE Client API Specification" (GP Client API), standardizes the ways how CAs communicate
 *    with TAs. GlobalPlatform also have other specifications for TEE but we only focus on this one specifically.
 *    The specification defines the C data types and functions for CAs to communicate with TAs.
 *    <p>
 *    3. Open-TEE<br>
 *    Open-TEE is an open virtual Trusted Execution Environment which conforms to the GP TEE Specifications.
 *    For devices which are not equipped with real hardware-based TEE, it can provide a virtual TEE for
 *    developers to debug and deploy TAs before shipping applications to a real TEE.
 *<p>
 *<STRONG>API Design</STRONG><br>
 *    1. What are these Java APIs and what their relationships with GP TEE Client Specification?<br>
 *    In general, these APIs are the Java version of C APIs in GP Client API specification with a reformed design to fit
 *    Java development conventions, which mainly target on the Android devices. It can be used to
 *    develop Android CAs which want to utilize the functionality which TAs offer.
 *    It provides all the necessary functions for CAs to communicate with remote TAs just like
 *    the C APIs defined in GP Specification.
 *    <p>
 *    2. Why they are needed?<br>
 *    In GP TEE Client Specification, it only specify the C data types and APIs which limit or complicate
 *    the development of CAs which aim for Android devices. Since Java is the mainstream programming
 *    language to develop Android applications, for Android developers who wants to utilize
 *    the GP C API to enable the communications between CAs and TAs, it would be troublesome
 *    to deal with native code development, especially for those who are not familiar with it, which can result in more
 *    potential bugs and unexpected behaviours if not handled correctly. Under such circumstances,
 *    every developers have to re-write these codes with the similar functionality, which can be a waste
 *    of efforts and error-prone. To avoid such awkward situations, an open-sourced design, which can enable the CAs
 *    communicate with TAs while provide nice and clean public interfaces for Android developers,
 *    is urgent to conquer this issue. With such a thought, the coming public Java APIs are available
 *    for Android developers, which can release them from the burdens of dealing native development in Android.
 *    It might be not that efficient as directly dealing with C APIs but the performance should be in a tolerant level.
 *    In addition, all the implementations of public APIS are open-source for everyone. By taking feedback from developers,
 *    these shared codes can be more bug-free and efficient.
 *    <p>
 *    3. How to use it and what to expect from the APIs?<br>
 *    a. Prerequisites<br>
 *    -  The TA is already deployed in Open-TEE.<br>
 *    -  The Android application which provides a remote TEE Proxy service should be running.<br>
 *    b. Check the descriptions for each API.<br>
 *<p>
 *Bug report to:<br>
 *      rui.yang at aalto.fi<br>
 *<p>
 *Organization:<br>
 *      Security System Group, Aalto University School of Science, Espoo, Finland.<br>
 *<p>
 * <STRONG>Appendix: Example code chapter</STRONG><br>
 *The following example codes demonstrate how to utilize the Java API
 * to communicate with the TAs residing in the TEE.
 * <p>
 *  Firstly, we assume that we get an <code>ITEEClient</code> interface by calling a factory method. The way to obtain an <code>ITEEClient</code>
 *  interface depends on real implementation. The following code is just an example.
 *  <pre>
 *  <code>ITEEClient client = FactoryMethodWrappers.newTEEClient();</code>
 *  </pre>
 *
 *  Right now, we want to establish a connection to a remote TEE Proxy service so that we can
 *  interact with the TAs running inside of TEE. By using the <code>ITEEClient</code> interface we obtained from last step,
 *  we can establish a connection to a remote TEE by calling <code>initializeContext</code> method in <code>ITEEClient</code> interface.
 *  For the two input parameters, please refer to the API definition in <code>ITEEClient.IContext</code> interface.
 *  If no exception is caught, a valid <code>IContext</code> interface will be returned and program flow continues to next block of code.
 *  Otherwise, the returned <code>IContext</code> interface will be null and an exception will be threw. For different
 *  kinds of exceptions can be threw by this API, please also refer to this API definition in <code>ITEEClient.IContext</code> interface.
 *  In the handling exception code block, it is recommended to re-initializeContext again and the program
 *  flow should not continue until it get a valid <code>IContext</code> interface.
 *<pre>
 * <code>
 *   ITEEClient.IContext ctx = null;
 *
 *  final String param_TEE_NAME = null; // connect to the default TEE.
 *  final android.context.Context param_app_context = getApplicationContext();
 *
 *  try {
 *      ctx = client.initializeContext(param_TEE_NAME,
 *                                     param_app_context);
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 * After we successfully connected to the remote TEE Proxy service, in order to interact with one TA,
 * we must open a session to the TA by providing correct authentication data. To open a session,
 * the function <code>openSession</code> within <code>IContext</code> interface must be called. For
 * the input parameters for the API and possible exceptions threw by it, please refer to the API
 * definition in <code>ITEEClient.IContext</code> interface. For the creation of param_operation parameter,
 * please refer to the example code which creates an <code>IOperation</code> interface using the factory method <code>newOperation</code> in the coming sections.
 *<pre>
 * <code>
 *   ITEEClient.ISession ses = null;
 *
 *  final UUID param_uuid = new UUID(0x1234567887654321L, 0x0102030405060708L);
 *  final ConnectionMethod param_conn_method =
 *          ITEEClient.IContext.ConnectionMethod.LoginPublic;
 *  final Integer param_conn_data = null;
 *
 *  try {
 *      ses = ctx.openSession(param_uuid,
 *                  param_conn_method,
 *                  param_conn_data,
 *                  param_operation);
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * After successfully opened a session to a specific TA, a valid <code>ITEEClient.ISession</code> interface will be returned.
 * So we can interact with TA by using <code>invokeCommand</code> API within the <code>ITEEClient.ISession</code> interface.
 * The creation of param_operation please also refer to the same example code which creates an <code>IOperation</code> interface.
 *<pre>
 * <code>
 *   final int param_comm_id = 0x12345678;
 *
 *  try{
 *      ses.invokeCommand(param_comm_id,
 *                        param_operation);
 *  }catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * In some cases, data is needed to be transferred between CAs and TAs. So the API provides two different kinds of
 * data encapsulation mechanisms. After that, they can be encapsulated again within <code>ITEEClient.IOperation</code> which can be sent to TA during <code>openSession</code> or <code>invokeCommand</code> calls.
 *
 * <p>
 * The first approach is to create an <code>ITEEClient.IValue</code> interface by calling
 * <code>newValue</code> factory method in <code>ITEEClient</code>. So up to 2 integer
 * values can be encapsulated. The two values are given when calling <code>newValue</code> function and
 * further interactions with this pair of values are defined in the <code>ITEEClient.IValue</code> interface.
 *<pre>
 * <code>
 *   final ITEEClient.IValue.Flag param_flag = ITEEClient.IValue.Flag.TEEC_VALUE_INOUT;
 *
 *  int param_value_A = 66;
 *  int param_value_B = 88;
 *
 *  ITEEClient.IValue val = client.newValue(param_flag,
 *                                          param_value_A,
 *                                          param_value_B);
 * </code>
 *</pre>
 *
 * Another approach to transfer the data is using shared memory. The notation shared memory in here works
 * as follows. Firstly, CA create a byte array as the buffer for the shared memory. Then, the CA registers
 * the byte array as a shared memory to the TA so that TA can also operate on the buffer. To create a
 * shared memory, the CA must call <code>registerSharedMemory</code> method in <code>ITEEClient.IContext</code>.
 * An <code>ITEEClient.ISharedMemory</code> interface will be returned.
 *<pre>
 * <code>
 *   ITEEClient.ISharedMemory sm = null;
 *
 *  byte[] param_byte_array = new byte[256];
 *
 *  ITEEClient.ISharedMemory param_flags =
 *          ITEEClient.ISharedMemory.TEEC_MEM_INPUT |
 *          ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT;
 *
 *  try{
 *      sm = ctx.registerSharedMemory(param_byte_array,
 *                                    param_flags);
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * After encapsulating the data within <code>IValue</code> interface or <code>ISharedMemory</code> interface,
 * in order to share the data with TA, we must encapsulate these interfaces again into an <code>ITEEClient.IOperation</code>
 * interface which then can be passed to TA during <code>openSession</code> or <code>invokeCommand</code> calls.
 * The <code>IValue</code> interface can be directly used. However, to use the shared memory, the <code>ISharedMemory</code>
 * interface must be encapsulated again into an <code>ITEEClient.IRegisteredMemoryReference</code> interface.
 * Then along with the <code>IValue</code> interface, it can be used to create an <code>ITEEClient.IOperation</code> interface.
 * To create an <code>IRegisteredMemoryReference</code> interface, the factory method <code>newRegisteredMemoryReference</code>
 * within <code>ITEEClient</code> must be called.
 *<pre>
 * <code>
 *   ITEEClient.IRegisteredMemoryReference.Flag param_flags =
 *          ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INOUT;
 *
 *  final param_offset = 0;
 *
 *  ITEEClient.IRegisteredMemoryReference rmr =
 *          client.newRegisteredMemoryReference(sm,
 *                                              param_flags,
 *                                              param_offset);
 * </code>
 *</pre>
 *
 * To create an <code>IOperation</code> interface, the factory method <code>newOperation</code>
 * within <code>ITEEClient</code> must be called. The input parameters can be up to 4 <code>IValue</code>
 * or <code>IRegisteredMemoryReference</code> interfaces.
 *<pre>
 * <code>
 *   ITEEClient.IOperation op = client.newOperation(rmr, val);
 * </code>
 *</pre>
 *
 * <STRONG>Resource cleaning up</STRONG>
 * <p>
 *
 * If shared memory is no longer needed, it must be released by calling <code>releaseSharedMemory</code>
 * function within <code>IContext</code> interface.
 *<pre>
 * <code>
 *   try {
 *      ctx.releaseSharedMemory(sm);
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * The session also must be closed if CA no longer wants to interact with the TA.
 *<pre>
 * <code>
 *   try {
 *      ses.closeSession();
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 *
 * Once CA no longer need to communicate with TEE, the context must be finalized. Be aware to release
 * all the resources, mainly shared memory, and close all sessions before finalizing context.
 *<pre>
 * <code>
 *   try {
 *      ctx.finalizeContext();
 *  } catch (TEEClientException e) {
 *      // handle TEEClientException here.
 *  }
 * </code>
 *</pre>
 */
package fi.aalto.ssg.opentee;