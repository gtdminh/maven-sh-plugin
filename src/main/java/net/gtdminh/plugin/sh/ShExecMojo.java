package net.gtdminh.plugin.sh;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.commons.exec.*;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * 
 * @phase process-sources
 */
@Mojo(name = "exec", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ShExecMojo
    extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "cmd", required = true)
    private String cmd;

    @Parameter(property = "workingDirectory", defaultValue = "${project.basedir}")
    private File workingDirectory;

    @Parameter(property = "envs")
    private String[] envs;

    public void execute()
        throws MojoExecutionException
    {
        // execute shell command defined in 'cmd' in directory defined in 'workingDirectory'
        // and log out the output to maven log
        try {
            getLog().info(MessageFormat.format("Executing {0} in \"{1}\"", cmd, workingDirectory));

            CommandLine cli = CommandLine.parse(cmd);

            Map<String, String> envp = System.getenv();

            if(envs != null && envs.length > 0) {
                Arrays.stream(envs).filter(e -> !e.contains("="))
                        .map(e -> {
                            if(e.contains("PATH=")) {
                                e += System.getenv("PATH");
                            }
                            return e;
                        })
                        .map(e -> e.split("="))
                        .forEach(e -> envp.put(e[0], e[1]));
            }
            Executor executor = createExecturor(workingDirectory);

            int exitCode = executor.execute(cli, envp);

            getLog().info(MessageFormat.format("Command exited with code {0}", exitCode));
        }catch (IOException e) {
            getLog().error(e);
        }
    }

    protected Executor createExecturor( File workingDirectory) {
        Executor executor = DefaultExecutor.builder().setWorkingDirectory(workingDirectory).get();
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

        return executor;
    }
}
