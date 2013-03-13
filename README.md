# testacular-maven-plugin

Provides the ability to run tests via [Testacular](http://testacular.github.com/) as part of your Maven build.

## Usage

Note that the plugin expects Testacular (and nodejs of course) to have been installed beforehand and for the `testacular`
binary to be on the system path.

Example of a typical usage:

    <plugin>
        <groupId>com.kelveden.plugins</groupId>
        <artifactId>testacular-maven-plugin</artifactId>
        <version>0.2</version>
        <executions>
            <execution>
                <goals>
                    <goal>start</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <browsers>PhantomJS</browsers>
            <singleRun>true</singleRun>
        </configuration>
    </plugin>

## More information

Just run:

    mvn help:describe -Dplugin=com.kelveden.plugins:testacular-maven-plugin -Ddetail

The plugin simply shells out to `testacular`; so the properties you specify in the configuration section will
roughly match up with the arguments to Testacular itself. See the Testacular
[configuration documentation](http://testacular.github.com/0.6.0/config/configuration-file.html) for more
information on the arguments available.
