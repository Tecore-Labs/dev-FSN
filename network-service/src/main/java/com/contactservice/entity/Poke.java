package com.contactservice.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("poke")
public class Poke {

    private UUID id;
    @PrimaryKeyColumn(ordinal = 0, name = "poker_id", type = PrimaryKeyType.PARTITIONED)
    private String poker_id;
    @PrimaryKeyColumn(ordinal = 1, name = "poked_id", type = PrimaryKeyType.CLUSTERED)
    private String poked_id;
    private Long created_ts;
    private Short status;

    public Poke(UUID id, String poker_id, String poked_id, Long created_ts, Short status) {
        this.id = id;
        this.poker_id = poker_id;
        this.poked_id = poked_id;
        this.created_ts = created_ts;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPoker_id() {
        return poker_id;
    }

    public void setPoker_id(String poker_id) {
        this.poker_id = poker_id;
    }

    public String getPoked_id() {
        return poked_id;
    }

    public void setPoked_id(String poked_id) {
        this.poked_id = poked_id;
    }

    public Long getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(Long created_ts) {
        this.created_ts = created_ts;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Poke{" +
                "id=" + id +
                ", poker_id='" + poker_id + '\'' +
                ", poked_id='" + poked_id + '\'' +
                ", created_ts=" + created_ts +
                ", status=" + status +
                '}';
    }
}
