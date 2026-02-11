package com.noom.interview.fullstack.sleep.repository;

import com.noom.interview.fullstack.sleep.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUserEntity, UUID> {
}
