/*
 * Copyright (c) 2016 Aalto University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.aalto.ssg.opentee.openteeandroid;

/**
 * This class defines all the global constant variables in this project
 * Credit: based on previous opentee-android project.
 */
public class OTConstants {
    //directory names
    public static final String OT_DIR_NAME = "opentee";
    public static final String OT_BIN_DIR = "bin";
    public static final String OT_LIB_DIR = "lib";
    public static final String OT_TEE_DIR = "tee";
    public static final String OT_TA_DIR = "ta";

    //file name
    public static final String OPENTEE_ENGINE_ASSET_BIN_NAME = "opentee-engine";
    public static final String OPENTEE_CONF_NAME = "opentee.conf.android";
    public static final String OPENTEE_SECURE_STORAGE_DIRNAME = ".TEE_secure_storage";
    public static final String OPENTEE_SOCKET_FILENAME = "open_tee_socket";
    public static final String OPENTEE_PID_FILENAME = "opentee-engine.pid";

    //lib paths
    public static final String LIB_LAUNCHER_API_ASSET_TEE_NAME = "libLauncherApi.so";
    public static final String LIB_MANAGER_API_ASSET_TEE_NAME = "libManagerApi.so";

    //utility names
    public static final String OPENTEE_DIR_CONF_PLACEHOLDER = "OPENTEEDIR";
}
