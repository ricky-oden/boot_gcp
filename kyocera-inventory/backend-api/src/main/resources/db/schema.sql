CREATE TABLE IF NOT EXISTS item_master (
    item_code VARCHAR(30) PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS warehouse (
    warehouse_id BIGINT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory (
    inventory_id BIGINT PRIMARY KEY,
    item_code VARCHAR(30) NOT NULL REFERENCES item_master(item_code),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(warehouse_id),
    quantity INTEGER NOT NULL CHECK (quantity >= 0),
    status VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_inventory_item_warehouse UNIQUE (item_code, warehouse_id)
);
