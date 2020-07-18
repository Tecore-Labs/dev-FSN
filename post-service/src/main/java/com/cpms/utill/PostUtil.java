package com.cpms.utill;

import com.cpms.entity.PostLike;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.cpms.constants.Constants.FollowerType.COMPANY;

@Component
public class PostUtil {

    public Set<UUID> getLikedPost(List<PostLike> postLikes) {

        Set<UUID> postIds = new HashSet<>(postLikes.size());
        for (PostLike postLike : postLikes) {
            postIds.add(postLike.getPost_id());
        }

        return postIds;
    }

    public Map<String, Set<String>> getCompanyAndUserId(List<Map<String, Object>> followerAndConnections) {

        Set<String> companyIds = new HashSet<>(followerAndConnections.size());
        Set<String> userIds = new HashSet<>(followerAndConnections.size());
        Map<String, Set<String>> result = new HashMap<>(2);

        for (Map<String, Object> data : followerAndConnections) {

            if (data.get("follower_id") != null) {

                String followerId = data.get("follower_id").toString();

                if (data.get("follower_type").equals(COMPANY)) {
                    companyIds.add(followerId);
                } else {
                    userIds.add(followerId);
                }

            } else {
                companyIds.add(data.get("companyId").toString());
            }
        }

        result.put("companyIds", companyIds);
        result.put("userIds", userIds);

        return result;
    }
}
