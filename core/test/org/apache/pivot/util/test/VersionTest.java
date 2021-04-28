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
package org.apache.pivot.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import org.apache.pivot.util.Version;


/**
 * Tests of the {@link Version} class.
 */
public class VersionTest {
    /**
     * Test version decode that happens at application startup.
     */
    @Test
    public void testApplicationStartup() {
        // These are the things that happen right away for ApplicationContext
        // and therefore would break immediately if Version was broken (as did
        // happen for Java 8u131).
        // Get the JVM version
        Version jvmVersion = Version.decode(System.getProperty("java.vm.version"));
        System.out.format("JVM Version: %1$s%n", jvmVersion.toString());
        Version pivotVersion = null;
        Package corePackage = Version.class.getPackage();

        // Get the Java runtime version
        Version javaVersion = Version.decode(System.getProperty("java.version"));
        System.out.format("Java version: %1$s%n", javaVersion);

        // Get the Pivot version
        String version = corePackage.getImplementationVersion();
        if (version == null) {
            pivotVersion = new Version(0, 0, 0, 0);
            assertEquals("default Pivot version", "0.0.0_00", pivotVersion.toString());
        } else {
            pivotVersion = Version.decode(version);
            System.out.format("Pivot Version: %1$s%n", pivotVersion);
        }
    }

    /** The Java version string that broke the old code (build overflows a byte). */
    private static final String S1_8_131 = "1.8.0_131";
    /** Decoding that breaking version string. */
    private static final Version V1 = Version.decode(S1_8_131);
    /** The version object that should match that decoded value. */
    private static final Version V8_131 = new Version(1, 8, 0, 131);
    /** A version 1.0 string. */
    private static final String S1_0_0 = "1.0.0_00";
    /** Decoding version 1. */
    private static final Version V0 = Version.decode(S1_0_0);
    /** The version object that should match version 1. */
    private static final Version V1_0 = new Version(1, 0, 0, 0);
    /** The null/empty version object. */
    private static final Version EMPTY = new Version(0, 0, 0, 0);

    /**
     * Test basic parsing (including our overflow fail case).
     */
    @Test
    public void testVersionParsing() {
        assertEquals("version decode", V8_131, V1);
        assertEquals("version to string", V8_131.toString(), S1_8_131);

        assertEquals("version 0 decode", V1_0, V0);
        assertEquals("version 0 to string", S1_0_0, V1_0.toString());

        // New Java 9 version number scheme
        String j9 = "9.0.1+11";
        Version vj9 = Version.decode(j9);
        Version vj90 = new Version(9, 0, 1, 0);
        assertEquals("Java version 9 decode", vj90, vj9);
        assertEquals("Java version 9 to string", "9.0.1_00-11", vj9.toString());
    }

    /**
     * Test overflows of the new short values.
     */
    @Test
    public void testLimits() {
        Version vMax = new Version(32767, 32767, 32767, 32767);
        String sMax = "32767.32767.32767_32767";
        assertEquals("max versions", sMax, vMax.toString());
        IllegalArgumentException argFailure = null;
        try {
            Version vOverflow = new Version(32768, 0, 1, 0);
        } catch (IllegalArgumentException iae) {
            argFailure = iae;
        }
        assertNotNull("illegal argument exception", argFailure);
        assertEquals("exception message",
            "majorVersion must be less than or equal 32767.",
            argFailure.getMessage());
    }

    /**
     * Test {@link Version#getNumber}.
     */
    @Test
    public void testNumber() {
        Version vNum = new Version(2, 1, 1, 100);
        long num = vNum.getNumber();
        System.out.format("test getNumber(): %1$s -> %2$d (0x%2$016x)%n", vNum, num);
        assertEquals("long number", 0x0002000100010064L, num);
    }

    /** Taken from PIVOT-996 test case -- the build suffix ... */
    private static final String PIVOT_996_SUFFIX = "25.51-b14";
    /** PIVOT-996 complete input string. */
    private static final String PIVOT_996_INPUT  = "8.1.028 " + PIVOT_996_SUFFIX;
    /** What we expect from PIVOT-996 test case on output. */
    private static final String PIVOT_996_OUTPUT = "8.1.28_00-" + PIVOT_996_SUFFIX;

