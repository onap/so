package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;

public class V4_2__DummyMigration implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        //does nothing
    }
}
