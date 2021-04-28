/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.util;

import java.io.Serializable;


/**
 * Represents a version number. Version numbers are defined as: <p>
 * <i>major</i>.<i>minor</i>.<i>maintenance</i>_<i>update</i> <p> for example,
 * "JDK 1.6.0_10".
 */
public class Version implements Comparable<Version>, Serializable {
    private static final long serialVersionUID = -3677773163272115116L;

    /**
     * The default version object (0, 0, 0, 0).
     */
    public static final Version ZERO_VERSION = new Version(0, 0, 0, 0L);

    /**
     * The real major version number.
     */
    private short majorVersion = 0;
    /**
     * The real minor version number.
     */
    private short minorVersion = 0;
    /**
     * The real maintenance version number.
     */
    private short maintenanceVersion = 0;
    /**
     * The real update version number.
     */
    private long updateRevision = 0;
    /**
     * The build string.
     */
    private String build = null;

    /**
     * Construct a version given all the numeric values.
     *
     * @param major       The new major version.
     * @param minor       The new minor version.
     * @param maintenance The new maintenance version.
     * @param update      The new update version.
     */
    public Version(final int major, final int minor, final int maintenance, final long update) {
        this(major, minor, maintenance, update, null);
    }

    /**
     * Construct a version given all the information.
     *
     * @param major       The new major version.
     * @param minor       The new minor version.
     * @param maintenance The new maintenance version.
     * @param update      The new update version.
     * @param buildString The new build string.
     */
    public Version(final int major, final int minor, final int maintenance, final long update,
            final String buildString) {
        Utils.checkInRangeOfShort(major, "majorVersion");
        Utils.checkInRangeOfShort(minor, "minorVersion");
        Utils.checkInRangeOfShort(maintenance, "maintenanceVersion");

        majorVersion = (short) major;
        minorVersion = (short) minor;
        maintenanceVersion = (short) maintenance;
        updateRevision = update;
        build = buildString;
    }

    /**
     * @return The major version number.
     */
    public short getMajorRevision() {
        return majorVersion;
    }

    /**
     * @return The minor version number.
     */
    public short getMinorRevision() {
        return minorVersion;
    }

    /**
     * @return The maintenance version number.
     */
    public short getMaintenanceRevision() {
        return maintenanceVersion;
    }

    /**
     * @return The update revision number.
     */
    public long getUpdateRevision() {
        return updateRevision;
    }

    /**
     * @return A composite value, consisting of all the numeric components
     *         shifted into parts of a long.
     */
    public long getNumber() {
        long number = ((long) ((majorVersion) & 0xffff) << (16 * 3)
            | (long) ((minorVersion) & 0xffff) << (16 * 2)
            | (long) ((maintenanceVersion) & 0xffff) << (16 * 1))
            + updateRevision;

        return number;
    }

    @Override
    public int compareTo(final Version version) {
        return Long.compare(getNumber(), version.getNumber());
    }

    @Override
    public boolean equals(final Object object) {
        return (object instanceof Version && compareTo((Version) object) == 0);
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getNumber()).hashCode();
    }

    @Override
    public String toString() {
        String string = majorVersion
            + "." + minorVersion
            + "." + maintenanceVersion
            + "_" + String.format("%02d", updateRevision);

        if (build != null) {
            string += "-" + build;
        }

        return string;
    }

    /**
     * @return A three-component string with "major.minor.maintenance".
     */
    public String simpleToString() {
        return String.format("%1$d.%2$d.%3$d",
            majorVersion,
            minorVersion,
            maintenanceVersion);
    }

    /**
     * Decode a string into the version parts.
     *
     * @param string The input string in a format recognizable as a version string.
     * @return       The new version object constructed from the string information.
     */
    public static Version decode(final String string) {
        Version version = null;

        short major = 0;
        short minor = 0;
        short maintenance = 0;
        long update = 0;
        String buildString = null;

        String revision;

        // Some "version" strings separate fields with a space
        // While Java 9 uses a new scheme where "build" uses a "+"
        String[] parts = string.split("[ +\\-]");
        if (parts.length == 1) {
            revision = string;
        } else {
            int len = parts[0].length();
            revision = string.substring(0, len);
            buildString = string.substring(len + 1);
        }

        String[] revisionNumbers = revision.split("\\.");

        if (revisionNumbers.length > 0) {
            major = Short.parseShort(revisionNumbers[0]);

            if (revisionNumbers.length > 1) {
                minor = Short.parseShort(revisionNumbers[1]);

                if (revisionNumbers.length > 2) {
                    String[] maintenanceVersionNumbers = revisionNumbers[2].split("[_\\-]");

                    if (maintenanceVersionNumbers.length > 0) {
                        maintenance = Short.parseShort(maintenanceVersionNumbers[0]);

                        if (maintenanceVersionNumbers.length > 1) {
                            update = Long.parseLong(maintenanceVersionNumbers[1]);
                        }
                    }
                }
            }

            version = new Version(major, minor, maintenance, update, buildString);
        }

        return version;
    }

    /**
     * Added so that any unexpected version string formats that might cause an error
     * will not also cause the application to fail to start.
     *
     * @param versionString A potential version string to parse/decode.
     * @return The parsed version information (if possible), or an empty version
     * (that will look like: "0.0.0_00") if there was a parsing problem of any kind.
     */
    public static Version safelyDecode(final String versionString) {
        if (!Utils.isNullOrEmpty(versionString)) {
            try {
                return decode(versionString);
            } catch (Throwable ex) {
                String exMsg = ExceptionUtils.toString(ex);
                System.err.println("Error decoding version string \"" + versionString + "\": " + exMsg);
            }
        }
        return ZERO_VERSION;
    }


    /**
     * Decode the implementation version found in our jar file manifest into
     * a {@link Version} object.
     *
     * @return Our version.
     */
    public static Version implementationVersion() {
        return decode(Version.class.getPackage().getImplementationVersion());
    }
}

