package ch.xenia.todojpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JPA - Java persistence API
// Hibernate - a common implementation of JPA
// Spring Data JPA - Spring abstraction which makes JPA easier to use

@SpringBootApplication
public class TodoJpaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoJpaApplication.class, args);
    }

}
