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
import java.util.Arrays;
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

import org.apache.pivot.util.ExceptionUtils;


/**
 * Read the file(s) given on the command line, which are presumed to be
 * the output of the "check styles" process, and generate a summary of the
 * results for each one.
 * <p> Results can be filtered on one or more file names, and on one or more
 * of the checkstyle category names (can be wildcards with "*" or "?").
 */
public final class StyleChecks {
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
    private StyleChecks() {
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
            return String.format("[%1$s]", problemCategory);
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
        /** @return The name only in this object. */
        String getNameOnly() {
            return fileNameOnly(fileName);
        }
        /** @return The count for this object. */
        int getCount() {
            return count;
        }
        @Override
        public String toString() {
            return fileName;
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
     * Our file name lists have "short" names that are all relative to
     * the {@link #PATH_PREFIX} path. But to report names properly
     * that may be duplicates in the name, but not in the full path
     * we need to decide if there are any such duplicates before beginning
     * to print. That's what we do here.
     *
     * @param strings The set of strings we are preparing to print.
     * @return        Whether there are any duplicates in name only.
     */
    private static boolean anyDuplicateNames(final Iterable<String> strings) {
        Set<String> nonDuplicateNames = new HashSet<>();
        // The strategy is to take the name only and put into the non-duplicate
        // set, which will eliminate duplicate names. If the final size of that
        // set is less than the original list size, then there are duplicate
        // names in the original list.
        int count = 0;
        for (String s : strings) {
            count++;
            nonDuplicateNames.add(fileNameOnly(s));
        }
        return nonDuplicateNames.size() < count;
    }

    /**
     * Search for duplicate names in a list of {@link FileInfo} objects.
     *
     * @param infos The set of info objects.
     * @return      Whether or not there are duplicate names.
     */
    private static boolean anyDuplicateFileInfos(final Iterable<FileInfo> infos) {
        Set<String> nonDuplicateNames = new HashSet<>();
        int count = 0;
        for (FileInfo info : infos) {
            count++;
            nonDuplicateNames.add(fileNameOnly(info.getName()));
        }
        return nonDuplicateNames.size() < count;
    }

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

    /**
     * Get a list of the short form of strings as a parenthesized list,
     * using the names only (for the case of no duplicates).
     *
     * @param strings The set of strings to traverse.
     * @return A nicely formatted list.
     */
    private static String listShortName(final Iterable<String> strings) {
        StringBuilder buf = new StringBuilder("(");
        int i = 0;
        for (String s : strings) {
            if (i++ > 0) {
                buf.append(", ");
            }
            buf.append(fileNameOnly(s));
        }
        buf.append(')');
        return buf.toString();
    }

    /**
     * Find one or more files by name if any exist starting at the given directory location.
     * <p> This is used to ensure that the <code>"-f"</code> file names actually exist,
     * and aren't not found in the style log just because the name is a typo.
     *
     * @param baseFile   The starting directory to search.
     * @param searchName The file name to search for, which could be a partial path.
     * @param fileList   The list of files to add to as they are found.
     * @return           {@code true} or {@code false} depending on whether any files
     *                   with the given name were found.
     */
    private static boolean findFiles(final File baseFile, final String searchName, final List<File> fileList) {
        boolean foundAny = false;

        if (baseFile.isDirectory()) {
            // Skip (some) directory hierarchies that will be unfruitful no matter what
            if (baseFile.getName().equals("ant-bin")) {
                return foundAny;
            }

            File newFile = new File(baseFile, searchName);
            if (newFile.exists() && newFile.isFile()) {
                fileList.add(newFile);
                foundAny = true;
            } else {
                for (File f : baseFile.listFiles()) {
                    if (findFiles(f, searchName, fileList)) {
                        foundAny = true;
                    }
                }
            }
        } else {
            if (searchName.equals(baseFile.getName())) {
                fileList.add(baseFile);
                foundAny = true;
            }
        }

        return foundAny;
    }

    /**
     * Look for the file starting under the {@link #CURRENT_DIR_FILE} and if found, add it to
     * the file name filter list, otherwise print an error.
     *
     * @param value The file name to potentially add to the file name filter list.
     * @return      Whether or not the file name was valid.
     * @see #filterFileNames
     */
    private static boolean addToFileNamesList(final String value) {
        String fileName;

        if (value.endsWith(".java")) {
            fileName = value;
        } else if (value.indexOf(".") >= 0) {
            fileName = value;
        } else {
            fileName = value + ".java";
        }

        List<File> foundFiles = new ArrayList<>();
        if (findFiles(CURRENT_DIR_FILE, fileName, foundFiles)) {
            for (File foundFile : foundFiles) {
                filterFileNames.add(shortFileName(foundFile.getPath()));
            }
            return true;
        } else {
            error("The '%1$s' file wasn't found!", fileName);
            return false;
        }
    }

    /**
     * Lookup the given category name in the list of valid ones and add to the category filter
     * list if found, otherwise print an error.
     *
     * @param value The category to potentially add to the filter list.
     * @return      Whether or not the category name was valid.
     * @see #filterCategories
     */
    private static boolean addToCategoriesList(final String value) {
        String categoryName;

        if (value.startsWith("[") && value.endsWith("]")) {
            categoryName = value.substring(1, value.length() - 1);
        } else {
            categoryName = value;
        }

        if (Arrays.binarySearch(VALID_CATEGORIES, categoryName.toLowerCase()) >= 0) {
            filterCategories.add(categoryName);
            return true;
        } else {
            error("The '[%1$s]' category wasn't found!", categoryName);
            return false;
        }
    }

    /**
     * Fixup the given package name and add to the list of searched ones.
     *
     * @param value The package name to potentially add to the filter list.
     * @see #filterPackages
     */
    private static void addToPackagesList(final String value) {
        String packageName;

        if (value.startsWith(PACKAGE_PREFIX)) {
            packageName = value;
        } else if (value.startsWith(".")) {
            packageName = PACKAGE_PREFIX + value;
        } else {
            packageName = PACKAGE_PREFIX + "." + value;
        }

        filterPackages.add(packageName.replaceAll(SEP_PATTERN, "."));
    }


    /** Default name of the input file if none is given on the command line. */
    private static final String DEFAULT_INPUT_FILE = "style_checks.log";
    /** Pattern used to parse each input line. */
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^\\[([A-Z]+)\\]\\s+(([a-zA-Z]\\:)?([^:]+))(\\:[0-9]+\\:)([0-9]+\\:)?\\s+(.+)\\s+\\[([a-zA-Z]+)\\]$"
        );
    /** The pattern to replace any kind of file name separator with something else. */
    private static final String SEP_PATTERN = "[\\\\/]";
    /** The group in the {@link #LINE_PATTERN} that contains the severity of the problem. */
    private static final int SEVERITY_GROUP = 1;
    /** The group in the {@link #LINE_PATTERN} that contains the file name. */
    private static final int FILE_NAME_GROUP = 2;
    /** The group in the {@link #LINE_PATTERN} that contains the checkstyle problem name. */
    private static final int CATEGORY_NAME_GROUP = 8;
    /** Limit on the number of files to enumerate vs just give the number. */
    private static final int NUMBER_OF_FILES_LIMIT = 4;
    /** A severity of "warning". */
    private static final String SEV_WARN = "WARN";
    /** A severity of "error". */
    private static final String SEV_ERROR = "ERROR";
    /** Report footer title. */
    private static final String TOTAL = "Total";
    /** Format problem info with a number of files suffix. */
    private static final String FORMAT1 = "%1$2d. %2$5s %3$-30s%4$,6d (%5$,d)%n";
    /** Same as {@link #FORMAT1} except we have a list of file names instead of a number. */
    private static final String FORMAT2 = "%1$2d. %2$5s %3$-30s%4$,6d %5$s%n";
    /** Format postreport info. */
    private static final String FORMAT3 = "          %1$-30s%2$,6d (%3$,d)%n";
    /** Format second postreport info. */
    private static final String FORMAT3A = "    %1$5s %2$-30s%3$,6d (%4$,d)%n";
    /** Format string used to print the underlines. */
    private static final String UNDER_FORMAT = "%1$3s %2$5s %3$-30s%4$6s %5$s%n";

    /** First column header: number. */
    private static final String HDR_NUMBER = " # ";
    /** Second column header: severity. */
    private static final String HDR_SEVERITY =  " Sev ";
    /** Third column header: category. */
    private static final String HDR_CATEGORY =  "Category";
    /** Fourth column header: count. */
    private static final String HDR_COUNT =  " Count";
    /** Last column header: file list. */
    private static final String HDR_FILES =  "File(s)";

    /** Format string for the file vs problem count report. */
    private static final String FORMAT4 = "    %1$-42s %2$,6d%n";
    /** Alternate format string for the file vs problem count report. */
    private static final String FORMAT4A = "    %1$-48s %2$,6d%n";
    /** The set of unique file names found in the list. */
    private static Set<String> fileNameSet = new HashSet<>();
    /** The set of unique file names with warnings in the list. */
    private static Set<String> fileNameWarnSet = new HashSet<>();
    /** The set of unique file names with errors in the list. */
    private static Set<String> fileNameErrorSet = new HashSet<>();
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
    /** The starting directory location. */
    private static final File CURRENT_DIR_FILE = new File(System.getProperty("user.dir"));
    /** The starting directory (used to strip off the leading part of the file paths). */
    private static final String CURRENT_DIR = CURRENT_DIR_FILE.getPath() + SEPARATOR;
    /** The standard path prefix. */
    private static final String PATH_PREFIX = "org/apache/pivot";
    /** Our package name prefix. */
    private static final String PACKAGE_PREFIX = PATH_PREFIX.replaceAll(SEP_PATTERN, ".");
    /** Whether to report all the file problem counts, or just the least/most. */
    private static boolean verbose = false;
    /** A list of bare file names (can be wildcards) that are used to filter the summary report. */
    private static List<String> filterFileNames = new ArrayList<>();
    /** A list of problem categories (can be wildcards) that are used to filter the summary report. */
    private static List<String> filterCategories = new ArrayList<>();
    /** A list of package names (can be wildcards) that are used to filter the summary report. */
    private static List<String> filterPackages = new ArrayList<>();
    /** Whether the next file name on the command line is a filter name or not. */
    private static boolean filter = false;
    /** Whether the next field on the command line is a category name or not. */
    private static boolean category = false;
    /** Whether the next field is a package name. */
    private static boolean packages = false;
    /** Whether the filtering is case-sensitive or not (defauult not). */
    private static boolean caseSensitive = false;
    /** Whether we filter the file names by any file names (if {@link #filterFileNames} list
     * is non-empty. */
    private static boolean filteredByFile = false;
    /** Whether we filter the results by problem categories (if {@link #filterCategories} list
     * is non-empty. */
    private static boolean filteredByCategory = false;
    /** Whether we filter the results by package names (if {@link #filterPackages} list
     * is non-empty. */
    private static boolean filteredByPackage = false;
    /** The list of filter file names converted to regular expression patterns for matching. */
    private static List<Pattern> fileNameFilterPatterns;
    /** The list of filter categories converted to regular expression patterns for matching. */
    private static List<Pattern> categoryFilterPatterns;
    /** The list of filter packages converted to regular expression patterns for matching. */
    private static List<Pattern> packageFilterPatterns;


    /**
     * A list of the possible checkstyle categories we can filter on.
     * <p> Note: these are the "module" entries in our "pivot_style_checks.xml".
     */
    private static final String[] VALID_CATEGORIES = {
        "arraytypestyle", "avoidinlineconditionals", "avoidnestedblocks",
        "avoidstarimport", "constantname", "designforextension", "emptyblock",
        "emptyforiteratorpad", "emptystatement", "equalshashcode", "finalclass",
        "finalparameters", "genericwhitespace", "hiddenfield",
        "hideutilityclassconstructor", "illegalimport", "illegalinstantiation",
        "innerassignment", "interfaceistype", "javadocmethod", "javadocstyle",
        "javadoctype", "javadocvariable", "leftcurly", "linelength",
        "localfinalvariablename", "localvariablename", "magicnumber", "membername",
        "methodlength", "methodname", "methodparampad", "missingswitchdefault",
        "modifierorder", "needbraces", "nowhitespaceafter", "nowhitespacebefore",
        "operatorwrap", "packagename", "parametername", "parameternumber", "parenpad",
        "redundantimport", "redundantmodifier", "rightcurly",
        "simplifybooleanexpression", "simplifybooleanreturn", "staticvariablename",
        "todocomment", "typename", "typecastparenpad", "unusedimports", "upperell",
        "visibilitymodifier", "whitespaceafter", "whitespacearound", "filelength",
        "filetabcharacter", "javadocpackage", "newlineatendoffile", "regexpsingleline",
        "translation"
    };


    /** Upper-left rounded corner character (single line). */
    private static final char ULC = '\u256D';
    /** Upper-right rounded corner character (single line). */
    private static final char URC = '\u256E';
    /** Horizontal line character (single line). */
    private static final char HZL = '\u2500';
    /** Vertical line character (single line). */
    private static final char VTL = '\u2502';
    /** Lower-left rounded corner character (single line). */
    private static final char LLC = '\u2570';
    /** Lower-right rounded corner character (single line). */
    private static final char LRC = '\u256F';

    /** Width of our banner (pretty arbitrary). */
    private static final int WIDTH = 66;
    /** Width of the Category column. */
    private static final int CATEGORY_WIDTH = 28;
    /** Width of the File(s) column. */
    private static final int FILES_WIDTH = 19;


    /**
     * Line types.
     */
    private enum LineType {
        /** The top border of the banner box. */
        TOP,
        /** The bottom border of the banner box. */
        BOTTOM,
        /** One of the middle lines of the banner box. */
        MIDDLE,
        /** A line under a heading. */
        UNDER
    }


    /**
     * Construct box drawing lines.
     * @param lineType 0 = top, 1 = bottom, 2 = blank or message
     * @param width    total width of the line
     * @param message  message for type 2
     * @return Constructed line to print.
     */
    private static String constructBoxLine(final LineType lineType, final int width, final String message) {
        char[] chars = new char[width];

        switch (lineType) {
            case TOP:
                chars[0] = ULC;
                chars[width - 1] = URC;
                break;
            case BOTTOM:
                chars[0] = LLC;
                chars[width - 1] = LRC;
                break;
            case MIDDLE:
                chars[0] = VTL;
                chars[width - 1] = VTL;
                break;
            case UNDER:
                chars[0] = HZL;
                chars[width - 1] = HZL;
                break;
            default:
                break;
        }

        switch (lineType) {
            case TOP:
            case BOTTOM:
            case UNDER:
                Arrays.fill(chars, 1, width - 1, HZL);
                break;
            case MIDDLE:
                Arrays.fill(chars, 1, width - 1, ' ');
                break;
            default:
                break;
        }

        if (lineType == LineType.MIDDLE && message != null) {
            int startPos = (width + 1 - message.length() * 2) / 2;
            for (int i = 0, pos = startPos; i < message.length(); i++, pos += 2) {
                chars[pos] = message.charAt(i);
            }
        }

        return new String(chars);
    }

    /**
     * Output a header message with an underline of the same length.
     *
     * @param message The (header) message to output.
     */
    private static void underlinedMessage(final String message) {
        System.out.println(message);
        System.out.println(constructBoxLine(LineType.UNDER, message.length(), null));
    }

    /**
     * Output a formatted error message to {@link System#err}.
     * @param format The format string as input to {@link String#format}.
     * @param args   The (optional) arguments to be interpolated in the message.
     */
    private static void error(final String format, final Object... args) {
        String message = String.format(format, args);
        System.err.println(message);
        System.err.println();
    }

    /**
     * Process one option from the command line.
     * @param option The option string to process.
     * @param prefix For error messages, the option prefix used.
     */
    private static void processOption(final String option, final String prefix) {
        switch (option.toLowerCase()) {
            case "casesensitive":
            case "sensitive":
            case "case":
            case "sense":
            case "s":
                caseSensitive = true;
                break;
            case "v":
            case "ver":
            case "verb":
            case "verbose":
                verbose = true;
                break;
            case "f":
            case "file":
            case "files":
            case "filter":
                filter = true;
                break;
            case "c":
            case "cat":
            case "cats":
            case "category":
            case "categories":
                category = true;
                break;
            case "p":
            case "pack":
            case "packs":
            case "package":
            case "packages":
                packages = true;
                break;
            default:
                filter   = false;
                category = false;
                packages = false;

                error("Ignoring unrecognized option: \"%1$s%2$s\"!", prefix, option);
                break;
        }
    }

    /**
     * Report on this particular piece of information.
     * @param num Which category this is in the list.
     * @param info The summary information for the category.
     * @param shortName Whether to use the short names or not.
     * @see #FORMAT1
     * @see #FORMAT2
     */
    private static void reportInfo(final int num, final Info info, final boolean shortName) {
        Set<String> fileSet = info.getFileSet();
        int size = fileSet.size();
        if (size > NUMBER_OF_FILES_LIMIT) {
            System.out.format(FORMAT1, num, info.getSeverity(), info.getProblemCategory(),
                    info.getCount(), size);
        } else {
            System.out.format(FORMAT2, num, info.getSeverity(), info.getProblemCategory(),
                    info.getCount(), shortName ? listShortName(fileSet) : list(fileSet));
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
            error("Unable to find or read the input file: \"%1$s\"!", file.getPath());
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
     * @param args  The command line arguments to process.
     * @return      <code>true</code> if everything was fine, but <code>false</code>
     *              if there was a problem with the arguments.
     */
    private static boolean processArguments(final List<File> files, final String[] args) {
        boolean result;

        // Process options and save the straight file names
        for (String arg : args) {
            if (arg.startsWith("--")) {
                processOption(arg.substring(2), "--");
            } else if (arg.startsWith("-")) {
                processOption(arg.substring(1), "-");
            } else if (ON_WINDOWS && arg.startsWith("/")) {
                processOption(arg.substring(1), "/");
            } else {
                if (filter) {
                    // The argument could be a comma or semicolon separated list
                    String[] values = arg.split("[;,]");
                    for (String value : values) {
                        if (!addToFileNamesList(value)) {
                            return false;
                        }
                    }
                    filter = false;
                } else if (category) {
                    // One or more of the category names (with or without [ ])
                    String[] values = arg.split("[;,]");
                    for (String value : values) {
                        if (!addToCategoriesList(value)) {
                            return false;
                        }
                    }
                    category = false;
                } else if (packages) {
                    // One or more package names (same as above)
                    String[] values = arg.split("[;,]");
                    for (String value : values) {
                        addToPackagesList(value);
                    }
                    packages = false;
                } else {
                    addFile(files, arg);
                }
            }
        }
        filteredByFile     = filterFileNames.size()  != 0;
        filteredByCategory = filterCategories.size() != 0;
        filteredByPackage  = filterPackages.size()   != 0;

        // For each filter group, convert the strings to regular expressions
        fileNameFilterPatterns = makePatterns(filterFileNames);
        categoryFilterPatterns = makePatterns(filterCategories);
        packageFilterPatterns  = makePatterns(filterPackages);

        return true;
    }

    /**
     * Convert a list of potentially wildcard string patterns into real regular expressions.
     *
     * @param filterList The list of filter strings (can be empty).
     * @return           A new list of compiled patterns, or {@code null} if the input list
     *                   is empty.
     */
    private static List<Pattern> makePatterns(final List<String> filterList) {
        List<Pattern> patterns = null;
        if (filterList.size() != 0) {
            patterns = new ArrayList<>(filterList.size());
            for (String filterString : filterList) {
                patterns.add(convertToPattern(filterString));
            }
        }
        return patterns;
    }

    /**
     * Convert a conventional wildcard specification (containing only "*" or "?") to a compiled
     * regular expression pattern for use in filtering the input file.
     * <p> Note: the compiled pattern is marked as case-insensitive depending on the command-line
     * option "sensitive".
     *
     * @param filterExpr The filter expression as a wildcard.
     * @return           The filter expression converted to a {@link Pattern}.
     */
    private static Pattern convertToPattern(final String filterExpr) {
        StringBuilder filterRegEx = new StringBuilder(filterExpr.length() * 2);

        // Translate wildcards to regular expression constructs,
        // and fixup other things
        for (int i = 0; i < filterExpr.length(); i++) {
            char ch = filterExpr.charAt(i);
            switch (ch) {
                case '*':
                    filterRegEx.append(".*");
                    break;
                case '?':
                    filterRegEx.append('.');
                    break;
                case '.':
                case '[':
                case ']':
                    filterRegEx.append('\\');
                    filterRegEx.append(ch);
                    break;
                default:
                    filterRegEx.append(ch);
                    break;
            }
        }
        return Pattern.compile(filterRegEx.toString(),
                caseSensitive ? 0 : (Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
    }

    /**
     * Determine whether the given input value is matched by any of the (possibly wildcard)
     * filter entries in the list. The list entries are converted from "*?" wildcards to
     * regular expressions before each test.
     *
     * @param filterPatterns The list of filter patterns (should not be empty).
     * @param inputArg       The input argument to be tested against the filters.
     * @return               Whether or not the input matches any of the filters.
     */
    private static boolean matches(final List<Pattern> filterPatterns, final String inputArg) {
        for (Pattern pattern : filterPatterns) {
            Matcher m = pattern.matcher(inputArg);
            if (m.matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get a unique, but short file name with only the part after <code>org/apache/pivot</code> saved.
     *
     * @param fullFileName The full (but still relative to the base directory)
     *                     file name (in OS-specific form).
     * @return             The abbreviated form, which should be unique.
     */
    private static String shortFileName(final String fullFileName) {
        // First, translate the path separators to a canonical form
        String canonicalName = fullFileName.replaceAll(SEP_PATTERN, "/");
        int ix = canonicalName.lastIndexOf(PATH_PREFIX);
        if (ix > 0) {
            return canonicalName.substring(ix + PATH_PREFIX.length() + 1);
        } else {
            return fileNameOnly(canonicalName);
        }
    }

    /**
     * Get the file name only from a short file name.
     *
     * @param shortFileName The result of {@link #shortFileName}.
     * @return              The name-only part.
     */
    private static String fileNameOnly(final String shortFileName) {
        int ix = shortFileName.lastIndexOf('/');
        return ix < 0 ? shortFileName : shortFileName.substring(ix + 1);
    }

    /**
     * Display the final report for one style input file.
     *
     * @param totalWarn  The number of warnings.
     * @param totalError The number of errors.
     * @param total      Total number of warnings plus errors.
     */
    private static void displayReport(final int totalWarn, final int totalError, final int total) {
        String numberLine   = constructBoxLine(LineType.UNDER, HDR_NUMBER.length(), null);
        String severityLine = constructBoxLine(LineType.UNDER, HDR_SEVERITY.length(), null);
        String countLine    = constructBoxLine(LineType.UNDER, HDR_COUNT.length(), null);
        String filesLine    = constructBoxLine(LineType.UNDER, FILES_WIDTH, null);
        String categoryLine = constructBoxLine(LineType.UNDER, CATEGORY_WIDTH, null);

        System.out.println(constructBoxLine(LineType.TOP,    WIDTH, null));
        System.out.println(constructBoxLine(LineType.MIDDLE, WIDTH, null));
        System.out.println(constructBoxLine(LineType.MIDDLE, WIDTH, "STYLE CHECK RESULTS"));
        System.out.println(constructBoxLine(LineType.MIDDLE, WIDTH, null));
        System.out.println(constructBoxLine(LineType.BOTTOM, WIDTH, null));
        System.out.println();

        if (sortedList.isEmpty()) {
            StringBuilder buf = new StringBuilder("No results");
            boolean anyFilters = false;
            if (filteredByFile) {
                buf.append(anyFilters ? " or" : " for").append(" the filtered ");
                buf.append(filterFileNames.size() == 1 ? "file " : "files ");
                buf.append(list(filterFileNames));
                anyFilters = true;
            }
            if (filteredByCategory) {
                buf.append(anyFilters ? " or" : " for").append(" the filtered ");
                buf.append(filterCategories.size() == 1 ? "category " : "categories ");
                buf.append(list(filterCategories));
                anyFilters = true;
            }
            if (filteredByPackage) {
                buf.append(anyFilters ? " or" : " for").append(" the filtered ");
                buf.append(filterPackages.size() == 1 ? "package " : "packages ");
                buf.append(list(filterPackages));
                anyFilters = true;
            }
            if (!anyFilters) {
                buf.append(" to show");
            }
            buf.append("!");
            System.out.println(buf.toString());
            System.out.println();
        } else {
            Collections.sort(sortedList, comparator);

            // Find out if there any duplicates in any of the Info lists
            boolean duplicates = false;
            for (Info info : sortedList) {
                if (anyDuplicateNames(info.getFileSet())) {
                    duplicates = true;
                    break;
                }
            }

            // Output the final summary report for this input file
            System.out.format(UNDER_FORMAT, HDR_NUMBER, HDR_SEVERITY, HDR_CATEGORY, HDR_COUNT, HDR_FILES);
            System.out.format(UNDER_FORMAT, numberLine, severityLine, categoryLine, countLine, filesLine);

            int categoryNo = 0;
            for (Info info : sortedList) {
                reportInfo(++categoryNo, info, !duplicates);
            }

            System.out.format(UNDER_FORMAT, numberLine, severityLine, categoryLine, countLine, filesLine);

            System.out.format(FORMAT3A, SEV_WARN, TOTAL, totalWarn, fileNameWarnSet.size());
            System.out.format(FORMAT3A, SEV_ERROR, TOTAL, totalError, fileNameErrorSet.size());
            System.out.format(FORMAT3, "Total of All", total, fileNameSet.size());
            System.out.println();
        }

        // Take the file counts and generate a list of the data for sorting
        fileCountList.clear();
        for (Map.Entry<String, Integer> entry : fileCounts.entrySet()) {
            FileInfo info = new FileInfo(entry.getKey(), entry.getValue());
            fileCountList.add(info);
        }

        if (fileCountList.size() > 1) {
            boolean duplicates = anyDuplicateFileInfos(fileCountList);
            String format4 = duplicates ? FORMAT4A : FORMAT4;
            int remaining = fileCountList.size() - NUMBER_OF_FILES_TO_REPORT;
            int leastRemaining = Math.min(remaining, NUMBER_OF_FILES_TO_REPORT);
            boolean twoReports = !verbose && remaining > NUMBER_OF_FILES_TO_REPORT;

            // The list is sorted by count, with highest count first
            fileCountList.sort((o1, o2) -> o2.getCount() - o1.getCount());

            underlinedMessage(twoReports ? "Files With the Most Problems" : "File Problem Counts");

            int num = 1;
            for (FileInfo info : fileCountList) {
                String name = duplicates ? info.getName() : info.getNameOnly();
                System.out.format(format4, name, info.getCount());
                if (twoReports && num++ >= NUMBER_OF_FILES_TO_REPORT) {
                    break;
                }
            }
            System.out.println();

            if (twoReports) {
                if (leastRemaining > 0) {
                    // The list is sorted by count, with lowest count first
                    fileCountList.sort((o1, o2) -> o1.getCount() - o2.getCount());

                    underlinedMessage("Files With the Fewest Problems");

                    for (int i = leastRemaining; i > 0; i--) {
                        FileInfo info = fileCountList.get(i - 1);
                        String name = duplicates ? info.getName() : info.getNameOnly();
                        System.out.format(format4, name, info.getCount());
                    }
                    System.out.println();
                }
            }
        }
    }

    /**
     * The main method, executed from the command line, which reads through each file
     * and processes it.
     * @param args The command line arguments.
     */
    public static void main(final String[] args) {
        List<File> files = new ArrayList<>(args.length);

        if (!processArguments(files, args)) {
            System.exit(1);
        }

        // Try to process the default error log if none was specified on the command line
        if (files.isEmpty()) {
            addFile(files, DEFAULT_INPUT_FILE);
        }

        // Now process just the saved file names
        for (File file : files) {
            int total = 0;
            int totalWarn = 0;
            int totalError = 0;
            int totalForThisFile = 0;
            int lineNo = 0;
            fileNameSet.clear();
            fileNameWarnSet.clear();
            fileNameErrorSet.clear();
            workingSet.clear();
            sortedList.clear();
            currentFileName = null;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineNo++;
                    Matcher m = LINE_PATTERN.matcher(line);
                    if (m.matches()) {
                        String severity        = m.group(SEVERITY_GROUP);
                        String fullFileName    = m.group(FILE_NAME_GROUP);
                        String problemCategory = m.group(CATEGORY_NAME_GROUP);

                        File f = new File(fullFileName);
                        String relativeFileName = f.getPath().replace(CURRENT_DIR, "");
                        String shortFileName = shortFileName(relativeFileName);
                        String nameOnly = f.getName();
                        if (filteredByFile) {
                            if (!matches(fileNameFilterPatterns, nameOnly)
                             && !matches(fileNameFilterPatterns, shortFileName)) {
                                continue;
                            }
                        }
                        if (filteredByCategory) {
                            if (!matches(categoryFilterPatterns, problemCategory)) {
                                continue;
                            }
                        }
                        String parent = removeNDirs(f.getParent().replace(CURRENT_DIR, ""), 2);
                        String packageName = parent.replace(SEPARATOR, ".");
                        if (filteredByPackage) {
                            if (!matches(packageFilterPatterns, packageName)) {
                                continue;
                            }
                        }
                        fileNameSet.add(relativeFileName);
                        Info info = workingSet.get(problemCategory);
                        if (info == null) {
                            workingSet.put(problemCategory, new Info(problemCategory, severity, shortFileName));
                        } else {
                            info.addFile(shortFileName);
                        }
                        total++;
                        if (severity.equals(SEV_ERROR)) {
                            fileNameErrorSet.add(relativeFileName);
                            totalError++;
                        }
                        if (severity.equals(SEV_WARN)) {
                            fileNameWarnSet.add(relativeFileName);
                            totalWarn++;
                        }
                        if (shortFileName.equals(currentFileName)) {
                            totalForThisFile++;
                        } else {
                            if (currentFileName != null) {
                                fileCounts.put(currentFileName, Integer.valueOf(totalForThisFile));
                            }
                            currentFileName = shortFileName;
                            totalForThisFile = 1;
                        }
                    } else if (line.equals("Starting audit...") || line.equals("Audit done.")) {
                        continue;
                    } else {
                        error("Line %1$d. Doesn't match the expected pattern.%n\t\"%2$s\"", lineNo, line);
                    }
                }
                if (currentFileName != null) {
                    fileCounts.put(currentFileName, Integer.valueOf(totalForThisFile));
                }
            } catch (IOException ioe) {
                error("Error reading the \"%1$s\" file: %2$s", file.getPath(), ExceptionUtils.toString(ioe));
            }

            // Once we're done, resort according to problem counts per category
            for (String key : workingSet.keySet()) {
                Info info = workingSet.get(key);
                sortedList.add(info);
            }

            displayReport(totalWarn, totalError, total);
        }
    }

}

