package com.contactservice.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("block")
public class BlockAccount {

    private UUID id;
    @PrimaryKeyColumn(ordinal = 0, name = "blocker_id", type = PrimaryKeyType.PARTITIONED)
    private String blocker_id;
    @PrimaryKeyColumn(ordinal = 0, name = "blocked_id", type = PrimaryKeyType.CLUSTERED)
    private String blocked_id;
    private Long created_ts;


    public BlockAccount() {
    }

    public BlockAccount(UUID id, String blocker_id, String blocked_id, Long created_ts) {
        this.id = id;
        this.blocker_id = blocker_id;
        this.blocked_id = blocked_id;
        this.created_ts = created_ts;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBlocker_id() {
        return blocker_id;
    }

    public void setBlocker_id(String blocker_id) {
        this.blocker_id = blocker_id;
    }

    public String getBlocked_id() {
        return blocked_id;
    }

    public void setBlocked_id(String blocked_id) {
        this.blocked_id = blocked_id;
    }

    public Long getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(Long created_ts) {
        this.created_ts = created_ts;
    }

    @Override
    public String toString() {
        return "BlockAccounts{" +
                "id=" + id +
                ", blocker_id='" + blocker_id + '\'' +
                ", blocked_id='" + blocked_id + '\'' +
                ", created_ts=" + created_ts +
                '}';
    }
}
