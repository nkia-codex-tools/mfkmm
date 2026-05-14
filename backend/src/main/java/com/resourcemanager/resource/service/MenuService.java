package com.resourcemanager.resource.service;

import com.resourcemanager.resource.dto.*;
import com.resourcemanager.resource.entity.FunctionResource;
import com.resourcemanager.resource.entity.MenuResource;
import com.resourcemanager.resource.repository.FunctionRepository;
import com.resourcemanager.resource.repository.MenuRepository;
import com.resourcemanager.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    private final MenuRepository repository;
    private final FunctionRepository functionRepository;

    public MenuService(MenuRepository repository, FunctionRepository functionRepository) {
        this.repository = repository;
        this.functionRepository = functionRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getAll() {
        return repository.findAllByOrderByRowOrderAsc().stream()
                .map(e -> MenuResponse.from(e, resolveFunctionDescription(e.getFunctionId())))
                .toList();
    }

    @Transactional
    public MenuResponse create(MenuRequest request, Long userId) {
        int nextOrder = repository.findMaxRowOrder().orElse(0) + 1;

        MenuResource entity = new MenuResource();
        applyRequest(entity, request);
        entity.setRowOrder(nextOrder);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        MenuResource saved = repository.save(entity);
        return MenuResponse.from(saved, resolveFunctionDescription(saved.getFunctionId()));
    }

    @Transactional
    public MenuResponse update(Long id, MenuRequest request, Long userId) {
        MenuResource entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Menu not found: " + id));

        applyRequest(entity, request);
        entity.setUpdatedBy(userId);

        MenuResource saved = repository.save(entity);
        return MenuResponse.from(saved, resolveFunctionDescription(saved.getFunctionId()));
    }

    @Transactional
    public void delete(Long id, Long userId) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Menu not found: " + id);
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

    private String resolveFunctionDescription(String functionId) {
        if (functionId == null || functionId.isBlank()) return "";
        return functionRepository.findByFunctionId(functionId)
                .map(FunctionResource::getFunctionName)
                .orElse("");
    }

    private void applyRequest(MenuResource entity, MenuRequest req) {
        entity.setMainMenu(req.mainMenu());
        entity.setSubMenuGroup(req.subMenuGroup());
        entity.setSubMenu(req.subMenu());
        entity.setMenuLevel1(req.menuLevel1());
        entity.setMenuLevel2(req.menuLevel2());
        entity.setMenuLevel3(req.menuLevel3());
        entity.setMenuId(req.menuId());
        entity.setIsMenu(req.isMenu() != null ? req.isMenu() : true);
        entity.setIsSystemMenu(req.isSystemMenu() != null ? req.isSystemMenu() : false);
        entity.setFunctionId(req.functionId());
        entity.setMenuIcon(req.menuIcon());
    }

    private void reassignRowOrders() {
        List<MenuResource> all = repository.findAllByOrderByRowOrderAsc();
        for (int i = 0; i < all.size(); i++) {
            all.get(i).setRowOrder(i + 1);
        }
        repository.saveAll(all);
    }
}
