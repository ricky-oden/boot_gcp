# Docker Network 学習

## localhostは「今いるContainer自身」

```text
Host browser -- localhost:3000 --> frontend公開port
Host curl    -- localhost:8080 --> backend公開port
backend container -- db:5432 --> PostgreSQL container
frontend build時 API URL -- localhost:8080 --> ブラウザから見たHost公開port
```

`docker-compose.yml`のserviceは既定network内でservice名DNSを持つ。そのためbackendのJDBC URLは`jdbc:postgresql://db:5432/workflow`。backend containerから`localhost:5432`へ接続するとbackend自身を探すので失敗する。

| 見る場所 | その場所の`localhost` | この構成での到達先 |
|---|---|---|
| Mac Host | Mac自身 | frontend `:3000`、backend `:8080`、DB `:5432` |
| DevContainer | DevContainer自身 | Docker CLIは外側daemonへsocket接続。Compose serviceへは公開port経由なら`host.docker.internal`を使う |
| Frontend Container | Nginx container自身 | 静的fileを`:80`で配信。Browserの`localhost:8080`はMac側backend公開port |
| Backend Container | Spring container自身 | DBは`localhost`でなく`db:5432` |
| PostgreSQL Container | PostgreSQL container自身 | PostgreSQLが`:5432`でlisten |

```text
Mac Browser -- localhost:3000 --> frontend:80
Mac/Browser -- localhost:8080 --> backend:8080
backend     -- db:5432 --------> PostgreSQL:5432
DevContainer Docker CLI -- mounted socket --> Mac側 Docker Engine
```

## 切り分け

```bash
docker compose ps
docker compose logs backend
docker compose exec backend getent hosts db
curl -i http://localhost:8080/applications
```

`ports`はHostとの入口、`expose`/container portはnetwork内通信、と役割を分けて考える。DevContainerのdocker-outside-of-dockerではCLIはDevContainer内、Docker daemonとCompose container群は外側にあり、bind mount pathにも注意する。