    /**
     * Other test cases (from issues, or other sources).
     */
    @Test
    public void testOtherVersions() {
        Version jvmVersionParsed = Version.decode(PIVOT_996_INPUT);
        Version jvmVersionExplicit = new Version(8, 1, 28, 0, PIVOT_996_SUFFIX);
        String parsedToString = jvmVersionParsed.toString();

        System.out.println("Information only: our version = " + Version.implementationVersion());

        assertEquals("PIVOT-996 test case", jvmVersionExplicit, jvmVersionParsed);
        System.out.format("PIVOT-996 parsed/toString: %1$s, expected: %2$s%n",
            parsedToString, PIVOT_996_OUTPUT);
        assertEquals("PIVOT-996 toString", PIVOT_996_OUTPUT, parsedToString);

        Pattern versionPattern = Pattern.compile("(\\d+(\\.\\d+\\.\\d+)?).*");
        String sysJavaVersion = System.getProperty("java.runtime.version");
        Version javaVersion = Version.decode(sysJavaVersion);
        String formattedJavaVersion = javaVersion.toString();
        String simpleVersion = javaVersion.simpleToString();
        System.out.format("Java Runtime version (parsed and formatted): %1$s, raw: %2$s, simple: %3$s%n",
            formattedJavaVersion, sysJavaVersion, simpleVersion);
        Matcher sysMatcher = versionPattern.matcher(sysJavaVersion);
        boolean matches = sysMatcher.matches()
            && simpleVersion.startsWith(sysMatcher.group(1));
        assertTrue("Java Runtime version match", matches);

        String newJava9Version = "9-ea+19";
        Version newJava9 = Version.decode(newJava9Version);
        String newJava9Formatted = newJava9.toString();
        System.out.format("Potential new Java version: %1$s, parsed and formatted: %2$s%n",
            newJava9Version, newJava9Formatted);
        assertEquals("new Java 9 version", "9.0.0_00-ea+19", newJava9Formatted);

        String newJava10Version = "10+-ea";
        Version newJava10 = Version.decode(newJava10Version);
        String newJava10Formatted = newJava10.toString();
        System.out.format("Potential new Java 10 version: %1$s, parsed and formatted: %2$s%n",
            newJava10Version, newJava10Formatted);
        assertEquals("new Java 10 version", "10.0.0_00--ea", newJava10Formatted);
    }

    /**
     * Complete test of the new version strings possible with Java 9
     * (taken from <a href="http://openjdk.java.net/jeps/223">http://openjdk.java.net/jeps/223</a>).
     */
    @Test
    public void testJava9Versions() {
        String[] versions = {
            "9-ea+19",
            "9+100",
            "9.0.1+20",
            "9.0.2+12",
            "9.1.2+62",
            "9.1.3+15",
            "9.1.4+8",
            "9.2.4+45",
            "7.4.10+11",
            "7.4.11+15",
            "7.5.11+43",
            "7.5.12+18",
            "7.5.13+13",
            "7.5.14+13",
            "7.6.14+19",
            "7.6.15+20",
            "9-ea",
            "9-ea+73",
            "9+100",
            "9",
            "9.1.2",
            "9.1.2+62",
            "9.0.1",
            "9.0.1+20"
        };

        // Just make sure we don't throw or get other errors decoding all these
        for (String version : versions) {
            Version v = Version.decode(version);
            System.out.format("Raw %1$s -> %2$s%n", version, v.toString());
        }
    }

    /**
     * Test the new {@link Version#safelyDecode}.
     */
    @Test
    public void testSafelyDecode() {
        try {
            Version junk = Version.safelyDecode("this is junk");
            assertEquals("safely decode default", EMPTY, junk);
        } catch (Exception ex) {
            fail("safelyDecode threw an exception: " + ex.getMessage());
        }
    }
}

