package com.example.kyocera.inventory.batch.job;

import com.example.kyocera.inventory.batch.domain.DailyStockSummary;
import com.example.kyocera.inventory.batch.domain.StockHistoryAggregate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Configuration
@EnableBatchProcessing
public class DailyStockSummaryJobConfiguration {

    public static final String JOB_NAME = "dailyStockSummaryJob";
    public static final String STEP_NAME = "dailyStockSummaryStep";
    public static final int CHUNK_SIZE = 2;

    @Bean
    public Job dailyStockSummaryJob(
            JobBuilderFactory jobBuilderFactory,
            Step dailyStockSummaryStep,
            JobParametersValidator jobParametersValidator) {
        return jobBuilderFactory.get(JOB_NAME)
                .validator(jobParametersValidator)
                .start(dailyStockSummaryStep)
                .build();
    }

    @Bean
    public Step dailyStockSummaryStep(
            StepBuilderFactory stepBuilderFactory,
            ItemReader<StockHistoryAggregate> stockHistoryAggregateReader,
            ItemProcessor<StockHistoryAggregate, DailyStockSummary> dailyStockSummaryProcessor,
            ItemWriter<DailyStockSummary> dailyStockSummaryWriter) {
        return stepBuilderFactory.get(STEP_NAME)
                .<StockHistoryAggregate, DailyStockSummary>chunk(CHUNK_SIZE)
                .reader(stockHistoryAggregateReader)
                .processor(dailyStockSummaryProcessor)
                .writer(dailyStockSummaryWriter)
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<StockHistoryAggregate> stockHistoryAggregateReader(
            DataSource dataSource,
            @Value("#{jobParameters['businessDate']}") String businessDateValue,
            @Value("#{jobParameters['warehouseId']}") String warehouseIdValue) {
        LocalDate businessDate = LocalDate.parse(businessDateValue);
        Long warehouseId = warehouseIdValue != null
                ? Long.parseLong(warehouseIdValue)
                : null;
        Timestamp start = Timestamp.from(businessDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        Timestamp end = Timestamp.from(businessDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));

        String sql = "SELECT i.item_code, i.warehouse_id, "
                + "SUM(CASE WHEN sh.movement_type = 'IN' THEN sh.quantity ELSE 0 END) "
                + "AS total_in_quantity, "
                + "SUM(CASE WHEN sh.movement_type = 'OUT' THEN sh.quantity ELSE 0 END) "
                + "AS total_out_quantity, "
                + "COUNT(*) AS movement_count "
                + "FROM stock_history sh "
                + "INNER JOIN inventory i ON i.inventory_id = sh.inventory_id "
                + "WHERE sh.processed_at >= ? AND sh.processed_at < ? ";

        if (warehouseId != null) {
            sql += "AND i.warehouse_id = ? ";
        }

        sql += "GROUP BY i.item_code, i.warehouse_id "
                + "ORDER BY i.item_code, i.warehouse_id";

        return new JdbcCursorItemReaderBuilder<StockHistoryAggregate>()
                .name("stockHistoryAggregateReader")
                .dataSource(dataSource)
                .sql(sql)
                .rowMapper((resultSet, rowNumber) -> {
                    StockHistoryAggregate row = new StockHistoryAggregate();
                    row.setItemCode(resultSet.getString("item_code"));
                    row.setWarehouseId(resultSet.getLong("warehouse_id"));
                    row.setTotalInQuantity(resultSet.getInt("total_in_quantity"));
                    row.setTotalOutQuantity(resultSet.getInt("total_out_quantity"));
                    row.setMovementCount(resultSet.getInt("movement_count"));
                    return row;
                })
                .preparedStatementSetter((preparedStatement) -> {
                    preparedStatement.setTimestamp(1, start);
                    preparedStatement.setTimestamp(2, end);
                    if (warehouseId != null) {
                        preparedStatement.setLong(3, warehouseId);
                    }
                })
                .saveState(true)
                .build();
    }

    @Bean
    @StepScope
    public DailyStockSummaryProcessor dailyStockSummaryProcessor(
            @Value("#{jobParameters['businessDate']}") String businessDateValue,
            @Value("#{jobParameters['failOnItemCode']}") String failOnItemCode) {
        return new DailyStockSummaryProcessor(
                LocalDate.parse(businessDateValue),
                failOnItemCode);
    }

    @Bean
    public ItemWriter<DailyStockSummary> dailyStockSummaryWriter(JdbcTemplate jdbcTemplate) {
        return new DailyStockSummaryWriter(jdbcTemplate);
    }

    @Bean
    public JobParametersValidator jobParametersValidator() {
        return parameters -> {
            JobParameter businessDate = parameters.getParameters().get("businessDate");
            JobParameter warehouseId = parameters.getParameters().get("warehouseId");
            if (businessDate == null || businessDate.getValue() == null) {
                throw new JobParametersInvalidException("businessDateは必須です。例: 2026-09-18");
            }
            try {
                LocalDate.parse(businessDate.getValue().toString());
            } catch (RuntimeException exception) {
                throw new JobParametersInvalidException(
                        "businessDateはyyyy-MM-dd形式で指定してください。");
            }
            if (warehouseId != null && warehouseId.getValue() != null) {

                try {
                    Long warehouseIdValue = Long.parseLong(warehouseId.getValue().toString());
                    if (warehouseIdValue <= 0) {
                        throw new JobParametersInvalidException(
                                "warehouseIdは正の数で指定してください。");
                    }
                } catch (NumberFormatException exception) {
                    throw new JobParametersInvalidException(
                            "warehouseIdは数値で指定してください。");
                }
            }
        };
    }
}
