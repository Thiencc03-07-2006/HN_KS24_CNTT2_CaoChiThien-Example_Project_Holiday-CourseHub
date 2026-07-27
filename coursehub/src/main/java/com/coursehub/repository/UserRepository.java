package com.coursehub.repository;

import com.coursehub.entity.UserEntity;
import com.coursehub.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID excludeId);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.roles WHERE u.email = :email AND u.deletedAt IS NULL")
    Optional<UserEntity> findByEmailWithRoles(@Param("email") String email);

    @Modifying
    @Query("UPDATE UserEntity u SET u.status = :status WHERE u.id = :id")
    int updateStatus(@Param("id") UUID id, @Param("status") UserStatus status);

    @Query("SELECT COUNT(u) FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName AND u.deletedAt IS NULL")
    long countUsersByRoleName(@Param("roleName") String roleName);
}
