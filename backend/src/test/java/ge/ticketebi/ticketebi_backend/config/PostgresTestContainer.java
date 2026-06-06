package ge.ticketebi.ticketebi_backend.config;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTestContainer extends PostgreSQLContainer<PostgresTestContainer> {
    private static final String IMAGE_VERSION = "postgres:16";
    private static PostgresTestContainer container;

    private PostgresTestContainer() {
        super(IMAGE_VERSION);
    }

    public static PostgresTestContainer getInstance() {
        if (container == null) {
            container = new PostgresTestContainer()
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USER", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        // do nothing, JVM handles shut down
    }
}
