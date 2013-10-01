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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Executes the 'start' task against Karma. See the Karma documentation itself for information: http://karma.github.com.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.TEST)
public class StartMojo extends AbstractMojo {

    /**
     * Name of the Junit reporter for the Karma configuration reporters array
     *
     * @see <a href="https://github.com/karma-runner/karma-junit-reporter">https://github.com/karma-runner/karma-junit-reporter</a>}
     */
    private static final String KARMA_JUNIT_REPORTER = "junit";

    /**
     * Name of the Junit reporter plugin for the Karma configuration plugins array
     *
     * @see <a href="https://github.com/karma-runner/karma-junit-reporter">https://github.com/karma-runner/karma-junit-reporter</a>}
     */
    private static final String KARMA_JUNIT_REPORTER_PLUGIN = "karma-junit-reporter";

    /**
     * Base directory where all Karma reports are written to.
     */
    @Parameter(defaultValue = "${project.build.directory}/karma-reports", required = false)
    private File reportsDirectory;

    /**
     * Path to the Karma configuration file.
     */
    @Parameter(defaultValue = "${basedir}/karma.conf.js", property = "configFile", required = true)
    private File configFile;

    /**
     * Karma-junit-reporter results file. Setting this location will export the results file to the specified reportsDirectory. For this
     * to function, the karma-junit-reporter plugin must be included in the karma configuration file and the reportsDirectory
     * must be available for writing
     */
    @Parameter(property = "junitReportFile", required = false)
    private File junitReportFile;

    /**
     * Path to the working directory.  The working directory should be where node_modules is installed.
     */
    @Parameter(defaultValue = "${basedir}", property = "nodeModulePath", required = false)
    private File workingDirectory;

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
     * Override the colors flag to enable/disable karma colors output present in the karma configuration file
     */
    @Parameter(property = "colors", required = false)
    private Boolean colors;

    /**
     * Flag that when set to true indicates that execution of the goal should be skipped. Note that setting this property
     * will skip Karma tests *only*. If you also want to skip tests such as those run by the maven-surefire-plugin, consider
     * using the skipTests property instead.
     */
    @Parameter(property = "skipKarma", required = false, defaultValue = "false")
    private Boolean skipKarma;

    /**
     * Flag that when set to true indicates that execution of the goal should be skipped. Note that setting this property
     * also has the effect of skipping tests under plugins such as the maven-surefire-plugin. If you want to *just* skip
     * Karma tests, use the skipKarma property instead.
     */
    @Parameter(property = "skipTests", required = false, defaultValue = "false")
    private Boolean skipTests;

    /**
     * Flag that when set to to true ensures that the Maven build does not fail when if the Karma tests fail. As
     * for the similar property on the maven-surefire-plugin: its use is not recommended, but quite convenient on occasion.
     */
    @Parameter(property = "karmaFailureIgnore", required = false, defaultValue = "false")
    private Boolean karmaFailureIgnore;

