# GKE / kubectl Practice

`backend-api.yaml`はApplication Developer向けの学習用manifestです。実案件のmanifestではなく、実ClusterへそのままDeployするものでもありません。Image、DB接続先、Secretはplaceholderです。

## 関係

- `Deployment`: PodのTemplateと希望Replica数を管理する。
- `Pod`: Containerが実際に動く最小単位。
- `Container`: Artifact Registry等から取得したImageを実行する。
- `Service`: Labelに一致するPodへ安定した宛先を提供する。
- `ConfigMap`: 非機密の環境変数を渡す。
- `Secret`: Password等の機密値を渡す。値をGitへCommitしない。

## Local validation

Cluster接続済みの場合だけ、Server-sideへ送らず構文を確認します。

```bash
kubectl apply --dry-run=client -f kyocera-inventory/k8s/backend-api.yaml
```

## Application Developerが最初に使うCommand

```bash
kubectl get deployments
kubectl get pods
kubectl get services
kubectl describe pod <pod-name>
kubectl logs <pod-name> -c backend-api
kubectl logs -f <pod-name> -c backend-api
kubectl logs <pod-name> -c backend-api --previous
```

CrashLoopBackOffなどでは、まず`get`で状態と再起動回数、次に`describe`でEvent・Probe・Image・Environment、最後に`logs`と`logs --previous`でApplicationの例外を確認します。

このmanifestを実Clusterへ適用する操作、新しいGKE Cluster作成、IAM・VPC設計はDay6の範囲外です。
