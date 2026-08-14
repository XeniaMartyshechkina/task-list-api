package ch.xenia.todojpa.config;

import ch.xenia.todojpa.domain.Person;
import ch.xenia.todojpa.domain.PersonRoleEnum;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.firstName}")
    private String adminFirstName;

    @Value("${app.admin.lastName}")
    private String adminLastName;

    @Value("${app.admin.address}")
    private String adminAddress;

    @Value("${app.admin.password}")
    private String adminPassword;


    public AdminInitializer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public void run(String... args) throws Exception {

        if (entityManager.find(Person.class, adminEmail) != null) {
            return;
        }

        Person admin = new Person();
        admin.setEmail(adminEmail);
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setAddress(adminAddress);
        admin.setRole(PersonRoleEnum.ADMIN);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        entityManager.persist(admin);
    }
}
