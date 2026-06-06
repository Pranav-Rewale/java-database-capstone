package com.project.back_end.config;

import com.project.back_end.entity.Admin;
import com.project.back_end.repo.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

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
        List<Admin> admins = adminRepository.findAll();
        boolean adminExists = false;
        for (Admin a : admins) {
            if ("admin".equals(a.getUsername())) {
                if (adminExists) {
                    adminRepository.delete(a);
                    System.out.println(">>> Deleted duplicate Admin account <<<");
                } else {
                    adminExists = true;
                }
            }
        }
        if (!adminExists) {
            Admin defaultAdmin = new Admin("admin", "admin");
            adminRepository.save(defaultAdmin);
            System.out.println(">>> Default Admin created (admin / admin) <<<");
        }
    }
}
