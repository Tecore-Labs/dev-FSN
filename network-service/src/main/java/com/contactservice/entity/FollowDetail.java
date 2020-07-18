package com.contactservice.entity;

import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("follow_detail")
public class FollowDetail {

    @PrimaryKeyColumn(ordinal = 0, name = "id", type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    @PrimaryKeyColumn(ordinal = 0, name = "follower_id", type = PrimaryKeyType.PARTITIONED)
    private String follower_id;
    private String following_id;
    private Long creation_ts;
    private Long modified_ts;
    private Short follower_type;
    private Short status;
    private Short is_connected; //if they are also connected togethher

    public FollowDetail(UUID id, String follower_id, String following_id, Long creation_ts, Long modified_ts, Short follower_type, Short status, Short is_connected) {
        this.id = id;
        this.follower_id = follower_id;
        this.following_id = following_id;
        this.creation_ts = creation_ts;
        this.modified_ts = modified_ts;
        this.follower_type = follower_type;
        this.status = status;
        this.is_connected = is_connected;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFollower_id() {
        return follower_id;
    }

    public void setFollower_id(String follower_id) {
        this.follower_id = follower_id;
    }

    public String getFollowing_id() {
        return following_id;
    }

    public void setFollowing_id(String following_id) {
        this.following_id = following_id;
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

    public Short getFollower_type() {
        return follower_type;
    }

    public void setFollower_type(Short follower_type) {
        this.follower_type = follower_type;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Short getIs_connected() {
        return is_connected;
    }

    public void setIs_connected(Short is_connected) {
        this.is_connected = is_connected;
    }

    @Override
    public String toString() {
        return "FollowDetail{" +
                "id=" + id +
                ", follower_id=" + follower_id +
                ", following_id=" + following_id +
                ", creation_ts=" + creation_ts +
                ", modified_ts=" + modified_ts +
                ", follower_type=" + follower_type +
                ", status=" + status +
                ", is_connected=" + is_connected +
                '}';
    }
}