    @Parameter(property = "karmaExecutable", required = false, defaultValue = "karma")
    private String karmaExecutable;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skipKarma || skipTests) {
            getLog().info("Skipping Karma test suite execution.");
            return;
        }

        preExecution();

        final Process karma = createKarmaProcess();

        if (!executeKarma(karma) && singleRun) {
            if (karmaFailureIgnore) {
                getLog().warn("There were Karma test failures.");
            } else {
                throw new MojoFailureException("There were Karma test failures.");
            }
        }

        postExecution();
        System.out.flush();
    }

    private void preExecution() throws MojoFailureException {
        String karmaConfiguration;

        if (!configFile.exists()) {
            throw new MojoFailureException("Cannot read the supplied Karma configuration file because it does not exist: " + configFile.getAbsolutePath());
        }

        try {
            karmaConfiguration = FileUtils.readFileToString(configFile);
        } catch (IOException e) {
            throw new MojoFailureException("Cannot read the supplied Karma configuration at " + configFile.getAbsolutePath() + ". Do you have read permission?");
        }

        if (!reportsDirectory.exists() && !reportsDirectory.mkdirs()) {
            throw new MojoFailureException("Cannot create reporting directory " + reportsDirectory.getAbsolutePath());
        }

        if (!reportsDirectory.isDirectory() || !reportsDirectory.canWrite()) {
            throw new MojoFailureException("Cannot write to the supplied reporting directory " + reportsDirectory.getAbsolutePath());
        }

        if (junitReportFile != null) {
            getLog().info("Enabling Karma's junit reporter plugin (" + KARMA_JUNIT_REPORTER_PLUGIN + ")");

            // Ensure that the junit reporter is executed
            if (reporters == null) {
                if (!karmaConfiguration.contains("'" + KARMA_JUNIT_REPORTER + "'")) {
                    reporters = KARMA_JUNIT_REPORTER;
                }
            } else if (!reporters.contains(KARMA_JUNIT_REPORTER)) {
                reporters += "," + KARMA_JUNIT_REPORTER;
            }

            if (!karmaConfiguration.contains("'" + KARMA_JUNIT_REPORTER_PLUGIN + "'")) {
                getLog().warn("Could not find the " + KARMA_JUNIT_REPORTER_PLUGIN + " plugin in the supplied configuration file. Test results may be unavailable or incorrect!");
            }
        }
    }

    private Process createKarmaProcess() throws MojoExecutionException {
        final ProcessBuilder builder;

        if (isWindows()) {
            builder = new ProcessBuilder("cmd", "/C", karmaExecutable, "start", configFile.getAbsolutePath());
        } else {
            builder = new ProcessBuilder(karmaExecutable, "start", configFile.getAbsolutePath());
        }

        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }

        final List<String> command = builder.command();

        command.addAll(valueToKarmaArgument(browsers, "--browsers"));
        command.addAll(valueToKarmaArgument(reporters, "--reporters"));
        command.addAll(valueToKarmaArgument(singleRun, "--single-run", "--no-single-run"));
        command.addAll(valueToKarmaArgument(autoWatch, "--auto-watch", "--no-auto-watch"));
        command.addAll(valueToKarmaArgument(captureTimeout, "--capture-timeout"));
        command.addAll(valueToKarmaArgument(reportSlowerThan, "--report-slower-than"));
        command.addAll(valueToKarmaArgument(colors, "--colors"));

        builder.redirectErrorStream(true);

        try {
            AnsiConsole.systemInstall();

            getLog().info("Executing Karma Test Suite ...");
            System.out.println(StringUtils.join(command.iterator(), " "));

            return builder.start();

        } catch (IOException e) {
            resetAnsiConsole();
            throw new MojoExecutionException("There was an error executing Karma.", e);
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    @SuppressWarnings("unchecked")
    private List<String> valueToKarmaArgument(final Boolean value, final String trueSwitch, final String falseSwitch) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(value ? trueSwitch : falseSwitch);
    }

    @SuppressWarnings("unchecked")
    private List<String> valueToKarmaArgument(final Integer value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    private List<String> valueToKarmaArgument(final Boolean value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value.toString());
    }

    @SuppressWarnings("unchecked")
    private List<String> valueToKarmaArgument(final String value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value);
    }

    private boolean executeKarma(final Process karma) throws MojoExecutionException {

        BufferedReader karmaOutputReader = null;
        try {
            karmaOutputReader = createKarmaOutputReader(karma);

            for (String line = karmaOutputReader.readLine(); line != null; line = karmaOutputReader.readLine()) {
                AnsiConsole.out.print(line);
                AnsiConsole.out.println("\033[0m ");
            }

            resetAnsiConsole();

            return (karma.waitFor() == 0);

        } catch (IOException e) {
            resetAnsiConsole();
            throw new MojoExecutionException("There was an error reading the output from Karma.", e);
        } catch (InterruptedException e) {
            resetAnsiConsole();
            throw new MojoExecutionException("The Karma process was interrupted.", e);
        } finally {
            IOUtils.closeQuietly(karmaOutputReader);
        }
    }

    private BufferedReader createKarmaOutputReader(final Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private void resetAnsiConsole() {
        AnsiConsole.out.println("\033[0m ");
        AnsiConsole.systemInstall();
    }

    private void postExecution() {
        if (junitReportFile != null) {

            if (!junitReportFile.exists() || !junitReportFile.isFile()) {
                getLog().warn("Karma's junit reporter was enabled but no results were found at " + junitReportFile.getAbsolutePath() + ". Is the reporter plugin (" + KARMA_JUNIT_REPORTER_PLUGIN + ") installed correctly and enabled in the Karma configuration file?");
            } else {
                try {
                    FileUtils.copyFile(junitReportFile, new File(reportsDirectory, junitReportFile.getName()));
                } catch (IOException e) {
                    getLog().warn("Could not copy Karma's junit report to " + reportsDirectory.getAbsolutePath());
                }
            }

        }
    }
}
