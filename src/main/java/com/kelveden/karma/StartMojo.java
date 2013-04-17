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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Executes the 'start' task against Karma. See the Karma documentation itself for information: http://karma.github.com.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.TEST)
public class StartMojo extends AbstractMojo {

    /**
     * Path to the Karma configuration file.
     */
    @Parameter(defaultValue = "${basedir}/karma.conf.js", property = "configFile", required = true)
    private File configFile;

    @Parameter(defaultValue = "karma", property="karmaExecuteablePath", required=false)
    private String karmaExecutablePath;

    /**
     * Comma-separated list of browsers. See the "browsers" section of the Karma online configuration documentation for
     * supported values.
     */
    @Parameter(property = "browsers", required = false)
    private String browsers;

    /**
     * Flag indicating whether the Karma server will automatically re-run when watched files change. See the "autoWatch"
     * section of the Karma online configuration documentation for more information. Defaults to Karma default.
     */
    @Parameter(property = "autoWatch", required = false)
    private Boolean autoWatch;

    /**
     * Comma-separated list of reporters. See the "reporters" section of the Karma online configuration documentation for
     * supported values.
     */
    @Parameter(property = "reporters", required = false)
    private String reporters;

    /**
     * Browser capture timeout in milliseconds. See the "captureTimeout" section of the Karma online configuration documentation for
     * more information.
     */
    @Parameter(property = "captureTimeout", required = false)
    private Integer captureTimeout;

    /**
     * Flag indicating whether the Karma server will exit after a single test run. See the "singleRun" section of
     * the Karma online configuration documentation for more information. Defaults to true.
     */
    @Parameter(property = "singleRun", required = false, defaultValue = "true")
    private Boolean singleRun;

    /**
     * Threshold (in milliseconds) beyond which slow-running tests will be reported. See the "reportSlowerThan" section
     * of the Karma online configuration documentation for more information.
     */
    @Parameter(property = "reportSlowerThan", required = false)
    private Integer reportSlowerThan;

    /**
     * Flag that when set to true indicates that execution of the goal should be skipped.
     */
    @Parameter(property = "skipKarma", required = false, defaultValue = "false")
    private Boolean skipKarma;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skipKarma) {
            getLog().info("Skipping execution.");
            return;
        }

        final Process karma = createKarmaProcess();

        if (!executeKarma(karma) && singleRun) {
            throw new MojoFailureException("There were Karma test failures.");
        }

        System.out.flush();
    }

    private boolean executeKarma(final Process karma) throws MojoExecutionException {

        BufferedReader karmaOutputReader = null;
        try {
            karmaOutputReader = createKarmaOutputReader(karma);

            for (String line = karmaOutputReader.readLine(); line != null; line = karmaOutputReader.readLine()) {
                System.out.println(line);
            }

            return (karma.waitFor() == 0);

        } catch (IOException e) {
            throw new MojoExecutionException("There was an error reading the output from Karma.", e);

        } catch (InterruptedException e) {
            throw new MojoExecutionException("The Karma process was interrupted.", e);

        } finally {
            IOUtils.closeQuietly(karmaOutputReader);
        }
    }

    private Process createKarmaProcess() throws MojoExecutionException {

        final ProcessBuilder builder = new ProcessBuilder(karmaExecutablePath, "start", configFile.getAbsolutePath());

        final List<String> command = builder.command();

        command.addAll(valueToKarmaArgument(browsers, "--browsers"));
        command.addAll(valueToKarmaArgument(reporters, "--reporters"));
        command.addAll(valueToKarmaArgument(singleRun, "--single-run", "--no-single-run"));
        command.addAll(valueToKarmaArgument(autoWatch, "--auto-watch", "--no-auto-watch"));
        command.addAll(valueToKarmaArgument(captureTimeout, "--capture-timeout"));
        command.addAll(valueToKarmaArgument(reportSlowerThan, "--report-slower-than"));

        builder.redirectErrorStream(true);

        try {

            System.out.println(StringUtils.join(command.iterator(), " "));

            return builder.start();

        } catch (IOException e) {
            throw new MojoExecutionException("There was an error executing Karma.", e);
        }
    }

    private List<String> valueToKarmaArgument(final Boolean value, final String trueSwitch, final String falseSwitch) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        if (value.booleanValue()) {
            return Arrays.asList(trueSwitch);
        } else {
            return Arrays.asList(falseSwitch);
        }
    }

    private List<String> valueToKarmaArgument(final Integer value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, String.valueOf(value));
    }

    private List<String> valueToKarmaArgument(final String value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value);
    }

    private BufferedReader createKarmaOutputReader(final Process p)
    {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

}
