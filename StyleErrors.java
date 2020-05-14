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
 * <p> Results can be filtered on one or more file names, and on one or more
 * of the checkstyle category names.
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
        /** The problem category from the check styles configuration. */
        private String problemCategory;
        /** The severity (ERROR or WARN) of this check. */
        private Severity severity;
        /** The final count of how many times this problem was encountered. */
        private Integer count;
        /** A set of the files it was found in. */
        private Set<String> files;

        /**
         * Construct one with a starting count of 1 and the given problem category
         * and first file name.
         * @param problem The checkstyle problem.
         * @param sev Whether this is an error or just a warning.
         * @param fileName The first file encountered with this problem.
         */
        Info(final String problem, final String sev, final String fileName) {
            this.problemCategory = problem;
            this.severity = Severity.lookup(sev);
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
        String getProblemCategory() {
            return problemCategory;
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
     * A comparator that sorts first by count and then by the problem category name.
     */
    private static Comparator<Info> comparator = new Comparator<Info>() {
        @Override
        public int compare(final Info o1, final Info o2) {
            // Order first by count, then by category name
            int c1 = o1.count.intValue();
            int c2 = o2.count.intValue();
            if (c1 == c2) {
                return o1.problemCategory.compareTo(o2.problemCategory);
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
    private static String list(final Iterable<String> strings) {
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

    /** Default name of the input file if none is given on the command line. */
    private static final String DEFAULT_INPUT_FILE = "style_errors.log";
    /** Pattern used to parse each input line. */
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^\\[([A-Z]+)\\]\\s+(([a-zA-Z]\\:)?([^:]+))(\\:[0-9]+\\:)([0-9]+\\:)?\\s+(.+)\\s+(\\[[a-zA-Z]+\\])$"
        );
    /** The group in the {@link #LINE_PATTERN} that contains the severity of the problem. */
    private static final int SEVERITY_GROUP = 1;
    /** The group in the {@link #LINE_PATTERN} that contains the file name. */
    private static final int FILE_NAME_GROUP = 2;
    /** The group in the {@link #LINE_PATTERN} that contains the checkstyle problem name. */
    private static final int CATEGORY_NAME_GROUP = 8;
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
    /** Three character underline. */
    private static final String THREE = "---";
    /** Five character underline. */
    private static final String FIVE = "-----";
    /** File name underline. */
    private static final String FILE = "-------------------";
    /** Category name underline. */
    private static final String CATEGORY = "----------------------------";
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
    /** The system file separator string. */
    private static final String SEPARATOR = System.getProperty("file.separator");
    /** The starting directory (used to strip off the leading part of the file paths). */
    private static final String CURRENT_DIR = new File(System.getProperty("user.dir")).getPath() + SEPARATOR;
    /** Our package name prefix. */
    private static final String PACKAGE_PREFIX = "org.apache.pivot";
    /** Whether to report all the file problem counts, or just the least/most. */
    private static boolean verbose = false;
    /** A list of bare file names that are used to filter the summary report. */
    private static List<String> filterFileNames = new ArrayList<>();
    /** A list of problem categories that are used to filter the summary report. */
    private static List<String> filterCategories = new ArrayList<>();
    /** A list of package names that are used to filter the summary report. */
    private static List<String> filterPackages = new ArrayList<>();
    /** Whether the next file name on the command line is a filter name or not. */
    private static boolean filter = false;
    /** Whether the next field on the command line is a category name or not. */
    private static boolean category = false;
    /** Whehter the next field is a package name. */
    private static boolean packages = false;
    /** Whether we filter the file names by any file names (if {@link #filterFileNames} list
     * is non-empty. */
    private static boolean filteredByFile = false;
    /** Whether we filter the results by problem categories (if {@link #filterCategories} list
     * is non-empty. */
    private static boolean filteredByCategory = false;
    /** Whether we filter the results by package names (if {@link #filterPackages} list
     * is non-empty. */
    private static boolean filteredByPackage = false;

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
            case "c":
            case "C":
            case "category":
            case "CATEGORY":
                category = true;
                break;
            case "p":
            case "P":
            case "package":
            case "PACKAGE":
                packages = true;
                break;
            default:
                filter = category = packages = false;
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
            System.out.format(FORMAT1, num, info.getSeverity(), info.getProblemCategory(),
                    info.getCount(), size);
        } else {
            System.out.format(FORMAT2, num, info.getSeverity(), info.getProblemCategory(),
                    info.getCount(), list(fileSet));
        }
    }

    /**
     * Add a file to the list of input files to process, if possible.
     * @param files The existing file list to add to.
     * @param arg The candidate input file name.
     */
    private static void addFile(final List<File> files, final String arg) {
        File file = new File(arg);
        if (!file.exists() || !file.isFile() || file.isHidden() || !file.canRead()) {
            System.err.println("Unable to find or read the input file: \"" + file.getPath() + "\"!");
        } else {
            files.add(file);
        }
    }

    /**
     * Strip off the first N pieces of a file path.
     * @param path The incoming path to edit.
     * @param n The number of pieces to elide.
     * @return The edited path string.
     */
    private static String removeNDirs(final String path, final int n) {
        int start = 0;
        for (int i = 0; start >= 0 && i < n; i++) {
            start = path.indexOf(SEPARATOR, start) + 1;
        }
        return start > 0 ? path.substring(start) : path;
    }

    /**
     * Process all the command-line arguments.
     * @param files The list of actual files to process (to add to).
     * @param args The command line arguments to process.
     */
    private static void processArguments(final List<File> files, final String[] args) {
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
                    // The argument could be a comma or semicolon separated list
                    String[] values = arg.split("[;,]");
                    for (String value : values) {
                        if (value.endsWith(".java")) {
                            filterFileNames.add(value);
                        } else if (value.indexOf(".") >= 0) {
                            filterFileNames.add(value);
                        } else {
                            filterFileNames.add(value + ".java");
                        }
                    }
                    filter = false;
                } else if (category) {
                    // One or more of the category names (with or without [ ])
                    String[] values = arg.split("[;,]");
                    for (String value : values) {
                        if (value.startsWith("[") && value.endsWith("]")) {
                            filterCategories.add(value);
                        } else {
                            filterCategories.add("[" + value + "]");
                        }
                    }
                    category = false;
                } else if (packages) {
                    // One or more package names (same as above)
                    String[] values = arg.split("[;,]");
                    for (String value : values) {
                        if (value.startsWith(PACKAGE_PREFIX)) {
                            filterPackages.add(value);
                        } else if (value.startsWith(".")) {
                            filterPackages.add(PACKAGE_PREFIX + value);
                        } else {
                            filterPackages.add(PACKAGE_PREFIX + "." + value);
                        }
                    }
                    packages = false;
                } else {
                    addFile(files, arg);
                }
            }
        }
        filteredByFile = filterFileNames.size() != 0;
        filteredByCategory = filterCategories.size() != 0;
        filteredByPackage = filterPackages.size() != 0;
    }

    /**
     * The main method, executed from the command line, which reads through each file
     * and processes it.
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        List<File> files = new ArrayList<>(args.length);

        processArguments(files, args);

        // Try to process the default error log if none was specified on the command line
        if (files.isEmpty()) {
            addFile(files, DEFAULT_INPUT_FILE);
        }

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
                        String fullFileName = m.group(FILE_NAME_GROUP);
                        String problemCategory = m.group(CATEGORY_NAME_GROUP);
                        File f = new File(fullFileName);
                        String nameOnly = f.getName();
                        if (filteredByFile) {
                            if (!filterFileNames.contains(nameOnly)) {
                                continue;
                            }
                        }
                        if (filteredByCategory) {
                            if (!filterCategories.contains(problemCategory)) {
                                continue;
                            }
                        }
                        String parent = removeNDirs(f.getParent().replace(CURRENT_DIR, ""), 2);
                        String packageName = parent.replace(SEPARATOR, ".");
                        if (filteredByPackage) {
                            if (!filterPackages.contains(packageName)) {
                                continue;
                            }
                        }
                        String relativeFileName = f.getPath().replace(CURRENT_DIR, "");
                        fileNameSet.add(relativeFileName);
                        Info info = workingSet.get(problemCategory);
                        if (info == null) {
                            workingSet.put(problemCategory, new Info(problemCategory, severity, nameOnly));
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

            if (sortedList.isEmpty()) {
                StringBuilder buf = new StringBuilder("No results ");
                boolean anyFilters = false;
                if (filteredByFile) {
                    buf.append(anyFilters ? " or " : " for ");
                    buf.append("the filtered files ").append(list(filterFileNames));
                    anyFilters = true;
                }
                if (filteredByCategory) {
                    buf.append(anyFilters ? " or " : " for ");
                    buf.append("the filtered categories ").append(list(filterCategories));
                    anyFilters = true;
                }
                if (filteredByPackage) {
                    buf.append(anyFilters ? " or " : " for ");
                    buf.append("the filtered packages ").append(list(filterPackages));
                    anyFilters = true;
                }
                if (!anyFilters) {
                    buf.append(" to show");
                }
                buf.append("!");
                System.out.println(buf.toString());
            } else {
                Collections.sort(sortedList, comparator);

                // Output the final summary report for this input file
                System.out.format(UNDER_FORMAT, " # ", " Sev ", "Category", "Count", "File(s)");
                System.out.format(UNDER_FORMAT, THREE, FIVE, CATEGORY, FIVE, FILE);
                int categoryNo = 0;
                for (Info info : sortedList) {
                    reportInfo(++categoryNo, info);
                }

                System.out.format(UNDER_FORMAT, THREE, FIVE, CATEGORY, FIVE, FILE);
                System.out.format(FORMAT3, "Totals", total, fileNameSet.size());
                System.out.println();
            }

            // Take the file counts and generate a list of the data for sorting
            fileCountList.clear();
            for (Map.Entry<String, Integer> entry : fileCounts.entrySet()) {
                FileInfo info = new FileInfo(entry.getKey(), entry.getValue());
                fileCountList.add(info);
            }

            if (fileCountList.size() > 1) {
                int remaining = fileCountList.size() - NUMBER_OF_FILES_TO_REPORT;
                int leastRemaining = Math.min(remaining, NUMBER_OF_FILES_TO_REPORT);
                boolean twoReports = !verbose && remaining > NUMBER_OF_FILES_TO_REPORT;

                // The list is sorted by count, with highest count first
                fileCountList.sort((o1, o2) -> o2.getCount() - o1.getCount());
                System.out.println(twoReports ? "Files with the most problems:" : "File problem counts:");
                int num = 1;
                for (FileInfo info : fileCountList) {
                    System.out.format(FORMAT4, info.getName(), info.getCount());
                    if (twoReports && num++ >= NUMBER_OF_FILES_TO_REPORT) {
                        break;
                    }
                }
                System.out.println();

                if (twoReports) {
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

}

