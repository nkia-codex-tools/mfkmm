package com.resourcemanager.resource.dto;

import com.resourcemanager.resource.entity.MenuResource;

public record MenuResponse(
        Long id,
        String mainMenu, String subMenuGroup, String subMenu,
        String menuLevel1, String menuLevel2, String menuLevel3,
        String menuId, Boolean isMenu, Boolean isSystemMenu,
        String functionId, String functionDescription, String menuIcon,
        Integer rowOrder
) {
    public static MenuResponse from(MenuResource e, String functionDescription) {
        return new MenuResponse(
                e.getId(), e.getMainMenu(), e.getSubMenuGroup(), e.getSubMenu(),
                e.getMenuLevel1(), e.getMenuLevel2(), e.getMenuLevel3(),
                e.getMenuId(), e.getIsMenu(), e.getIsSystemMenu(),
                e.getFunctionId(), functionDescription, e.getMenuIcon(),
                e.getRowOrder()
        );
    }
}
