/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.ssg.opentee;

public class OTConstants {

    // Used as name for the directory containing our whole installation
    public static final String OPENTEE_DIR_NAME = "opentee";
    public static final String OPENTEE_BIN_DIR = "bin";
    public static final String OPENTEE_TA_DIR = "ta";
    public static final String OPENTEE_TEE_DIR = "tee";
    public static final String OPENTEE_LIB_DEPENDENCY_DIR = "lib";

    // These describe where the files are located in the assets/
    public static final String OPENTEE_ENGINE_ASSET_BIN_NAME = "opentee-engine";
    public static final String CONN_TEST_APP_ASSET_BIN_NAME = "conn_test_app";
    public static final String STORAGE_TEST_APP_ASSET_BIN_NAME = "storage_test";
    public static final String STORAGE_TEST_ASSET_BIN_NAME = "storage_test";
    public static final String STORAGE_TEST_CA_ASSET_BIN_NAME = "storage_test_ca";
    public static final String PKCS11_TEST_ASSET_BIN_NAME = "pkcs11_test";
    public static final String LIB_TA_STORAGE_TEST_ASSET_TA_NAME = "libta_storage_test.so";
    public static final String LIB_TA_PKCS11_ASSET_TA_NAME = "libta_pkcs11_ta.so";
    public static final String LIB_TA_CONN_TEST_APP_ASSET_TA_NAME = "libta_conn_test_app.so";
    public static final String LIB_LAUNCHER_API_ASSET_TEE_NAME = "libLauncherApi.so";
    public static final String LIB_MANAGER_API_ASSET_TEE_NAME = "libManagerApi.so";
    public static final String OPENTEE_CONF_NAME = "opentee.conf.android";

    public static final String OPENTEE_SOCKET_FILENAME = "open_tee_socket";
    public static final String OPENTEE_PID_FILENAME = "opentee-engine.pid";
    public static final String OPENTEE_SECURE_STORAGE_DIRNAME = ".TEE_secure_storage";

    // Placeholder used in the assets/opentee.conf.android to be replaced at installation with the app data home dir
    public static final String OPENTEE_DIR_CONF_PLACEHOLDER = "OPENTEEDIR";

}
