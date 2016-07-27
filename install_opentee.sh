#!/bin/bash
#
# Copyright (C) 2016 Aalto University.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Open-TEE install targets
OT_PROGS=${OT_PROGS-"opentee-engine"}
OT_LIBSX=${OT_LIBSX-"libLauncherApi libManagerApi"}

## Set your building abi version here.
ABIS=${ABIS-"armeabi"}

# Destination
OT_ASSETS=${OT_ASSETS-"opentee/src/main/assets/$ABIS"}
OT_JNILIBS=${OT_JNILIBS-"opentee/src/main/jniLibs/$ABIS"}

# Write error message to stdout and exit
fail(){
	echo "$basename $0: $1" >&2
	exit 1
}

# Make sure ANDROID_PRODUCT_OUT is set for the generated files of Open-TEE
if [ -z "$ANDROID_PRODUCT_OUT" ]; then
	fail "ANDROID_PRODUCT_OUT not set, aborting..."
fi

if [ -z "$ABIS" ]; then
	fail "ABIS not set, aborting..."
fi

# Push libs
cp $ANDROID_PRODUCT_OUT/system/lib/*.so $OT_JNILIBS/ || fail "unable to copy libraries, aborting..."

# Push bin
cp "$ANDROID_PRODUCT_OUT/system/bin/opentee-engine" $OT_ASSETS/ || fail "unable to copy opentee-engine, aborting..."

for target in $OT_LIBSX
do
	mv "$OT_JNILIBS/${target}.so" $OT_ASSETS/ || fail "unable to copy $target, aborting..."
done

echo "Done"
exit 0
