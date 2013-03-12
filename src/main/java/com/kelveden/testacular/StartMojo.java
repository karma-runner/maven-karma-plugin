package com.kelveden.testacular;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.TEST)
public class StartMojo extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${basedir}/testacular.conf.js", property = "configFile", required = false)
    private File configFile;

    @Parameter(property = "browsers", required = false)
    private String browsers;

    public void execute() throws MojoExecutionException, MojoFailureException {

        final StringBuilder buffer = new StringBuilder();

        try {
            final ProcessBuilder builder = new ProcessBuilder("testacular", "start", configFile.getAbsolutePath(), "--single-run");
            if (!StringUtils.isEmpty(browsers)) {
                builder.command().add("--browsers");
                builder.command().add(browsers);
            }

            builder.redirectErrorStream(true);

            final Process p = builder.start();

            String currentLine = "";
            BufferedReader inputReader = getInputStream(p);
            for (String line = inputReader.readLine(); line != null; line = inputReader.readLine())
            {
                currentLine = line;
                System.out.println(line);
                buffer.append(line).append("\n");
            }

            p.waitFor();

            if (currentLine.contains("FAILED")) {
                throw new MojoFailureException("Testacular tests failed.");
            }

            System.out.flush();

        } catch (IOException e) {
            throw new MojoExecutionException("Could not shell out to Testacular.", e);
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Could not shell out to Testacular.", e);
        }
    }

    private BufferedReader getInputStream(Process p)
    {
        InputStream inputStream = p.getInputStream();
        return new BufferedReader(new InputStreamReader(inputStream));
    }

}
