package com.project.ecommerce.domain.user.repository;

import com.project.ecommerce.domain.user.entity.Address;
import com.project.ecommerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    Optional<Address> findByUserAndIsDefault(User user, boolean flag);

    Optional<Address> findByIdAndUser(Long addressId, User user);
}
