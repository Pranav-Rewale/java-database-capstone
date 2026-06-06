package com.project.back_end.config;

import com.project.back_end.entity.Admin;
import com.project.back_end.repo.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Automatically initializes database records on application startup.
 * Creates a default administrator account if the admin table is empty.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;

    @Autowired
    public DatabaseInitializer(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (adminRepository.count() == 0) {
            Admin defaultAdmin = new Admin("admin", "admin");
            adminRepository.save(defaultAdmin);
            System.out.println(">>> Default Admin created (admin / admin) <<<");
        }
    }
}
