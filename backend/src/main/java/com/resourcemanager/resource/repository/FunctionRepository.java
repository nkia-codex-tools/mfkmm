package com.resourcemanager.resource.repository;

import com.resourcemanager.resource.entity.FunctionResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionResource, Long> {

    List<FunctionResource> findAllByOrderByRowOrderAsc();

    @Query("SELECT MAX(f.rowOrder) FROM FunctionResource f")
    Optional<Integer> findMaxRowOrder();

    @Modifying
    @Query("UPDATE FunctionResource f SET f.rowOrder = :newOrder WHERE f.id = :id")
    void updateRowOrder(Long id, int newOrder);

    List<FunctionResource> findByFunctionIdContainingIgnoreCase(String query);

    Optional<FunctionResource> findByFunctionId(String functionId);
}
