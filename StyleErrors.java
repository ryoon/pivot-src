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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read the file(s) given on the command line, which are presumed to be
 * the output of the "check styles", and generate a summary of the results
 * for each one.
 */
public final class StyleErrors {
    /** Private constructor because we only use static methods here. */
    private StyleErrors() {
    }

    /**
     * A summary object holding one type of error and the number of times
     * it was encountered.
     */
    private static class Info {
        /** The error class from the check styles configuration. */
        private String errorClass;
        /** The final count of how many times this error was encountered. */
        private Integer count;
        /** A set of the files it was found in. */
        private Set<String> files;

        /**
         * Construct one with a starting count of 1 and the given error class
         * and first file name.
         * @param errClass The checkstyle error.
         * @param fileName The first file encountered with this error.
         */
        Info(final String errClass, final String fileName) {
            this.errorClass = errClass;
            this.count = Integer.valueOf(1);
            this.files = new HashSet<>();
            this.files.add(fileName);
        }

        /**
         * Record another occurrence of this error in the given file.
         * @param fileName The next file name to add (which also
         * increases the count).
         */
        void addFile(final String fileName) {
            count++;
            files.add(fileName);
        }

        /** @return The saved checkstyle error name. */
        String getErrorClass() {
            return errorClass;
        }
        /** @return The final count of this error type. */
        Integer getCount() {
            return count;
        }
        /** @return The set of files this error was found in. */
        Set<String> getFileSet() {
            return files;
        }
    }

    /**
     * A comparator that sorts first by count and then by the error class name.
     */
    private static Comparator<Info> comparator = new Comparator<Info>() {
        @Override
        public int compare(final Info o1, final Info o2) {
            // Order first by count, then by class name
            int c1 = o1.count.intValue();
            int c2 = o2.count.intValue();
            if (c1 == c2) {
                return o1.errorClass.compareTo(o2.errorClass);
            } else {
                return Integer.signum(c1 - c2);
            }
        }
    };

    /**
     * Get a list of strings as a parenthesized list.
     */
    private static String list(Set<String> strings) {
        StringBuilder buf = new StringBuilder("(");
        int i = 0;
        for (String s : strings) {
            if (i++ > 0)
                buf.append(", ");
            buf.append(s);
        }
        buf.append(')');
        return buf.toString();
    }

    /** Pattern used to parse each input line. */
    private static final Pattern LINE_PATTERN =
        Pattern.compile("^\\[[A-Z]+\\]\\s+(([a-zA-Z]\\:)?([^:]+))(\\:[0-9]+\\:)([0-9]+\\:)?\\s+(.+)\\s+(\\[[a-zA-Z]+\\])$");
    /** The group in the {@link #LINE_PATTERN} that contains the file name. */
    private static final int FILE_NAME_GROUP = 1;
    /** The group in the {@link #LINE_PATTERN} that contains the checkstyle error name. */
    private static final int CLASS_NAME_GROUP = 7;
    /** Format error info with a number of files suffix. */
    private static final String FORMAT1 = "%1$2d. %2$-30s%3$5d (%4$d)%n";
    /** Same as {@link #FORMAT1} except we have a list of file names instead of a number. */
    private static final String FORMAT2 = "%1$2d. %2$-30s%3$5d %4$s%n";
    /** Format postreport info. */
    private static final String FORMAT3 = "    %1$-30s%2$5d (%3$d)%n";
    /** Format string used to print the underlines. */
    private static final String UNDER_FORMAT = "%1$3s %2$-30s%3$5s %4$s%n";
    /** The set of unique file names found in the list. */
    private static Set<String> fileNameSet = new HashSet<>();
    /** For each type of checkstyle error, the name and running count for each. */
    private static Map<String, Info> workingSet = new TreeMap<>();
    /** At the end of each file, the list used to sort by count and name. */
    private static List<Info> sortedList = new ArrayList<>();

    /**
     * The main method, executed from the command line, which reads through each file
     * and processes it.
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        for (String arg : args) {
            int total = 0;
            int lineNo = 0;
            workingSet.clear();
            sortedList.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(arg)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNo++;
                    Matcher m = LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        String fileName = m.group(FILE_NAME_GROUP);
                        fileNameSet.add(fileName);
                        File f = new File(fileName);
                        String nameOnly = f.getName();
                        String errorClass = m.group(CLASS_NAME_GROUP);
                        Info info = workingSet.get(errorClass);
                        if (info == null) {
                            workingSet.put(errorClass, new Info(errorClass, nameOnly));
                        } else {
                            info.addFile(nameOnly);
                        }
                        total++;
                    } else if (line.equals("Starting audit...") || line.equals("Audit done.")) {
                        continue;
                    } else {
                        System.err.println("Line " + lineNo + ". Doesn't match the pattern.");
                        System.err.println("\t" + line);
                    }
                }
            } catch (IOException ioe) {
                System.err.println("Error reading the \"" + arg + "\" file: " + ioe.getMessage());
            }

            // Once we're done, resort according to error counts
            for (String key : workingSet.keySet()) {
                Info info = workingSet.get(key);
                sortedList.add(info);
            }
            Collections.sort(sortedList, comparator);

            // Output the final summary report for this input file
            System.out.format(UNDER_FORMAT, " # ", "Category", "Count", "File(s)");
            System.out.format(UNDER_FORMAT, "---", "----------------------------", "-----", "---------------");
            int categoryNo = 0;
            for (Info info : sortedList) {
                categoryNo++;
                Set<String> files = info.getFileSet();
                if (files.size() > 3) {
                    System.out.format(FORMAT1, categoryNo, info.getErrorClass(), info.getCount(), files.size());
                } else {
                    System.out.format(FORMAT2, categoryNo, info.getErrorClass(), info.getCount(), list(files));
                }
            }

            System.out.format(UNDER_FORMAT, "---", "----------------------------", "-----", "---------------");
            System.out.format(FORMAT3, "Totals", total, fileNameSet.size());
            System.out.println();
        }
    }

}

