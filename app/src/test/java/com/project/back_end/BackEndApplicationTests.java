package com.project.back_end;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import com.project.back_end.repo.PrescriptionRepository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@SpringBootTest(properties = {
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect",
    "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"
})
@Import(BackEndApplicationTests.MockConfig.class)
class BackEndApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class MockConfig {

		@Bean
		@SuppressWarnings("unchecked")
		public DataSource dataSource() throws SQLException {
			DataSource ds = Mockito.mock(DataSource.class);
			Connection conn = Mockito.mock(Connection.class);
			DatabaseMetaData meta = Mockito.mock(DatabaseMetaData.class);
			
			Mockito.when(ds.getConnection()).thenReturn(conn);
			Mockito.when(conn.getMetaData()).thenReturn(meta);
			Mockito.when(meta.getDatabaseProductName()).thenReturn("MySQL");
			Mockito.when(meta.getDatabaseProductVersion()).thenReturn("8.0");
			Mockito.when(meta.getDriverName()).thenReturn("MySQL Connector/J");
			Mockito.when(meta.getDriverVersion()).thenReturn("8.0");
			
			return ds;
		}

		@Bean
		public MongoTemplate mongoTemplate() {
			return Mockito.mock(MongoTemplate.class);
		}

		@Bean
		public PrescriptionRepository prescriptionRepository() {
			return Mockito.mock(PrescriptionRepository.class);
		}
	}
}
