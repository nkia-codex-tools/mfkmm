package com.mkfmm.history.adapter.outbound.persistence;

import com.mkfmm.history.adapter.outbound.persistence.document.WorkLogDocument;
import com.mkfmm.history.adapter.outbound.persistence.repository.SpringDataWorkLogRepository;
import com.mkfmm.history.application.port.outbound.WorkLogRepository;
import com.mkfmm.history.domain.model.WorkLog;
import com.mkfmm.history.domain.model.WorkLogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class MongoWorkLogRepositoryAdapter implements WorkLogRepository {

    private final SpringDataWorkLogRepository springDataRepo;
    private final MongoTemplate mongoTemplate;

    public MongoWorkLogRepositoryAdapter(SpringDataWorkLogRepository springDataRepo, MongoTemplate mongoTemplate) {
        this.springDataRepo = springDataRepo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public WorkLog save(WorkLog workLog) {
        WorkLogDocument doc = toDocument(workLog);
        WorkLogDocument saved = springDataRepo.save(doc);
        workLog.setId(saved.getId());
        return workLog;
    }

    @Override
    public boolean existsBySourceEvent(String sourceEvent) {
        return springDataRepo.existsBySourceEvent(sourceEvent);
    }

    @Override
    public Page<WorkLog> findAll(String workLogType, String userId, String resourceKey,
                                  Instant startDate, Instant endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("markedForDeletion").is(false));
        if (workLogType != null) criteriaList.add(Criteria.where("workLogType").is(workLogType));
        if (userId != null) criteriaList.add(Criteria.where("userId").is(userId));
        if (resourceKey != null) criteriaList.add(Criteria.where("resourceKey").regex(resourceKey, "i"));
        if (startDate != null) criteriaList.add(Criteria.where("performedAt").gte(startDate));
        if (endDate != null) criteriaList.add(Criteria.where("performedAt").lte(endDate));

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        query.with(pageable);

        List<WorkLogDocument> docs = mongoTemplate.find(query, WorkLogDocument.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), WorkLogDocument.class);

        return PageableExecutionUtils.getPage(
                docs.stream().map(this::toDomain).toList(), pageable, () -> count);
    }

    @Override
    public Page<WorkLog> findByUserId(String userId, String workLogType,
                                       Instant startDate, Instant endDate, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        criteriaList.add(Criteria.where("userId").is(userId));
        criteriaList.add(Criteria.where("markedForDeletion").is(false));
        if (workLogType != null) criteriaList.add(Criteria.where("workLogType").is(workLogType));
        if (startDate != null) criteriaList.add(Criteria.where("performedAt").gte(startDate));
        if (endDate != null) criteriaList.add(Criteria.where("performedAt").lte(endDate));

        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        query.with(pageable);

        List<WorkLogDocument> docs = mongoTemplate.find(query, WorkLogDocument.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), WorkLogDocument.class);

        return PageableExecutionUtils.getPage(
                docs.stream().map(this::toDomain).toList(), pageable, () -> count);
    }

    @Override
    public long markForDeletionByUserId(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        Update update = new Update().set("markedForDeletion", true);
        return mongoTemplate.updateMulti(query, update, WorkLogDocument.class).getModifiedCount();
    }

    @Override
    public long deleteMarkedForDeletion() {
        return springDataRepo.deleteByMarkedForDeletionTrue();
    }

    @Override
    public long deleteSearchLogsOlderThan(Instant cutoff) {
        return springDataRepo.deleteByWorkLogTypeAndPerformedAtBefore("SEARCH", cutoff);
    }

    private WorkLogDocument toDocument(WorkLog w) {
        WorkLogDocument doc = new WorkLogDocument();
        doc.setId(w.getId());
        doc.setWorkLogType(w.getWorkLogType().name());
        doc.setUserId(w.getUserId());
        doc.setResourceId(w.getResourceId());
        doc.setResourceKey(w.getResourceKey());
        doc.setPreviousValue(w.getPreviousValue());
        doc.setNewValue(w.getNewValue());
        doc.setDetails(w.getDetails());
        doc.setSuccess(w.isSuccess());
        doc.setFailureReason(w.getFailureReason());
        doc.setPerformedAt(w.getPerformedAt());
        doc.setSourceEvent(w.getSourceEvent());
        doc.setMarkedForDeletion(w.isMarkedForDeletion());
        return doc;
    }

    private WorkLog toDomain(WorkLogDocument doc) {
        WorkLog w = new WorkLog(WorkLogType.valueOf(doc.getWorkLogType()),
                doc.getUserId(), doc.getPerformedAt(), doc.getSourceEvent());
        w.setId(doc.getId());
        w.setResourceId(doc.getResourceId());
        w.setResourceKey(doc.getResourceKey());
        w.setPreviousValue(doc.getPreviousValue());
        w.setNewValue(doc.getNewValue());
        w.setDetails(doc.getDetails());
        w.setMarkedForDeletion(doc.isMarkedForDeletion());
        return w;
    }
}
