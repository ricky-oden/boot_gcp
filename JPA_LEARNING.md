# JPAをこの教材で読む

## 実案件と教材の線引き

直近案件でSpring Data JPAを使っていたことはFIX済みです。一方、この教材のEntity名、項目数、Table設計、`ddl-auto=update`などは学習用の簡略化です。

## 見る順番

1. `Application.java`: `@Entity`がJava Objectと`applications` Tableを対応付けます。
2. `ApplicationRepository.java`: `JpaRepository<Application, Long>`を継承すると、`findById`や`save`などの実装をSpring Dataが用意します。
3. `ApplicationService.java`: Repositoryを使い、HTTPではなく業務ルールとTransactionを担当します。
4. `ApplicationHistory.java`: `@ManyToOne`と`@JoinColumn`で、履歴がどの申請に属するかを表します。

## JPAとSpring Data JPAとPostgreSQLの違い

- JPA: Java ObjectとRDB Tableの対応付けに関する仕様。
- Hibernate: この教材でJPAを実行する実装。
- Spring Data JPA: Repositoryの定型処理を減らすSpringの仕組み。
- PostgreSQL: 実際にTableとRowを保存するDatabase。

## 承認処理とTransaction

`ApplicationService.approve`は、申請のStatus更新と履歴登録を行います。`@Transactional`により、両方成功または両方rollbackとなるのが重要です。

## Unit TestでDBを使わない理由

`ApplicationApprovalPracticeTest`ではRepositoryをMockitoのMockに差し替えます。これはJPAやPostgreSQL自体をTestするのではなく、`ApplicationService`の「PENDINGをAPPROVEDにし、履歴を保存する」業務ルールを速く確認するためです。JPAからPostgreSQLまで含めた確認はDocker Composeの結合Testで行います。
