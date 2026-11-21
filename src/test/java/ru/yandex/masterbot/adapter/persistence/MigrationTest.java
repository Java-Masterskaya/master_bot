package ru.yandex.masterbot.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.masterbot.AbstractIntegrationTest;

class MigrationTest extends AbstractIntegrationTest {

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void shouldApplyInitialMigration() {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM flyway_schema_history
            WHERE version = '1'
              AND success = true
            """,
            Integer.class);

    assertThat(count).isEqualTo(1);
  }
}
