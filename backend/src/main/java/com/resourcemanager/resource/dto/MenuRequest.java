package com.resourcemanager.resource.dto;

import jakarta.validation.constraints.Size;

public record MenuRequest(
        @Size(max = 255) String mainMenu,
        @Size(max = 255) String subMenuGroup,
        @Size(max = 255) String subMenu,
        @Size(max = 255) String menuLevel1,
        @Size(max = 255) String menuLevel2,
        @Size(max = 255) String menuLevel3,
        @Size(max = 255) String menuId,
        Boolean isMenu,
        Boolean isSystemMenu,
        @Size(max = 255) String functionId,
        @Size(max = 100) String menuIcon
) {}
