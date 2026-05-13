package com.mkfmm.user.adapter.outbound.persistence;

import com.mkfmm.user.adapter.outbound.persistence.document.UserDocument;
import com.mkfmm.user.adapter.outbound.persistence.repository.SpringDataUserRepository;
import com.mkfmm.user.application.port.outbound.UserRepository;
import com.mkfmm.user.domain.model.Role;
import com.mkfmm.user.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MongoUserRepositoryAdapter implements UserRepository {

    private final SpringDataUserRepository springDataRepo;
    private final MongoTemplate mongoTemplate;

    public MongoUserRepositoryAdapter(SpringDataUserRepository springDataRepo, MongoTemplate mongoTemplate) {
        this.springDataRepo = springDataRepo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public User save(User user) {
        UserDocument doc = toDocument(user);
        UserDocument saved = springDataRepo.save(doc);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findById(String id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        return springDataRepo.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return springDataRepo.existsByUserId(userId);
    }

    @Override
    public Page<User> findAll(String userId, String name, String department, String role, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();
        if (userId != null) criteriaList.add(Criteria.where("userId").regex(userId, "i"));
        if (name != null) criteriaList.add(Criteria.where("name").regex(name, "i"));
        if (department != null) criteriaList.add(Criteria.where("department").regex(department, "i"));
        if (role != null) criteriaList.add(Criteria.where("role").is(role));

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        query.with(pageable);

        List<UserDocument> docs = mongoTemplate.find(query, UserDocument.class);
        long count = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), UserDocument.class);

        return PageableExecutionUtils.getPage(
                docs.stream().map(this::toDomain).toList(), pageable, () -> count);
    }

    @Override
    public void deleteById(String id) {
        springDataRepo.deleteById(id);
    }

    private UserDocument toDocument(User user) {
        UserDocument doc = new UserDocument();
        doc.setId(user.getId());
        doc.setUserId(user.getUserId());
        doc.setName(user.getName());
        doc.setEmail(user.getEmail());
        doc.setDepartment(user.getDepartment());
        doc.setRole(user.getRole().name());
        doc.setMemo(user.getMemo());
        doc.setRootAdmin(user.isRootAdmin());
        doc.setLocked(user.isLocked());
        doc.setMustChangePassword(user.isMustChangePassword());
        doc.setCreatedAt(user.getCreatedAt());
        doc.setCreatedBy(user.getCreatedBy());
        doc.setUpdatedAt(user.getUpdatedAt());
        doc.setUpdatedBy(user.getUpdatedBy());
        return doc;
    }

    private User toDomain(UserDocument doc) {
        User user = new User(doc.getUserId(), doc.getName(), doc.getEmail(),
                doc.getDepartment(), Role.valueOf(doc.getRole()), doc.getMemo(), doc.getCreatedBy());
        user.setId(doc.getId());
        return user;
    }
}
