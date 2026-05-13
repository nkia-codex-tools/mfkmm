package com.mkfmm.user.adapter.outbound.persistence;

import com.mkfmm.user.adapter.outbound.persistence.document.AuditLogDocument;
import com.mkfmm.user.adapter.outbound.persistence.repository.SpringDataAuditLogRepository;
import com.mkfmm.user.application.port.outbound.AuditLogRepository;
import com.mkfmm.user.domain.model.AuditAction;
import com.mkfmm.user.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class MongoAuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository springDataRepo;
    private final MongoTemplate mongoTemplate;

    public MongoAuditLogRepositoryAdapter(SpringDataAuditLogRepository springDataRepo, MongoTemplate mongoTemplate) {
        this.springDataRepo = springDataRepo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogDocument doc = toDocument(auditLog);
        AuditLogDocument saved = springDataRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public Page<AuditLog> findAll(String action, String targetUserId, String performedBy,
                                   Instant startDate, Instant endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (action != null) criteriaList.add(Criteria.where("action").is(action));
        if (targetUserId != null) criteriaList.add(Criteria.where("targetUserId").is(targetUserId));
        if (performedBy != null) criteriaList.add(Criteria.where("performedBy").is(performedBy));
        if (startDate != null) criteriaList.add(Criteria.where("performedAt").gte(startDate));
        if (endDate != null) criteriaList.add(Criteria.where("performedAt").lte(endDate));

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        query.with(pageable);

        List<AuditLogDocument> docs = mongoTemplate.find(query, AuditLogDocument.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), AuditLogDocument.class);

        return PageableExecutionUtils.getPage(
                docs.stream().map(this::toDomain).toList(), pageable, () -> count);
    }

    @Override
    public long deleteByPerformedAtBefore(Instant cutoff) {
        return springDataRepo.deleteByPerformedAtBefore(cutoff);
    }

    private AuditLogDocument toDocument(AuditLog log) {
        AuditLogDocument doc = new AuditLogDocument();
        doc.setAction(log.getAction().name());
        doc.setTargetUserId(log.getTargetUserId());
        doc.setPerformedBy(log.getPerformedBy());
        doc.setPreviousValue(log.getPreviousValue());
        doc.setNewValue(log.getNewValue());
        doc.setReason(log.getReason());
        doc.setPerformedAt(log.getPerformedAt());
        return doc;
    }

    private AuditLog toDomain(AuditLogDocument doc) {
        AuditLog log = new AuditLog(AuditAction.valueOf(doc.getAction()),
                doc.getTargetUserId(), doc.getPerformedBy(), doc.getPreviousValue(), doc.getNewValue());
        log.setId(doc.getId());
        return log;
    }
}
