package com.contactservice.service;

import com.contactservice.entity.BlockAccount;
import com.contactservice.repository.BlockRepository;
import com.datastax.driver.core.querybuilder.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class BlockService {

    @Autowired
    BlockRepository blockRepository;

    public void addBlockDetail(BlockAccount blockAccount) {
        blockRepository.addBlockDetail(blockAccount);
    }

    public boolean removeBlockDetail(String blocker_id, String blocked_id) {
        return blockRepository.removeBlockDetail(blocker_id, blocked_id);
    }

    public List<BlockAccount> getBlockDetails(String blocker_id) {
        return blockRepository.getBlockDetails(blocker_id);
    }

    public BlockAccount getBlockDetail(String blocker_id, String blocked_id) {
        return blockRepository.getBlockDetail(blocker_id, blocked_id);
    }

    public List<BlockAccount> getBlockDetail(Set<String> user_ids) {
        return blockRepository.getBlockDetail(user_ids);
    }
}
