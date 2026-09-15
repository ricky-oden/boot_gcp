DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS warehouse;
DROP TABLE IF EXISTS item_master;

CREATE TABLE item_master (
    item_code VARCHAR(30) PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL
);

CREATE TABLE warehouse (
    warehouse_id BIGINT PRIMARY KEY,
    warehouse_name VARCHAR(100) NOT NULL
);

CREATE TABLE inventory (
    inventory_id BIGINT PRIMARY KEY,
    item_code VARCHAR(30) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_test_inventory_item FOREIGN KEY (item_code) REFERENCES item_master(item_code),
    CONSTRAINT fk_test_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(warehouse_id)
);
