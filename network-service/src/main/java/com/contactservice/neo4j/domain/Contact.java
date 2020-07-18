package com.contactservice.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.id.UuidStrategy;

import java.util.Set;

@NodeEntity(label = "Company")
public class Contact {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String id;
    private String companyId;
    private Long creationTs;
    private Long modifiedTs;

    @JsonIgnoreProperties("connectedFrom")
    @Relationship( type = "CONNECTED_TO", direction = Relationship.UNDIRECTED)
    private Set<ContactRelationship> connections;

    public Contact() {
    }

    public Contact(String companyId, Long creationTs, Long modifiedTs) {
        this.companyId = companyId;
        this.creationTs = creationTs;
        this.modifiedTs = modifiedTs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public Long getCreationTs() {
        return creationTs;
    }

    public void setCreationTs(Long creationTs) {
        this.creationTs = creationTs;
    }

    public Long getModifiedTs() {
        return modifiedTs;
    }

    public void setModifiedTs(Long modifiedTs) {
        this.modifiedTs = modifiedTs;
    }

    public Set<ContactRelationship> getConnections() {
        return connections;
    }

    public void setConnections(Set<ContactRelationship> connections) {
        this.connections = connections;
    }


    @Override
    public String toString() {
        return "Contact{" +
                "id='" + id + '\'' +
                ", companyId='" + companyId + '\'' +
                ", creationTs=" + creationTs +
                ", modifiedTs=" + modifiedTs +
                ", connections=" + connections +
                '}';
    }
}
