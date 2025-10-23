package org.userregistrationspringsecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.userregistrationspringsecurity.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
