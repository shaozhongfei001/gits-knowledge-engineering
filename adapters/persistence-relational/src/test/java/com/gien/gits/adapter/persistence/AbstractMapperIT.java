package com.gien.gits.adapter.persistence;

import javax.sql.DataSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.Driver;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.gien.gits.adapter.persistence.common.handler.AgendaItemListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.CommitmentItemListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.FactReconciliationItemListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.InteractionExtractionListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.KycQuestionItemListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.OpportunitySignalItemListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.ProductDiscussionItemListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.ProductSchemeListTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.StringListJsonTypeHandler;
import com.gien.gits.adapter.persistence.common.handler.TalkingPointListTypeHandler;
import com.gien.gits.adapter.persistence.common.typehandler.InstantTypeHandler;
import com.gien.gits.adapter.persistence.common.typehandler.UuidTypeHandler;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;

/**
 * Base class for Mapper integration tests.
 * Sets up H2 in-memory database with Flyway migrations and MyBatis SqlSessionFactory.
 */
public abstract class AbstractMapperIT {

    private static int dbCounter = 0;

    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;
    protected SqlSessionFactory sqlSessionFactory;

    @BeforeEach
    void setUpDatabase() {
        // Each test gets its own H2 database to avoid cross-test contamination
        String dbName = "mapper_it_" + (++dbCounter) + "_" + System.nanoTime();
        String jdbcUrl = "jdbc:h2:mem:" + dbName + ";MODE=MySQL;DB_CLOSE_DELAY=-1";

        dataSource = new SimpleDriverDataSource(new Driver(), jdbcUrl, "sa", "");
        jdbcTemplate = new JdbcTemplate(dataSource);

        Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/h2")
                .load()
                .migrate();

        sqlSessionFactory = buildSqlSessionFactory();
    }

    private SqlSessionFactory buildSqlSessionFactory() {
        Environment environment = new Environment("test", new JdbcTransactionFactory(), dataSource);
        Configuration configuration = new Configuration(environment);

        // Register TypeHandlers before loading XML mappers
        configuration.getTypeHandlerRegistry().register(InstantTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(UuidTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(AgendaItemListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(CommitmentItemListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(FactReconciliationItemListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(InteractionExtractionListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(KycQuestionItemListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(OpportunitySignalItemListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(ProductDiscussionItemListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(ProductSchemeListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(StringListJsonTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(TalkingPointListTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(java.time.Instant.class, InstantTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(java.util.UUID.class, UuidTypeHandler.class);

        // Register enum type handlers for constructor arg resolution
        org.apache.ibatis.type.TypeHandlerRegistry thr = configuration.getTypeHandlerRegistry();
        registerEnumTypeHandler(thr, com.gien.gits.ontology.OpportunitySignal.SignalType.class);
        registerEnumTypeHandler(thr, com.gien.gits.ontology.OpportunitySignal.SignalSourceType.class);
        registerEnumTypeHandler(thr, com.gien.gits.ontology.OpportunitySignal.SignalStatus.class);
        registerEnumTypeHandler(thr, com.gien.gits.ontology.Commitment.CommitmentType.class);
        registerEnumTypeHandler(thr, com.gien.gits.ontology.Commitment.CommitmentStatus.class);
        registerEnumTypeHandler(thr, com.gien.gits.engagement.OutreachScript.OutreachChannel.class);

        // Load XML mapper files explicitly
        String[] mapperXmls = {
                "mapper/foundation/engagement/MeetingScriptMapper.xml",
                "mapper/foundation/engagement/OutreachScriptMapper.xml",
                "mapper/foundation/engagement/PrevisitReportContentMapper.xml",
                "mapper/foundation/engagement/PostvisitAnalysisContentMapper.xml",
                "mapper/foundation/ontology/OpportunitySignalMapper.xml",
                "mapper/foundation/ontology/CommitmentMapper.xml"
        };

        for (String mapperXml : mapperXmls) {
            InputStream is = getClass().getClassLoader().getResourceAsStream(mapperXml);
            if (is == null) {
                throw new IllegalStateException("Mapper XML not found: " + mapperXml);
            }
            Reader reader = new InputStreamReader(is);
            XMLMapperBuilder builder = new XMLMapperBuilder(reader, configuration, mapperXml, configuration.getSqlFragments());
            builder.parse();
        }

        return new SqlSessionFactoryBuilder().build(configuration);
    }

    /**
     * Inserts a minimal operating_case row to satisfy foreign key constraints.
     */
    protected void insertOperatingCase(String caseId) {
        jdbcTemplate.update(
                "INSERT INTO operating_case (case_id, case_type, status, purpose, valid_from, recorded_at, created_by) " +
                "VALUES (?, 'CLAIM_RECONCILIATION', 'OPEN', 'IT test case', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'it-test')"
                , caseId);
    }

    /**
     * Inserts a minimal customer_journey row to satisfy foreign key constraints.
     */
    protected void insertJourney(String journeyId, String operatingCaseId) {
        jdbcTemplate.update(
                "INSERT INTO customer_journey (journey_id, case_id, customer_id, customer_name, phase, started_at) " +
                "VALUES (?, ?, 'IT-CUST-001', 'IT Test Customer', 'PREVISIT', CURRENT_TIMESTAMP)"
                , journeyId, operatingCaseId);
    }

    private static <E extends Enum<E>> void registerEnumTypeHandler(
            org.apache.ibatis.type.TypeHandlerRegistry thr, Class<E> enumClass) {
        thr.register(enumClass, new org.apache.ibatis.type.EnumTypeHandler<>(enumClass));
    }
}
