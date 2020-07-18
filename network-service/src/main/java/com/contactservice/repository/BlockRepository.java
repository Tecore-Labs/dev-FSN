package com.contactservice.repository;

import com.contactservice.entity.BlockAccount;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class BlockRepository {

    @Autowired
    CassandraTemplate cassandraTemplate;

    public void addBlockDetail(BlockAccount blockAccount) {
        cassandraTemplate.insert(blockAccount);
    }

    public boolean removeBlockDetail(String blocker_id, String blocked_id) {
        Delete delete = QueryBuilder.delete().from(cassandraTemplate.getTableName(BlockAccount.class).toCql());
        delete.where(QueryBuilder.eq("blocker_id", blocker_id))
                .and(QueryBuilder.eq("blocked_id", blocked_id));
        delete.ifExists();

        return cassandraTemplate.getCqlOperations().execute(delete);
    }

    public List<BlockAccount> getBlockDetails(String blocker_id) {
        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(BlockAccount.class).toCql());
        select.where(QueryBuilder.eq("blocker_id", blocker_id));

        return cassandraTemplate.select(select, BlockAccount.class);
    }

    public BlockAccount getBlockDetail(String blocker_id, String blocked_id) {
        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(BlockAccount.class).toCql());
        select.where(QueryBuilder.eq("blocker_id", blocker_id))
                .and(QueryBuilder.eq("blocked_id", blocked_id));

        return cassandraTemplate.selectOne(select, BlockAccount.class);
    }

    public List<BlockAccount> getBlockDetail(Set<String> user_ids){

        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(BlockAccount.class).toCql());
        select.where(QueryBuilder.in("blocker_id", user_ids))
                .and(QueryBuilder.in("blocked_id", user_ids));

        return cassandraTemplate.select(select, BlockAccount.class);
//        select.where(QueryBuilder.in("follower_id", follower_id)).and(QueryBuilder.in("following_id", follower_id));
    }
}
