package com.cpms.entity;

import com.cpms.enumeration.CommenterType;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Table("post_comment")
public class PostComment {

    // CLUSTER KEY = id, created_ts
    // PRIMARY KEY = post_id

    @NotNull
    @PrimaryKeyColumn(name = "id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;
    private String poster_id;
    private String text;
    @NotNull
    @PrimaryKeyColumn(name = "post_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private UUID post_id;
    @NotNull
    @PrimaryKeyColumn(name = "created_ts", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private long created_ts;
    private long modified_ts;
    private int total_likes;
    private String commenter_id;

    private CommenterType commenter;
    private int commenter_type;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPoster_id() {
        return poster_id;
    }

    public void setPoster_id(String poster_id) {
        this.poster_id = poster_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public UUID getPost_id() {
        return post_id;
    }

    public void setPost_id(UUID post_id) {
        this.post_id = post_id;
    }

    public long getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(long created_ts) {
        this.created_ts = created_ts;
    }

    public long getModified_ts() {
        return modified_ts;
    }

    public void setModified_ts(long modified_ts) {
        this.modified_ts = modified_ts;
    }

    public int getTotal_likes() {
        return total_likes;
    }

    public void setTotal_likes(int total_likes) {
        this.total_likes = total_likes;
    }

    public String getCommenter_id() {
        return commenter_id;
    }

    public void setCommenter_id(String commenter_id) {
        this.commenter_id = commenter_id;
    }

    public CommenterType getCommenter() {
        return commenter;
    }

    public void setCommenter(CommenterType commenter) {
        this.commenter = commenter;
    }

    public int getCommenter_type() {
        return commenter_type;
    }

    public void setCommenter_type(int commenter_type) {
        this.commenter_type = commenter_type;
    }

    @Override
    public String toString() {
        return "CommentPost{" +
                "id=" + id +
                ", poster_id='" + poster_id + '\'' +
                ", text='" + text + '\'' +
                ", post_id=" + post_id +
                ", created_ts=" + created_ts +
                ", modified_ts=" + modified_ts +
                ", total_likes=" + total_likes +
                ", commenter_id='" + commenter_id + '\'' +
                ", commenter=" + commenter +
                ", commenter_type=" + commenter_type +
                '}';
    }
}
