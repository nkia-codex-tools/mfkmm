package com.mkfmm.auth.config;

import com.mkfmm.auth.application.port.outbound.UserRepository;
import com.mkfmm.auth.domain.model.Role;
import com.mkfmm.auth.domain.model.User;
import com.mkfmm.auth.domain.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RootAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RootAdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final String rootUser;
    private final String rootPassword;

    public RootAdminInitializer(
            UserRepository userRepository,
            PasswordService passwordService,
            @Value("${mkfmm.root.user}") String rootUser,
            @Value("${mkfmm.root.password}") String rootPassword) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.rootUser = rootUser;
        this.rootPassword = rootPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByRole(Role.ROOT_ADMIN)) {
            log.info("Root administrator already exists, skipping initialization");
            return;
        }

        String hashedPassword = passwordService.hash(rootPassword);
        User rootAdmin = new User(rootUser, hashedPassword, Role.ROOT_ADMIN, true);
        userRepository.save(rootAdmin);
        log.info("Root administrator initialized: {}", rootUser);
    }
}
