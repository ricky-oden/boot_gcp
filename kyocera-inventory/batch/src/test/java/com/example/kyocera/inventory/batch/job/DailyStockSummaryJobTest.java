package com.example.kyocera.inventory.batch.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "classpath:db/batch-test-reset.sql")
class DailyStockSummaryJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void summarizesOnlySpecifiedBusinessDateIncludingInAndOut() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(parameters(null, null));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<Map<String, Object>> summaries = jdbcTemplate.queryForList(
                "SELECT item_code, warehouse_id, total_in_quantity, "
                        + "total_out_quantity, movement_count "
                        + "FROM daily_stock_summary ORDER BY item_code, warehouse_id"
        );

        assertThat(summaries).hasSize(3);
        assertThat(summaries.get(0))
                .containsEntry("ITEM_CODE", "ITEM001")
                .containsEntry("TOTAL_IN_QUANTITY", 5)
                .containsEntry("TOTAL_OUT_QUANTITY", 2)
                .containsEntry("MOVEMENT_COUNT", 2);
        assertThat(summaries.get(1))
                .containsEntry("ITEM_CODE", "ITEM001")
                .containsEntry("WAREHOUSE_ID", 2L)
                .containsEntry("TOTAL_IN_QUANTITY", 7);
        assertThat(summaries.get(2))
                .containsEntry("ITEM_CODE", "ITEM002")
                .containsEntry("TOTAL_IN_QUANTITY", 10)
                .containsEntry("TOTAL_OUT_QUANTITY", 4);

        Integer includedQuantity = jdbcTemplate.queryForObject(
                "SELECT total_in_quantity FROM daily_stock_summary "
                        + "WHERE business_date = DATE '2026-09-18' "
                        + "AND item_code = 'ITEM001' AND warehouse_id = 1",
                Integer.class
        );
        assertThat(includedQuantity).isEqualTo(5);
    }

    @Test
    void summarizesOnlySpecifiedWarehouseId() throws Exception {
        JobExecution execution = jobLauncherTestUtils.launchJob(parameters(null, 1L));

        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        List<Map<String, Object>> summaries = jdbcTemplate.queryForList(
                "SELECT item_code, warehouse_id, total_in_quantity, "
                        + "total_out_quantity, movement_count "
                        + "FROM daily_stock_summary ORDER BY item_code, warehouse_id"
        );

        assertThat(summaries).hasSize(2);
        assertThat(summaries.get(0))
                .containsEntry("ITEM_CODE", "ITEM001")
                .containsEntry("WAREHOUSE_ID", 1L)
                .containsEntry("TOTAL_IN_QUANTITY", 5)
                .containsEntry("TOTAL_OUT_QUANTITY", 2)
                .containsEntry("MOVEMENT_COUNT", 2);

        assertThat(summaries.get(1))
                .containsEntry("ITEM_CODE", "ITEM002")
                .containsEntry("WAREHOUSE_ID", 1L)
                .containsEntry("TOTAL_IN_QUANTITY", 10)
                .containsEntry("TOTAL_OUT_QUANTITY", 4)
                .containsEntry("MOVEMENT_COUNT", 2);
        assertThat(summaries)
                .allSatisfy(summary ->
                        assertThat(summary)
                                .containsEntry("WAREHOUSE_ID", 1L));
    }


    @Test
    void restartsFailedJobFromCheckpointWithoutDoubleCounting() throws Exception {
        JobExecution failed = jobLauncherTestUtils.launchJob(parameters("ITEM002", null));

        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(failed.getJobInstance().getJobName())
                .isEqualTo(DailyStockSummaryJobConfiguration.JOB_NAME);
        assertThat(summaryCount()).isEqualTo(2);

        StepExecution failedStep = failed.getStepExecutions().iterator().next();
        assertThat(failedStep.getWriteCount()).isEqualTo(2);
        assertThat(failedStep.getCommitCount()).isEqualTo(1);
        assertThat(failedStep.getRollbackCount()).isGreaterThanOrEqualTo(1);

        JobExecution restarted = jobLauncherTestUtils.launchJob(parameters("NONE", null));

        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(restarted.getJobInstance().getInstanceId())
                .isEqualTo(failed.getJobInstance().getInstanceId());
        assertThat(restarted.getId()).isNotEqualTo(failed.getId());
        assertThat(summaryCount()).isEqualTo(3);

        Integer itemOneTokyoIn = jdbcTemplate.queryForObject(
                "SELECT total_in_quantity FROM daily_stock_summary "
                        + "WHERE business_date = DATE '2026-09-18' "
                        + "AND item_code = 'ITEM001' AND warehouse_id = 1",
                Integer.class
        );
        assertThat(itemOneTokyoIn).isEqualTo(5);
    }

    @Test
    void restartsFailedJobWithWarehouseIdWithoutDoubleCounting() throws Exception {
        JobExecution failed =
            jobLauncherTestUtils.launchJob(parameters("ITEM002", 1L));

        assertThat(failed.getStatus()).isEqualTo(BatchStatus.FAILED);
        assertThat(failed.getJobInstance().getJobName())
            .isEqualTo(DailyStockSummaryJobConfiguration.JOB_NAME);
        assertThat(summaryCount()).isEqualTo(0);

        StepExecution failedStep =
            failed.getStepExecutions().iterator().next();

        assertThat(failedStep.getWriteCount()).isEqualTo(0);
        assertThat(failedStep.getRollbackCount()).isGreaterThanOrEqualTo(1);

        JobExecution restarted =
            jobLauncherTestUtils.launchJob(parameters("NONE", 1L));

        assertThat(restarted.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(restarted.getJobInstance().getInstanceId())
            .isEqualTo(failed.getJobInstance().getInstanceId());

        assertThat(restarted.getId())
            .isNotEqualTo(failed.getId());

        assertThat(summaryCount()).isEqualTo(2);

        Integer itemOneTokyoIn = jdbcTemplate.queryForObject(
            "SELECT total_in_quantity FROM daily_stock_summary "
                    + "WHERE business_date = DATE '2026-09-18' "
                    + "AND item_code = 'ITEM001' AND warehouse_id = 1",
            Integer.class
        );

        assertThat(itemOneTokyoIn).isEqualTo(5);
}

    private JobParameters parameters(String failOnItemCode, Long warehouseId) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("businessDate", "2026-09-18", true);
        if (failOnItemCode != null) {
            builder.addString("failOnItemCode", failOnItemCode, false);
        }
        if (warehouseId != null) {
            builder.addLong("warehouseId", warehouseId, true);
        }
        return builder.toJobParameters();
    }

    private int summaryCount() {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM daily_stock_summary", Integer.class);
    }
}
