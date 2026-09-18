package io.digdag.standards.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigFactory;
import io.digdag.client.config.ConfigUtils;
import io.digdag.core.Environment;
import io.digdag.core.agent.ConfigEvalEngine;
import io.digdag.core.agent.OperatorRegistry;
import io.digdag.core.agent.TaskContextCommandLogger;
import io.digdag.core.plugin.PluginSet;
import io.digdag.core.workflow.MockCommandExecutor;
import io.digdag.core.workflow.MockCommandExecutorFactory;
import io.digdag.spi.CommandExecutor;
import io.digdag.spi.CommandExecutorFactory;
import io.digdag.spi.CommandLogger;
import io.digdag.spi.TemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;

public class CommandExecutorProviderTest
{
    private final ObjectMapper om = new ObjectMapper();
    private final ConfigFactory configFactory = new ConfigFactory(om);
    private Config systemConfig;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        this.systemConfig = configFactory.create();
    }

    private static  <T extends CommandExecutorProvider> T newCommandExecutorProvider(Class<T> providerClass, Config systemConfig)
    {
        Injector injector = Guice.createInjector((binder) -> {
            Multibinder<CommandExecutorFactory> commandExecutorBinder = Multibinder.newSetBinder(binder, CommandExecutorFactory.class);
            commandExecutorBinder.addBinding().to(MockCommandExecutorFactory.class).in(Scopes.SINGLETON);
            commandExecutorBinder.addBinding().to(DockerCommandExecutorFactory.class).in(Scopes.SINGLETON);
            binder.bind(CommandExecutor.class).toProvider(CommandExecutorProvider.class).in(Scopes.SINGLETON);
            binder.bind(PluginSet.WithInjector.class).toInstance(mock(PluginSet.WithInjector.class));
            binder.bind(CommandLogger.class).to(TaskContextCommandLogger.class).in(Scopes.SINGLETON);
            binder.bind(TemplateEngine.class).to(ConfigEvalEngine.class).in(Scopes.SINGLETON);
            binder.bind(ConfigFactory.class).toInstance(ConfigUtils.configFactory);
            binder.bind(Config.class).toInstance(systemConfig);
            binder.bind(OperatorRegistry.DynamicOperatorPluginInjectionModule.class).in(Scopes.SINGLETON);
            binder.bind(new TypeLiteral<Map<String, String>>() {}).annotatedWith(Environment.class).toInstance(ImmutableMap.<String, String>of());
        });
        return injector.getInstance(providerClass);
    }

    @Test
    public void testCommandExecutorType()
    {
        {
            systemConfig.set("agent.command_executor.type", "mock");
            assertThat(newCommandExecutorProvider(CommandExecutorProvider.class, systemConfig).get(), is(instanceOf(MockCommandExecutor.class)));
        }
        {
            systemConfig.set("agent.command_executor.type", "docker");
            assertThat(newCommandExecutorProvider(CommandExecutorProvider.class, systemConfig).get(), is(instanceOf(DockerCommandExecutor.class)));
        }
        {
            systemConfig.set("agent.command_executor.type", "ooo");
            try {
                newCommandExecutorProvider(CommandExecutorProvider.class, systemConfig).get();
                fail();
            }
            catch (Exception e) {
                assertThat(e.getMessage(), containsString("Configured commandExecutor name is not found: ooo"));
            }
        }
    }
}
