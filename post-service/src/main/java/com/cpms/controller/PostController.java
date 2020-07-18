package com.cpms.controller;

import com.cpms.entity.Post;
import com.cpms.entity.PostComment;
import com.cpms.entity.PostLike;
import com.cpms.enumeration.*;
import com.cpms.service.CommentService;
import com.cpms.service.PostService;
import com.cpms.utill.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

import static com.cpms.constants.Constants.FollowStatus.FOLLOW;
import static com.cpms.constants.Constants.NotificationTemplateTypes.*;
import static com.cpms.enumeration.MediaFileSize.ImageSize;
import static com.cpms.enumeration.MediaFileSize.VideoSize;
import static com.cpms.enumeration.PostPrivacy.ONLY_ME;
import static com.cpms.enumeration.Status.ACTIVE;
import static com.cpms.enumeration.Status.DELETED;
import static com.cpms.utill.HttpConstants.*;

@RestController
public class PostController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    PostService postService;
    @Autowired
    Validation validation;
    @Autowired
    CommentService commentService;
    @Autowired
    PostUtil postUtil;
    @Autowired
    private CassandraOperations cassandraTemplate;
    @Autowired
    private HttpResponse httpResponse;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    private Post postUser = null;
    private PostLike postLike = null;
    private PostComment postComment = null;

    @GetMapping(value = "/")
    public String index() {
        return "CPMS SERVICE";
    }

    /*
     * Created API for post
     *
     * */
    @ResponseBody
    @PostMapping(value = "/company/post/create", produces = "application/json")
    public Map<String, Object> createPost(@RequestParam(value = "poster_id", required = true) String poster_id,                     // Account id
                                          @RequestParam(value = "poster_type", required = true) String poster_type,                 // Type of poster is it USER OR COMPANY
                                          @RequestParam(value = "post_privacy", required = true) String post_privacy,               // Privacy on post is it PUBLIC, NETWORK & ONLY_ME
                                          @RequestParam(value = "post_text", required = false) String post_text,                    // Text of post
                                          @RequestParam(value = "heading", required = false) String heading,                        // Post heading
                                          @RequestParam(value = "tag", required = false) Set<String> tag,                              // Tag post to multiple connectors
                                          @RequestParam(value = "media_file", required = false) MultipartFile[] media_file,         // Upload multiple media file(images/videos) with text post
                                          @RequestHeader("Authorization") String token,
                                          @RequestParam("token_type") String token_type,
                                          @RequestParam("account_id") String account_id,
                                          HttpServletRequest request) throws IOException {

        System.out.println("------------------------");
        System.out.println("");
        System.out.println("ADD POST");
        System.out.println("");
        System.out.println("poster_id:" + poster_id);
        System.out.println("poster_type: " + poster_type);
        System.out.println("post_privacy: " + post_privacy);
        System.out.println("post_text: " + post_text);
        System.out.println("heading: " + heading);
        System.out.println("tag:" + tag);
        System.out.println("media_file: " + media_file);
        System.out.println("");
        System.out.println("------------------------");

        Map<String, String> imagePathSet = new HashMap<>();
        Map<String, String> videoPathSet = new HashMap<>();

        String fileArray[] = null;
        byte[] bytes = null;
        String subType = null;

        // String null declaration for image file
        String imageFileName = null;
        String imageFileExtension = null;
        long imageFileSize = 0L;

        // String null declaration for video file
        String videoFileName = null;
        String videoFileExtension = null;
        long videoFileSize = 0L;

        // CREATE POST USER BEAN OBJECT
        postUser = new Post();

        // -------------------------------------------------- Validate field's -----------------------------------------

        /*
         *  If user input invalid input or empty
         *
         * */
        if (poster_id == null || poster_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POSTER_ID, null);
        }

        /*
         * Check poster type if not USER or COMPANY
         *
         * */
        if (!(PosterType.USER.name().equals(poster_type)) && !(PosterType.COMPANY.name().equals(poster_type))) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POSTER_TYPE, null);
        }

        /*
         * Check post privacy if not NETWORK, PUBLIC or ONLY_ME
         *
         * */
        if (!(PostPrivacy.PUBLIC.name().equals(post_privacy)) && !(PostPrivacy.NETWORK.name().equals(post_privacy)) && !(PostPrivacy.ONLY_ME.name().equals(post_privacy))) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_PRIVACY_TYPE, null);
        }

        /*
         * Check if post text & media file both are empty together,
         * Condition apply at lest one filed should be required,
         * Either post text or media files.
         * Without one of them post can't be create,
         * At least one filed must be required.
         *
         * */
        if (("".equals(post_text)) && !(media_file.length > 0)) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST, null);
        }

        if (heading == null && post_text == null && media_file == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST, null);
        }
        if (tag != null && tag.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST, null);
        }

        /*
         * Check file extension
         *
         * */
        if (media_file != null && media_file.length > 0) {
            for (MultipartFile file : media_file) {
                if (!validation.validateCommonFileExtension(file.getOriginalFilename())) {
                    return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE_EXTENSION, null);
                }
                System.out.println("VALID FILE EXTENSION");
            }
        }

        /*
         * Either upload image and video files.
         *
         * */

        String validImage = null;
        String validVideo = null;
        if (media_file != null && media_file.length > 0) {
            for (MultipartFile file : media_file) {
                if (validation.validateImageFileExtension(file.getOriginalFilename())) {
                    validImage = "IMAGES";
                } else if (validation.validateVideoFileExtension(file.getOriginalFilename())) {
                    validVideo = "VIDEOS";
                }
            }
            if (validImage != null && validVideo != null) {
                if (validImage.equals("IMAGES") && validVideo.equals("VIDEOS"))
                    System.out.println("Please upload either image or Video file at a time");
                return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE, null);
            }
        }

        /*
         * Get file one by one from MultipartFile and check file's having a valid extension or not.
         * image extension : jpg, png, gif, bmp
         * video extension : 	mp4, 3gp, mpeg, avi, mov
         * note: user/company can upload image & video together or not. In both situation condition shell not be break.
         *
         * */
        if (media_file != null) {
            for (MultipartFile extractFile : media_file) {

                /*
                 * This condition check file extension should be jpg|png|gif|bmp
                 *
                 * */
                if (validation.validateImageFileExtension(extractFile.getOriginalFilename())) {
                    fileArray = extractFile.getOriginalFilename().split("\\.");
                    imageFileName = fileArray[0];
                    imageFileExtension = fileArray[fileArray.length - 1];
                    imageFileSize = extractFile.getSize();
                    // validate size < 1MB
                    if (!(imageFileSize < 1000000)) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.LARGE_FILE_SIZE, null);
                    }
                    subType = "images";
                }

                /*
                 * This condition check file extension should be mp4|3gp|mpeg|avi|mov
                 *
                 * */
                if (validation.validateVideoFileExtension(extractFile.getOriginalFilename())) {
                    fileArray = extractFile.getOriginalFilename().split("\\.");
                    videoFileName = fileArray[0];
                    videoFileExtension = fileArray[fileArray.length - 1];
                    videoFileSize = extractFile.getSize();
                    // validate size < 5MB
                    if (!(videoFileSize < 5000000)) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.LARGE_FILE_SIZE, null);
                    }
                    subType = "video";
                }
            }
        }

        UUID id = UUID.randomUUID();

        /*
         * Calling POST-SERVICE to FILE-SERVICE for uploading media file
         *
         * */
        if (media_file != null && media_file.length > 0) {
            Map<String, Object> mapImageFile = restTemplateUtil.accessMediaImageFile(id.toString(), media_file, subType, null, token, token_type, account_id);

            for (Map.Entry<String, Object> fileData : mapImageFile.entrySet()) {
                System.out.println(fileData.getKey() + " " + fileData.getValue());

                if (fileData.getKey().equals("response")) {
                    LinkedHashMap<String, String> linkedHashMap = (LinkedHashMap<String, String>) fileData.getValue();
                    Set<String> keys = linkedHashMap.keySet();

                    /*
                        UPDATED BY CHETNA JOSHI
                        * */

                    if (subType.equals("images")) {
                        imagePathSet.put("imagePath", linkedHashMap.get("imagePath"));
                        imagePathSet.put("imageId", linkedHashMap.get("id"));
                        System.out.println("Image file response:" + imagePathSet);
                        System.out.println("");
                    } else {
                        videoPathSet.put("videoPath", linkedHashMap.get("imagePath"));
                        videoPathSet.put("videoId", linkedHashMap.get("id"));
                        System.out.println("Video file response:" + videoPathSet);
                        System.out.println("");
                    }

                    /*for (String fileKey : keys) {
                        if (fileKey.equals("imagePath") || fileKey.equals("image_type") || fileKey.equals("imageUrl")) {
                            imagePathSet.put(fileKey, linkedHashMap.get(fileKey));
                            System.out.println("Image file response:" + imagePathSet);
                            System.out.println("");
                        } else if (fileKey.equals("videoPath") || fileKey.equals("video_type") || fileKey.equals("videoUrl")) {
                            videoPathSet.put(fileKey, linkedHashMap.get(fileKey));
                            System.out.println("Video file response:" + videoPathSet);
                            System.out.println("");
                        }

                    }*/


                    /*
                     *  If file extension not matched
                     *
                     * */
                    if ((int) mapImageFile.get("status") != SUCCESS_STATUS_CODE) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE_EXTENSION, null);
                    }

                    /*
                     * If file type not matched
                     *
                     * */
                    if ((int) mapImageFile.get("status") != SUCCESS_STATUS_CODE) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE_TYPE, null);
                    }

                    /*
                     * If file sub type not matched
                     *
                     * */
                    if ((int) mapImageFile.get("status") != SUCCESS_STATUS_CODE) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE_SUB_TYPE, null);
                    }
                }
            }
        } else {
            System.out.println("No files is uploaed");
        }


        // SET VALUE IN BEAN OBJECT
        postUser.setPoster_id(poster_id);

        // Creating TimeStamp
        long create_timestamp = EpochUtil.epoch();
        postUser.setCreated_ts(create_timestamp);

        postUser.setId(id);

        postUser.setHeading(heading == null ? "" : heading);

        // Set media image
        if (CollectionUtils.isEmpty(imagePathSet)) {
            postUser.setMedia_image(null);
        } else {
            postUser.setMedia_image(imagePathSet);
        }

        // Set media video
        if (CollectionUtils.isEmpty(videoPathSet)) {
            postUser.setMedia_video(null);
        } else {
            postUser.setMedia_video(videoPathSet);
        }
        // Modified TimeStamp
        long modified_timestamp = 0L;
        postUser.setModified_ts(modified_timestamp);

        // Setting Values of Type PostPrivacy
        if (PostPrivacy.PUBLIC.name().equals(post_privacy)) {
            postUser.setPost_privacy(PostPrivacy.PUBLIC.getValue());
        } else if (PostPrivacy.NETWORK.name().equals(post_privacy)) {
            postUser.setPost_privacy(PostPrivacy.NETWORK.getValue());
        } else {
            postUser.setPost_privacy(PostPrivacy.ONLY_ME.getValue());
        }

        postUser.setPost_text(post_text == null ? "" : post_text);

        // Setting value of Type PosterType
        if (PosterType.USER.name().equals(poster_type)) {
            postUser.setPoster_type(PosterType.USER.getValue());
        } else {
            postUser.setPoster_type(PosterType.COMPANY.getValue());
        }

        // Check status of post
        postUser.setStatus(ACTIVE.getValue());

        // Tag post
        postUser.setTag(tag);

        // Default value of total comments will zero
        postUser.setTotal_comments(0);
        // Default value of total likes will zero
        postUser.setTotal_likes(0);
        // Default value of total shares will zero
        postUser.setTotal_shares(0);

        Map<String, Object> contactServiceResponse = restTemplateUtil.getFollowerAndConnections(poster_id, token, token_type, account_id);

        System.out.println("");
        System.out.println("RESPONSE OF CONTACT SERVICE");
        System.out.println(contactServiceResponse);
        System.out.println("");

        String posterName = null;
        Map<String, Object> posterImage = null;
        if ((int) contactServiceResponse.get("status") == SUCCESS_STATUS_CODE) {
            List<Map<String, Object>> followerAndConnections = (List<Map<String, Object>>) contactServiceResponse.get("data");

            System.out.println("");
            System.out.println("followerAndConnections");
            System.out.println(followerAndConnections);
            System.out.println("");

            Map<String, Set<String>> resultMap = postUtil.getCompanyAndUserId(followerAndConnections);

            Map<String, Map<String, Object>> notificationData = new HashMap<>(followerAndConnections.size());
            Set<String> userIds = resultMap.get("userIds");
            Set<String> companyIds = resultMap.get("companyIds");

            System.out.println("userIds");
            System.out.println(userIds);
            System.out.println("companyIds");
            System.out.println(companyIds);

            if (!userIds.isEmpty() || !companyIds.isEmpty()) {
                if (postUser.getPoster_type() == PosterType.USER.getValue()) {
                    userIds.add(poster_id);
                } else {
                    companyIds.add(poster_id);
                }
            }

            if (!userIds.isEmpty()) {

                //call account service
                Set<String> fields = new HashSet<>(2);
                fields.add("first_name");
                fields.add("last_name");
                fields.add("avatar_image");
                fields.add("email_id");

                Map<String, Object> userDetailsResponse = restTemplateUtil.getUserDetails(userIds, fields, token, account_id, token_type);

                System.out.println("");
                System.out.println("userDetailsResponse");
                System.out.println(userDetailsResponse);
                System.out.println("");

                if ((int) userDetailsResponse.get("status") == SUCCESS_STATUS_CODE) {

                    List<Map<String, Object>> data = (List<Map<String, Object>>) userDetailsResponse.get("data");

                    for (Map<String, Object> user : data) {

                        String name = user.get("first_name").toString() + " " + user.get("last_name");
                        Map<String, Object> image = user.get("avatar_image") != null ? (Map<String, Object>) user.get("avatar_image") : null;

                        Map<String, Object> notificationDetail = new HashMap<>(5);
                        notificationDetail.put("name", name);
                        notificationDetail.put("email", user.get("email_id"));
                        notificationDetail.put("image", image);
                        notificationDetail.put("user_id", user.get("account_id"));

                        notificationData.put(user.get("account_id").toString(), notificationDetail);
                    }
                }
            }

            if (!companyIds.isEmpty()) {

                //call company service

                Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, token_type, account_id);

                System.out.println("");
                System.out.println("companyDetailResponse");
                System.out.println(companyDetailResponse);
                System.out.println("");

                if ((int) companyDetailResponse.get("status") == SUCCESS_STATUS_CODE) {

                    List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

                    for (Map<String, Object> companyData : data) {

                        Map<String, Object> image = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;

                        Map<String, Object> notificationDetail = new HashMap<>(5);
                        notificationDetail.put("name", companyData.get("company_name"));
                        notificationDetail.put("email", companyData.get("company_email"));
                        notificationDetail.put("image", image);
                        notificationDetail.put("user_id", companyData.get("account_id"));

                        notificationData.put(companyData.get("company_id").toString(), notificationDetail);
                    }
                }
            }

            //send notification
            Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
            Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);

            posterName = notificationData.get(poster_id).get("name").toString();
            posterImage = (Map<String, Object>) notificationData.get(poster_id).get("image");
            notificationData.remove(poster_id);
            for (String key : notificationData.keySet()) {

                Map<String, Object> payloadData = new HashMap<>(4);
                payloadData.put("poster_name", posterName);
                payloadData.put("email_id", notificationData.get(key).get("email"));
//                payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

                templatePayload.put(key, payloadData);

                Map<String, Object> bgPayloadData = new HashMap<>(4);
                bgPayloadData.put("poster_id", poster_id);
                bgPayloadData.put("post_id", postUser.getId().toString());
                bgPayloadData.put("poster_type", String.valueOf(postUser.getPoster_type()));
                bgPayloadData.put("name", posterName);

                if (posterImage != null && !posterImage.isEmpty()) {
                    bgPayloadData.put("image", posterImage.get("image_path"));
                }

                bgPayloadData.put("type", "post"); //this is used by client to show icon for the notification

                backgroundPayload.put(notificationData.get(key).get("user_id").toString(), bgPayloadData);
            }

            System.out.println("");
            System.out.println("templatePayload");
            System.out.println(templatePayload);
            System.out.println("backgroundPayload");
            System.out.println(backgroundPayload);
            System.out.println("");

            Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

            Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(poster_id,
                    "", "", templatePayload, backgroundPayload, is_batch, NEW_POST_ADDED);

            System.out.println("");
            System.out.println("NOTIFICATION SENT RESPONSE");
            System.out.println("");
            System.out.println(sendNotificationResponse);
            System.out.println("");
        }

        // CALLING SERVICE
        postService.createPost(postUser);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> postData = objectMapper.convertValue(postUser, Map.class);

        Map<String, Object> searchServiceResponse = restTemplateUtil.addUpdatePost(postData, postUser.getId().toString(), false, token, account_id, token_type);

        System.out.println("");
        System.out.println("SEARCH SERVICE RESPONSE");
        System.out.println(searchServiceResponse);
        System.out.println("");

        List<Post> postUserList = new ArrayList<>();
        postUserList.add(postUser);

        return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.POST_SUCCESS, postUser);
    }

    /*
     * Created API to fetch post
     *
     * */
    @GetMapping(value = "/company/post/get", produces = "application/json")
    public Map<String, Object> getUser(@RequestParam(value = "id", required = false) UUID id,                           // Post id
                                       @RequestParam(value = "poster_id", required = true) String poster_id,            // Account id
                                       @RequestParam(value = "created_ts", required = true) long created_ts,            // create time stamp
                                       HttpServletRequest request) throws IOException {

        System.out.println("----------");
        System.out.println("");
        System.out.println("GET POST");
        System.out.println("");
        System.out.println("id:" + id);
        System.out.println("poster_id:" + poster_id);
        System.out.println("created_ts: " + created_ts);
        System.out.println("");
        System.out.println("--------------");

        // ---------------------------------------------- Validate filed's ---------------------------------------------

        /*
         *  If user input invalid input or empty
         *
         * */
        if (poster_id == null || poster_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POSTER_ID, null);
        }

        /*
         * Get post data in list of object from post table is null or not
         *
         * */
        List<Post> postUserList = postService.getPostByPosterId(poster_id, id, created_ts);

        if (CollectionUtils.isEmpty(postUserList)) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        List<Map<String, Object>> responseMapList = new ArrayList<>(10);

        for (Post postUser : postUserList) {
            Map<String, Object> postUserMap = new HashMap<>(5);

            postUserMap.put("poster_id", postUser.getPoster_id());
            postUserMap.put("created_ts", postUser.getCreated_ts());
            postUserMap.put("id", postUser.getId());
            postUserMap.put("heading", postUser.getHeading());
            postUserMap.put("media_image", postUser.getMedia_image());
            postUserMap.put("media_video", postUser.getMedia_video());
            postUserMap.put("modified_ts", postUser.getModified_ts());
            postUserMap.put("post privacy", postUser.getPost_privacy());
            postUserMap.put("poster text", postUser.getPost_text());
            postUserMap.put("poster type", postUser.getPoster_type());
            postUserMap.put("post status", postUser.getStatus());
            postUserMap.put("tag", postUser.getTag());
            postUserMap.put("Total_Comments", postUser.getTotal_comments());
            postUserMap.put("Total_Likes", postUser.getTotal_likes());
            postUserMap.put("Total_Shares", postUser.getTotal_shares());
            responseMapList.add(postUserMap);

            System.out.println("");
            System.out.println("Get all post successfully");
            System.out.println(responseMapList);
            System.out.println("");
        }
        return (responseMapList != null && responseMapList.size() > 0) ? httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.GET_POST_SUCCESS, responseMapList) : httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
    }

    /*
     * Created API for update post
     *
     * */
    @CrossOrigin
    @PutMapping(value = "/company/post/update", produces = "application/json")
    public Map<String, Object> updatePostText(@RequestParam(value = "id", required = true) UUID id,                         // Post id
                                              @RequestParam(value = "poster_id", required = true) String poster_id,         // Account id
                                              @RequestParam(value = "created_ts", required = true) long created_ts,        // created timestamp
                                              @RequestParam(value = "post_text", required = false) String post_text,         // Update old post
                                              @RequestParam(value = "post_privacy", required = false) String post_privacy,   // Privacy on post is it PUBLIC, NETWORK & ONLY_ME
                                              @RequestParam(value = "heading", required = false) String heading,             // Post heading
                                              @RequestParam(value = "tag", required = false) String[] tag,                   // Tag post to multiple connectors
                                              @RequestParam(value = "media_file", required = false) MultipartFile[] media_file,   // Upload multiple media file(images/videos) with text post
                                              @RequestParam(value = "ismediaFileDeleletd", required = false) Integer ismediaFileDeleletd,   // 2 if media file is deleted
                                              @RequestParam(value = "fileType", required = false) Integer fileType,   // file type to be deleted (1 image,video 2)
                                              @RequestParam(value = "fileId", required = false) UUID fileId,// id of media file that will be updated
                                              @RequestHeader("Authorization") String token,
                                              @RequestParam("token_type") String token_type,
                                              @RequestParam("account_id") String account_id,
                                              HttpServletRequest request) throws IOException {

        System.out.println("------------------------");
        System.out.println("");
        System.out.println("UPDATE POST");
        System.out.println("");
        System.out.println("id:" + id);
        System.out.println("poster_id:" + poster_id);
        System.out.println("post_text:" + post_text);
        System.out.println("post_privacy: " + post_privacy);
        System.out.println("heading: " + heading);
        System.out.println("tag: " + tag);
        System.out.println("media_file: " + media_file);
        System.out.println("");
        System.out.println("------------------------");

        // ---------------------------------------------- Validate filed's ---------------------------------------------

        /*
         *  If user input invalid input or empty
         *
         * */
        if (poster_id == null || poster_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POSTER_ID, null);
        }

        /*
         * Get post data from post table is null or not
         *
         * */
        postUser = getPosterDetails(poster_id, id, created_ts);

        System.out.println("");
        System.out.println("Get post data" + postUser);
        System.out.println("");


        if (postUser == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        if (postUser.getStatus() == DELETED.getValue()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.DELETE_POST, null);
        }


        if (post_privacy != null && !(PostPrivacy.PUBLIC.name().equals(post_privacy)) && !(PostPrivacy.NETWORK.name().equals(post_privacy)) && !(PostPrivacy.ONLY_ME.name().equals(post_privacy))) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_PRIVACY_TYPE, null);
        }

        // update map
        Map<String, Object> updateMap = new HashMap<>();

        if (ismediaFileDeleletd != null && fileType != null) {

            if (ismediaFileDeleletd != DELETED.getValue())
                return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_DELETING_MEDIA_FILE, null);

            if (fileType != MediaFileType.ImageType.getValue() && fileType != MediaFileType.VideoType.getValue()) {
                System.out.println("Image Type error");
                return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FILE_TYPE_ERROR, null);
            }


            Map<String, String> media_image = postUser.getMedia_image();
            Map<String, String> media_video = postUser.getMedia_video();

            if (media_image != null && fileType == MediaFileType.ImageType.getValue() && media_image.get("imageId").equals(fileId.toString())) {
                Map<String, Object> mapImageFile = restTemplateUtil.accessMediaImageFile(null, null, null, fileId, token, token_type, account_id);
                if ((int) mapImageFile.get("status") != SUCCESS_STATUS_CODE) {
                    return mapImageFile;
                }
                System.out.println("delete media_image");

                updateMap.put("media_image", null);
            }


            if (media_video != null && fileType == MediaFileType.VideoType.getValue() && media_video.get("videoId").equals(fileId.toString())) {
                Map<String, Object> mapImageFile = restTemplateUtil.accessMediaImageFile(null, null, null, fileId, token, token_type, account_id);
                if ((int) mapImageFile.get("status") != SUCCESS_STATUS_CODE) {
                    return mapImageFile;
                }
                System.out.println("delete media_video");
                updateMap.put("media_video", null);
            }


        }


        // updating fields
        if (post_text != null) {
            updateMap.put("post_text", post_text);
            //postUser.setPost_text((post_text!=null)?post_text:postUser.getPost_text());
        }

        // Setting Values of Type PostPrivacy
        if (post_privacy != null) {
            if (PostPrivacy.PUBLIC.name().equals(post_privacy)) {
                updateMap.put("post_privacy", PostPrivacy.PUBLIC.getValue());
                //  postUser.setPost_privacy( PostPrivacy.PUBLIC.getValue() );
            } else if (PostPrivacy.NETWORK.name().equals(post_privacy)) {
                updateMap.put("post_privacy", PostPrivacy.NETWORK.getValue());
                // postUser.setPost_privacy( PostPrivacy.NETWORK.getValue() );
            } else {
                updateMap.put("post_privacy", PostPrivacy.ONLY_ME.getValue());
                // postUser.setPost_privacy( PostPrivacy.ONLY_ME.getValue() );
            }
        }

        // #Tag post
        if (tag != null) {
            if (tag.length > 0) {
                HashSet<String> postTag = new HashSet<>(Arrays.asList(tag));
                updateMap.put("tag", postTag);
            }
            //postUser.setTag( postTag );}
            else {
                updateMap.put("tag", null);
                //   postUser.setTag( null );
            }
        }

        if (heading != null) {

            updateMap.put("heading", heading);
            //  postUser.setHeading( (heading != null) ? heading : postUser.getHeading() );
        }

        /*
         * Check file extension
         *
         * */


        String validImage = null;
        String validVideo = null;
        String subType = null;
        long imageFileSize = 0L;

        // String null declaration for video file

        long videoFileSize = 0L;
        Map<String, String> imagePathSet = new HashMap<>();
        Map<String, String> videoPathSet = new HashMap<>();


        if (media_file != null && media_file.length > 0) {
            for (MultipartFile file : media_file) {
                if (!validation.validateCommonFileExtension(file.getOriginalFilename())) {
                    return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE_EXTENSION, null);
                }


                System.out.println("VALID FILE EXTENSION");

                if (validation.validateImageFileExtension(file.getOriginalFilename())) {
                    imageFileSize = file.getSize();
                    // validate size < 1MB
                    if (!(imageFileSize < ImageSize.getValue())) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.LARGE_FILE_SIZE, null);
                    }
                    subType = "images";
                    validImage = "IMAGES";
                } else if (validation.validateVideoFileExtension(file.getOriginalFilename())) {
                    videoFileSize = file.getSize();
                    // validate size < 5MB
                    if (!(videoFileSize < VideoSize.getValue())) {
                        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.LARGE_FILE_SIZE, null);
                    }
                    subType = "video";
                    validVideo = "VIDEOS";
                }
            }

            if (validImage != null && validVideo != null) {
                if (validImage.equals("IMAGES") && validVideo.equals("VIDEOS"))
                    System.out.println("Please upload either image or Video file at a time");
                return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.INVALID_FILE, null);
            }




            /*
             * Calling POST-SERVICE to FILE-SERVICE for uploading media file
             *
             * */

            Map<String, Object> mapImageFile = restTemplateUtil.accessMediaImageFile(id.toString(), media_file, subType, fileId, token, token_type, account_id);
            System.out.println("mapImageFile:" + mapImageFile);


            if ((int) mapImageFile.get("status") != SUCCESS_STATUS_CODE) {
                return mapImageFile;
            }

            Map<String, Object> responseMap = (Map<String, Object>) mapImageFile.get("response");
            System.out.println("responseMap:" + responseMap);
