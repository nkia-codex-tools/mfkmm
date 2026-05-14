package com.resourcemanager.resource.repository;

import com.resourcemanager.resource.entity.MessageResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageResourceRepository extends JpaRepository<MessageResource, Long> {

    List<MessageResource> findAllByOrderByRowOrderAsc();

    @Query("SELECT MAX(m.rowOrder) FROM MessageResource m")
    Optional<Integer> findMaxRowOrder();

    @Modifying
    @Query("UPDATE MessageResource m SET m.rowOrder = :newOrder WHERE m.id = :id")
    void updateRowOrder(Long id, int newOrder);
}
