package org.couchbase.devex;

import java.io.File;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.util.rawQuerying.RawQueryExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StoreSearchFilesSyncApplication {

	@Autowired
	BinaryStoreConfiguration configuration;

	public static void main(String[] args) {
		SpringApplication.run(StoreSearchFilesSyncApplication.class, args);
	}

	@Bean
	CommandLineRunner init() {
		return (String[] args) -> {
			new File(configuration.getBinaryStoreRoot()).mkdirs();
		};
	}

	@Bean
	public RawQueryExecutor rawQueryExecutor(final Bucket bucket){
		return  new RawQueryExecutor(bucket.name(), "", bucket.core(), bucket.environment());
	}
}
