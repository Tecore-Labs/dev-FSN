package com.cpms.service;

import com.cpms.entity.PostComment;
import com.cpms.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CommentService {

    @Autowired
    CommentRepository commentRepository;

    public List<PostComment> getPostComments(Set<UUID> postIds){
        return commentRepository.getPostComments(postIds);
    }

    // ADD POST COMMENT
    public void addCommentOnPost(PostComment commentPost) {
        commentRepository.addCommentOnPost(commentPost);
    }

    // GET POST COMMENT
    public List<PostComment> getPostCommentList(UUID post_id) {
        List<PostComment> commentList = commentRepository.getPostCommentList(post_id);
        return commentList;
    }

    // GET POST COMMENT FOR UPDATE COMMENT
    public PostComment getPostCommentDetail(UUID post_id, UUID id, long created_ts) {
        return commentRepository.getPostCommentDetail(post_id, id, created_ts);
    }

    // UPDATE COMMENT POST
    public boolean updatePostComment(PostComment postComment) {
        return commentRepository.updatePostComment(postComment);
    }

    // DELETE COMMENT FROM POST
    public boolean deleteCommentFromPost(UUID post_id, UUID id, long created_ts) {
        return commentRepository.deleteCommentFromPost(post_id, id, created_ts);
    }

    // GET TOTAL LIKES ON COMMENT
    public PostComment getTotalLikeOnComment(UUID post_id, UUID id, long created_ts) {
        return commentRepository.getTotalLikeOnComment(post_id, id, created_ts);
    }

    // LIKE ON COMMENT
    public boolean updatePostCommentLike(PostComment postComment) {
        return commentRepository.updatePostCommentLike(postComment);
    }

    // GET ALL COMMENT ON POST
    public Map<String, Object> getPostCommentUsingPagination(UUID post_id, String orderBy, String localPageStage, int fetchSize) {
        return commentRepository.getPostCommentUsingPagination(post_id, orderBy, localPageStage, fetchSize);

    }
}

