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
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Executes the 'start' task against Karma and keeps the process in background. Should be used together with the 'run' goal.
 * See the Karma documentation itself for information: http://karma.github.com.
 */
@Mojo(name = "startServer", defaultPhase = LifecyclePhase.INITIALIZE)
public class StartServerMojo extends AbstractStartMojo {

    public void execute() throws MojoExecutionException, MojoFailureException {

        if (skipKarma || skipTests) {
            getLog().info("Skipping execution.");
            return;
        }

        killNodeProcesses();

        final Process karma = createKarmaProcess();
        executeKarma(karma);

        System.out.flush();
    }

    private void killNodeProcesses() throws MojoExecutionException {
        try {
            ProcessBuilder killall;
            if (isWindows()) {
                killall = new ProcessBuilder("cmd", "/C", "taskkill /f /im node.exe");
            } else {
                killall = new ProcessBuilder("killall", "-v", "node");
            }
            killall.start().waitFor();
        } catch (Exception e) {
            throw new MojoExecutionException("There was an error killing all node processes", e);
        }
    }

    private Process createKarmaProcess() throws MojoExecutionException {

        final ProcessBuilder builder = new ProcessBuilder("karma", "start", configFile.getAbsolutePath());

        final List<String> command = builder.command();
        command.add("--no-single-run");

        addKarmaArguments(command);


        if (isWindows()) {
            builder.command("cmd", "/C", "start", "/min", StringUtils.join(command.iterator(), " "));
            return startKarmaProcess(builder);
        } else {
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter("start_karma.sh"));
                out.write(StringUtils.join(command.iterator(), " ") + " 2>out.log" + " 1>out.log" + " &");
                out.close();
                new ProcessBuilder("bash", "-l", "-c", "chmod +x start_karma.sh").start().waitFor();
                return startKarmaProcess(new ProcessBuilder("bash", "-l", "-c", "./start_karma.sh"));
            } catch (IOException e) {
                throw new MojoExecutionException("",e);
            } catch (InterruptedException e) {
                throw new MojoExecutionException("",e);
            }
        }
    }


}
