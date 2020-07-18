package com.contactservice.repository;


import com.contactservice.entity.ConnectionRequest;
import com.contactservice.entity.FollowDetail;
import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.contactservice.constants.ContactConstants.MaterializedViews.CONNECTION_REQUEST_MV;
import static com.contactservice.constants.ContactConstants.MaterializedViews.FOLLOW_DETAIL_MV;

@Repository
public class ContactRepository {

    @Autowired
    private CassandraTemplate cassandraTemplate;

    public void addConnectionRequest(ConnectionRequest connectionRequest) {
        cassandraTemplate.insert(connectionRequest);
    }

    public boolean updateConnectionRequest(UUID id, String company_id, Map<String, Object> updateDataMap) {

        Update update = QueryBuilder.update(cassandraTemplate.getTableName(ConnectionRequest.class).toCql());

        for (String key : updateDataMap.keySet()) {

            if (updateDataMap.get(key) != null) {
                update.with(QueryBuilder.set(key, updateDataMap.get(key)));
            }
        }

        update.where(QueryBuilder.eq("id", id)).and(QueryBuilder.eq("company_id", company_id)).ifExists();

        return cassandraTemplate.getCqlOperations().execute(update);
    }

    public List<ConnectionRequest> getConnectionRequest(Map<String, Object> searchDataMap) {

//        Query query = null;
        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(ConnectionRequest.class).toCql());
        int count = 0;

        Select.Where where = null;

        for (String key : searchDataMap.keySet()) {
            count++;

            if (count == 1) {
//                query = Query.query(Criteria.where(key).is(searchDataMap.get(key)));
                where = select.where(QueryBuilder.eq(key, searchDataMap.get(key)));

            } else {
//                query = query.and(Criteria.where(key).is(searchDataMap.get(key)));
                where = where.and(QueryBuilder.eq(key, searchDataMap.get(key)));
            }
        }

