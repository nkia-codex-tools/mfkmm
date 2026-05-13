package com.mkfmm.auth.adapter.outbound.persistence;

import com.mkfmm.auth.adapter.outbound.persistence.document.AccountLockEventDocument;
import com.mkfmm.auth.adapter.outbound.persistence.repository.SpringDataAccountLockEventRepository;
import com.mkfmm.auth.application.port.outbound.AccountLockEventRepository;
import com.mkfmm.auth.domain.model.AccountLockEvent;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoAccountLockEventRepositoryAdapter implements AccountLockEventRepository {

    private final SpringDataAccountLockEventRepository springRepo;

    public MongoAccountLockEventRepositoryAdapter(SpringDataAccountLockEventRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public AccountLockEvent save(AccountLockEvent event) {
        AccountLockEventDocument doc = toDocument(event);
        AccountLockEventDocument saved = springRepo.save(doc);
        event.setId(saved.getId());
        return event;
    }

    @Override
    public Optional<AccountLockEvent> findLatestByUserId(String userId) {
        return springRepo.findFirstByUserIdOrderByLockedAtDesc(userId).map(this::toDomain);
    }

    private AccountLockEvent toDomain(AccountLockEventDocument doc) {
        AccountLockEvent event = new AccountLockEvent();
        event.setId(doc.getId());
        event.setUserId(doc.getUserId());
        event.setLockedAt(doc.getLockedAt());
        event.setUnlockedAt(doc.getUnlockedAt());
        event.setUnlockedBy(doc.getUnlockedBy());
        return event;
    }

    private AccountLockEventDocument toDocument(AccountLockEvent event) {
        AccountLockEventDocument doc = new AccountLockEventDocument();
        doc.setId(event.getId());
        doc.setUserId(event.getUserId());
        doc.setLockedAt(event.getLockedAt());
        doc.setUnlockedAt(event.getUnlockedAt());
        doc.setUnlockedBy(event.getUnlockedBy());
        return doc;
    }
}
