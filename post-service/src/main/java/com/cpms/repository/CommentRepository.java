package com.cpms.repository;

import com.cpms.entity.PostComment;
import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;

@Repository
public class CommentRepository {

    @Autowired
    CassandraOperations cassandraTemplate;

    private static final Class COMMENT_POST_ENTITY_CLASS = PostComment.class;

    // ADD COMMENT ON POST
    public void addCommentOnPost(PostComment commentPost) {
        cassandraTemplate.insert(commentPost);
    }

    public List<PostComment> getPostComments(Set<UUID> postIds){



        return cassandraTemplate.select(Query.query(Criteria.where("post_id").in(postIds.toArray())), COMMENT_POST_ENTITY_CLASS);
    }
    // GET POST COMMENT
    public List<PostComment> getPostCommentList(UUID post_id) {

        Statement selectQuery = QueryBuilder.select().all().from("post_comment").where(eq("post_id", post_id));
        List<PostComment> postCommentList = cassandraTemplate.select(selectQuery, COMMENT_POST_ENTITY_CLASS);
        return postCommentList;
    }

    // GET POST COMMENT FOR UPDATE COMMENT
    public PostComment getPostCommentDetail(UUID post_id, UUID id, long created_ts) {

        Query query = Query.query(Criteria.where("post_id").is(post_id)).and(Criteria.where("id").is(id)).and(Criteria.where("created_ts").is(created_ts));
        return (PostComment) cassandraTemplate.selectOne(query, COMMENT_POST_ENTITY_CLASS);
    }

    // UPDATE COMMENT POST
    public boolean updatePostComment(PostComment postComment) {

        Update update = QueryBuilder.update(cassandraTemplate.getTableName(COMMENT_POST_ENTITY_CLASS).toCql());
        update.with(set("text", postComment.getText())).and(set("modified_ts", postComment.getModified_ts())).and(set("total_likes", postComment.getTotal_likes()));
        update.where(eq("id", postComment.getId())).and(eq("post_id", postComment.getPost_id())).and(eq("created_ts", postComment.getCreated_ts())).ifExists();
        return cassandraTemplate.getCqlOperations().execute(update);
    }

    // DELETE COMMENT FROM POST
    public boolean deleteCommentFromPost(UUID post_id, UUID id, long created_ts) {

        Query query = Query.query(Criteria.where("post_id").is(post_id)).and(Criteria.where("id").is(id)).and(Criteria.where("created_ts").is(created_ts));
        return cassandraTemplate.delete(query, COMMENT_POST_ENTITY_CLASS);
    }

    // LIKE ON COMMENT
    public void getExistingLikeId(UUID id) {

    }

    // GET TOTAL LIKES ON COMMENT
    public PostComment getTotalLikeOnComment(UUID post_id, UUID id, long created_ts) {

        // Select select = QueryBuilder.select("total_likes").from("post_comment");
        Statement selectQuery = QueryBuilder.select().all().from("post_comment").where(eq("post_id", post_id)).and(eq("id", id)).and(eq("created_ts", created_ts));

        //select.where(QueryBuilder.eq("post_id", post_id)).and(QueryBuilder.eq("id", id)).and(QueryBuilder.eq("created_ts", created_ts));
        return (PostComment) cassandraTemplate.selectOne(selectQuery, COMMENT_POST_ENTITY_CLASS);
    }

    // LIKE ON COMMENT
    public boolean updatePostCommentLike(PostComment postComment) {
        Update update = QueryBuilder.update(cassandraTemplate.getTableName(COMMENT_POST_ENTITY_CLASS).toCql());
        update.with(set("total_likes", postComment.getTotal_likes()));
        update.where(eq("id", postComment.getId())).and(eq("post_id", postComment.getPost_id())).and(eq("created_ts", postComment.getCreated_ts())).ifExists();
        return cassandraTemplate.getCqlOperations().execute(update);
    }

    // GET ALL COMMENT ON POST
    public Map<String, Object> getPostCommentUsingPagination(UUID post_id, String orderBy, String localPageStage, int fetchSize) {
        System.out.println("");
        System.out.println("Call repository function");
        System.out.println("");
        Select select = null;
        Statement statement = null;

        select = QueryBuilder.select().from(cassandraTemplate.getTableName(COMMENT_POST_ENTITY_CLASS).toString());

        if (orderBy.equals("ASC")) {
            select.orderBy(QueryBuilder.asc("created_ts"));

        } else {
            select.orderBy(QueryBuilder.desc("created_ts"));
        }
        statement = select.where(QueryBuilder.eq("post_id", post_id)).setFetchSize(fetchSize);

        if (localPageStage != null) {
            statement.setPagingState(PagingState.fromString(localPageStage));
        }

        Slice<PostComment> testSlice = cassandraTemplate.slice(statement, COMMENT_POST_ENTITY_CLASS);

        List<PostComment> testList = testSlice.getContent();

        String nextPageState = null;
        if (testList == null || testList.size() == 0) {

            System.out.println("");
            System.out.println("No more data in post pagination");
            return null;

        } else if (testList.size() < fetchSize) {
            System.out.println("");
            System.out.println("No more data in post next page");
            nextPageState = "end";

        } else {

            CassandraPageRequest next = (CassandraPageRequest) testSlice.nextPageable();
            nextPageState = next.getPagingState().toString();
        }

        System.out.println("");
        System.out.println("Has more Records : " + testSlice.hasNext());
        System.out.println("Next Page : " + nextPageState);
        System.out.println("");

        Map<String, Object> finalData = new HashMap<>(2);
        finalData.put("data", testList);
        finalData.put("nextPage", nextPageState);
        finalData.put("hasMoreRecords", testSlice.hasNext());
        return finalData;
    }
}


