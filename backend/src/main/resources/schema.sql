CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'PENDING',
    status TEXT NOT NULL DEFAULT 'PENDING',
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS functions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    a_class TEXT,
    b_class TEXT,
    c_class TEXT,
    action TEXT,
    function_name TEXT,
    function_id TEXT NOT NULL,
    type TEXT,
    light INTEGER NOT NULL DEFAULT 0,
    standard INTEGER NOT NULL DEFAULT 0,
    enterprise INTEGER NOT NULL DEFAULT 0,
    system_menu INTEGER NOT NULL DEFAULT 0,
    product_domain TEXT,
    domain_license_resource_type TEXT,
    related_services TEXT,
    row_order INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    updated_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_functions_row_order ON functions(row_order);
CREATE INDEX IF NOT EXISTS idx_functions_function_id ON functions(function_id);

CREATE TABLE IF NOT EXISTS menus (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    main_menu TEXT,
    sub_menu_group TEXT,
    sub_menu TEXT,
    menu_level1 TEXT,
    menu_level2 TEXT,
    menu_level3 TEXT,
    menu_id TEXT,
    is_menu INTEGER NOT NULL DEFAULT 1,
    is_system_menu INTEGER NOT NULL DEFAULT 0,
    function_id TEXT,
    menu_icon TEXT,
    row_order INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    updated_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_menus_row_order ON menus(row_order);
CREATE INDEX IF NOT EXISTS idx_menus_function_id ON menus(function_id);

CREATE TABLE IF NOT EXISTS message_resources (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    module TEXT,
    resource_key TEXT,
    korean TEXT,
    english TEXT,
    japanese TEXT,
    description TEXT,
    registered_date TEXT,
    registered_by TEXT,
    row_order INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    updated_by INTEGER NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_message_resources_row_order ON message_resources(row_order);
CREATE INDEX IF NOT EXISTS idx_message_resources_resource_key ON message_resources(resource_key);

CREATE TABLE IF NOT EXISTS change_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    resource_type TEXT NOT NULL,
    resource_id INTEGER NOT NULL,
    change_type TEXT NOT NULL,
    field_name TEXT,
    old_value TEXT,
    new_value TEXT,
    changed_by INTEGER NOT NULL,
    changed_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_change_history_changed_at ON change_history(changed_at);
CREATE INDEX IF NOT EXISTS idx_change_history_resource_type ON change_history(resource_type);
CREATE INDEX IF NOT EXISTS idx_change_history_changed_by ON change_history(changed_by);

CREATE TABLE IF NOT EXISTS version_tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    tag_name TEXT NOT NULL,
    description TEXT,
    resource_type TEXT NOT NULL,
    snapshot_data TEXT NOT NULL,
    created_by INTEGER NOT NULL,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_version_tags_resource_type ON version_tags(resource_type);
CREATE INDEX IF NOT EXISTS idx_version_tags_created_at ON version_tags(created_at);
