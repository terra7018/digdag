package io.digdag.server.metrics;

import com.google.common.base.Optional;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigElement;
import io.digdag.client.config.ConfigFactory;
import io.digdag.server.metrics.fluency.FluencyMonitorSystemConfig;
import io.digdag.server.metrics.jmx.JmxMonitorSystemConfig;
import io.digdag.spi.metrics.DigdagMetrics;
import static io.digdag.client.DigdagClient.objectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;

public class DigdagMetricsConfigTest
{
    Logger logger = LoggerFactory.getLogger(DigdagMetricsConfigTest.class);

    Config config;

    @BeforeEach
    public void setup()
    {
        config = ConfigElement.ofMap(new HashMap<String,String>()).toConfig(new ConfigFactory(objectMapper()));
    }

    public Config fromJson(String json)
    {
        return ConfigElement.fromJson(json).toConfig(new ConfigFactory(objectMapper()));
    }

    @Test
    public void testDefault()
    {
        DigdagMetricsConfig metricsConfig = new DigdagMetricsConfig(config);
        assertFalse(metricsConfig.getMonitorSystemConfig("jmx").isPresent(), "No jmx config");
    }

    @Test
    public void testJmxEnabled()
    {
        /**
         *  metrics.enable: " jmx "
         */
        config = fromJson("{ \"metrics.enable\": \" jmx \" }");
        DigdagMetricsConfig metricsConfig = new DigdagMetricsConfig(config);

        Optional<JmxMonitorSystemConfig> jmxConfig = metricsConfig.getMonitorSystemConfig("jmx").transform((p) -> (JmxMonitorSystemConfig)p);
        assertTrue(jmxConfig.isPresent(), "Exist jmx config");
        assertTrue(jmxConfig.get().getMonitorSystemEnable(), "plugin is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.AGENT), "category 'agent' is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.API), "category 'api' is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.DB), "category 'db' is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.EXECUTOR), "category 'executor' is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.DEFAULT), "category 'default' is enable");
    }

    @Test
    public void testJmxEnabledWithParams()
    {
        /**
         *  server.metrics.enable: " jmx "
         *  server.metrics.jmx.categories: "agent, executors "
         */
        config = fromJson(
                "{ " +
                        "\"metrics.enable\": \" jmx \", " +
                        "\"metrics.jmx.categories\": \"agent, executor\" " +
                 "}");
        DigdagMetricsConfig metricsConfig = new DigdagMetricsConfig(config);

        Optional<JmxMonitorSystemConfig> jmxConfig = metricsConfig.getMonitorSystemConfig("jmx").transform((p) -> (JmxMonitorSystemConfig)p);
        assertTrue(jmxConfig.isPresent(), "Exist jmx config");
        assertTrue(jmxConfig.get().getMonitorSystemEnable(), "plugin is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.AGENT), "category 'agent' is enable");
        assertFalse(jmxConfig.get().enable(DigdagMetrics.Category.API), "category 'api' is enable");
        assertFalse(jmxConfig.get().enable(DigdagMetrics.Category.DB), "category 'db' is enable");
        assertTrue(jmxConfig.get().enable(DigdagMetrics.Category.EXECUTOR), "category 'executor' is enable");
        assertFalse(jmxConfig.get().enable(DigdagMetrics.Category.DEFAULT), "category 'default' is enable");
    }


    @Test
    public void testFluencyEnabled()
    {
        /**
         *  metrics.enable: " fluency "
         */
        config = fromJson("{ \"metrics.enable\": \" fluency \" }");
        DigdagMetricsConfig metricsConfig = new DigdagMetricsConfig(config);

        Optional<FluencyMonitorSystemConfig> fluencyConfig = metricsConfig.getMonitorSystemConfig("fluency").transform((p) -> (FluencyMonitorSystemConfig)p);
        assertTrue(fluencyConfig.isPresent(), "Exist fluency config");
        assertTrue(fluencyConfig.get().getMonitorSystemEnable(), "plugin is enable");
        assertTrue(fluencyConfig.get().getMonitorSystemEnable(), "category 'agent' is enable");
        assertTrue(fluencyConfig.get().enable(DigdagMetrics.Category.API), "category 'api' is enable");
        assertTrue(fluencyConfig.get().enable(DigdagMetrics.Category.DB), "category 'db' is enable");
        assertTrue(fluencyConfig.get().enable(DigdagMetrics.Category.EXECUTOR), "category 'executor' is enable");
        assertTrue(fluencyConfig.get().enable(DigdagMetrics.Category.DEFAULT), "category 'default' is enable");
        assertEquals(60L, fluencyConfig.get().getStep(), "step is 60 secs as default");
    }

    @Test
    public void testFluencyEnabledWithParams()
    {
        /**
         *  server.metrics.enable: " fluency "
         *  server.metrics.jmx.categories: "agent, executors "
         */
        config = fromJson(
                "{ " +
                        "\"metrics.enable\": \" fluency \", " +
                        "\"metrics.fluency.categories\": \"agent, executor\", " +
                        "\"metrics.fluency.host\": \"server01:9999\", " +
                        "\"metrics.fluency.tag\": \"tag0001\", " +
                        "\"metrics.fluency.step\": 120" +
                        "}");
        DigdagMetricsConfig metricsConfig = new DigdagMetricsConfig(config);

        Optional<FluencyMonitorSystemConfig> fluencyConfig = metricsConfig.getMonitorSystemConfig("fluency").transform((p) -> (FluencyMonitorSystemConfig)p);
        assertTrue(fluencyConfig.isPresent(), "Exist fluency config");
        assertTrue(fluencyConfig.get().getMonitorSystemEnable(), "plugin is enable");
        assertTrue(fluencyConfig.get().enable(DigdagMetrics.Category.AGENT), "category 'agent' is enable");
        assertFalse(fluencyConfig.get().enable(DigdagMetrics.Category.API), "category 'api' is enable");
        assertFalse(fluencyConfig.get().enable(DigdagMetrics.Category.DB), "category 'db' is enable");
        assertTrue(fluencyConfig.get().enable(DigdagMetrics.Category.EXECUTOR), "category 'executor' is enable");
        assertFalse(fluencyConfig.get().enable(DigdagMetrics.Category.DEFAULT), "category 'default' is enable");
        assertEquals("server01:9999", fluencyConfig.get().getHost(), "host");
        assertEquals("tag0001", fluencyConfig.get().getTag(), "tag");
        assertEquals(120L, fluencyConfig.get().getStep(), "step");
    }
}
