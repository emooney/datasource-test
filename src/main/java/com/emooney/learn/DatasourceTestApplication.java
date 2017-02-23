package com.emooney.learn;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jca.context.SpringContextResourceAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@SpringBootApplication
public class DatasourceTestApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DatasourceTestApplication.class);
	
	public static void main(String[] args) {
		
		
		SpringApplication.run(DatasourceTestApplication.class, args);
	}

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Override
	public void run(String... arg0) throws Exception {

	       log.info("Creating tables");

	        jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
	        jdbcTemplate.execute("CREATE TABLE customers(" +
	                "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

	        // Split up the array of whole names into an array of first/last names
	        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long", "Eric Mooney", "Elvis Presley").stream()
	                .map(name -> name.split(" "))
	                .collect(Collectors.toList());

	        // Use a Java 8 stream to print out each tuple of the list
	        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

	        // Uses JdbcTemplate's batchUpdate operation to bulk load data
	        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

	        // adding a row from a sql file
	        ScriptUtils.executeSqlScript(jdbcTemplate.getDataSource().getConnection(), new ClassPathResource("write.sql"));
	        
	        // get the sql file object
	        LineNumberReader lnr = new LineNumberReader(new FileReader("C:\\WS\\sts-repos\\datasource-test\\src\\main\\resources\\read.sql"));
	        
	        // read the contents as a string
	        String mySql = ScriptUtils.readScript(lnr, ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR);
	        
	        log.info("----------");
	        log.info("This is what came back from reading the sql in a file:");
	        log.info("sql file contents: " + mySql);
	        jdbcTemplate.query(
	                mySql, (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
	        ).forEach(customer -> log.info(customer.toString()));
	        
	        
	        log.info("-------");
	        	log.info("sql statement coming from read.sql is " + mySql);
	        log.info("-------");
	        
	        log.info("Querying for customer records where first_name = 'Josh':");
	        jdbcTemplate.query(
	                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
	                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
	        ).forEach(customer -> log.info(customer.toString()));
	    
	}

	
	// for RowMapping code, look here: https://www.mkyong.com/spring/spring-jdbctemplate-querying-examples/
	// next, I need to be able to map Result sets back with java objects and I think this article talks about how to do that. 
	
}
