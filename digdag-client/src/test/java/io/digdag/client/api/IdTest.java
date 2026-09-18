package io.digdag.client.api;

import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IdTest
{
    @Test
    public void testAsIntAndLong()
            throws Exception
    {
        assertThat(Id.of("1").asInt(), is(1));
        assertThat(Id.of("2").asInt(), is(2));
        assertThat(Id.of("1").asLong(), is(1L));
    }

    @Test
    public void testIntOverflow()
            throws Exception
    {
        assertThrows(NumberFormatException.class, () -> Id.of("2147483648").asInt());
    }

    @Test
    public void testLongOverflow()
            throws Exception
    {
        assertThrows(NumberFormatException.class, () -> Id.of("-9223372036854775809").asLong());
    }
}
