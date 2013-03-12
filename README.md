testacular-maven-plugin
=======================

Provides the ability to run tests via [Testacular]() as part of your Maven build.

Usage
-----

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
