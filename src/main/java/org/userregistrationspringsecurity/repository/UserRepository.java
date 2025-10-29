package org.userregistrationspringsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.userregistrationspringsecurity.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    Optional<User> findByVerificationToken(String token);

}
