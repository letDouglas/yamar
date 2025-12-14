package com.yamar.userservice;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@SpringBootApplication
@Slf4j
@EnableJpaAuditing
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}

	@Bean
	public OncePerRequestFilter logFilter() {
		return new OncePerRequestFilter() {
			@Override
			protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
					throws ServletException, IOException {
				log.info(">>> [DEBUG-LOG] RICEVUTA RICHIESTA: {} {}", request.getMethod(), request.getRequestURI());
				try {
					filterChain.doFilter(request, response);
				} catch (Exception e) {
					log.error(">>> [DEBUG-LOG] ERRORE DURANTE IL FILTRO: ", e);
					throw e;
				} finally {
					log.info(">>> [DEBUG-LOG] RISPOSTA INVIATA: {}", response.getStatus());
				}
			}
		};
	}
}