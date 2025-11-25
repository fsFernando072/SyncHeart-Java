package school.sptech;

import org.apache.commons.dbcp2.BasicDataSource;

public class DatabaseConfiguration {
    private final BasicDataSource dataSource;

    public DatabaseConfiguration() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/syncheart");
        dataSource.setUsername("heart");
        dataSource.setPassword("Sptech#2024");
    }

    public BasicDataSource getDataSource() {
        return dataSource;
    }
}
