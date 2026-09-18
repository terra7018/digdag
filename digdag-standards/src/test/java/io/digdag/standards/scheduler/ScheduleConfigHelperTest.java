package io.digdag.standards.scheduler;

import com.google.common.base.Optional;
import io.digdag.client.config.Config;
import io.digdag.client.config.ConfigException;
import io.digdag.client.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;

import static io.digdag.client.DigdagClient.objectMapper;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScheduleConfigHelperTest {
    static Config newConfig() {
        return new ConfigFactory(objectMapper()).create();
    }

    private ScheduleConfigHelper helper = new ScheduleConfigHelper();

    @Test
    public void testStart()
    {
        Config config = newConfig()
                .set("start", "2022-03-15")
                .set("end", "2022-04-30");
        // Expected "2022-03-15T00:00:00+0900 (1647270000)"
        assertThat(helper.getDateTimeStart(config, "start", ZoneId.of("Asia/Tokyo")), is(Optional.of(Instant.ofEpochSecond(1647270000L))));
        // Expected "2022-03-15T00:00:00+0000 (1647302400)"
        assertThat(helper.getDateTimeStart(config, "start", ZoneId.of("UTC")), is(Optional.of(Instant.ofEpochSecond(1647302400L))));
    }

    @Test
    public void testEnd()
    {
        Config config = newConfig()
                .set("start", "2022-03-15")
                .set("end", "2022-04-30");
        // Expected "2022-05-01T00:00:00+0900 (1651330800)"
        assertThat(helper.getDateTimeEnd(config, "end", ZoneId.of("Asia/Tokyo")), is(Optional.of(Instant.ofEpochSecond(1651330800L))));
        // Expected "2022-05-01T00:00:00+0000 (1651363200)"
        assertThat(helper.getDateTimeEnd(config, "end", ZoneId.of("UTC")), is(Optional.of(Instant.ofEpochSecond(1651363200L))));
    }

    @Test
    public void testNonExistentDate()
    {
        // Non existent date
        Config config = newConfig()
                .set("start", "2022-04-31");
        assertThrows(ConfigException.class, () -> helper.getDateTimeStart(config, "start", ZoneId.of("Asia/Tokyo")));
    }

    @Test
    public void testInvalidFormat()
    {
        // Non existent date
        Config config = newConfig()
                .set("start", "2022-04-ii");
        assertThrows(ConfigException.class, () -> helper.getDateTimeStart(config, "start", ZoneId.of("Asia/Tokyo")));
    }

    @Test
    public void testValidateStartEndSuccess()
    {
        helper.validateStartEnd(Optional.of(Instant.ofEpochSecond(1651330801L)), Optional.of(Instant.ofEpochSecond(1651330802L)));
    }

    @Test
    public void testValidateStartEndFail1()
    {
        assertThrows(ConfigException.class, () -> helper.validateStartEnd(Optional.of(Instant.ofEpochSecond(1651330801L)), Optional.of(Instant.ofEpochSecond(1651330800L))));
    }

    @Test
    public void testValidateStartEndFail2()
    {
        // Can't accept start == end because end date will be added 1day internally
        // start: 2022-04-02
        // end: 2022-04-01
        // In above case, start will be parsed as "2022-04-02 00:00:00" and end will be parsed as "2022-04-02 00:00:00" (plus 1day)
        assertThrows(ConfigException.class, () -> helper.validateStartEnd(Optional.of(Instant.ofEpochSecond(1651330801L)), Optional.of(Instant.ofEpochSecond(1651330801L))));
    }

}
