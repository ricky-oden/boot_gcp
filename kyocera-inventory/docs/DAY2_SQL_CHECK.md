# Day2: PostgreSQL確認SQL

以下のTable定義とDataは学習用仮定であり、実案件の実Table定義ではありません。

## psqlへ入る

Repository rootから:

```bash
docker compose -f kyocera-inventory/docker-compose.yml exec kyocera-db \
  psql -U kyocera -d kyocera_inventory
```

## Table一覧と定義

```sql
\dt
\d item_master
\d warehouse
\d inventory
```

## Seed Data

```sql
SELECT * FROM item_master ORDER BY item_code;
SELECT * FROM warehouse ORDER BY warehouse_id;
SELECT * FROM inventory ORDER BY inventory_id;
```

## APIと同じ考え方の在庫検索SQL

```sql
SELECT
    i.inventory_id,
    i.item_code,
    im.item_name,
    i.warehouse_id,
    w.warehouse_name,
    i.quantity,
    i.status
FROM inventory i
INNER JOIN item_master im ON im.item_code = i.item_code
INNER JOIN warehouse w ON w.warehouse_id = i.warehouse_id
WHERE i.item_code = 'ITEM001'
ORDER BY i.inventory_id;
```

DBeaverではHost=`localhost`、Port=`5433`、Database=`kyocera_inventory`、User=`kyocera`を指定します。Passwordはlocal学習用Compose設定を参照してください。
