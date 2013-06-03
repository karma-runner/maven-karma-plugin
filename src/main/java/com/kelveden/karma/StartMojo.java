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
public class StartMojo extends AbstractStartMojo {

    /**
     * Flag indicating whether the Karma server will exit after a single test run. See the "singleRun" section of
     * the Karma online configuration documentation for more information. Defaults to true.
     */
    @Parameter(property = "singleRun", required = false, defaultValue = "true")
    private Boolean singleRun;

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skipKarma || skipTests) {
            getLog().info("Skipping execution.");
            return;
        }

        final Process karma = createKarmaProcess();

        if (!executeKarma(karma) && singleRun) {
            if (karmaFailureIgnore) {
                getLog().warn("There were Karma test failures.");
            } else {
                throw new MojoFailureException("There were Karma test failures.");
            }
        }

        System.out.flush();
    }

    private Process createKarmaProcess() throws MojoExecutionException {

        final ProcessBuilder builder;

        if (isWindows()) {
            builder = new ProcessBuilder("cmd", "/C", "karma", "start", configFile.getAbsolutePath());
        } else {
            builder = new ProcessBuilder("karma", "start", configFile.getAbsolutePath());
        }

        final List<String> command = builder.command();

        command.addAll(valueToKarmaArgument(singleRun, "--single-run", "--no-single-run"));
        addKarmaArguments(command);

        return startKarmaProcess(builder);
    }


}
