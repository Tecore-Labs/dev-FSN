package com.contactservice.neo4j.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.id.UuidStrategy;

@RelationshipEntity(type = "CONNECTED_TO")
public class ContactRelationship {

    @Id
    @GeneratedValue(strategy = UuidStrategy.class)
    private String id;
    private Short relationType;
    private Short status;

    @JsonIgnore
    @StartNode
    private Contact connectedFrom; // company which is connected (from) the (to) company

    @JsonIgnore
    @EndNode
    private Contact connectedTo; //company which is connected (to) the (from) company

    public ContactRelationship() {
    }

    public ContactRelationship(Contact connectedTo, Contact connectedFrom) {
        this.connectedTo = connectedTo;
        this.connectedFrom = connectedFrom;
    }

    public Short getRelation_type() {
        return relationType;
    }

    public void setRelation_type(Short relationType) {
        this.relationType = relationType;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ContactRelationship{" +
                "id='" + id + '\'' +
                ", relationType=" + relationType +
                ", status=" + status +
//                ", connectedFrom=" + connectedFrom +
//                ", connectedTo=" + connectedTo +
                '}';
    }
}