        return cassandraTemplate.select(select, ConnectionRequest.class);
    }


    public List<ConnectionRequest> getConnectionRequestByMV(UUID id, String requester_id) {

        Select select = QueryBuilder.select().from(CONNECTION_REQUEST_MV);

        if (id != null && requester_id != null) {
            select.where(QueryBuilder.eq("id", id)).and(QueryBuilder.eq("requester_id", requester_id));
        }

        if (id == null && requester_id != null) {
            select.where(QueryBuilder.eq("requester_id", requester_id));
        }

        return cassandraTemplate.select(select, ConnectionRequest.class);
    }

    //This method will be used for getting connection request by giving requester and company id
    public List<ConnectionRequest> getConnectionRequest(String company_id, String requester_id) {

        Select select = QueryBuilder.select().from(CONNECTION_REQUEST_MV);
        select.where(QueryBuilder.eq("company_id", company_id))
                .and(QueryBuilder.eq("requester_id", requester_id));

        return cassandraTemplate.select(select, ConnectionRequest.class);
    }

    public void addFollowDetail(FollowDetail followDetail) {
        cassandraTemplate.insert(followDetail);
    }

    public List<FollowDetail> searchByFollowerId(String follower_id) {

        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(FollowDetail.class).toString());
        select.where(QueryBuilder.eq("follower_id", follower_id));

        return cassandraTemplate.select(select, FollowDetail.class);
    }

    public List<FollowDetail> searchByFollowingId(String following_id) {

        Select select = QueryBuilder.select().from(FOLLOW_DETAIL_MV);
        select.where(QueryBuilder.eq("following_id", following_id));

        return cassandraTemplate.select(select, FollowDetail.class);
    }

    public List<FollowDetail> getFollowDetail(String following_id, String follower_id) {

        Select select = QueryBuilder.select().from(FOLLOW_DETAIL_MV);
        select.where(QueryBuilder.eq("following_id", following_id))
                .and(QueryBuilder.eq("follower_id", follower_id));

        return cassandraTemplate.select(select, FollowDetail.class);
    }

    public boolean updateFollowDetail(UUID id, String follower_id, Map<String, Object> updateDataMap) {

        Update update = QueryBuilder.update(cassandraTemplate.getTableName(FollowDetail.class).toCql());

        for (String key : updateDataMap.keySet()) {

            if (updateDataMap.get(key) != null) {
                update.with(QueryBuilder.set(key, updateDataMap.get(key)));
            }
        }

        update.where(QueryBuilder.eq("id", id)).and(QueryBuilder.eq("follower_id", follower_id)).ifExists();

        return cassandraTemplate.getCqlOperations().execute(update);
    }

    public boolean deleteConnectionRequest(UUID connection_request_id, String company_id) {

        Delete delete = QueryBuilder.delete().from(cassandraTemplate.getTableName(ConnectionRequest.class).toCql());
        delete.where(QueryBuilder.eq("id", connection_request_id))
                .and(QueryBuilder.eq("company_id", company_id));
        delete.ifExists();

        return cassandraTemplate.getCqlOperations().execute(delete);
    }

    public Map<String, Object> searchFollowDetailByPagination(String id, int fetch_size, String page_state,
                                                              boolean isSearchByFollowerId) {

        Select select = null;
        if (isSearchByFollowerId) {
            select = QueryBuilder.select().from(cassandraTemplate.getTableName(FollowDetail.class).toString());
            select.where(QueryBuilder.eq("follower_id", id));
        } else {
            select = QueryBuilder.select().from(FOLLOW_DETAIL_MV);
            select.where(QueryBuilder.eq("following_id", id));
        }

        select.setFetchSize(fetch_size);

        if (page_state != null) {
            select.setPagingState(PagingState.fromString(page_state));
        }

        Slice<FollowDetail> followDetails = cassandraTemplate.slice(select, FollowDetail.class);
        List<FollowDetail> followDetailList = followDetails.getContent();

        System.out.println("");
        System.out.println("followDetailList");
        System.out.println(followDetailList);
        System.out.println("");
        System.out.println("Follow Detail has next : " + followDetails.hasNext());
        System.out.println("");

        Map<String, Object> followDetailPaginatedData = new HashMap<>(2);

        String nextPageState = null;

        if (followDetailList == null || followDetailList.size() == 0) {
            System.out.println("");
            System.out.println("No data in follow detail list");
            return null;

        } else if (followDetailList.size() < fetch_size) {
            System.out.println("");
            System.out.println("No data in next round");

            nextPageState = "end";

        } else {

            CassandraPageRequest next = (CassandraPageRequest) followDetails.nextPageable();
            nextPageState = next.getPagingState().toString();
        }

        followDetailPaginatedData.put("pageState", nextPageState);
        followDetailPaginatedData.put("followDetails", followDetailList);

        return followDetailPaginatedData;
    }

    public List<FollowDetail> searchByFollowerIds(Set<String> follower_id) {

        Select select = QueryBuilder.select().from(FOLLOW_DETAIL_MV);
        select.where(QueryBuilder.in("follower_id", follower_id)).and(QueryBuilder.in("following_id", follower_id));

        return cassandraTemplate.select(select, FollowDetail.class);
    }

    public List<ConnectionRequest> getConnectionRequestByIds(Set<String> companyIds) {
        Select select = QueryBuilder.select().from(CONNECTION_REQUEST_MV);
        select.where(QueryBuilder.in("company_id", companyIds)).and(QueryBuilder.in("requester_id", companyIds));

        return cassandraTemplate.select(select, ConnectionRequest.class);
    }

    public boolean deleteFollowDetail(String follower_id, UUID id) {
        Delete delete = QueryBuilder.delete().from(cassandraTemplate.getTableName(FollowDetail.class).toCql());
        delete.where(QueryBuilder.eq("follower_id", follower_id))
                .and(QueryBuilder.eq("id", id));
        delete.ifExists();

        return cassandraTemplate.getCqlOperations().execute(delete);
    }

    public FollowDetail getFollowDetail(String follower_id, UUID id) {

        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(FollowDetail.class).toCql());
        select.where(QueryBuilder.eq("id", id))
                .and(QueryBuilder.eq("follower_id", follower_id));

        return cassandraTemplate.selectOne(select, FollowDetail.class);
    }

    //no need
//    public boolean deleteFollowDetailByFollowingId(String following_id) {
//        Delete delete = QueryBuilder.delete().from(FOLLOW_DETAIL_MV);
//        delete.where(QueryBuilder.eq("following_id", following_id));
//        delete.ifExists();
//
//        return cassandraTemplate.getCqlOperations().execute(delete);
//    }

}
