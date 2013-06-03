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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 */
public abstract class AbstractKarmaMojo extends AbstractMojo {
    /**
     * Flag that when set to true indicates that execution of the goal should be skipped. Note that setting this property
     * will skip Karma tests *only*. If you also want to skip tests such as those run by the maven-surefire-plugin, consider
     * using the skipTests property instead.
     */
    @Parameter(property = "skipKarma", required = false, defaultValue = "false")
    protected Boolean skipKarma;

    /**
     * Flag that when set to true indicates that execution of the goal should be skipped. Note that setting this property
     * also has the effect of skipping tests under plugins such as the maven-surefire-plugin. If you want to *just* skip
     * Karma tests, use the skipKarma property instead.
     */
    @Parameter(property = "skipTests", required = false, defaultValue = "false")
    protected Boolean skipTests;

    /**
     * Flag that when set to to true ensures that the Maven build does not fail when if the Karma tests fail. As
     * for the similar property on the maven-surefire-plugin: its use is not recommended, but quite convenient on occasion.
     */
    @Parameter(property = "karmaFailureIgnore", required = false, defaultValue = "false")
    protected Boolean karmaFailureIgnore;

}
