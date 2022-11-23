package com.itheima;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
@EnableCaching
public class ReggieTakeOutApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReggieTakeOutApplication.class, args);
		log.info("Project Initialized Successfully...");
	}

}
