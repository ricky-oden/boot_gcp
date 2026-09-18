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
            JobParametersValidator businessDateValidator
    ) {
        return jobBuilderFactory.get(JOB_NAME)
                .validator(businessDateValidator)
                .start(dailyStockSummaryStep)
                .build();
    }

    @Bean
    public Step dailyStockSummaryStep(
            StepBuilderFactory stepBuilderFactory,
            ItemReader<StockHistoryAggregate> stockHistoryAggregateReader,
            ItemProcessor<StockHistoryAggregate, DailyStockSummary> dailyStockSummaryProcessor,
            ItemWriter<DailyStockSummary> dailyStockSummaryWriter
    ) {
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
            @Value("#{jobParameters['businessDate']}") String businessDateValue
    ) {
        LocalDate businessDate = LocalDate.parse(businessDateValue);
        Timestamp start = Timestamp.from(businessDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        Timestamp end = Timestamp.from(businessDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));

        return new JdbcCursorItemReaderBuilder<StockHistoryAggregate>()
                .name("stockHistoryAggregateReader")
                .dataSource(dataSource)
                .sql("SELECT i.item_code, i.warehouse_id, "
                        + "SUM(CASE WHEN sh.movement_type = 'IN' THEN sh.quantity ELSE 0 END) "
                        + "AS total_in_quantity, "
                        + "SUM(CASE WHEN sh.movement_type = 'OUT' THEN sh.quantity ELSE 0 END) "
                        + "AS total_out_quantity, "
                        + "COUNT(*) AS movement_count "
                        + "FROM stock_history sh "
                        + "INNER JOIN inventory i ON i.inventory_id = sh.inventory_id "
                        + "WHERE sh.processed_at >= ? AND sh.processed_at < ? "
                        + "GROUP BY i.item_code, i.warehouse_id "
                        + "ORDER BY i.item_code, i.warehouse_id")
                .preparedStatementSetter(statement -> {
                    statement.setTimestamp(1, start);
                    statement.setTimestamp(2, end);
                })
                .rowMapper((resultSet, rowNumber) -> {
                    StockHistoryAggregate row = new StockHistoryAggregate();
                    row.setItemCode(resultSet.getString("item_code"));
                    row.setWarehouseId(resultSet.getLong("warehouse_id"));
                    row.setTotalInQuantity(resultSet.getInt("total_in_quantity"));
                    row.setTotalOutQuantity(resultSet.getInt("total_out_quantity"));
                    row.setMovementCount(resultSet.getInt("movement_count"));
                    return row;
                })
                .saveState(true)
                .build();
    }

    @Bean
    @StepScope
    public DailyStockSummaryProcessor dailyStockSummaryProcessor(
            @Value("#{jobParameters['businessDate']}") String businessDateValue,
            @Value("#{jobParameters['failOnItemCode']}") String failOnItemCode
    ) {
        return new DailyStockSummaryProcessor(
                LocalDate.parse(businessDateValue),
                failOnItemCode
        );
    }

    @Bean
    public ItemWriter<DailyStockSummary> dailyStockSummaryWriter(JdbcTemplate jdbcTemplate) {
        return new DailyStockSummaryWriter(jdbcTemplate);
    }

    @Bean
    public JobParametersValidator businessDateValidator() {
        return parameters -> {
            JobParameter parameter = parameters.getParameters().get("businessDate");
            if (parameter == null || parameter.getValue() == null) {
                throw new JobParametersInvalidException("businessDateは必須です。例: 2026-09-18");
            }
            try {
                LocalDate.parse(parameter.getValue().toString());
            } catch (RuntimeException exception) {
                throw new JobParametersInvalidException(
                        "businessDateはyyyy-MM-dd形式で指定してください。");
            }
        };
    }
}
