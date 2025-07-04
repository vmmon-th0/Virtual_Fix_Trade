package com.vmmon.database;

import org.flywaydb.core.Flyway;

public class DatabaseMigrator {
    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD)
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }
}
