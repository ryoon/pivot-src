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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read the file(s) given on the command line, which are presumed to be
 * the output of the "check styles" process, and generate a summary of the
 * results for each one.
 */
public final class StyleErrors {
    /**
     * Enumeration of the severity of a check-style report value.
     * <p> Corresponds directly to the text found in the report.
     */
    private enum Severity {
        /** Only a warning, that is, not necessarily in need of fixing. */
        WARN,
        /** An error, considered something that should be fixed. */
        ERROR;

        /**
         * Lookup one of these from the corresponding string value.
         * @param input The string as found in the report.
         * @return The corresponding enum value.
         * @throws IllegalArgumentException if the input can't be matched.
         */
        static Severity lookup(final String input) {
            for (Severity s : values()) {
                if (s.toString().equalsIgnoreCase(input)) {
                    return s;
                }
            }
            throw new IllegalArgumentException("Unknown Severity value: " + input);
        }
    }

    /** Private constructor because we only use static methods here. */
    private StyleErrors() {
    }

    /**
     * A summary object holding one type of problem and the number of times
     * it was encountered.
     */
    private static class Info {
        /** The problem class from the check styles configuration. */
        private String problemClass;
        /** The severity (ERROR or WARN) of this check. */
        private Severity severity;
        /** The final count of how many times this problem was encountered. */
        private Integer count;
        /** A set of the files it was found in. */
        private Set<String> files;

        /**
         * Construct one with a starting count of 1 and the given problem class
         * and first file name.
         * @param errClass The checkstyle problem.
         * @param fileName The first file encountered with this problem.
         * @param severity Whether this is an error or just a warning.
         */
        Info(final String problemClass, final String fileName, final String severity) {
            this.problemClass = problemClass;
            this.severity = Severity.lookup(severity);
            this.count = Integer.valueOf(1);
            this.files = new HashSet<>();
            this.files.add(fileName);
        }

        /**
         * Record another occurrence of this problem in the given file.
         * @param fileName The next file name to add (which also
         * increases the count).
         */
        void addFile(final String fileName) {
            count++;
            files.add(fileName);
        }

        /** @return The saved checkstyle problem name. */
        String getProblemClass() {
            return problemClass;
        }
        /** @return The severity of this problem. */
        Severity getSeverity() {
            return severity;
        }
        /** @return The final count of this problem type. */
        Integer getCount() {
            return count;
        }
        /** @return The set of files this problem was found in. */
        Set<String> getFileSet() {
            return files;
        }
    }

    /**
     * Keep track of how many problems are in each file.
     */
    private static class FileInfo {
        /** The name (only) of this file. */
        private String fileName;
        /** Count of how many style problems were found in it. */
        private int count;

        /** Construct one from the basic information.
         * @param name Name (only) of the file (that is, no path).
         * @param c Count of how many problems were found.
         */
        FileInfo(final String name, final int c) {
            this.fileName = name;
            this.count = c;
        }

        /** @return The file name in this object. */
        String getName() {
            return fileName;
        }
        /** @return The count for this object. */
        int getCount() {
            return count;
        }
    }

    /**
     * A comparator that sorts first by count and then by the problem class name.
     */
    private static Comparator<Info> comparator = new Comparator<Info>() {
        @Override
        public int compare(final Info o1, final Info o2) {
            // Order first by count, then by class name
            int c1 = o1.count.intValue();
            int c2 = o2.count.intValue();
            if (c1 == c2) {
                return o1.problemClass.compareTo(o2.problemClass);
            } else {
                return Integer.signum(c1 - c2);
            }
        }
    };

    /**
     * Get a list of strings as a parenthesized list.
     * @param strings The set of strings to traverse.
     * @return A nicely formatted list.
     */
    private static String list(final Set<String> strings) {
        StringBuilder buf = new StringBuilder("(");
        int i = 0;
        for (String s : strings) {
            if (i++ > 0) {
                buf.append(", ");
            }
            buf.append(s);
        }
        buf.append(')');
        return buf.toString();
    }

