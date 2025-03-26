package com.hackonauts.hackonauts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mail.MailSenderValidatorAutoConfiguration;

@SpringBootApplication
public class HackonautsApplication {

	public static void main(String[] args) {
		SpringApplication.run(HackonautsApplication.class, args);
	}

}
