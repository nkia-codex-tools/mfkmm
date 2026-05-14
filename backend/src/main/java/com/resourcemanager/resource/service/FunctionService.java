package com.resourcemanager.resource.service;

import com.resourcemanager.resource.dto.*;
import com.resourcemanager.resource.entity.FunctionResource;
import com.resourcemanager.resource.repository.FunctionRepository;
import com.resourcemanager.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FunctionService {

    private final FunctionRepository repository;

    public FunctionService(FunctionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<FunctionResponse> getAll() {
        return repository.findAllByOrderByRowOrderAsc().stream()
                .map(FunctionResponse::from)
                .toList();
    }

    @Transactional
    public FunctionResponse create(FunctionRequest request, Long userId) {
        int nextOrder = repository.findMaxRowOrder().orElse(0) + 1;

        FunctionResource entity = new FunctionResource();
        applyRequest(entity, request);
        entity.setRowOrder(nextOrder);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        return FunctionResponse.from(repository.save(entity));
    }

    @Transactional
    public FunctionResponse update(Long id, FunctionRequest request, Long userId) {
        FunctionResource entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Function not found: " + id));

        applyRequest(entity, request);
        entity.setUpdatedBy(userId);

        return FunctionResponse.from(repository.save(entity));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Function not found: " + id);
        }
        repository.deleteById(id);
        reassignRowOrders();
    }

    @Transactional
    public void deleteBatch(List<Long> ids, Long userId) {
        repository.deleteAllByIdInBatch(ids);
        reassignRowOrders();
    }

    @Transactional
    public void reorder(ReorderRequest request, Long userId) {
        List<Long> orderedIds = request.orderedIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            repository.updateRowOrder(orderedIds.get(i), i + 1);
        }
    }

    @Transactional(readOnly = true)
    public List<String> searchFunctionIds(String query) {
        return repository.findByFunctionIdContainingIgnoreCase(query).stream()
                .map(FunctionResource::getFunctionId)
                .limit(20)
                .toList();
    }

    private void applyRequest(FunctionResource entity, FunctionRequest req) {
        entity.setAClass(req.aClass());
        entity.setBClass(req.bClass());
        entity.setCClass(req.cClass());
        entity.setAction(req.action());
        entity.setFunctionName(req.functionName());
        entity.setFunctionId(req.functionId() != null ? req.functionId() : "");
        entity.setType(req.type());
        entity.setLight(req.light() != null ? req.light() : false);
        entity.setStandard(req.standard() != null ? req.standard() : false);
        entity.setEnterprise(req.enterprise() != null ? req.enterprise() : false);
        entity.setSystemMenu(req.systemMenu() != null ? req.systemMenu() : false);
        entity.setProductDomain(req.productDomain());
        entity.setDomainLicenseResourceType(req.domainLicenseResourceType());
        entity.setRelatedServices(req.relatedServices());
    }

    private void reassignRowOrders() {
        List<FunctionResource> all = repository.findAllByOrderByRowOrderAsc();
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setRowOrder(i + 1);
        }
        repository.saveAll(all);
    }
}
