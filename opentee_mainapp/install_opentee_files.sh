#!/bin/bash

# the usage message
usage() {
    echo "A quick script that copies all the binary and .so library files that"
    echo "are generated when compiling OpenTEE for android to the"
    echo "appropriate directories in the android studio project that packages it"
    echo
    echo "The directories that are given as input to the script should correspond to"
    echo "the directories that contain the subfolders {system,symbols,obj} and are generated"
    echo "when compiling opentee for each android cpu architecture. Usually after compiling against"
    echo "the Android source tree the \$OUT variable contains the directory with the output expected here."
    echo "Usage is: $(basename $0) OPTION"
    echo
    echo "  Options:"
    echo "    -x  [directory]"
    echo "    -a  [directory]"
    echo "    -n  [directory] (in most cases this can be the same as the [arm] directory"
    echo "    -h  this help message"
    exit 1
}

if [ $# -eq 0 ]; then
    echo "No arguments provided"
    echo
    usage
    exit 1
fi

copy_files () {
    # $1 is origin folder, $2 is the destination subfolder (x86,armeabi,armeabi-v7a)
    echo "Copying files from $1 to $2 subfolder..."
    rsync -avv --exclude "libtee.so" --exclude "libtee_pkcs11.so" $1/system/lib/*.so src/main/libs/$2
    rsync -avv $1/system/bin/* src/main/assets/$2
    rsync -avv $1/system/lib/*.so src/main/assets/$2
    echo
}

args=$(getopt -l "searchpath:" -o "x:a:n:h" -- "$@")

eval set -- "$args"

while [ $# -ge 1 ]; do
        case "$1" in
                --)
                    # No more options left.
                    shift
                    break
                   ;;
                -x)
                        copy_files $2 "x86"
                        shift
                        ;;
                -a)
                        copy_files $2 "armeabi"
                        shift
                        ;;
                -n)
                        copy_files $2 "armeabi-v7a"
                        shift
                        ;;
                -h)
                        usage
                        shift
                        ;;
        esac

        shift
done

