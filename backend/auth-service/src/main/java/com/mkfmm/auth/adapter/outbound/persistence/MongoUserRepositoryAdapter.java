package com.mkfmm.auth.adapter.outbound.persistence;

import com.mkfmm.auth.adapter.outbound.persistence.document.UserDocument;
import com.mkfmm.auth.adapter.outbound.persistence.repository.SpringDataUserRepository;
import com.mkfmm.auth.application.port.outbound.UserRepository;
import com.mkfmm.auth.domain.model.Role;
import com.mkfmm.auth.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class MongoUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springRepo;

    public MongoUserRepositoryAdapter(SpringDataUserRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        return springRepo.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserDocument doc = toDocument(user);
        UserDocument saved = springRepo.save(doc);
        user.setId(saved.getId());
        return user;
    }

    @Override
    public boolean existsByRole(Role role) {
        return springRepo.existsByRole(role);
    }

    private User toDomain(UserDocument doc) {
        User user = new User();
        user.setId(doc.getId());
        user.setUserId(doc.getUserId());
        user.setPasswordHash(doc.getPasswordHash());
        user.setRole(doc.getRole());
        user.setLocked(doc.isLocked());
        user.setFailedLoginAttempts(doc.getFailedLoginAttempts());
        user.setRootAdmin(doc.isRootAdmin());
        user.setCreatedAt(doc.getCreatedAt());
        user.setUpdatedAt(doc.getUpdatedAt());
        return user;
    }

    private UserDocument toDocument(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setUserId(user.getUserId());
        doc.setPasswordHash(user.getPasswordHash());
        doc.setRole(user.getRole());
        doc.setLocked(user.isLocked());
        doc.setFailedLoginAttempts(user.getFailedLoginAttempts());
        doc.setRootAdmin(user.isRootAdmin());
        doc.setCreatedAt(user.getCreatedAt());
        doc.setUpdatedAt(user.getUpdatedAt());
        return doc;
    }
}
