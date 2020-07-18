package com.cpms.repository;

import com.cpms.entity.Post;
import com.cpms.entity.PostLike;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;

@Repository
public class PostDao {

    @Autowired
    CassandraOperations cassandraTemplate;

    private static final Class POST_ENTITY_CLASS = Post.class;
    private static final Class POST_LIKE_ENTITY_CLASS = PostLike.class;

    // ADD POST
    public void createPost(Post postuser) {
        cassandraTemplate.insert(postuser);
        System.out.println(postuser);
    }

    // UPDATE POST
    public boolean updatePost(Map<String ,Object> postuser, UUID id,                         // Post id
                              String poster_id,         // Account id
                             long created_ts) {

        Update update = QueryBuilder.update(cassandraTemplate.getTableName(POST_ENTITY_CLASS).toCql());

        Set<String> keys=postuser.keySet();
        for(String key:keys)
        {
            update.with( set(key,postuser.get(key)) );
        }

        update.where(eq("id", id)).and(eq("poster_id", poster_id)).and(eq("created_ts",created_ts )).ifExists();

        return cassandraTemplate.getCqlOperations().execute(update);
    }

    // UPDATE POST LIKE, SHARE & COMMENTS
    public boolean updatePostLikes(Post postuser) {

        Update update = QueryBuilder.update(cassandraTemplate.getTableName(POST_ENTITY_CLASS).toCql());
        update.with((set("total_likes", postuser.getTotal_likes()))).and(set("total_shares", postuser.getTotal_shares())).and(set("total_comments", postuser.getTotal_comments()));
        update.where(eq("poster_id", postuser.getPoster_id())).and(eq("id", postuser.getId())).and(eq("created_ts", postuser.getCreated_ts())).ifExists();

        return cassandraTemplate.getCqlOperations().execute(update);
    }

    // DELETE POST
    public boolean deletePost(Post postuser) {

        Update update = QueryBuilder.update(cassandraTemplate.getTableName(POST_ENTITY_CLASS).toCql());
        update.with(set("status", postuser.getStatus()));
        update.where(eq("id", postuser.getId())).and(eq("poster_id", postuser.getPoster_id().trim())).and(eq("created_ts", postuser.getCreated_ts())).ifExists();
        return cassandraTemplate.getCqlOperations().execute(update);
    }

    // GET POST
    public Post getPostId(String poster_id, UUID id, long created_ts) {

        Query query = Query.query(Criteria.where("poster_id").is(poster_id.trim())).and(Criteria.where("id").is(id)).and(Criteria.where("created_ts").is(created_ts));
        //Statement selectQuery = QueryBuilder.select().all().from("post").where(eq("poster_id", poster_id.trim())).and(eq("id", id)).and(eq("created_ts", created_ts));
        return (Post) cassandraTemplate.selectOne(query, POST_ENTITY_CLASS);
    }

    // GET ALL POST CREATED BY SAME POSTER ID
    public List<Post> getPostByPosterId(String poster_id, UUID id, long created_ts) {
        Statement selectQuery = QueryBuilder.select().all().from("post").where(eq("poster_id", poster_id.trim())).and(eq("id", id)).and(eq("created_ts", created_ts));
        List<Post> posts = cassandraTemplate.select(selectQuery, POST_ENTITY_CLASS);
        return posts;
    }

    // FOLLOWER PAGINATION FUNCTION
    public List<Post> getPostByPosterIds(Set<String> posterIds, int fetchLimit, Long created_ts, boolean is_lt) {

        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(POST_ENTITY_CLASS).toString());

        Statement statement = null;
        if (is_lt) {
            statement = select.where(in("poster_id", posterIds)).and(lt("created_ts", created_ts))
                    .orderBy(desc("created_ts")).limit(fetchLimit);
        } else {
            statement = select.where(in("poster_id", posterIds)).and(lte("created_ts", created_ts))
                    .orderBy(desc("created_ts")).limit(fetchLimit);
        }
        statement.setFetchSize(Integer.MAX_VALUE);

        List<Post> postUserList = cassandraTemplate.select(statement, Post.class);

        return postUserList;
    }

    // FETCH POST DATA
    public Map<String, Object> getPostUsingPagination(String poster_id, Long created_ts, String orderBy, String localPageStage, int fetchSize) {

        System.out.println("");
        System.out.println("Call repository function");
        System.out.println("");
        Select select = null;
        Statement statement = null;

        select = QueryBuilder.select().from(cassandraTemplate.getTableName(POST_ENTITY_CLASS).toString());

        if (orderBy.equals("ASC")) {
            select.orderBy(QueryBuilder.asc("created_ts"));

        } else {
            select.orderBy(QueryBuilder.desc("created_ts"));
        }

        //statement = select.where(QueryBuilder.eq("poster_id", poster_id.trim())).and(QueryBuilder.gte("created_ts", created_ts)).setFetchSize(fetchSize);
        statement = select.where(QueryBuilder.eq("poster_id", poster_id.trim())).setFetchSize(fetchSize);

        if (localPageStage != null) {
            statement.setPagingState(PagingState.fromString(localPageStage));
        }

        Slice<Post> testSlice = cassandraTemplate.slice(statement, POST_ENTITY_CLASS);

        List<Post> testList = testSlice.getContent();

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


    // ADD LIKE POST DATA
    public void addLikePost(PostLike postLike) {
        cassandraTemplate.insert(postLike);
    }

    // FOR LIKE POST
    public List<PostLike> getExistingLikeId(String liker_id, UUID id) {
        Query query = null;
        query = Query.query(Criteria.where("post_id").is(id)).and(Criteria.where("liker_id").is(liker_id));
        List<PostLike> postLike = cassandraTemplate.select(query, POST_LIKE_ENTITY_CLASS);
        return postLike;
    }

    // UPDATE LIKER IF NEW
    public boolean updateLikePost(PostLike postLike, UUID post_id, long like_ts) {
        Update update = QueryBuilder.update(cassandraTemplate.getTableName(POST_LIKE_ENTITY_CLASS).toCql());
        update.with(set("liker_id", postLike.getLiker_id()));
        update.where(eq("post_id", post_id)).and(eq("like_ts", like_ts)).ifExists();
        return cassandraTemplate.getCqlOperations().execute(update);
    }

    // DELETE LIKE
    public boolean deletePostLike(UUID postId, String liker_id) {

        Query query = null;
        query = Query.query(Criteria.where("liker_id").is(liker_id)).and(Criteria.where("post_id").is(postId));

        return cassandraTemplate.delete(query, POST_LIKE_ENTITY_CLASS);
    }

    // get postLiked  data from post_id
    public List<PostLike> getPostLike(String liker_id, Set<UUID> post_ids) {

        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(PostLike.class).toCql());
        select.where(eq("liker_id", liker_id)).and(in("post_id", post_ids));

        return cassandraTemplate.select(select, PostLike.class);
    }
}

