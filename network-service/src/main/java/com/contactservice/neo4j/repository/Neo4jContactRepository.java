package com.contactservice.neo4j.repository;

import com.contactservice.neo4j.domain.Contact;
import com.contactservice.util.ContactUtil;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.contactservice.constants.ContactConstants.CONNECTION_LEVEL_LENGTH;

@Repository
@Transactional
public class Neo4jContactRepository {

    private static final String NODE_LABEL = "Company";
    private static final String RELATION_TYPE = "CONNECTED_TO";

    @Autowired
    private Session session;

    public String addContact(Contact contact) {
        session.save(contact);
        return contact.getId();
    }

    public Contact getNodeByCompanyId(String companyId) {

        Map<String, Object> params = new HashMap<>(1);
        params.put("companyId", companyId);

        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$companyId}) RETURN c";

        return session.queryForObject(Contact.class, cypher, params);
    }

    // This method is used for getting contact suggestion using companyId and depth level
    public List<Contact> getSuggestionContact(String companyId, int level) {

//        level = 2; //because we want connections of connections, so we have to give it level to 2
        Map<String, Object> params = new HashMap<>(1);
        params.put("companyId", companyId);

        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$companyId}) " +
                "MATCH (c)-[:" + RELATION_TYPE + "*" + level + "]-(n) " +
                "WHERE NOT (c)-[:" + RELATION_TYPE + "]-(n) AND n <> c RETURN n";

        List<Contact> contactList = Lists.newArrayList(session.query(Contact.class, cypher, params));
        return contactList;
    }

    public List<Contact> getContactByDepth(String companyId, int level) {

        Map<String, Object> params = new HashMap<>(1);
        params.put("companyId", companyId);

        // //this query will be used when want connected nodes at particular level only for ex. ->level 2
//        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$companyId})-[:" + RELATION_TYPE + "*" + level + "]-(f) " +
//                "MATCH (c)-[rel:CONNECTED_TO]-(n) " +
//                "RETURN c,f,rel";

        //this query will be used to get connected nodes from a range of level for ex. ->level 1 to 2
        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$companyId})-[:" + RELATION_TYPE + "*1.." + level + "]-(f) " +
                "MATCH (c)-[rel:CONNECTED_TO]-(n) " +
                "RETURN c,f,rel";

        System.out.println("");
        System.out.println("CYPHER QUERY");
        System.out.println(cypher);
        System.out.println("");

        List<Contact> contactList = Lists.newArrayList(session.query(Contact.class, cypher, params));
        return contactList;
    }

    public List<Contact> getRelationOfTwoNodes(String toCompanyId, String fromCompanyId, int level) {

        Map<String, Object> params = new HashMap<>(1);
        params.put("toCompanyId", toCompanyId);
        params.put("fromCompanyId", fromCompanyId);

        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$toCompanyId})-[r:" + RELATION_TYPE + "*.."+level+"]-(f:" + NODE_LABEL + " {companyId:$fromCompanyId}) " +
                "RETURN c,f,r";

        List<Contact> contactList = Lists.newArrayList(session.query(Contact.class, cypher, params));
        return contactList;
    }

    public void updateRelationOfTwoNodes(String toCompanyId, String fromCompanyId, Short status, Short relationType) {

        Map<String, Object> params = new HashMap<>(1);
        params.put("toCompanyId", toCompanyId);
        params.put("fromCompanyId", fromCompanyId);
        params.put("status", status);
        params.put("relationType", relationType);
        params.put("modifiedTs", ContactUtil.epoch());

//        String cypher = "MATCH (c:Company {companyId:$toCompanyId})-[r:CONNECTED_TO]-(f:Company {companyId:$fromCompanyId}) " +
//                "SET r.relationType = $relationType, r.status = $status, c.modifiedTs = $modifiedTs, f.modifiedTs = $modifiedTs" ;
//                "RETURN c,f";

        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$toCompanyId})-[r:" + RELATION_TYPE + "]-(f:" + NODE_LABEL + " {companyId:$fromCompanyId}) " +
                "SET( " +
                "CASE " +
                "WHEN {relationType} IS NOT NULL " +
                "THEN r END ).relationType = $relationType, " +
                "( " +
                "CASE " +
                "WHEN {status} IS NOT NULL " +
                "THEN r END ).status = $status, c.modifiedTs = $modifiedTs, f.modifiedTs = $modifiedTs";

        Result result = session.query(cypher, params);

        System.out.println("");
        System.out.println("Result");
        System.out.println(result.queryStatistics().containsUpdates());
        System.out.println("");
    }

    public void deleteRelationOfTwoNode(String fromCompanyId, String toCompanyId) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("toCompanyId", toCompanyId);
        params.put("fromCompanyId", fromCompanyId);

//        MATCH(n:Company {companyId:"5dcbad56a10e2a2838e5a03b"})-[r:CONNECTED_TO]->(p:Company{ companyId:"5dc2a150a10e2a41a043afe3"})
//        DETACH DELETE r

        String cypher = "MATCH (c:" + NODE_LABEL + " {companyId:$toCompanyId})-[r:" + RELATION_TYPE + "]-(f:" + NODE_LABEL + " {companyId:$fromCompanyId}) " +
                "DETACH DELETE r";

        Result result = session.query(cypher, params);

        System.out.println("");
        System.out.println("Result");
        System.out.println(result.queryStatistics().containsUpdates());
        System.out.println("");
    }

    public Integer getConnectionLevel(String fromCompanyId, String toCompanyId) {

        Map<String, Object> params = new HashMap<>(1);
        params.put("toCompanyId", toCompanyId);
        params.put("fromCompanyId", fromCompanyId);

        String cypherQuery = "MATCH path = shortestpath((c1:"+NODE_LABEL+")-[:"+RELATION_TYPE+"*.."+CONNECTION_LEVEL_LENGTH+"]-(c2:"+NODE_LABEL+")) " +
                "WHERE c1.companyId = $fromCompanyId AND c2.companyId = $toCompanyId " +
                "RETURN LENGTH(path) AS level";

        System.out.println("cypherQuery");
        System.out.println(cypherQuery);
        System.out.println("");

        Integer level = session.queryForObject(Integer.class, cypherQuery, params);

        System.out.println("level");
        System.out.println(level);
        System.out.println("");

        return level;
    }

}
