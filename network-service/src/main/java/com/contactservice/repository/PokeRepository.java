package com.contactservice.repository;

import com.contactservice.entity.Poke;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.querybuilder.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.contactservice.constants.ContactConstants.MaterializedViews.POKE_MV;

@Repository
public class PokeRepository {

    @Autowired
    CassandraTemplate cassandraTemplate;

    public void addPokeDetail(Poke poke) {
        cassandraTemplate.insert(poke);
    }

    public boolean deletePokeDetail(String poker_id, String poked_id) {
        Delete delete = QueryBuilder.delete().from(cassandraTemplate.getTableName(Poke.class).toCql());
        delete.where(QueryBuilder.eq("poker_id", poker_id))
                .and(QueryBuilder.eq("poked_id", poked_id));
        delete.ifExists();

        return cassandraTemplate.getCqlOperations().execute(delete);
    }

    public Poke getPokeDetail(String poker_id, String poked_id) {
        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(Poke.class).toCql());
        select.where(QueryBuilder.eq("poker_id", poker_id))
                .and(QueryBuilder.eq("poked_id", poked_id));

        return cassandraTemplate.selectOne(select, Poke.class);
    }

    public boolean updatePokeStatus(String poker_id, String poked_id, Short status) {
        Update update = QueryBuilder.update(cassandraTemplate.getTableName(Poke.class).toCql());
        update.with(QueryBuilder.set("status", status));
        update.where(QueryBuilder.eq("poker_id", poker_id)).and(QueryBuilder.eq("poked_id", poked_id)).ifExists();

        return cassandraTemplate.getCqlOperations().execute(update);
    }

    public List<Poke> getPokeDetailsByPokedId(String poked_id) {
        Select select = QueryBuilder.select().from(POKE_MV);
        select.where(QueryBuilder.eq("poked_id", poked_id));

        return cassandraTemplate.select(select, Poke.class);
    }

    public List<Poke> getPokeDetailsByPokerId(String poker_id) {
        Select select = QueryBuilder.select().from(cassandraTemplate.getTableName(Poke.class).toCql());
        select.where(QueryBuilder.eq("poker_id", poker_id));

        return cassandraTemplate.select(select, Poke.class);
    }
}