    /** Pattern used to parse each input line. */
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^\\[([A-Z]+)\\]\\s+(([a-zA-Z]\\:)?([^:]+))(\\:[0-9]+\\:)([0-9]+\\:)?\\s+(.+)\\s+(\\[[a-zA-Z]+\\])$"
        );
    /** The group in the {@link #LINE_PATTERN} that contains the severity of the problem. */
    private static final int SEVERITY_GROUP = 1;
    /** The group in the {@link #LINE_PATTERN} that contains the file name. */
    private static final int FILE_NAME_GROUP = 2;
    /** The group in the {@link #LINE_PATTERN} that contains the checkstyle problem name. */
    private static final int CLASS_NAME_GROUP = 8;
    /** Limit on the number of files to enumerate vs just give the number. */
    private static final int NUMBER_OF_FILES_LIMIT = 3;
    /** Format problem info with a number of files suffix. */
    private static final String FORMAT1 = "%1$2d. %2$5s %3$-30s%4$5d (%5$d)%n";
    /** Same as {@link #FORMAT1} except we have a list of file names instead of a number. */
    private static final String FORMAT2 = "%1$2d. %2$5s %3$-30s%4$5d %5$s%n";
    /** Format postreport info. */
    private static final String FORMAT3 = "          %1$-30s%2$5d (%3$d)%n";
    /** Format string used to print the underlines. */
    private static final String UNDER_FORMAT = "%1$3s %2$5s %3$-30s%4$5s %5$s%n";
    /** Format string for the file vs problem count report. */
    private static final String FORMAT4 = "    %1$-42s %2$5d%n";
    /** The set of unique file names found in the list. */
    private static Set<String> fileNameSet = new HashSet<>();
    /** For each type of checkstyle problem, the name and running count for each. */
    private static Map<String, Info> workingSet = new TreeMap<>();
    /** At the end of each file, the list used to sort by count and name. */
    private static List<Info> sortedList = new ArrayList<>();
    /** The count of problems for each file. */
    private static Map<String, Integer> fileCounts = new HashMap<>();
    /** The list of file names, to be sorted by problem counts. */
    private static List<FileInfo> fileCountList = new ArrayList<>();
    /** Number of files to list with least and most problems. */
    private static final int NUMBER_OF_FILES_TO_REPORT = 12;
    /** The latest file name we're working on in the current log file. */
    private static String currentFileName;
    /** Whether we are running on a Windows system. */
    private static final boolean ON_WINDOWS = System.getProperty("os.name").startsWith("Windows");
    /** Whether to report all the file problem counts, or just the least/most. */
    private static boolean verbose = false;
    /** A list of bare file names that are used to filter the summary report. */
    private static List<String> filterFileNames = new ArrayList<>();
    /** Whether the next file name on the command line is a filter name or not. */
    private static boolean filter = false;
    /** Whether we filter the file names by any file names (if {@link #filterFileNames} list
     * is non-empty.
     */
    private static boolean filtered = false;

    /**
     * Process one option from the command line.
     * @param option The option string to process.
     */
    private static void processOption(final String option) {
        switch (option) {
            case "v":
            case "V":
            case "verbose":
            case "VERBOSE":
                verbose = true;
                filter = false;
                break;
            case "f":
            case "F":
            case "filter":
            case "FILTER":
                filter = true;
                break;
            default:
                filter = false;
                System.err.println("Ignoring unrecognized option: \"--" + option + "\"!");
                break;
        }
    }

    /**
     * Report on this particular piece of information.
     * @param num Which category this is in the list.
     * @param info The summary information for the category.
     * @see #FORMAT1
     * @see #FORMAT2
     */
    private static void reportInfo(final int num, final Info info) {
        Set<String> fileSet = info.getFileSet();
        int size = fileSet.size();
        if (size > NUMBER_OF_FILES_LIMIT) {
            System.out.format(FORMAT1, num, info.getSeverity(), info.getProblemClass(),
                    info.getCount(), size);
        } else {
            System.out.format(FORMAT2, num, info.getSeverity(), info.getProblemClass(),
                    info.getCount(), list(fileSet));
        }
    }

    /**
     * The main method, executed from the command line, which reads through each file
     * and processes it.
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        List<File> files = new ArrayList<>(args.length);
        // Process options and save the straight file names
        for (String arg : args) {
            if (arg.startsWith("--")) {
                processOption(arg.substring(2));
            } else if (arg.startsWith("-")) {
                processOption(arg.substring(1));
            } else if (ON_WINDOWS && arg.startsWith("/")) {
                processOption(arg.substring(1));
            } else {
                if (filter) {
                    if (arg.endsWith(".java")) {
                        filterFileNames.add(arg);
                    } else if (arg.indexOf(".") >= 0) {
                        filterFileNames.add(arg);
                    } else {
                        filterFileNames.add(arg + ".java");
                    }
                    filter = false;
                } else {
                    files.add(new File(arg));
                }
            }
        }
        filtered = filterFileNames.size() != 0;

        // Now process just the saved file names
        for (File file : files) {
            int total = 0;
            int totalForThisFile = 0;
            int lineNo = 0;
            fileNameSet.clear();
            workingSet.clear();
            sortedList.clear();
            currentFileName = null;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNo++;
                    Matcher m = LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        String severity = m.group(SEVERITY_GROUP);
                        String fileName = m.group(FILE_NAME_GROUP);
                        File f = new File(fileName);
                        String nameOnly = f.getName();
                        if (filtered) {
                            if (!filterFileNames.contains(nameOnly)) {
                                continue;
                            }
                        }
                        fileNameSet.add(fileName);
                        String problemClass = m.group(CLASS_NAME_GROUP);
                        Info info = workingSet.get(problemClass);
                        if (info == null) {
                            workingSet.put(problemClass, new Info(problemClass, nameOnly, severity));
                        } else {
                            info.addFile(nameOnly);
                        }
                        total++;
                        if (nameOnly.equals(currentFileName)) {
                            totalForThisFile++;
                        } else {
                            if (currentFileName != null) {
                                fileCounts.put(currentFileName, Integer.valueOf(totalForThisFile));
                            }
                            currentFileName = nameOnly;
                            totalForThisFile = 1;
                        }
                    } else if (line.equals("Starting audit...") || line.equals("Audit done.")) {
                        continue;
                    } else {
                        System.err.println("Line " + lineNo + ". Doesn't match the pattern.");
                        System.err.println("\t" + line);
                    }
                }
                if (currentFileName != null) {
                    fileCounts.put(currentFileName, Integer.valueOf(totalForThisFile));
                }
            } catch (IOException ioe) {
                System.err.println("Error reading the \"" + file.getPath() + "\" file: " + ioe.getMessage());
            }

            // Once we're done, resort according to problem counts per category
            for (String key : workingSet.keySet()) {
                Info info = workingSet.get(key);
                sortedList.add(info);
            }
            Collections.sort(sortedList, comparator);

            // Output the final summary report for this input file
            System.out.format(UNDER_FORMAT, " # ", " Sev ", "Category", "Count", "File(s)");
            System.out.format(UNDER_FORMAT, "---", "-----", "----------------------------", "-----", "---------------");
            int categoryNo = 0;
            for (Info info : sortedList) {
                reportInfo(++categoryNo, info);
            }

            System.out.format(UNDER_FORMAT, "---", "-----", "----------------------------", "-----", "---------------");
            System.out.format(FORMAT3, "Totals", total, fileNameSet.size());
            System.out.println();

            // Take the file counts and generate a list of the data for sorting
            fileCountList.clear();
            for (Map.Entry<String, Integer> entry : fileCounts.entrySet()) {
                FileInfo info = new FileInfo(entry.getKey(), entry.getValue());
                fileCountList.add(info);
            }

            // The list is sorted by count, with highest count first
            fileCountList.sort((o1, o2) -> o2.getCount() - o1.getCount());
            System.out.println(verbose ? "File problem counts:" : "Files with the most problems:");
            int num = 1;
            for (FileInfo info : fileCountList) {
                System.out.format(FORMAT4, info.getName(), info.getCount());
                if (!verbose && num++ >= NUMBER_OF_FILES_TO_REPORT) {
                    break;
                }
            }
            System.out.println();

            if (!verbose) {
                int leastRemaining = Math.min(fileCountList.size() - NUMBER_OF_FILES_TO_REPORT,
                    NUMBER_OF_FILES_TO_REPORT);
                if (leastRemaining > 0) {
                    // The list is sorted by count, with lowest count first
                    fileCountList.sort((o1, o2) -> o1.getCount() - o2.getCount());
                    System.out.println("Files with the fewest problems:");
                    for (int i = leastRemaining; i > 0; i--) {
                        FileInfo info = fileCountList.get(i - 1);
                        System.out.format(FORMAT4, info.getName(), info.getCount());
                    }
                    System.out.println();
                }
            }
        }
    }

}