//
                    /*
                        UPDATED BY CHETNA JOSHI
                        * */

            if (subType.equals("images")) {
                imagePathSet.put("imagePath", (String) responseMap.get("imagePath"));
                imagePathSet.put("imageId", (String) responseMap.get("id"));

                System.out.println("Image file response:" + imagePathSet);
                System.out.println("");

                updateMap.put("media_image", imagePathSet);
                // postUser.setMedia_image( imagePathSet );
            } else {
                videoPathSet.put("videoPath", (String) responseMap.get("imagePath"));
                videoPathSet.put("videoId", (String) responseMap.get("id"));

                System.out.println("Video file response:" + videoPathSet);
                System.out.println("");

                updateMap.put("media_video", videoPathSet);
                // postUser.setMedia_video( videoPathSet );
            }


//
//
        } else {
            System.out.println("No files is uploaed");
        }


        long update_timestamp = EpochUtil.epoch();
        updateMap.put("modified_ts", update_timestamp);
        //postUser.setModified_ts(update_timestamp);

        // GIVING RESPONSE
        Map<String, Object> data = new HashMap<>(2);
        data.put("updated_post_text", post_text);
        data.put("modify_timeStamp", update_timestamp);
        //  data.put("m", update_timestamp);

        // CALLING SERVICE
        if (postService.updatePost(updateMap, id, poster_id, created_ts)) {

            Map<String, Object> searchServiceResponse = restTemplateUtil.addUpdatePost(updateMap, postUser.getId().toString(), true, token, account_id, token_type);

            System.out.println("");
            System.out.println("SEARCH SERVICE RESPONSE");
            System.out.println(searchServiceResponse);
            System.out.println("");

            return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.UPDATE_POST, updateMap);
        } else {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_UPDATE_POST, null);
        }
    }

    /*
     * Create API for soft delete post
     * When post will be deleted, status should be change( 1 -> 2)
     *
     * */
    @CrossOrigin
    @DeleteMapping(value = "/company/post/delete", produces = "application/json")
    public Map<String, Object> deletePost(@RequestParam(value = "id", required = true) UUID id,                        // Post id
                                          @RequestParam(value = "poster_id", required = true) String poster_id,        // Account id
                                          @RequestParam(value = "created_ts", required = false) long created_ts,        // created timestamp
                                          HttpServletRequest request) throws IOException {


        System.out.println("------------------");
        System.out.println("DELETE POST");
        System.out.println("");
        System.out.println("id:" + id);
        System.out.println("poster_id: " + poster_id);
        System.out.println("created_ts: " + created_ts);
        System.out.println("");
        System.out.println("--------------------");

        /*
         *  If user input invalid input or empty
         *
         * */
        if (poster_id == null || poster_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POSTER_ID, null);
        }

        /*
         * Get post data from post table is null or not
         *
         * */
        postUser = getPosterDetails(poster_id, id, created_ts);

        System.out.println("");
        System.out.println("Get post data" + postUser);
        System.out.println("");

        if (postUser == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        postUser.setStatus(Status.DELETED.getValue());

        // CALLING SERVICE
        if (postService.deletePost(postUser)) {
            return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.DELETE_POST, null);
        } else {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_DELETE_POST, null);
        }
    }

    // ------------------------------------------ PAGINATION FUNCTION --------------------------------------------------

    /*
     * Post will be paginated in following way:
     * recent 10 post will be sorted on descending order (created post timestamp wise) in other words, recent 10 post will be fetched per company/user.
     *
     * */
    @GetMapping(value = "/company/post/detail", produces = "application/json")
    public Map<String, Object> getPostDataInPage(@RequestParam(value = "poster_id") String poster_id,
                                                 @RequestParam(value = "created_ts", required = false) Long created_ts,
                                                 @RequestParam(value = "requester_id", required = false) String requester_id,
                                                 @RequestParam(value = "pageStage", required = false) String pageStage,
                                                 @RequestParam(value = "fetch_size", required = false, defaultValue = "10") int fetch_size,
                                                 @RequestParam(value = "is_poster", required = false, defaultValue = "0") int is_poster,
                                                 @RequestHeader("Authorization") String token,
                                                 @RequestParam("token_type") String token_type,
                                                 @RequestParam("account_id") String account_id,
                                                 HttpServletRequest request) throws IOException {

        System.out.println("-------------");
        System.out.println("");
        System.out.println("Running test Query");
        System.out.println("");
        System.out.println("requester_id : " + requester_id);
        System.out.println("poster_id : " + poster_id);
        System.out.println("cTs : " + created_ts);
        System.out.println("pageStage : " + pageStage);
        System.out.println("fetch_size : " + fetch_size);
        System.out.println("is_poster : " + is_poster);
        System.out.println("");
        System.out.println("-------------");

        String likeSet = null;
        Map<String, Object> nextKey = null;

        if (is_poster != 0 && is_poster != 1) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        if (requester_id != null && requester_id.equals("")) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, FAILED_POSTER_ID, null);
        }

        String localPageStage = null;
        if (pageStage != null && !pageStage.equals("")) {
            System.out.println("");
            System.out.println("PageStage is not null");
            System.out.println("");

            localPageStage = pageStage;
        }

        System.out.println("");
        System.out.println("LocalPageStage : " + localPageStage);
        System.out.println("");

        nextKey = postService.getPostUsingPagination(poster_id, created_ts, "DESC", localPageStage, fetch_size);

        if (nextKey == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        List<Post> postDetailList = (List<Post>) nextKey.get("data");
        List<Map<String, Object>> activePostList = new ArrayList<>(postDetailList.size());
        Set<UUID> postIds = new HashSet<UUID>(((List<Post>) nextKey.get("data")).size());

//        for (Post post : ((List<Post>) nextKey.get("data"))) {
//
//            System.out.println("post User");
//            System.out.println(post.getId());
//            System.out.println("");
//
//            if (post.getPost_privacy() != ONLY_ME.getValue()) {
//
//            }
//        }

        Set<String> posterIds = new HashSet<>(1);
        posterIds.add(poster_id);
        Map<String, Object> companyResponse = restTemplateUtil.accessCompanyDetails(posterIds, token, account_id, token_type);

        System.out.println("");
        System.out.println("RESPONSE OF COMPANY SERVICE");
        System.out.println(companyResponse);
        System.out.println("");

        if ((int) companyResponse.get("status") != SUCCESS_STATUS_CODE) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        List<Map<String, Object>> companiesDetail = (List<Map<String, Object>>) companyResponse.get("data");

        System.out.println("companiesDetail");
        System.out.println(companiesDetail);
        System.out.println("");

        String companyImage = null;
        String companyName = null;
        if (companiesDetail != null && !companiesDetail.isEmpty()) {

            Map<String, Object> companyImageMap = (Map<String, Object>) companiesDetail.get(0).get("company_logo");
            companyImage = companyImageMap != null ? companyImageMap.get("image_path").toString() : null;
            companyName = companiesDetail.get(0).get("company_name").toString();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        for (Post post : postDetailList) {

            if (is_poster == 0) {
                if (post.getStatus() == Status.ACTIVE.getValue() && post.getPost_privacy() != ONLY_ME.getValue()) {
                    postIds.add(post.getId());

                    Map<String, Object> postMap = objectMapper.convertValue(post, Map.class);
                    postMap.put("poster_name", companyName);
                    postMap.put("poster_image", companyImage);

                    activePostList.add(postMap);
                }
            } else {
                if (post.getStatus() == Status.ACTIVE.getValue()) {

                    postIds.add(post.getId());

                    Map<String, Object> postMap = objectMapper.convertValue(post, Map.class);
                    postMap.put("poster_name", companyName);
                    postMap.put("poster_image", companyImage);

                    activePostList.add(postMap);
                }
            }
        }

        if(activePostList.isEmpty()){
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        nextKey.replace("data", activePostList);

//        Map<Object, Object> commentIdData = new HashMap<>();
        Map<String, Object> response = new HashMap<>(2);

//        ObjectMapper mapper = new ObjectMapper();
//        List<String> PostIdList = new ArrayList<>();

        /*
            UPDATED BY CHETNA JOSHI
         */

//        List<PostComment> commentList = null;
//        List<PostComment> postComments = commentService.getPostComments(postIds);

        System.out.println("poster_id : " + poster_id);
        System.out.println("postIds : " + postIds);
        System.out.println("");

        if (!postIds.isEmpty()) {
            String liker_id = requester_id != null ? requester_id : poster_id;
            List<PostLike> postLikeList = postService.getPostLike(liker_id, postIds);

            System.out.println("postLikeList");
            System.out.println(postLikeList);

            Set<UUID> likedPostIds = new HashSet<>(postLikeList.size());
            if (!postLikeList.isEmpty()) {
                likedPostIds = postUtil.getLikedPost(postLikeList);

//            for (PostLike LikeDetails : postLikeList) {
//                likeSet = LikeDetails.getLiker_id();
//
//                System.out.println("likeset");
//                System.out.println(likeSet);
//
//                if (likeSet != null) {
//                    PostIdList.add(likeSet);
//                }

               /* if (likeSet != null) {
                    int sizeOfSet = likeSet.size();
                    likeSet.add(poster_id.toString());
                    int newSizeOfSet = likeSet.size();
                    if (sizeOfSet == newSizeOfSet) {
                        PostIdList.add(LikeDetails.getPost_id().toString());
                    }
                }*/
            }

            System.out.println("likedPostIds");
            System.out.println(likedPostIds);
            System.out.println("");

            response.put("like_post", likedPostIds);
        }


//        Map<String, List<PostComment>> commentMap = new HashMap<>();
//        List<PostComment> postCommentList=new ArrayList<>();
//        UUID postID = null;


//        if (postComments != null)
//        {
//
//            for (PostComment postComment : postComments) {
//
//
////                List<PostComment> comment = commentMap.get(postComment.getPost_id().toString());
////
////                System.out.println("");
////                System.out.println("COMMENT");
////                System.out.println(comment);
////                System.out.println("post id : " + postComment.getPost_id());
////                System.out.println("");
////                System.out.println("commentMap");
////                System.out.println(commentMap);
////                System.out.println("");
////
////                if (comment == null) {
////                    System.out.println("im in comment list null");
////                    List<PostComment> postCommentList = new ArrayList<>(1);
////                    postCommentList.add(postComment);

////                    commentMap.put(String.valueOf(postComment.getPost_id()), postCommentList);
////                } else {
////                    System.out.println("im in comment list not null");
////                    comment.add(postComment);
////                    commentMap.replace(String.valueOf(postComment.getPost_id()), comment);
////                }
//            }
//
//
//    }
//        List<Object> responseList = new ArrayList<>();
//        List<PostComment> postCommentList = new ArrayList<>();
//        if (commentMap != null && !commentMap.isEmpty()) {
//            responseList.add(commentMap);
//        }

        response.put("posts", nextKey);

//        response.put("comments", postComments != null && !postComments.isEmpty() ? postComments : postCommentList);


        return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.GET_POST_SUCCESS, response);
    }

    /*
     * CREATED BY CHETNA JOSHI
     * This api will be used for fetching post on the basis of following user's post by using follower_id
     *
     * */
    @GetMapping("/post/details")
    public Map<String, Object> getPostDetails(@RequestParam("follower_id") String followerId,    //followerId will be user or company id
                                              @RequestParam(value = "fetch_limit", required = false, defaultValue = "10") int fetchLimit,
                                              @RequestParam(value = "created_ts", required = false) Long createdTs,
                                              @RequestHeader("Authorization") String token,
                                              @RequestParam("token_type") String token_type,
                                              @RequestParam("account_id") String account_id) {

        System.out.println("");
        System.out.println("GET POST DETAILS BASED ON FOLLOW REQUEST");
        System.out.println("");
        System.out.println("followerId : " + followerId);
        System.out.println("fetchLimit : " + fetchLimit);
        System.out.println("pageState : " + createdTs);
        System.out.println("");

        if (followerId == null || followerId.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, FAILED_FOLLOWER_ID, null);
        }

        Map<String, Object> followDetailsMap = restTemplateUtil.getFollowDetails(followerId, token, account_id, token_type);

        System.out.println("");
        System.out.println("RESPONSE OF CONTACT NETWORK SERVICE");
        System.out.println(followDetailsMap);
        System.out.println("");

        if ((int) followDetailsMap.get("status") != SUCCESS_STATUS_CODE) {

            System.out.println("FOLLOW DETAIL REQUEST NOT SUCCESS");
            return followDetailsMap;
        }

        List<Map<String, Object>> followDetailList = (List<Map<String, Object>>) followDetailsMap.get("data");

        System.out.println("");
        System.out.println("FOLLOW DETAIL LIST");
        System.out.println(followDetailList);
        System.out.println("");
        System.out.println("FOLLOW DETAIL LIST COUNT");
        System.out.println(followDetailList.size());
        System.out.println("");

        Set<String> posterIds = new HashSet<>(followDetailList.size());
        for (Map<String, Object> followData : followDetailList) {

            if ((int) followData.get("status") == (FOLLOW)) {

                String posterId = (String) followData.get("following_id");
                posterIds.add(posterId);
            }
        }

        if (posterIds != null && !posterIds.isEmpty()) {

            Map<String, Object> responseMap = new HashMap<>(6);

            Map<String, Object> contactServiceResponse = restTemplateUtil.getConnectionLevel(followerId, posterIds, token, account_id, token_type);

            System.out.println("");
            System.out.println("CONTACT SERVICE RESPONSE");
            System.out.println(contactServiceResponse);
            System.out.println("");

            if ((int) contactServiceResponse.get("status") == SUCCESS_STATUS_CODE) {
                responseMap.put("level_detail", contactServiceResponse.get("data"));
            }

            boolean isLt = true;
            if (createdTs == null) {
                createdTs = EpochUtil.epoch();
                isLt = false;
            }
            List<Post> postDetailList = postService.getPostByPosterIds(posterIds, fetchLimit, createdTs, isLt);

            System.out.println("");
            System.out.println("POST OF FOLLOWING USER");
            System.out.println(postDetailList);
            System.out.println("");

            Set<UUID> postIds = new HashSet<>(postDetailList.size());
            Set<UUID> likedPostIds = new HashSet<>(postDetailList.size());
            List<Post> newPostDetailList = new ArrayList<>(postDetailList.size());
            posterIds.clear();

            System.out.println("is posterIds set is cleared:" + posterIds.isEmpty());
            if (!postDetailList.isEmpty()) {

                for (Post postUser : postDetailList) {
                    if (postUser.getPost_privacy() != ONLY_ME.getValue()) {
                        postIds.add(postUser.getId());
                        newPostDetailList.add(postUser);
                        posterIds.add(postUser.getPoster_id());
                    }
                }

                List<PostLike> postLikes = postService.getPostLike(followerId, postIds);

                System.out.println("postLikes");
                System.out.println(postLikes);

                if (postLikes != null && !postLikes.isEmpty()) {
                    likedPostIds = postUtil.getLikedPost(postLikes);
                }

                Map<String, Object> companyResponse = restTemplateUtil.accessCompanyDetails(posterIds, token, account_id, token_type);

                System.out.println("");
                System.out.println("RESPONSE OF COMPANY SERVICE");
                System.out.println(companyResponse);
                System.out.println("");

                if ((int) companyResponse.get("status") != SUCCESS_STATUS_CODE) {
                    return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
                }

                List<Map<String, Object>> companiesDetail = (List<Map<String, Object>>) companyResponse.get("data");

                System.out.println("companiesDetail");
                System.out.println(companiesDetail);
                System.out.println("");

                Map<String, Map<String, Object>> companiesImage = null;
                if (companiesDetail != null && !companiesDetail.isEmpty()) {

                    companiesImage = new HashMap<>(companiesDetail.size());

                    for (Map<String, Object> companyDetail : companiesDetail) {

                        Map<String, Object> companyImage = (Map<String, Object>) companyDetail.get("company_logo");

                        Map<String, Object> companyDataMap = new HashMap<>(2);
                        companyDataMap.put("image", companyImage != null ? companyImage.get("image_path") : null);
                        companyDataMap.put("name", companyDetail.get("company_name"));

                        companiesImage.put(companyDetail.get("company_id").toString(), companyDataMap);
                    }
                }


//                Long nextCreationTs = postDetailList.get((postDetailList.size() - 1)).getCreated_ts();
                Long nextCreationTs = newPostDetailList.get((newPostDetailList.size() - 1)).getCreated_ts();

                responseMap.put("posts", newPostDetailList);
                responseMap.put("poster_image", companiesImage);
                responseMap.put("nextCreatedTs", nextCreationTs);
                responseMap.put("isNext", true);
                responseMap.put("like_post", likedPostIds);

                if (postDetailList.size() < fetchLimit) {
                    responseMap.put("isNext", false);
//                   responseMap.remove("nextCreatedTs");
                }
                return httpResponse.getResponse(SUCCESS_STATUS_CODE, DATA_FOUND, responseMap);
            }
        }
        return httpResponse.getResponse(CONTENT_NOT_FOUND_STATUS_CODE, FAILED_POST_DATA, null);
    }

    // --------------------------------------------- LIKE ON POST ------------------------------------------------------

    /*
     *  In like post api liker(user/company) can like any post.
     *  Once post like post by liker & again he would like to post, post shell be dislike.
     *  In post_like table data will be set and remove.
     *
     * */

    /*
     * Method create for post like
     *
     * */
    @PostMapping(value = "/company/post/like")
    public Map<String, Object> likePost(@RequestParam(value = "id", required = true) UUID id,                           // Post id
                                        @RequestParam(value = "poster_id", required = false) String poster_id,           // Account id
                                        @RequestParam(value = "created_ts", required = false) Long created_ts,        // created timestamp
                                        @RequestParam(value = "liker_id", required = true) String liker_id,             // liked post by liker
                                        @RequestParam(value = "liker_type", required = true) String liker_type,         // Type of liker(USER/COMPANY) liked the post
                                        @RequestHeader("Authorization") String token,
                                        @RequestParam("token_type") String token_type,
                                        @RequestParam("account_id") String account_id,
                                        HttpServletRequest request) throws IOException {

        System.out.println("----------------------");
        System.out.println("");
        System.out.println("LIKE ON POST");
        System.out.println("");
        System.out.println("id:" + id);
        System.out.println("poster_id:" + poster_id);
        System.out.println("created_ts: " + created_ts);
        System.out.println("liker_id:" + liker_id);
        System.out.println("liker_type:" + liker_type);
        System.out.println("");
        System.out.println("-----------------------");

        String postLikerId = null;
        UUID postLikeUniquePostId = null;
        long like_timestamp = 0L;
        boolean isInsert = true;


        // ---------------------------------------------- Validate filed's ---------------------------------------------

        /*
         *  If user input invalid input or empty
         *
         * */
        if (poster_id == null || poster_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, FAILED_POSTER_ID, null);
        }

        if (liker_id == null || liker_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, FAILED_LIKER_ID, null);
        }

        if (!(LikerType.USER.name().equals(liker_type)) && !(LikerType.COMPANY.name().equals(liker_type)) || liker_type == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_LIKER_TYPE, null);

        }

        /*
         * Get post data from post table and this post will be liked by liker
         *
         * */
        postUser = getPosterDetails(poster_id, id, created_ts);

        System.out.println("");
        System.out.println("Get post data" + postUser);
        System.out.println("");

        if (postUser == null) {
            return httpResponse.getResponse(HttpConstants.BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        // CREATE POST LIKE BEAN OBJECT
        postLike = new PostLike();

        /*
         * Check PostLike object id null or not.
         *
         * */
        List<PostLike> postLikeResult = postService.getExistingLikeId(liker_id, postUser.getId());

        if (!(CollectionUtils.isEmpty(postLikeResult))) {
            for (PostLike postListData : postLikeResult) {
                if (liker_id.equals(postListData.getLiker_id())) {
                    postLikerId = postListData.getLiker_id();
                    postLikeUniquePostId = postListData.getPost_id();
                    break;
                }
            }
        }

        if (postLikeUniquePostId != null) {
            postUser.setTotal_likes(postUser.getTotal_likes() - 1);
            isInsert = false;
        } else {
            UUID postLikeId = UUID.randomUUID();
            postLike.setLiker_id(liker_id);
            postLike.setId(postLikeId);
            like_timestamp = EpochUtil.epoch();
            postLike.setLike_ts(like_timestamp);
            postLike.setPost_id(postUser.getId());
            postLike.setPoster_id(poster_id);

            // Setting value of Type Liker Type
            if (LikerType.USER.name().equals(liker_type)) {
                postLike.setLiker_type(LikerType.USER.getValue());
            } else {
                postLike.setLiker_type(LikerType.COMPANY.getValue());
            }
            postUser.setTotal_likes(postUser.getTotal_likes() + 1);
        }

        if (isInsert) {

            postService.addLikePost(postLike);
            postService.updatePostLikes(postUser);
            List<PostLike> postUserList = new ArrayList<>();
            postUserList.add(postLike);

            /*
             *   UPDATED BY CHETNA JOSHI
             */

            //sending notification
            String posterEmailId = null;
            String likerName = null;
            String posterUserId = null;  //if poster is a company then we need user's id to get fbase token to send push notification.
            Map<String, Object> likerImage = new HashMap<>(2);
            Set<String> companyIds = new HashSet<>(2);
            Set<String> userIds = new HashSet<>(2);

            if (postUser.getPoster_type() == PosterType.COMPANY.getValue()) {
                companyIds.add(poster_id);
            } else {
                userIds.add(poster_id);
            }

            if (postLike.getLiker_type() == LikerType.COMPANY.getValue()) {
                companyIds.add(liker_id);
            } else {
                userIds.add(liker_id);
            }

            if (!userIds.isEmpty()) {

                //calling account service for user details

                Set<String> fields = new HashSet<>(2);
                fields.add("first_name");
                fields.add("last_name");
                fields.add("avatar_image");
                fields.add("email_id");

                Map<String, Object> userDetailsResponse = restTemplateUtil.getUserDetails(userIds, fields, token, account_id, token_type);

                System.out.println("");
                System.out.println("userDetailsResponse");
                System.out.println(userDetailsResponse);
                System.out.println("");

                if ((int) userDetailsResponse.get("status") == SUCCESS_STATUS_CODE) {

                    List<Map<String, Object>> data = (List<Map<String, Object>>) userDetailsResponse.get("data");

                    for (Map<String, Object> user : data) {

                        String userId = user.get("account_id").toString();

                        if (poster_id.equals(userId)) {
                            posterEmailId = user.get("email_id").toString();
                            posterUserId = userId;
                        }

                        if (liker_id.equals(userId)) {

                            String name = user.get("first_name").toString() + " " + user.get("last_name");
                            Map<String, Object> image = user.get("avatar_image") != null ? (Map<String, Object>) user.get("avatar_image") : null;

                            likerName = name;
                            likerImage = image;
                        }
                    }
                }
            }

            if (!companyIds.isEmpty()) {
                //calling company service for company details
                Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, token_type, account_id);

                System.out.println("");
                System.out.println("companyDetailResponse");
                System.out.println(companyDetailResponse);
                System.out.println("");

                if ((int) companyDetailResponse.get("status") == SUCCESS_STATUS_CODE) {

                    List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

                    for (Map<String, Object> companyData : data) {

                        String companyId = companyData.get("company_id").toString();

                        if (poster_id.equals(companyId)) {
                            posterEmailId = companyData.get("company_email").toString();
                            posterUserId = companyData.get("account_id").toString();
                        }

                        if (liker_id.equals(companyId)) {

                            Map<String, Object> image = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;

                            likerName = companyData.get("company_name").toString();
                            likerImage = image;
                        }
                    }
                }

            }

            System.out.println("");
            System.out.println("posterEmailId: " + posterEmailId);
            System.out.println("posterUserId : " + posterUserId);
            System.out.println("likerName : " + likerName);
            System.out.println("likerImage : " + likerImage);
            System.out.println("");

            if (likerName != null && posterEmailId != null && !poster_id.equals(liker_id)) {
                // sending notification
                Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
                Map<String, Object> payloadData = new HashMap<>(4);

                payloadData.put("liker_name", likerName);
                payloadData.put("email_id", posterEmailId);
//                payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

                templatePayload.put(posterUserId, payloadData);

                Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
                Map<String, Object> bgPayloadData = new HashMap<>(4);

                bgPayloadData.put("poster_id", poster_id);
                bgPayloadData.put("poster_type", String.valueOf(postUser.getPoster_type()));
                bgPayloadData.put("liker_id", liker_id);
                bgPayloadData.put("liker_type", String.valueOf(postLike.getLiker_type()));
                bgPayloadData.put("post_id", id.toString());
                bgPayloadData.put("like_id", postLike.getId().toString());
                bgPayloadData.put("name", likerName);

                if (likerImage != null && !likerImage.isEmpty()) {
                    bgPayloadData.put("image", likerImage.get("image_path"));
                }

                bgPayloadData.put("type", "like"); //this is used by client to show icon for the notification
                backgroundPayload.put(posterUserId, bgPayloadData);

                Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

                Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(poster_id,
                        "", "", templatePayload, backgroundPayload, is_batch, NEW_LIKE_ADDED);

                System.out.println("");
                System.out.println("NOTIFICATION SENT RESPONSE");
                System.out.println("");
                System.out.println(sendNotificationResponse);
                System.out.println("");
            }

            return httpResponse.getResponse(SUCCESS_STATUS_CODE, LIKE_POST, postUserList);
        } else {
            boolean deletionStatus = postService.deletePostLike(postLikeUniquePostId, postLikerId);
            postService.updatePostLikes(postUser);
            return httpResponse.getResponse(SUCCESS_STATUS_CODE, UNLIKE_POST, null);
        }

    }

    // ---------------------------------------------- COMMENT ON POST --------------------------------------------------

    @PostMapping(value = "/company/post/comments", produces = "application/json")
    public Map<String, Object> addCommentOnPost(@RequestParam(value = "id", required = true) UUID id,                        // Post id
                                                @RequestParam(value = "poster_id") String poster_id,                         // Account id
                                                @RequestParam(value = "created_ts", required = false) long created_ts,        // created timestamp
                                                @RequestParam(value = "text") String text,                                   // Comment on post
                                                @RequestParam(value = "commenter_id") String commenter_id,                  // Who's comment on your post
                                                @RequestParam(value = "commenter_type") String commenter_type,               // Type of commenter is it USER OR COMPANY
                                                @RequestHeader("Authorization") String token,
                                                @RequestParam("token_type") String token_type,
                                                @RequestParam("account_id") String account_id,
                                                HttpServletRequest request) throws IOException {

        System.out.println("----------------------------");
        System.out.println("");
        System.out.println("ADD COMMENT ON POST");
        System.out.println("");
        System.out.println("Poster id:" + poster_id);
        System.out.println("id:" + id);
        System.out.println("created_ts: " + created_ts);
        System.out.println("text:" + text);
        System.out.println("commenter_id:" + commenter_id);
        System.out.println("commenter_type:" + commenter_type);
        System.out.println("");
        System.out.println("----------------------------");

        // ---------------------------------------------- Validate filed's ---------------------------------------------

        /*
         *  If user input invalid input or empty
         *
         * */
        if (poster_id == null || poster_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, FAILED_POSTER_ID, null);
        }

        if (commenter_id == null || commenter_id.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENTER_ID, null);
        }

        /*
         * Check commenter type is USER/COMPANY
         *
         * */
        if (!(CommenterType.USER.name()).equals(commenter_type) && !(CommenterType.COMPANY.name()).equals(commenter_type)) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENTER_TYPE, null);
        }

        if (text == null || text.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENT_TEXT, null);
        }

        /*
         * Get post data from post table is null or not
         *
         * */
        postUser = getPosterDetails(poster_id, id, created_ts);

        System.out.println("");
        System.out.println("Get post data" + postUser);
        System.out.println("");

        if (postUser == null) {
            return httpResponse.getResponse(HttpConstants.BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        UUID commentPostId = UUID.randomUUID();

        // CREATE POST COMMENT BEAN OBJECT
        postComment = new PostComment();

        // SET VALUE ON BEAN OBJECT
        postComment.setPost_id(postUser.getId());

        // CREATE TIMESTAMP
        long create_timestamp = EpochUtil.epoch();
        postComment.setCreated_ts(create_timestamp);

        postComment.setId(commentPostId);
        postComment.setCommenter_id(commenter_id);

        // Setting value of Type Commenter Type
        if (CommenterType.USER.name().equals(commenter_type)) {
            postComment.setCommenter_type(PosterType.USER.getValue());
        } else {
            postComment.setCommenter_type(PosterType.COMPANY.getValue());
        }

        // Modified TimeStamp
        long update_timestamp = 0L;
        postComment.setModified_ts(update_timestamp);

        postComment.setPoster_id(poster_id);
        postComment.setText(text);

        // Default value of total likes will zero
        postComment.setTotal_likes(0);

        /*
         * Calling to post comment service to add comment
         *
         * */
        commentService.addCommentOnPost(postComment);
        List<PostComment> postCommentsList = new ArrayList<>();
        postCommentsList.add(postComment);
        postUser.setTotal_comments(postUser.getTotal_comments() + 1);

        // CALLING SERVICE
        postService.updatePostLikes(postUser);

        //sending notification
        String posterEmailId = null;
        String commenterName = null;
        String posterUserId = null;  //if poster is a company then we need user's id to get fbase token to send push notification.
        Map<String, Object> commenterImage = new HashMap<>(2);
        Set<String> companyIds = new HashSet<>(2);
        Set<String> userIds = new HashSet<>(2);

        if (postUser.getPoster_type() == PosterType.COMPANY.getValue()) {
            companyIds.add(poster_id);
        } else {
            userIds.add(poster_id);
        }

        if (postComment.getCommenter_type() == CommenterType.COMPANY.getValue()) {
            companyIds.add(commenter_id);
        } else {
            userIds.add(commenter_id);
        }

        if (!userIds.isEmpty()) {

            //calling account service for user details

            Set<String> fields = new HashSet<>(2);
            fields.add("first_name");
            fields.add("last_name");
            fields.add("avatar_image");
            fields.add("email_id");

            Map<String, Object> userDetailsResponse = restTemplateUtil.getUserDetails(userIds, fields, token, account_id, token_type);

            System.out.println("");
            System.out.println("userDetailsResponse");
            System.out.println(userDetailsResponse);
            System.out.println("");

            if ((int) userDetailsResponse.get("status") == SUCCESS_STATUS_CODE) {

                List<Map<String, Object>> data = (List<Map<String, Object>>) userDetailsResponse.get("data");

                for (Map<String, Object> user : data) {

                    String userId = user.get("account_id").toString();

                    if (poster_id.equals(userId)) {
                        posterEmailId = user.get("email_id").toString();
                        posterUserId = userId;
                    }

                    if (commenter_id.equals(userId)) {

                        String name = user.get("first_name").toString() + " " + user.get("last_name");
                        Map<String, Object> image = user.get("avatar_image") != null ? (Map<String, Object>) user.get("avatar_image") : null;

                        commenterName = name;
                        commenterImage = image;
                    }
                }
            }
        }

        if (!companyIds.isEmpty()) {
            //calling company service for company details
            Map<String, Object> companyDetailResponse = restTemplateUtil.getCompanyDetail(companyIds, token, token_type, account_id);

            System.out.println("");
            System.out.println("companyDetailResponse");
            System.out.println(companyDetailResponse);
            System.out.println("");

            if ((int) companyDetailResponse.get("status") == SUCCESS_STATUS_CODE) {

                List<Map<String, Object>> data = (List<Map<String, Object>>) companyDetailResponse.get("data");

                for (Map<String, Object> companyData : data) {

                    String companyId = companyData.get("company_id").toString();

                    if (poster_id.equals(companyId)) {
                        posterEmailId = companyData.get("company_email").toString();
                        posterUserId = companyData.get("account_id").toString();
                    }

                    if (commenter_id.equals(companyId)) {

                        Map<String, Object> image = companyData.get("company_logo") != null ? (Map<String, Object>) companyData.get("company_logo") : null;

                        commenterName = companyData.get("company_name").toString();
                        commenterImage = image;
                    }
                }
            }
        }

        System.out.println("");
        System.out.println("posterEmailId: " + posterEmailId);
        System.out.println("posterUserId : " + posterUserId);
        System.out.println("commenterName : " + commenterName);
        System.out.println("commenterImage : " + commenterImage);
        System.out.println("");

        if (posterEmailId != null && commenterName != null && !poster_id.equals(commenter_id)) {
            // sending notification
            Map<String, Map<String, Object>> templatePayload = new HashMap<>(3);
            Map<String, Object> payloadData = new HashMap<>(4);

            payloadData.put("commenter_name", commenterName);
            payloadData.put("email_id", posterEmailId);
//            payloadData.put("email_id", "t.chetnajoshi889@gmail.com");

            templatePayload.put(posterUserId, payloadData);

            Map<String, Map<String, Object>> backgroundPayload = new HashMap<>(3);
            Map<String, Object> bgPayloadData = new HashMap<>(4);

            bgPayloadData.put("poster_id", poster_id);
            bgPayloadData.put("poster_type", String.valueOf(postUser.getPoster_type()));
            bgPayloadData.put("commenter_id", commenter_id);
            bgPayloadData.put("commenter_type", String.valueOf(postComment.getCommenter_type()));
            bgPayloadData.put("post_id", id.toString());
            bgPayloadData.put("comment_id", postComment.getId().toString());
            bgPayloadData.put("name", commenterName);

            if (commenterImage != null && !commenterImage.isEmpty()) {
                bgPayloadData.put("image", commenterImage.get("image_path"));
            }

            bgPayloadData.put("type", "comment"); //this is used by client to show icon for the notification
            backgroundPayload.put(posterUserId, bgPayloadData);

            Integer is_batch = 0;  //This variable is used for email in sending multiple email at one time to users.

            Map<String, Object> sendNotificationResponse = restTemplateUtil.sendNotification(poster_id,
                    "", "", templatePayload, backgroundPayload, is_batch, NEW_COMMENT_ADDED);

            System.out.println("");
            System.out.println("NOTIFICATION SENT RESPONSE");
            System.out.println("");
            System.out.println(sendNotificationResponse);
            System.out.println("");
        }

        // Giving response to API caller's
        Map<String, Object> data = new HashMap<>(2);
        data.put("post_id", id);
        data.put("created_ts", create_timestamp);
        data.put("id", commentPostId);
        data.put("commenter_id", commenter_id);
        data.put("commenter_type", commenter_type);
        data.put("modified_ts", update_timestamp);
        data.put("poster_id", poster_id);

        return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.COMMENT_POST, data);
    }

    /*
     * Create API for get comment
     * How many comment of one post
     *
     * */
    @GetMapping(value = "/company/post/getPostComments", produces = "application/json")
    public Map<String, Object> getCommentsOnPost(@RequestParam(value = "post_id") UUID post_id,                         // post id
                                                 // created time stamp
                                                 HttpServletRequest request) throws IOException {

        System.out.println("-----------------------");
        System.out.println("");
        System.out.println("GET COMMENT");
        System.out.println("post_id:" + post_id);
        //  System.out.println("id: " + id);
        //System.out.println("created_ts: " + created_ts);
        System.out.println("");
        System.out.println("-----------------------");

        List<PostComment> postCommentList = commentService.getPostCommentList(post_id);

        if (CollectionUtils.isEmpty(postCommentList)) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        List<Map<String, Object>> mapArrayList = new ArrayList<>(10);

        for (PostComment postComment : postCommentList) {
            Map<String, Object> commentMap = new HashMap<>(5);
            commentMap.put("post_id", postComment.getPost_id());
            commentMap.put("created_ts", postComment.getCreated_ts());
            commentMap.put("id", postComment.getId());
            commentMap.put("text", postComment.getText());
            commentMap.put("commenter_type", postComment.getCommenter_type());
            commentMap.put("commenter_id", postComment.getCommenter_id());
            commentMap.put("poster_id", postComment.getPoster_id());
            commentMap.put("created_ts", postComment.getCreated_ts());

            mapArrayList.add(commentMap);

            System.out.println("");
            System.out.println("Get post comments");
            System.out.println(mapArrayList);
            System.out.println("");
        }
        return (mapArrayList != null && mapArrayList.size() > 0) ? httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.GET_COMMENT_SUCCESS, mapArrayList) : httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENT_POST_DATA, null);
    }

    // ------------------------------------------- GET ALL COMMENT ON PARTICULAR POST ----------------------------------

    /*
     * Post comment will be paginated in following way:
     * recent 10 post comment will be sorted on ascending order (created post timestamp wise) in other words, recent 10 post will be fetched per company/user.
     *
     * */
    @GetMapping(value = "/company/post_comment/detail", produces = "application/json")
    public Map<String, Object> getPostCommentDataInPage(@RequestParam(value = "post_id", required = true) UUID post_id,
                                                        @RequestParam(value = "pageStage", required = false) String pageStage,
                                                        @RequestParam(value = "fetch_size", required = false, defaultValue = "10") int fetch_size,
                                                        @RequestHeader("Authorization") String token,
                                                        @RequestParam("token_type") String token_type,
                                                        @RequestParam("account_id") String account_id,
                                                        HttpServletRequest request) throws IOException {

        System.out.println("-------------");
        System.out.println("");
        System.out.println("Get comment of post");
        System.out.println("");
        System.out.println("post_id: " + post_id);
        System.out.println("pageStage: " + pageStage);
        System.out.println("fetch_size: " + fetch_size);
        System.out.println("");
        System.out.println("-------------");

        Set<String> userSet = new HashSet<>();
        Set<String> companySet = new HashSet<>();
//        Map<String, Object> nextKey = null;

        String localPageStage = null;
        if (pageStage != null && !pageStage.equals("")) {
            System.out.println("");
            System.out.println("PageStage is not null");
            System.out.println("");

            localPageStage = pageStage;
        }
        System.out.println("");
        System.out.println("LocalPageStage : " + localPageStage);
        System.out.println("");

        //--------------updated----by----chetna-----joshi----------------------------------------

        Map<String, Object> paginatedCommentDetails = commentService.getPostCommentUsingPagination(post_id, "DESC", localPageStage, fetch_size);

        System.out.println("");
        System.out.println("paginatedCommentDetails");
        System.out.println(paginatedCommentDetails);
        System.out.println("");

        if (paginatedCommentDetails == null || paginatedCommentDetails.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        List<PostComment> postComments = (List<PostComment>) paginatedCommentDetails.get("data");

        System.out.println("postComments");
        System.out.println(postComments);
        System.out.println("");

//        Map<String, Map<String, Object>> commentData = new HashMap<>(postComments.size());


//        ObjectMapper objectMapper = new ObjectMapper();
        for (PostComment postComment : postComments) {

//            Map<String, Object> postCommentMap = objectMapper.convertValue(postComment, Map.class);
//            commentData.put(postComment.getCommenter_id(), postCommentMap);

            if (postComment.getCommenter_type() == 1) {
                userSet.add(postComment.getCommenter_id());
            } else {
                companySet.add(postComment.getCommenter_id());
            }
        }

//        for (Map.Entry<String, Object> commentData : nextKey.entrySet()) {
//            System.out.println(commentData.getKey() + " " + commentData.getValue());
//
//            if (commentData.getKey().equals("data")) {
//                List<PostComment> postCommentList = (List<PostComment>) commentData.getValue();
//                for (PostComment postComment : postCommentList) {
//
//                    if (postComment.getCommenter_type() == 1) {
//                        userSet.add(postComment.getCommenter_id());
//                    } else {
//                        companySet.add(postComment.getCommenter_id());
//                    }
//                }
//            }
//        }

        /*
         * Create map for merge data
         * */
//        Map<String, Object> commonMap = new HashMap<>();
        Map<String, Object> commenter_detail = new HashMap<>();
//        List<Map<String, Object>> finalCommentDetails = new ArrayList<>(postComments.size());

        /*
         * Calling company service
         *
         * */
        if (companySet.size() > 0) {
            Map<String, Object> companyResponse = restTemplateUtil.accessCompanyDetails(companySet, token, account_id, token_type);

            System.out.println("");
            System.out.println("RESPONSE OF COMPANY SERVICE");
            System.out.println(companyResponse);
            System.out.println("");

            if ((int) companyResponse.get("status") != SUCCESS_STATUS_CODE) {
                return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
            }

            List<Map<String, Object>> companiesDetail = (List<Map<String, Object>>) companyResponse.get("data");

            System.out.println("companiesDetail");
            System.out.println(companiesDetail);
            System.out.println("");

            if (companiesDetail != null && !companiesDetail.isEmpty()) {

                for (Map<String, Object> companyDetail : companiesDetail) {

                    Map<String, Object> data = new HashMap<>(2);
                    data.put("name", companyDetail.get("company_name"));
                    data.put("image", companyDetail.get("company_logo"));

                    commenter_detail.put(companyDetail.get("company_id").toString(), data);
//                    data.putAll(commentData.get(companyDetail.get("company_id")));

//                    finalCommentDetails.add(data);
                }
            }

//            System.out.println("get company service data: " + companyDetails);
//            for (Map.Entry<String, Object> companyData : companyDetails.entrySet()) {
//                System.out.println(companyData.getKey() + " " + companyData.getValue());
//
//                if (companyData.getKey().equals("response")) {
//                    LinkedHashMap<String, String> linkedHashMap = (LinkedHashMap<String, String>) companyData.getValue();
//                    Set<String> keys = linkedHashMap.keySet();
//
//
//                    for (String fileKey : keys) {
//                        if (fileKey.equals("company_name") || fileKey.equals("company_logo")) {
//                            commonMap.put(fileKey, linkedHashMap.get(fileKey));
//                            System.out.println("company image and name: " + commonMap);
//                        }
//                    }
//                }
//            }
        }

        /*
         * Calling Account service
         *
         * */
        if (userSet.size() > 0) {
            Map<String, Object> accountResponse = restTemplateUtil.accessAccountDetails(userSet, token, token_type, account_id);

            System.out.println("");
            System.out.println("ACCOUNT SERVICE RESPONSE");
            System.out.println(accountResponse);
            System.out.println("");

            if ((int) accountResponse.get("status") != SUCCESS_STATUS_CODE) {
                return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
            }

            List<Map<String, Object>> accountDetails = (List<Map<String, Object>>) accountResponse.get("data");

            if (accountDetails != null && !accountDetails.isEmpty()) {

                for (Map<String, Object> accountDetail : accountDetails) {

                    Map<String, Object> data = new HashMap<>(2);
                    data.put("name", (accountDetail.get("first_name").toString() + " " + accountDetail.get("last_name")));
                    data.put("image", accountDetail.get("avatar_image"));
                    commenter_detail.put(accountDetail.get("account_id").toString(), data);
//                    data.putAll(commentData.get(accountDetail.get("account_id")));

//                    finalCommentDetails.add(data);
                }
            }

//            System.out.println("get account service data:" + accountDetails);
//            for (Map.Entry<String, Object> accountData : accountDetails.entrySet()) {
//                System.out.println(accountData.getKey() + " " + accountData.getValue());
//
//                if (accountData.getKey().equals("response")) {
//                    LinkedHashMap<String, String> linkedHashMap = (LinkedHashMap<String, String>) accountData.getValue();
//                    Set<String> keys = linkedHashMap.keySet();
//
//                    for (String fileKey : keys) {
//                        if (fileKey.equals("account_name") || fileKey.equals("account_logo")) {
//                            commonMap.put(fileKey, linkedHashMap.get(fileKey));
//                            System.out.println("account image and name: " + commonMap);
//                        }
//                    }
//                }
//            }
        }

//        if (finalCommentDetails != null && !finalCommentDetails.isEmpty()) {

        Map<String, Object> responseMap = new HashMap<>(3);

        responseMap.put("comments", postComments);
        responseMap.put("commenter_details", commenter_detail);
        responseMap.put("hasMoreRecords", paginatedCommentDetails.get("hasMoreRecords"));
        responseMap.put("nextPage", paginatedCommentDetails.get("nextPage"));

        return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.GET_COMMENT_SUCCESS, responseMap);
//        }

//        return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);

//        return (commonMap != null && commonMap.size() > 0) ? httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.GET_COMMENT_SUCCESS, commonMap) :
//                httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
    }

    /*
     * Create API for update comment
     *
     * */
    @CrossOrigin
    @PutMapping(value = "/company/post/updatePostComment", produces = "application/json")
    public Map<String, Object> updatePostCommentText(@RequestParam(value = "post_id") UUID post_id,                 // post id
                                                     @RequestParam(value = "id") UUID id,                           // unique id in comment table
                                                     @RequestParam(value = "created_ts") long created_ts,           // generate time
                                                     @RequestParam(value = "text") String text,                     // update comment text
                                                     HttpServletRequest request) throws IOException {

        System.out.println("------------------------");
        System.out.println("");
        System.out.println("UPDATE POST");
        System.out.println("post_id:" + post_id);
        System.out.println("id:" + id);
        System.out.println("created_ts:" + created_ts);
        System.out.println("text:" + text);
        System.out.println("");
        System.out.println("------------------------");

        // ---------------------------------------------- Validate filed's ---------------------------------------------

        /*
         *  If user input invalid input or empty
         *
         * */
        if (text == null || text.isEmpty()) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENT_TEXT, null);
        }

        /*
         * Get post comment data
         *
         * */
        postComment = commentService.getPostCommentDetail(post_id, id, created_ts);

        System.out.println("");
        System.out.println("Get post comment" + postComment);
        System.out.println("");

        if (postComment == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENT_POST_DATA, null);
        }

        // UPDATE FILED TO BE SET
        postComment.setText(text);
        long update_timestamp = EpochUtil.epoch();
        postComment.setModified_ts(update_timestamp);

        // CALLING SERVICE
        if (commentService.updatePostComment(postComment)) {

            // GIVING RESPONSE
            Map<String, Object> data = new HashMap<>(2);
            data.put("updated_text", text);
            data.put("modified_timestamp", update_timestamp);

            return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.UPDATE_COMMENT, data);
        } else {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_UPDATE_COMMENT, null);
        }

    }

    /*
     *  Delete post comment
     *
     * */
    //===========UPDATED BY CHETNA JOSHI================
    @CrossOrigin
    @DeleteMapping(value = "/company/post/deletePostComment", produces = "application/json")
    public Map<String, Object> deleteCommentOnPost(@RequestParam(value = "post_id") UUID post_id,                       // post id
                                                   @RequestParam(value = "id") UUID id,                                 // unique id in comment table
                                                   @RequestParam(value = "poster_id") String poster_id,                                 // unique id in comment table
                                                   @RequestParam(value = "created_ts") long created_ts,                 //comment generate time
                                                   @RequestParam(value = "post_created_ts") long post_created_ts,                 // post generate time
                                                   HttpServletRequest request) throws IOException {

        System.out.println("------------------------");
        System.out.println("");
        System.out.println("DELETE COMMENT ON POST");
        System.out.println("post_id:" + post_id);
        System.out.println("id:" + id);
        System.out.println("created_ts:" + created_ts);
        System.out.println("post_created_ts : " + post_created_ts);
        System.out.println("poster_id : " + poster_id);
        System.out.println("");
        System.out.println("------------------------");

        postUser = getPosterDetails(poster_id, post_id, post_created_ts);

        System.out.println("");
        System.out.println("Get post data" + postUser);
        System.out.println("");

        if (postUser == null) {
            return httpResponse.getResponse(HttpConstants.BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_POST_DATA, null);
        }

        boolean deletionStatus = commentService.deleteCommentFromPost(post_id, id, created_ts);

        System.out.println("");
        System.out.println("DELETE COMMENT FROM POST");
        System.out.println(deletionStatus);
        System.out.println("");

        if (deletionStatus) {
            postUser.setTotal_comments(postUser.getTotal_comments() - 1);
            postService.updatePostLikes(postUser);
            return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.DELETE_COMMENT, null);
        } else {
            return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.FAILED_TO_DELETE_COMMENT, null);
        }
    }

    // ------------------------------------------------ COMMENT LIKE ---------------------------------------------------
    /*
     * Like on comment
     *
     * */
    @PostMapping(value = "/company/comment/like")
    public Map<String, Object> likeOnComment(@RequestParam(value = "id") UUID id,
                                             @RequestParam(value = "post_id") UUID post_id,
                                             @RequestParam(value = "created_ts") long created_ts,
                                             // @RequestParam(value = "liker_id", required = true) String liker_id,
                                             // @RequestParam(value = "liker_type", required = true) String liker_type,
                                             HttpServletRequest request) throws IOException {

        System.out.println("----------------------");
        System.out.println("");
        System.out.println("LIKE ON COMMENT");
        //  System.out.println("liker_id:" + liker_id);
        // System.out.println("liker_type:" + liker_type);
        System.out.println("");
        System.out.println("-----------------------");

        // ---------------------------------------------- Validate filed's ---------------------------------------------

        /*
         * Get post comment data
         *
         * */
        postComment = commentService.getTotalLikeOnComment(post_id, id, created_ts);

        if (postComment == null) {
            return httpResponse.getResponse(BAD_REQUEST_STATUS_CODE, HttpConstants.FAILED_COMMENT_POST_DATA, null);
        }

        postComment.setTotal_likes(postComment.getTotal_likes() + 1);
        commentService.updatePostCommentLike(postComment);
        return httpResponse.getResponse(SUCCESS_STATUS_CODE, HttpConstants.LIKE_ON_COMMENT, null);
    }

    /*
     * Create generic method for all the CRUD operation
     *
     * */
    private Post getPosterDetails(String poster_id, UUID id, long created_ts) {
        return postService.getPostId(poster_id, id, created_ts);
    }
}
