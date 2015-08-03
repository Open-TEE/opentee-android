package fi.aalto.ssg.opentee_mainapp;

public class Constants {

    // Used as name for the directory containing our whole installation
    public static final String OPENTEE_DIR_NAME = "opentee";
    public static final String OPENTEE_BIN_DIR = "bin";
    public static final String OPENTEE_TA_DIR = "ta";
    public static final String OPENTEE_TEE_DIR = "tee";

    // These describe where the files are located in the assets/
    public static final String OPENTEE_ENGINE_ASSET_BIN_NAME = "opentee-engine";
    public static final String STORAGE_TEST_ASSET_BIN_NAME = "storage_test";
    public static final String STORAGE_TEST_CA_ASSET_BIN_NAME = "storage_test_ca";
    public static final String PKCS11_TEST_ASSET_BIN_NAME = "pkcs11_test";
    public static final String LIB_TA_STORAGE_TEST_ASSET_TA_NAME = "libta_storage_test.so";
    public static final String LIB_TA_PKCS11_ASSET_TA_NAME = "libta_pkcs11_ta.so";
    public static final String LIB_TA_CONN_TEST_APP_ASSET_TA_NAME = "libta_conn_test_app.so";
    public static final String LIB_LAUNCHER_API_ASSET_TEE_NAME = "libLauncherApi.so";
    public static final String LIB_MANAGER_API_ASSET_TEE_NAME = "libManagerApi.so";
    public static final String OPENTEE_CONF_NAME = "opentee.conf.android";

    // Placeholder used in the assets/opentee.conf.android to be replaced at installation with the app data home dir
    public static final String OPENTEE_DIR_CONF_PLACEHOLDER = "OPENTEEDIR";

}
