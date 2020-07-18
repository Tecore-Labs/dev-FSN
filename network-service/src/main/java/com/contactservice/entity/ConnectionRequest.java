package com.contactservice.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table(value = "connection_request")
public class ConnectionRequest {

    @PrimaryKeyColumn(ordinal = 0, name = "id", type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    private String requester_id;
    @PrimaryKeyColumn(ordinal = 0, name = "company_id", type = PrimaryKeyType.PARTITIONED)
    private String company_id;
    private Long creation_ts;
    private Long modified_ts;
    private Short request_status;
    private Short request_type;
    private Long request_accept_ts;

//    public ConnectionRequest(UUID id, String requester_id, String company_id, Long creation_ts, Long modified_ts, Short request_status, Short request_type, Long request_accept_ts) {
//        this.id = id;
//        this.requester_id = requester_id;
//        this.company_id = company_id;
//        this.creation_ts = creation_ts;
//        this.modified_ts = modified_ts;
//        this.request_status = request_status;
//        this.request_type = request_type;
//        this.request_accept_ts = request_accept_ts;
//    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRequester_id() {
        return requester_id;
    }

    public void setRequester_id(String requester_id) {
        this.requester_id = requester_id;
    }

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public Long getCreation_ts() {
        return creation_ts;
    }

    public void setCreation_ts(Long creation_ts) {
        this.creation_ts = creation_ts;
    }

    public Long getModified_ts() {
        return modified_ts;
    }

    public void setModified_ts(Long modified_ts) {
        this.modified_ts = modified_ts;
    }

    public Short getRequest_status() {
        return request_status;
    }

    public void setRequest_status(Short request_status) {
        this.request_status = request_status;
    }

    public Short getRequest_type() {
        return request_type;
    }

    public void setRequest_type(Short request_type) {
        this.request_type = request_type;
    }

    public Long getRequest_accept_ts() {
        return request_accept_ts;
    }

    public void setRequest_accept_ts(Long request_accept_ts) {
        this.request_accept_ts = request_accept_ts;
    }

    @Override
    public String toString() {
        return "ConnectionRequest{" +
                "id=" + id +
                ", requester_id='" + requester_id + '\'' +
                ", company_id='" + company_id + '\'' +
                ", creation_ts=" + creation_ts +
                ", modified_ts=" + modified_ts +
                ", request_status=" + request_status +
                ", request_type=" + request_type +
                ", request_accept_ts=" + request_accept_ts +
                '}';
    }
}
