package io.digdag.cli;

import com.beust.jcommander.ParameterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class BasicAuthParameterValidatorTest
{
    @Test
    public void validUserPassPasses()
    {
        new BasicAuthParameterValidator().validate(null, "user:pass");
    }

    @Test
    public void missingUsernameFails()
    {
        assertThrows(ParameterException.class, () -> new BasicAuthParameterValidator().validate(null, ":pass"));
    }

    @Test
    public void missingPasswordFails()
    {
        assertThrows(ParameterException.class, () -> new BasicAuthParameterValidator().validate(null, "user:"));
    }

    @Test
    public void missingColonFails()
    {
        assertThrows(ParameterException.class, () -> new BasicAuthParameterValidator().validate(null, "userpass"));
    }
}