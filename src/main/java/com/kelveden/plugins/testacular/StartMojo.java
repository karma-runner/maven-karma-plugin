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
package com.kelveden.plugins.testacular;

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
 * Executes the 'start' task against Testacular. See the Testacular documentation itself for information: http://testacular.github.com.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.TEST)
public class StartMojo extends AbstractMojo {

    /**
     * Path to the Testacular configuration file.
     */
    @Parameter(defaultValue = "${basedir}/testacular.conf.js", property = "configFile", required = true)
    private File configFile;

    /**
     * Comma-separated list of browsers. See the "browsers" section of the Testacular online configuration documentation for
     * supported values.
     */
    @Parameter(property = "browsers", required = false)
    private String browsers;

    /**
     * Flag indicating whether the Testacular server will automatically re-run when watched files change. See the "autoWatch"
     * section of the Testacular online configuration documentation for more information.
     */
    @Parameter(property = "autoWatch", required = false)
    private Boolean autoWatch;

    /**
     * Comma-separated list of reporters. See the "reporters" section of the Testacular online configuration documentation for
     * supported values.
     */
    @Parameter(property = "reporters", required = false)
    private String reporters;

    /**
     * Browser capture timeout in milliseconds. See the "captureTimeout" section of the Testacular online configuration documentation for
     * more information.
     */
    @Parameter(property = "captureTimeout", required = false)
    private Integer captureTimeout;

    /**
     * Flag indicating whether the Testacular server will exit after a single test run. See the "singleRun" section of
     * the Testacular online configuration documentation for more information.
     */
    @Parameter(property = "singleRun", required = false)
    private Boolean singleRun;

    /**
     * Threshold (in milliseconds) beyond which slow-running tests will be reported. See the "reportSlowerThan" section
     * of the Testacular online configuration documentation for more information.
     */
    @Parameter(property = "reportSlowerThan", required = false)
    private Integer reportSlowerThan;

    public void execute() throws MojoExecutionException, MojoFailureException {

        final Process testacular = createTestacularProcess();

        if (!executeTestacular(testacular) && singleRun) {
            throw new MojoFailureException("There were Testacular test failures.");
        }

        System.out.flush();
    }

    private boolean executeTestacular(final Process testacular) throws MojoExecutionException {

        BufferedReader testacularOutputReader = null;
        try {
            testacularOutputReader = createTestacularOutputReader(testacular);

            for (String line = testacularOutputReader.readLine(); line != null; line = testacularOutputReader.readLine()) {
                System.out.println(line);
            }

            return (testacular.waitFor() == 0);

        } catch (IOException e) {
            throw new MojoExecutionException("There was an error reading the output from Testacular.", e);

        } catch (InterruptedException e) {
            throw new MojoExecutionException("The Testacular process was interrupted.", e);

        } finally {
            IOUtils.closeQuietly(testacularOutputReader);
        }
    }

    private Process createTestacularProcess() throws MojoExecutionException {

        final ProcessBuilder builder = new ProcessBuilder("testacular", "start", configFile.getAbsolutePath());

        final List<String> command = builder.command();

        command.addAll(valueToTestacularArgument(browsers, "--browsers"));
        command.addAll(valueToTestacularArgument(reporters, "--reporters"));
        command.addAll(valueToTestacularArgument(singleRun, "--single-run", "--no-single-run"));
        command.addAll(valueToTestacularArgument(autoWatch, "--auto-watch", "--no-auto-watch"));
        command.addAll(valueToTestacularArgument(captureTimeout, "--capture-timeout"));
        command.addAll(valueToTestacularArgument(reportSlowerThan, "--report-slower-than"));

        builder.redirectErrorStream(true);

        try {

            System.out.println(StringUtils.join(command.iterator(), " "));

            return builder.start();

        } catch (IOException e) {
            throw new MojoExecutionException("There was an error executing Testacular.", e);
        }
    }

    private List<String> valueToTestacularArgument(final Boolean value, final String trueSwitch, final String falseSwitch) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        if (value.booleanValue()) {
            return Arrays.asList(trueSwitch);
        } else {
            return Arrays.asList(falseSwitch);
        }
    }

    private List<String> valueToTestacularArgument(final Integer value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, String.valueOf(value));
    }

    private List<String> valueToTestacularArgument(final String value, final String argName) {
        if (value == null) {
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(argName, value);
    }

    private BufferedReader createTestacularOutputReader(final Process p)
    {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

}
