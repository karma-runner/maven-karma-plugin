# testacular-maven-plugin

Provides the ability to run tests via [Testacular](http://testacular.github.com/) as part of your Maven build.

## Usage

Note that the plugin expects Testacular (and nodejs of course) to have been installed beforehand and for the `testacular`
executable to be on the system path.

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
        </configuration>
    </plugin>

## More information

Just run:

    mvn help:describe -Dplugin=com.kelveden.plugins:testacular-maven-plugin -Ddetail

The plugin simply shells out to `testacular`; so the properties you specify in the configuration section will
be passed on as arguments to Testacular itself. See the Testacular
[configuration documentation](http://testacular.github.com/0.6.0/config/configuration-file.html) for more
information on the arguments available.

Note that only the subset of `testacular start` arguments that are relevant are supported by the plugin - there's no
support for the `--port` argument, for example.

Note also that if a property isn't specified in the POM it will not be passed to `testacular start` at all - i.e. Testacular will
pick the default value for the corresponding argument. The exception to this rule is the "singleRun" property which is
set to "true" by default as this will be the most common use case in the context of a Maven build.

## Contributing

Bug reports are welcome - pull requests to fix aforementioned bugs even more so! Apart from that,
there really isn't much to the plugin and I think it's best to keep it that way. Am open to other thoughts on that though.
