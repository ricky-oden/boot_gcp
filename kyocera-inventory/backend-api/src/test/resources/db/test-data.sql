INSERT INTO item_master (item_code, item_name) VALUES
    ('ITEM001', '六角ボルト'),
    ('ITEM002', '六角ナット');

INSERT INTO warehouse (warehouse_id, warehouse_name) VALUES
    (1, '東京倉庫'),
    (2, '大阪倉庫');

INSERT INTO inventory (
    inventory_id, item_code, warehouse_id, quantity, status, updated_at
) VALUES
    (1001, 'ITEM001', 1, 120, 'AVAILABLE', '2026-09-15 09:00:00+09:00'),
    (1002, 'ITEM001', 2, 40, 'RESERVED', '2026-09-15 09:05:00+09:00'),
    (1003, 'ITEM002', 1, 75, 'AVAILABLE', '2026-09-15 09:10:00+09:00');
