package com.resourcemanager.resource.repository;

import com.resourcemanager.resource.entity.MenuResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuResource, Long> {

    List<MenuResource> findAllByOrderByRowOrderAsc();

    @Query("SELECT MAX(m.rowOrder) FROM MenuResource m")
    Optional<Integer> findMaxRowOrder();

    @Modifying
    @Query("UPDATE MenuResource m SET m.rowOrder = :newOrder WHERE m.id = :id")
    void updateRowOrder(Long id, int newOrder);
}
