package io.digdag.core.config;

import java.nio.file.Files;
import java.nio.file.Path;
import com.google.common.io.Resources;
import org.yaml.snakeyaml.error.YAMLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

public class YamlConfigLoaderTest
{
    YamlConfigLoader loader;

    @BeforeEach
    public void setUp()
            throws Exception
    {
        loader = new YamlConfigLoader();
    }

    @Test
    public void verifyDuplicateKeysDisallowed()
            throws Exception
    {
        assertThrows(YAMLException.class, () -> loader.loadString("{\"a\":1, \"a\":2}"));
    }

    @Test
    public void verifyDuplicateKeysDisallowedWithParameterizedLoad()
            throws Exception
    {
        Path temp = Files.createTempFile("digdag-YamlConfigLoaderTest", ".yml");
        Files.write(temp, "{\"a\":1, \"a\":2}".getBytes(UTF_8));
        assertThrows(YAMLException.class, () -> loader.loadParameterizedFile(temp.toFile(), null));
    }
}
