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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Executes the 'run' task against Karma. Should be used together with the 'startServer' goal.
 * See the Karma documentation itself for information: http://karma.github.com.
 */
@Mojo(name = "run", defaultPhase = LifecyclePhase.TEST)
public class RunMojo extends AbstractKarmaMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skipKarma || skipTests) {
            getLog().info("Skipping execution.");
            return;
        }

        final Process karma = createKarmaProcess();

        if (!executeKarma(karma)) {
            if (karmaFailureIgnore) {
                getLog().warn("There were Karma test failures.");
            } else {
                throw new MojoFailureException("There were Karma test failures.");
            }
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

        final ProcessBuilder builder;

        if (isWindows()) {
            builder = new ProcessBuilder("cmd", "/C", "karma", "run");
        } else {
            builder = new ProcessBuilder("karma", "run");
        }

        final List<String> command = builder.command();

        builder.redirectErrorStream(true);

        try {

            System.out.println(StringUtils.join(command.iterator(), " "));

            return builder.start();

        } catch (IOException e) {
            throw new MojoExecutionException("There was an error executing Karma.", e);
        }
    }

    private BufferedReader createKarmaOutputReader(final Process p) {
        return new BufferedReader(new InputStreamReader(p.getInputStream()));
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

}
