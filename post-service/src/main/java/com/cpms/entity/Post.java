package com.cpms.entity;

import com.cpms.enumeration.PostPrivacy;
import com.cpms.enumeration.PosterType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Table("post")
public class Post {

    // CLUSTER KEY = id, created_ts
    // PRIMARY KEY = poster id

    @NotNull
    @PrimaryKeyColumn(name = "id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
    private UUID id;

    @NotNull
    @PrimaryKeyColumn(name = "poster_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String poster_id;

    @NotNull
    @Size(min = 2, message = "Text field should be having min 2 char")
    private String post_text;

    private String heading;

    private PosterType poster;
    private int poster_type;

//    private PostPrivacy privacy;
    private int post_privacy;

    @PrimaryKeyColumn(name = "created_ts", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
    private long created_ts;

    private long modified_ts;

    // JSONObject data type
    private Map<String, String> media_image;
    private Map<String, String> media_video;

    private int total_likes;
    private int total_comments;
    private int total_shares;
    private Set<String> tag;
    private int status;

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

    public String getPost_text() {
        return post_text;
    }

    public void setPost_text(String post_text) {
        this.post_text = post_text;
    }

    public String getHeading() {
        return heading;
    }

    public void setHeading(String heading) {
        this.heading = heading;
    }

    public PosterType getPoster() {
        return poster;
    }

    public void setPoster(PosterType poster) {
        this.poster = poster;
    }

    public int getPoster_type() {
        return poster_type;
    }

    public void setPoster_type(int poster_type) {
        this.poster_type = poster_type;
    }

//    public PostPrivacy getPrivacy() {
//        return privacy;
//    }
//
//    public void setPrivacy(PostPrivacy privacy) {
//        this.privacy = privacy;
//    }

    public int getPost_privacy() {
        return post_privacy;
    }

    public void setPost_privacy(int post_privacy) {
        this.post_privacy = post_privacy;
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

    public Map<String, String> getMedia_image() {
        return media_image;
    }

    public void setMedia_image(Map<String, String> media_image) {
        this.media_image = media_image;
    }

    public Map<String, String> getMedia_video() {
        return media_video;
    }

    public void setMedia_video(Map<String, String> media_video) {
        this.media_video = media_video;
    }

    public int getTotal_likes() {
        return total_likes;
    }

    public void setTotal_likes(int total_likes) {
        this.total_likes = total_likes;
    }

    public int getTotal_comments() {
        return total_comments;
    }

    public void setTotal_comments(int total_comments) {
        this.total_comments = total_comments;
    }

    public int getTotal_shares() {
        return total_shares;
    }

    public void setTotal_shares(int total_shares) {
        this.total_shares = total_shares;
    }

    public Set<String> getTag() {
        return tag;
    }

    public void setTag(Set<String> tag) {
        this.tag = tag;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", poster_id='" + poster_id + '\'' +
                ", post_text='" + post_text + '\'' +
                ", heading='" + heading + '\'' +
                ", poster=" + poster +
                ", poster_type=" + poster_type +
//                ", privacy=" + privacy +
                ", post_privacy=" + post_privacy +
                ", created_ts=" + created_ts +
                ", modified_ts=" + modified_ts +
                ", media_image=" + media_image +
                ", media_video=" + media_video +
                ", total_likes=" + total_likes +
                ", total_comments=" + total_comments +
                ", total_shares=" + total_shares +
                ", tag=" + tag +
                ", status=" + status +
                '}';
    }
}
