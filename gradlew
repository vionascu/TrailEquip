#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls -ld "$PRG"
    PRG=`readlink "$PRG"`
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/" >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "`uname`" in
  CYGWIN* )
    cygwin=true
    ;;
  Darwin* )
    darwin=true
    ;;
  MSYS* | MINGW* )
    msys=true
    ;;
  NONSTOP* )
    nonstop=true
    ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar


# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/bin/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/bin/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Increase the maximum file descriptors if we can.
if [ "$cygwin" = "false" -a "$darwin" = "false" -a "$nonstop" = "false" ] ; then
    MAX_FD_LIMIT=`ulimit -H -n`
    if [ $? -eq 0 ] ; then
        if [ "$MAX_FD" = "maximum" -o "$MAX_FD" = "max" ] ; then
            MAX_FD="$MAX_FD_LIMIT"
        fi
        ulimit -n $MAX_FD
        if [ $? -ne 0 ] ; then
            warn "Could not set maximum file descriptor limit: $MAX_FD"
        fi
    else
        warn "Could not query maximum file descriptor limit: $MAX_FD_LIMIT"
    fi
fi

# For Darwin, add options to specify how the application appears in the dock
if [ "$darwin" = "true" ] ; then
    GRADLE_OPTS="$GRADLE_OPTS \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
fi

# For Cygwin or MSYS, switch paths to Windows format before running java
if [ "$cygwin" = "true" -o "$msys" = "true" ] ; then
    APP_HOME=`cygpath --path --mixed "$APP_HOME"`
    CLASSPATH=`cygpath --path --mixed "$CLASSPATH"`

    JAVACMD=`cygpath --unix "$JAVACMD"`

    # Now convert the arguments - kludge to limit ourselves to /bin/sh
    for arg do
        if expr "x$arg" : x'-.*' > /dev/null ; then
            SEP="="
            arg_a=`expr "x$arg" : 'x\([^=]*\)='`
            if [ -z "$arg_a" ] ; then
                SEP=" "
                arg_a=`expr "x$arg" : 'x\([^=]*\) .*'`
            fi
            arg_b=`expr "x$arg" : '.*=\(.*\)$'`
            expr "x$arg_b" : x'-.*' > /dev/null && CLASSPATH_MARKER="-"
            arg_b=`cygpath --path --ignore --mixed "$arg_b"`
            arg="$arg_a$SEP$arg_b"
            if [ -z "$CLASSPATH_MARKER" ] ; then
                args="$args \"$arg\""
            else
                ifs="$IFS"
                IFS=:
                for arg in $args; do
                    IFS="$ifs"
                    if expr "x$arg" : x'-.*' > /dev/null ; then
                        args="$args -classpath \"$CLASSPATH\" \"$arg\""
                        CLASSPATH=""
                        CLASSPATH_MARKER=""
                    else
                        args="$args \"$arg\""
                    fi
                done
                IFS="$ifs"
            fi
        else
            if expr "x$arg" : x'-.*' > /dev/null ; then
                CLASSPATH_MARKER="$arg"
            fi
            args="$args \"$arg\""
        fi
    done
    # Variadic args and unquoted args are separated via this marker
    # Adding a per-argument CLASSPATH to support using vindle.bat as test
    eval "set -- $args"

    # User values on command line have higher priority, so prepend them
    if [ -z "$JAVA_OPTS" ]; then
        JAVA_OPTS="$DEFAULT_JVM_OPTS"
    fi
fi

# Escape application args
save () {
    for i do printf %s\\n "$i" | sed "s/'/'\\\\''/g;1s/^/'/;\$s/\$/' \\\\/" ; done
    echo " "
}
APP_ARGS=`save "$@"`

# Collect all arguments for the java command, stacking in reverse order
for i
do
    case $i in
      -conf  ) 2>&1 ;;
      -? ) 2>&1; exit 1 ;;
      * )
        files="$files $i"
        ;;
    esac
done

# In Cygwin, switch paths to Unix format before running java
if [ "$cygwin" = "true" ]; then
    APP_HOME=`cygpath --unix "$APP_HOME"`
    CLASSPATH=`cygpath --unix "$CLASSPATH"`
    JAVACMD=`cygpath --unix "$JAVACMD"`
    for i in $args; do
        i=`cygpath --unix "$i"`
        args="$args $i"
    done
fi

# Collect all arguments for the java command
# In Cygwin, use the mixed mode
if [ "$cygwin" = "true" ]; then
    CLASSPATH=`cygpath --mixed "$CLASSPATH"`
fi

# Determine the location of the Java command
if [ ! -x "$JAVACMD" ] ; then
    echo "Error: JAVA_HOME is not properly set." >&2
    command -v java >/dev/null 2>&1 || { echo >&2 "Gradle requires Java but it is not installed."; exit 1; }
fi

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
	-classpath "$CLASSPATH" \
	org.gradle.wrapper.GradleWrapperMain \
	"$@"
