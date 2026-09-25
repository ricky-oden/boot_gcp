-- Day5学習用Reset。
-- 削除対象: Spring Batch Metadata、daily_stock_summary、stock_history。
-- 保持対象: item_master、warehouse、inventory自体（inventory数量は下記の既知値へ戻す）。

DO $reset_metadata$
BEGIN
    IF to_regclass('public.batch_job_instance') IS NOT NULL THEN
        EXECUTE 'DELETE FROM batch_step_execution_context';
        EXECUTE 'DELETE FROM batch_step_execution';
        EXECUTE 'DELETE FROM batch_job_execution_context';
        EXECUTE 'DELETE FROM batch_job_execution_params';
        EXECUTE 'DELETE FROM batch_job_execution';
        EXECUTE 'DELETE FROM batch_job_instance';
    END IF;
END
$reset_metadata$;

TRUNCATE TABLE daily_stock_summary, stock_history RESTART IDENTITY;

INSERT INTO item_master (item_code, item_name) VALUES
    ('ITEM001', '六角ボルト'),
    ('ITEM002', '六角ナット')
ON CONFLICT (item_code) DO UPDATE SET item_name = EXCLUDED.item_name;

INSERT INTO warehouse (warehouse_id, warehouse_name) VALUES
    (1, '東京倉庫'),
    (2, '大阪倉庫')
ON CONFLICT (warehouse_id) DO UPDATE SET warehouse_name = EXCLUDED.warehouse_name;

INSERT INTO inventory (
    inventory_id, item_code, warehouse_id, quantity, status, updated_at
) VALUES
    (1001, 'ITEM001', 1, 120, 'AVAILABLE', '2026-09-18 00:00:00+00:00'),
    (1002, 'ITEM001', 2,  40, 'AVAILABLE', '2026-09-18 00:00:00+00:00'),
    (1003, 'ITEM002', 1,  75, 'AVAILABLE', '2026-09-18 00:00:00+00:00')
ON CONFLICT (inventory_id) DO UPDATE SET
    item_code = EXCLUDED.item_code,
    warehouse_id = EXCLUDED.warehouse_id,
    quantity = EXCLUDED.quantity,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

INSERT INTO stock_history (
    inventory_id, movement_type, quantity,
    before_quantity, after_quantity, processed_at
) VALUES
    (1001, 'IN',   5, 117, 122, '2026-09-18 01:00:00+00:00'),
    (1001, 'OUT',  2, 122, 120, '2026-09-18 02:00:00+00:00'),
    (1002, 'IN',   7,  40,  47, '2026-09-18 03:00:00+00:00'),
    (1003, 'IN',  10,  75,  85, '2026-09-18 04:00:00+00:00'),
    (1003, 'OUT',  4,  85,  81, '2026-09-18 05:00:00+00:00'),
    (1001, 'IN',  99, 120, 219, '2026-09-17 01:00:00+00:00');

SELECT 'stock_history' AS table_name, COUNT(*) AS row_count FROM stock_history
UNION ALL
SELECT 'daily_stock_summary', COUNT(*) FROM daily_stock_summary;
