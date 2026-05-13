package com.mkfmm.auth.adapter.outbound.persistence;

import com.mkfmm.auth.adapter.outbound.persistence.document.LoginHistoryDocument;
import com.mkfmm.auth.adapter.outbound.persistence.repository.SpringDataLoginHistoryRepository;
import com.mkfmm.auth.application.port.outbound.LoginHistoryRepository;
import com.mkfmm.auth.domain.model.LoginHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MongoLoginHistoryRepositoryAdapter implements LoginHistoryRepository {

    private final SpringDataLoginHistoryRepository springRepo;
    private final MongoTemplate mongoTemplate;

    public MongoLoginHistoryRepositoryAdapter(SpringDataLoginHistoryRepository springRepo, MongoTemplate mongoTemplate) {
        this.springRepo = springRepo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public LoginHistory save(LoginHistory history) {
        LoginHistoryDocument doc = toDocument(history);
        LoginHistoryDocument saved = springRepo.save(doc);
        history.setId(saved.getId());
        return history;
    }

    @Override
    public Page<LoginHistory> findByFilters(String userId, Instant startDate, Instant endDate, Boolean success, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null && !userId.isBlank()) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        if (startDate != null) {
            criteriaList.add(Criteria.where("loginAt").gte(startDate));
        }
        if (endDate != null) {
            criteriaList.add(Criteria.where("loginAt").lte(endDate));
        }
        if (success != null) {
            criteriaList.add(Criteria.where("success").is(success));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, LoginHistoryDocument.class);
        query.with(pageable);
        List<LoginHistoryDocument> docs = mongoTemplate.find(query, LoginHistoryDocument.class);

        List<LoginHistory> histories = docs.stream().map(this::toDomain).toList();
        return PageableExecutionUtils.getPage(histories, pageable, () -> total);
    }

    private LoginHistory toDomain(LoginHistoryDocument doc) {
        LoginHistory h = new LoginHistory();
        h.setId(doc.getId());
        h.setUserId(doc.getUserId());
        h.setLoginAt(doc.getLoginAt());
        h.setIpAddress(doc.getIpAddress());
        h.setSuccess(doc.isSuccess());
        h.setFailureReason(doc.getFailureReason());
        return h;
    }

    private LoginHistoryDocument toDocument(LoginHistory h) {
        LoginHistoryDocument doc = new LoginHistoryDocument();
        doc.setId(h.getId());
        doc.setUserId(h.getUserId());
        doc.setLoginAt(h.getLoginAt());
        doc.setIpAddress(h.getIpAddress());
        doc.setSuccess(h.isSuccess());
        doc.setFailureReason(h.getFailureReason());
        return doc;
    }
}
