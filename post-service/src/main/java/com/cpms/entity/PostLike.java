package com.cpms.entity;

import com.cpms.enumeration.LikerType;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.Set;
import java.util.UUID;

@Table("post_like")
public class PostLike {

    private UUID id;
    @PrimaryKeyColumn(name = "post_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID post_id;
    private String poster_id;
    @PrimaryKeyColumn(name = "liker_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String liker_id;
    //    @PrimaryKeyColumn(name = "like_ts", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private long like_ts;
    private LikerType liker;
    private int liker_type;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPost_id() {
        return post_id;
    }

    public void setPost_id(UUID post_id) {
        this.post_id = post_id;
    }

    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }

    public String getLiker_id() {
        return liker_id;
    }

    public void setLiker_id(String liker_id) {
        this.liker_id = liker_id;
    }

    public long getLike_ts() {
        return like_ts;
    }

    public void setLike_ts(long like_ts) {
        this.like_ts = like_ts;
    }

    public LikerType getLiker() {
        return liker;
    }

    public void setLiker(LikerType liker) {
        this.liker = liker;
    }

    public int getLiker_type() {
        return liker_type;
    }

    public void setLiker_type(int liker_type) {
        this.liker_type = liker_type;
    }

    @Override
    public String toString() {
        return "PostLike{" +
                "id=" + id +
                ", post_id=" + post_id +
                ", poster_id='" + poster_id + '\'' +
                ", liker_id=" + liker_id +
                ", like_ts=" + like_ts +
                ", liker=" + liker +
                ", liker_type=" + liker_type +
                '}';
    }
}
