package com.amoura.book.role;

import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;

import java.util.Optional;


public interface RoleRepository extends JpaAttributeConverter<Role, Integer> {
    Optional<Role> findByName(String name);
}
