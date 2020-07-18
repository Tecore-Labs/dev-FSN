package com.contactservice.service;

import com.contactservice.entity.Poke;
import com.contactservice.repository.PokeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PokeService {

    @Autowired
    PokeRepository pokeRepository;

    public void addPokeDetail(Poke poke) {
        pokeRepository.addPokeDetail(poke);
    }

    public boolean deletePokeDetail(String poker_id, String poked_id) {
        return pokeRepository.deletePokeDetail(poker_id,poked_id);
    }

    public Poke getPokeDetail(String poker_id, String poked_id) {
        return pokeRepository.getPokeDetail(poker_id,poked_id);
    }

    public boolean updatePokeStatus(String poker_id, String poked_id, Short status) {
        return pokeRepository.updatePokeStatus(poker_id,poked_id,status);
    }

    public List<Poke> getPokeDetailsByPokedId(String poked_id){
        return pokeRepository.getPokeDetailsByPokedId(poked_id);
    }

    public List<Poke> getPokeDetailsByPokerId(String poker_id){
        return pokeRepository.getPokeDetailsByPokerId(poker_id);
    }
}
