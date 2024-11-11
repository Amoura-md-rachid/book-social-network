package com.amoura.book.role;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaAttributeConverter<Role, Integer> {
    Optional<Role> findByName(String name);
}
