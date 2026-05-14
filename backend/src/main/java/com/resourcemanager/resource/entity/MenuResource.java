package com.resourcemanager.resource.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "menus")
public class MenuResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "main_menu") private String mainMenu;
    @Column(name = "sub_menu_group") private String subMenuGroup;
    @Column(name = "sub_menu") private String subMenu;
    @Column(name = "menu_level1") private String menuLevel1;
    @Column(name = "menu_level2") private String menuLevel2;
    @Column(name = "menu_level3") private String menuLevel3;
    @Column(name = "menu_id") private String menuId;
    @Column(name = "is_menu", nullable = false) private Boolean isMenu = true;
    @Column(name = "is_system_menu", nullable = false) private Boolean isSystemMenu = false;
    @Column(name = "function_id") private String functionId;
    @Column(name = "menu_icon") private String menuIcon;
    @Column(name = "row_order", nullable = false) private Integer rowOrder;
    @Column(name = "created_by", nullable = false) private Long createdBy;
    @Column(name = "updated_by", nullable = false) private Long updatedBy;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); updatedAt = Instant.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMainMenu() { return mainMenu; }
    public void setMainMenu(String mainMenu) { this.mainMenu = mainMenu; }
    public String getSubMenuGroup() { return subMenuGroup; }
    public void setSubMenuGroup(String subMenuGroup) { this.subMenuGroup = subMenuGroup; }
    public String getSubMenu() { return subMenu; }
    public void setSubMenu(String subMenu) { this.subMenu = subMenu; }
    public String getMenuLevel1() { return menuLevel1; }
    public void setMenuLevel1(String menuLevel1) { this.menuLevel1 = menuLevel1; }
    public String getMenuLevel2() { return menuLevel2; }
    public void setMenuLevel2(String menuLevel2) { this.menuLevel2 = menuLevel2; }
    public String getMenuLevel3() { return menuLevel3; }
    public void setMenuLevel3(String menuLevel3) { this.menuLevel3 = menuLevel3; }
    public String getMenuId() { return menuId; }
    public void setMenuId(String menuId) { this.menuId = menuId; }
    public Boolean getIsMenu() { return isMenu; }
    public void setIsMenu(Boolean isMenu) { this.isMenu = isMenu; }
    public Boolean getIsSystemMenu() { return isSystemMenu; }
    public void setIsSystemMenu(Boolean isSystemMenu) { this.isSystemMenu = isSystemMenu; }
    public String getFunctionId() { return functionId; }
    public void setFunctionId(String functionId) { this.functionId = functionId; }
    public String getMenuIcon() { return menuIcon; }
    public void setMenuIcon(String menuIcon) { this.menuIcon = menuIcon; }
    public Integer getRowOrder() { return rowOrder; }
    public void setRowOrder(Integer rowOrder) { this.rowOrder = rowOrder; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
