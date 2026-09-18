CREATE TABLE IF NOT EXISTS daily_stock_summary (
    business_date DATE NOT NULL,
    item_code VARCHAR(30) NOT NULL REFERENCES item_master(item_code),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(warehouse_id),
    total_in_quantity INTEGER NOT NULL CHECK (total_in_quantity >= 0),
    total_out_quantity INTEGER NOT NULL CHECK (total_out_quantity >= 0),
    movement_count INTEGER NOT NULL CHECK (movement_count >= 0),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_daily_stock_summary
        PRIMARY KEY (business_date, item_code, warehouse_id)
);
