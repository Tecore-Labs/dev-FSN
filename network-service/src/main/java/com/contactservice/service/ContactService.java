package com.contactservice.service;


import com.contactservice.entity.ConnectionRequest;
import com.contactservice.entity.FollowDetail;
import com.contactservice.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public void addConnectionRequest(ConnectionRequest connectionRequest) {
        contactRepository.addConnectionRequest(connectionRequest);
    }

    public boolean updateConnectionRequest(UUID id, String company_id, Map<String, Object> updateDataMap) {
        return contactRepository.updateConnectionRequest(id, company_id, updateDataMap);
    }

    public List<ConnectionRequest> getConnectionRequest(Map<String, Object> searchDataMap) {
        return contactRepository.getConnectionRequest(searchDataMap);
    }

    public List<ConnectionRequest> getConnectionRequestByMV(UUID id, String requester_id) {
        return contactRepository.getConnectionRequestByMV(id, requester_id);
    }

    public void addFollowDetail(FollowDetail followDetail) {
        contactRepository.addFollowDetail(followDetail);
    }

    public List<FollowDetail> searchByFollowerId(String follower_id) {
        return contactRepository.searchByFollowerId(follower_id);
    }

    public List<FollowDetail> searchByFollowingId(String following_id) {
        return contactRepository.searchByFollowingId(following_id);
    }

    public boolean updateFollowDetail(UUID id, String follower_id, Map<String, Object> updateDataMap) {
        return contactRepository.updateFollowDetail(id, follower_id, updateDataMap);
    }

    public List<ConnectionRequest> getConnectionRequest(String company_id, String requester_id) {
        return contactRepository.getConnectionRequest(company_id, requester_id);
    }

    public List<FollowDetail> getFollowDetail(String following_id, String follower_id) {
        return contactRepository.getFollowDetail(following_id, follower_id);
    }

    public boolean deleteConnectionRequest(UUID connection_request_id, String company_id) {
        return contactRepository.deleteConnectionRequest(connection_request_id, company_id);
    }

    public Map<String, Object> searchFollowDetailByPagination(String id, int fetch_size, String page_state,
                                                              boolean isSearchByFollowerId) {
        return contactRepository.searchFollowDetailByPagination(id, fetch_size, page_state, isSearchByFollowerId);
    }

    public List<FollowDetail> searchByFollowerIds(Set<String> follower_id) {
        return contactRepository.searchByFollowerIds(follower_id);
    }

    public boolean deleteFollowDetail(String follower_id, UUID id) {
        return contactRepository.deleteFollowDetail(follower_id, id);
    }

    public List<ConnectionRequest> getConnectionRequestByIds(Set<String> companyIds){
        return contactRepository.getConnectionRequestByIds(companyIds);
    }

    public FollowDetail getFollowDetail(String follower_id, UUID id){
        return contactRepository.getFollowDetail(follower_id,id);
    }
}
