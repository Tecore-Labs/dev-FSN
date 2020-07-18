package com.cpms.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Constants {

    interface FollowStatus{
        Short FOLLOW = 1;
        Short UNFOLLOW = 0;
    }

    interface FollowerType {
        Short USER = 1;
        Short COMPANY = 2;
    }

    interface NotificationTemplateTypes {
        String NEW_POST_ADDED = "new_post_added";
        String NEW_COMMENT_ADDED = "new_comment_added";
        String NEW_LIKE_ADDED = "new_like_added";
    }

    interface InterceptorApiUrls {
        List<String> API_URLS = new ArrayList<>(Arrays.asList("/company/post/*","/post/details","/company/*"));
    }
}
