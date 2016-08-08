# Module description
This module does the following things:

- import the **opentee** module to deploy Open-TEE;
- add **libtee** source code and using _ndk-build_ to build the _libtee.so_;
- its **NativeLibtee** as the JNI layer which uses the _libtee.so_ to communicate with the Open-TEE and Google Protocol Buffers to serialize data between Java and native code;
- its TEE Proxy Service uese the **NativeLibtee** to interact with Open-TEE and exposes the functionality of Open-TEE to other CAs via Android Binder.

## install TA from CA
This method can allow CA to install TAs to remote Open-TEE even Open-TEE is running.

**Note**: If you uninstall the TEE Proxy service application, you have to install the TAs again. What's more, install TA before open a session to it.

This method is introduced by the **OTHelper** interface which accepts the TA either coming with a form of a byte array or put under the application **lib** directory. Be aware that the **OTHelper** is an Open-TEE specific interface which is not included in the Java API that we have proposed.

To use the utility functions that **OTHelper** provides, the CA must have a valid **IContext** interface. Since the class which implements the **IContext** interface also implements the **OTHelper** inteface, a valid **OTHelper** can be retreived by just casting the **IContext** interface like the following code.
```Java
OTHelper utils = (OTHelper)ctx;
```

```Java
try {
    // install TA under application lib directory by simply using its name.
    if( !utils.installTA("ta_name.so") ){
        Log.e(TAG, "Install " + OMNISHARE_TA + " failed.");
    }

    // install TA in the form of byte array.
    utils.installTA("ta_name.so", ta_in_bytes);

} catch (CommunicationErrorException e) {
    // handle exception here.
}
```


Make sure that your TA is not rejected by the Open-TEE which can be easily spoted in the log message from the Open-TEE. If error happened, you can see similar error msg as follows:
```shell
E/tee_manager: Open-TEE/emulator/manager/ta_dir_watch.c:add_new_ta:227  TA "incorrectTAExample.so" rejected
```
The rejected TA will not be started by Open-TEE. You can take the correct example TA from <https://github.com/Open-TEE/TAs>.

## License
This source code is available under the terms of the Apache License, Version 2.0: <http://www.apache.org/licenses/LICENSE-2.0>
This project has used [Protocol Buffers](https://developers.google.com/protocol-buffers/) 2.6.1 of Google Inc.
