package com.resourcemanager.version.repository;

import com.resourcemanager.version.entity.VersionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersionRepository extends JpaRepository<VersionTag, Long> {

    List<VersionTag> findByResourceTypeOrderByCreatedAtDesc(String resourceType);

    List<VersionTag> findAllByOrderByCreatedAtDesc();
}
