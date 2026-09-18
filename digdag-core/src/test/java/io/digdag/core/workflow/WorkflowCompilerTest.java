package io.digdag.core.workflow;

import com.google.common.io.Resources;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.client.config.ConfigFactory;
import io.digdag.commons.ThrowablesUtil;
import io.digdag.core.DigdagEmbed;
import io.digdag.core.config.YamlConfigLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorkflowCompilerTest
{
    private static DigdagEmbed embed;

    private WorkflowCompiler compiler;

    @BeforeAll
    public static void createDigdagEmbed()
            throws Exception
    {
        embed = WorkflowTestingUtils.setupEmbed();
    }

    @AfterAll
    public static void destroyDigdagEmbed()
            throws Exception
    {
        embed.close();
    }

    @BeforeEach
    public void setUp()
            throws Exception
    {
        compiler = new WorkflowCompiler();
    }

    private Config loadYamlResource(String name)
    {
        try {
            String content = Resources.toString(getClass().getResource(name), UTF_8);
            return embed.getInjector().getInstance(YamlConfigLoader.class)
                    .loadString(content)
                    .toConfig(embed.getInjector().getInstance(ConfigFactory.class));
        }
        catch (IOException ex) {
            throw ThrowablesUtil.propagate(ex);
        }
    }

    @Test
    public void verifySingleOperatorPasses()
    {
        Config config = loadYamlResource("/io/digdag/core/workflow/single_operator.dig");
        compiler.compile("single_operator", config);
    }

    @Test
    public void verifyMultipleOperatorsFail()
    {
        Config config = loadYamlResource("/io/digdag/core/workflow/multiple_operators.dig");
        assertThrows(ConfigException.class, () -> compiler.compile("multiple_operators", config));
    }

    @Test
    public void verifyUnusedKeysInGroupingTask()
    {
        Config config = loadYamlResource("/io/digdag/core/workflow/unused_keys_in_group.dig");
        assertThrows(ConfigException.class, () -> compiler.compile("unused_keys_in_group", config));
    }

    @Test
    public void verifyErrorTaskIsValidated()
    {
        Config config = loadYamlResource("/io/digdag/core/workflow/invalid_error_task.dig");
        assertThrows(ConfigException.class, () -> compiler.compile("invalid_error_task", config));
    }

    @Test
    public void verifyVariableForParallelInGroupFail()
    {
        Config config = loadYamlResource("/io/digdag/core/workflow/parallel_variable_group.dig");
        assertThrows(ConfigException.class, () -> compiler.compile("parallel_variable_group", config));
    }

    @Test
    public void verifyVariableForParallel()
    {
        Config config = loadYamlResource("/io/digdag/core/workflow/parallel_variable_loop.dig");
        // Currently there is no error in this case (loop operators with parallel)
        compiler.compile("parallel_variable_loop", config);
    }

}
