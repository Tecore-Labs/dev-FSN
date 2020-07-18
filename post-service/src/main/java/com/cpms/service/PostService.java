package com.cpms.service;

import com.cpms.entity.Post;
import com.cpms.entity.PostLike;
import com.cpms.repository.PostDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostDao postDao;

    // ADD POST
    public void createPost(Post postuser) {
        postDao.createPost(postuser);
    }

    // FOR UPDATE
    public boolean updatePost(Map<String ,Object> postuser, UUID id,                         // Post id
                            String poster_id,         // Account id
                               long created_ts) {
        return postDao.updatePost(postuser,id,poster_id,created_ts);
    }

    // DELETE POST
    public boolean deletePost(Post postuser) {
        return postDao.deletePost(postuser);
    }

    // GET POST
    public Post getPostId(String poster_id, UUID id, long created_ts) {
        return postDao.getPostId(poster_id, id, created_ts);
    }

    // FOR UPDATE POST LIKE, SHARE, COMMENTS
    public void updatePostLikes(Post postUser) {
        postDao.updatePostLikes(postUser);
    }

    // GET ALL POST CREATED BY SAME POSTER ID
    public List<Post> getPostByPosterId(String poster_id, UUID id, long created_ts) {
        List<Post> post = postDao.getPostByPosterId(poster_id, id, created_ts);
        return post;
    }

    // FOLLOWER PAGINATION FUNCTION
    public List<Post> getPostByPosterIds(Set<String> posterIds, int fetchLimit, long created_ts, boolean is_lt) {
        return postDao.getPostByPosterIds(posterIds, fetchLimit, created_ts, is_lt);
    }

    // FETCH POST DATA
    public Map<String, Object> getPostUsingPagination(String poster_id, Long created_ts, String orderBy, String localPageStage, int fetchSize) {
        return postDao.getPostUsingPagination(poster_id, created_ts, orderBy, localPageStage, fetchSize);
    }

    // ADD LIKE POST DATA
    public void addLikePost(PostLike postLike) {
        postDao.addLikePost(postLike);
    }

    //  FOR LIKE POST
    public List<PostLike> getExistingLikeId(String liker_id, UUID id) {
        return postDao.getExistingLikeId(liker_id,id);
    }

    // UPDATE LIKER IF NEW
    public boolean updateLikePost(PostLike postLike, UUID post_id, long like_ts) {
        return postDao.updateLikePost(postLike, post_id, like_ts);
    }

    // DELETE LIKE
    public boolean deletePostLike(UUID postId, String liker_id) {
        return postDao.deletePostLike(postId, liker_id);
    }

    // get post  from post_id
    public List<PostLike> getPostLike(String liker_id, Set<UUID> post_ids) {
        return postDao.getPostLike(liker_id, post_ids);
    }
}



