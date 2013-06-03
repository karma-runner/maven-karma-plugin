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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

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


    private Process createKarmaProcess() throws MojoExecutionException {

        final ProcessBuilder builder;

        if (isWindows()) {
            builder = new ProcessBuilder("cmd", "/C", "karma", "run");
        } else {
            builder = new ProcessBuilder("karma", "run");
        }

        return startKarmaProcess(builder);
    }
}
