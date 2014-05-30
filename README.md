# maven-karma-plugin
Provides the ability to run tests via [Karma](http://karma-runner.github.com/) as part of your Maven build.

## Usage

Note that the plugin expects Karma (and nodejs of course) to have been installed beforehand and for the `karma`
executable to be on the system path.

Example of a typical usage:

    <plugin>
        <groupId>com.kelveden</groupId>
        <artifactId>maven-karma-plugin</artifactId>
        <version>1.6</version>
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

Full Example:

    <plugin>
        <groupId>com.kelveden</groupId>
        <artifactId>maven-karma-plugin</artifactId>
        <version>1.6</version>
        <executions>
            <execution>
                <phase>test</phase>
                <goals>
                    <goal>start</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <karmaExecutable>${basedir}/node_modules/.bin/karma</karmaExecutable>        
            <configFile>src/main/webapp/resources/karma-0.10.2.conf.js</configFile>
            <junitReportFile>src/main/webapp/resources/test-results.xml</junitReportFile>
            <reportsDirectory>${project.build.directory}/karma-reports</reportsDirectory>
            <browsers>PhantomJS</browsers>
            <autoWatch>false</autoWatch>
            <singleRun>true</singleRun>
            <colors>true</colors>
            <skipKarma>false</skipKarma>
            <skipTests>false</skipTests>
            <karmaFailureIgnore>false</karmaFailureIgnore>
            <reporters>dots,junit</reporters>
        </configuration>
    </plugin>

(In particular, note the use of the `karmaExecutable` property that implies that the karma executable installed to the local node_modules folder will be used instead of the globally installed version.)

## More information

Just run:

    mvn help:describe -Dplugin=com.kelveden:maven-karma-plugin -Ddetail

The plugin simply shells out to `karma`; so the properties you specify in the configuration section will
be passed on as arguments to Karma itself. See the Karma
[configuration documentation](http://karma-runner.github.com/0.8/config/configuration-file.html) for more
information on the arguments available.

Note that only the subset of `karma start` arguments that are relevant are supported by the plugin - there's no
support for the `--port` argument, for example.

Note also that if a property isn't specified in the POM it will not be passed to `karma start` at all - i.e. Karma will
pick the default value for the corresponding argument. The exception to this rule is the "singleRun" property which is
set to "true" by default as this will be the most common use case in the context of a Maven build.

### Using a local karma installation
By default, the plugin assumes that karma is installed globally via `npm install -g karma`. However, if you prefer to use a locally installed karma you can do so by telling the plugin where to find it with the `karmaExecutable` configuration property; e.g. `${basedir}/node_modules/.bin/karma`. (See the full example pom configuration above.)

## Contributing

Bug reports are welcome - pull requests to fix aforementioned bugs even more so! Apart from that,
there really isn't much to the plugin and I think it's best to keep it that way. Am open to other thoughts on that though.
