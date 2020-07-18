package com.contactservice.neo4j.service;


import com.contactservice.neo4j.domain.Contact;
import com.contactservice.neo4j.repository.Neo4jContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Neo4jContactService {

    @Autowired
    private Neo4jContactRepository neo4jContactRepository;

    public String addContact(Contact contact) {
        return neo4jContactRepository.addContact(contact);
    }

    public Contact getNodeByCompanyId(String companyId) {
        return neo4jContactRepository.getNodeByCompanyId(companyId);
    }

    public List<Contact> getSuggestionContact(String companyId, int level) {
        return neo4jContactRepository.getSuggestionContact(companyId, level);
    }

    public List<Contact> getContactByDepth(String companyId, int level) {
        return neo4jContactRepository.getContactByDepth(companyId, level);
    }

    public List<Contact> getRelationOfTwoNodes(String toCompanyId, String fromCompanyId, int level) {
        return neo4jContactRepository.getRelationOfTwoNodes(toCompanyId, fromCompanyId, level);
    }

    public void updateRelationOfTwoNodes(String toCompanyId, String fromCompanyId, Short status, Short relationType) {
        neo4jContactRepository.updateRelationOfTwoNodes(toCompanyId, fromCompanyId, status, relationType);
    }

    public void deleteRelationOfTwoNode(String fromCompanyId, String toCompanyId) {
        neo4jContactRepository.deleteRelationOfTwoNode(fromCompanyId, toCompanyId);
    }

    public Integer getConnectionLevel(String fromCompanyId, String toCompanyId) {
        return neo4jContactRepository.getConnectionLevel(fromCompanyId, toCompanyId);
    }
}
