/**
 * Copyright 2013 Alistair Dutton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kelveden.karma;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Karma Utils
 * <p/>
 * Simple utility class
 */
public class KarmaUtils {

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    @SuppressWarnings("unchecked")
    public static List<String> valueToKarmaArgument(final Boolean value, final String trueSwitch, final String falseSwitch) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(value ? trueSwitch : falseSwitch);
    }

    @SuppressWarnings("unchecked")
    public static List<String> valueToKarmaArgument(final Integer value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    public static List<String> valueToKarmaArgument(final Boolean value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value.toString());
    }

    @SuppressWarnings("unchecked")
    public static List<String> valueToKarmaArgument(final String value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value);
    }

    /**
     * Get a process builder for the current environment
     * @param karmaExecutable name of the karma executable
     * @param configFileWithPath file name and path to the configuration file for Karma
     * @return a new ProcessBuilder for the current environment for karma
     */
    public static ProcessBuilder getKarmaProcessBuilder(String karmaExecutable, String configFileWithPath) {
        String osAgnosticPathToExe = karmaExecutable;
        if(!karmaExecutable.equals(StartMojo.defaultKarmaExe)) {
            osAgnosticPathToExe = replacePathSeparatorsWithOSAgnosticSeparator(karmaExecutable);
        }
        String quotedConfigFilePath = "\"" + configFileWithPath + "\"";
        if (isWindows()) {
            return new ProcessBuilder("cmd", "/C", osAgnosticPathToExe, "start", quotedConfigFilePath);
        } else {
            return new ProcessBuilder(osAgnosticPathToExe, "start", quotedConfigFilePath);
        }
    }

    /**
     * Splits the path on / or \ and reconstructs the path with the file.separator character so it can be executed on
     * any OS
     * @param karmaExecutablePath
     * @return reconstructed path
     */
    private static String replacePathSeparatorsWithOSAgnosticSeparator(String karmaExecutablePath) {
        String deUnixedPath = karmaExecutablePath.replace("/", File.separator);
        String deWindowsedAndDeUnixedpath = deUnixedPath.replace("\\", File.separator);
        return deWindowsedAndDeUnixedpath;
    }
}