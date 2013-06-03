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

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public abstract class AbstractStartMojo extends AbstractKarmaMojo {

    /**
     * Path to the Karma configuration file.
     */
    @Parameter(defaultValue = "${basedir}/karma.conf.js", property = "configFile", required = true)
    protected File configFile;

    /**
     * Comma-separated list of browsers. See the "browsers" section of the Karma online configuration documentation for
     * supported values.
     */
    @Parameter(property = "browsers", required = false)
    protected String browsers;

    /**
     * Flag indicating whether the Karma server will automatically re-run when watched files change. See the "autoWatch"
     * section of the Karma online configuration documentation for more information. Defaults to Karma default.
     */
    @Parameter(property = "autoWatch", required = false)
    protected Boolean autoWatch;

    /**
     * Comma-separated list of reporters. See the "reporters" section of the Karma online configuration documentation for
     * supported values.
     */
    @Parameter(property = "reporters", required = false)
    protected String reporters;

    /**
     * Browser capture timeout in milliseconds. See the "captureTimeout" section of the Karma online configuration documentation for
     * more information.
     */
    @Parameter(property = "captureTimeout", required = false)
    protected Integer captureTimeout;

    /**
     * Threshold (in milliseconds) beyond which slow-running tests will be reported. See the "reportSlowerThan" section
     * of the Karma online configuration documentation for more information.
     */
    @Parameter(property = "reportSlowerThan", required = false)
    protected Integer reportSlowerThan;


    protected void addKarmaArguments(List<String> command) {
        command.addAll(valueToKarmaArgument(browsers, "--browsers"));
        command.addAll(valueToKarmaArgument(reporters, "--reporters"));
        command.addAll(valueToKarmaArgument(autoWatch, "--auto-watch", "--no-auto-watch"));
        command.addAll(valueToKarmaArgument(captureTimeout, "--capture-timeout"));
        command.addAll(valueToKarmaArgument(reportSlowerThan, "--report-slower-than"));
    }

    protected List<String> valueToKarmaArgument(final Boolean value, final String trueSwitch, final String falseSwitch) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        if (value.booleanValue()) {
            return Arrays.asList(trueSwitch);
        } else {
            return Arrays.asList(falseSwitch);
        }
    }

    protected List<String> valueToKarmaArgument(final Integer value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, String.valueOf(value));
    }

    protected List<String> valueToKarmaArgument(final String value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value);
    }


}
