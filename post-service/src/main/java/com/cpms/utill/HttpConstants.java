package com.cpms.utill;

public class HttpConstants {

    //  HTTP RESPONSE STATUS
    public static final String SUCCESS_STATUS = "success";
    public static final String ERROR_STATUS = "Error";

    // HTTP RESPONSE STATUS CODE
    public static int SUCCESS_STATUS_CODE = 200;
    public static int ERROR_STATUS_CODE = 201;
    public static int PIN_VALID_USER_CODE = 202;
    public static int INTERNAL_SERVER_ERROR_STATUS_CODE = 500;
    public static int CONTENT_NOT_FOUND_STATUS_CODE = 204;
    public static int FORBIDDEN_STATUS_CODE = 403;
    public static int UNAUTHORIZED_STATUS_CODE = 401;
    public static int BAD_REQUEST_STATUS_CODE = 400;
    public static int NOT_FOUND_STATUS_CODE = 404;
    public static int SERVICE_UNAVAILABLE_STATUS_CODE = 502;

    // --------------------------------------------- RESPONSE MESSAGE --------------------------------------------------

    // POST CREATE SUCCESS & FAILED MESSAGE
    public static final String FAILED_POSTER_ID = "Failed poster id!";
    public static final String FAILED_POSTER_TYPE = "Failed post type!";
    public static final String FAILED_POST_PRIVACY_TYPE = "Failed post privacy type!";
    public static final String FAILED_POST = "Failed to add post!";
    public static final String INVALID_FILE_EXTENSION = "File extension not matched!";
    public static final String INVALID_FILE_TYPE = "File type not matched!";
    public static final String INVALID_FILE_SUB_TYPE = "File sub type not matched!";
    public static final String POST_SUCCESS = "Post has been added successfully!";
    public static final String INVALID_INPUT = "Your input is not valid.";

    // GET POST SUCCESS & FAILED MESSAGE
    public static final String GET_POST_SUCCESS = "Successfully data found!";
    public static final String FAILED_POST_DATA = "Sorry, no data found!";

    // UPDATE POST SUCCESS & FAILED MESSAGE
    public static final String UPDATE_POST = "Post updated successfully!";
    public static final String FAILED_UPDATE_POST = "Failed to post update!";
    public static final String FAILED_POST_TEXT = "Field can not be empty!";
    public static final String FAILED_DELETING_MEDIA_FILE= "To delete media file ismediaFileDeleletd should be 2";
    public static final String FILE_TYPE_ERROR= "To delete image fileType=1 or to delete video fileType=2";

    // DELETE POST SUCCESS & FAILED MESSAGE
    public static final String DELETE_POST = "Post deleted successfully!";
    public static final String FAILED_DELETE_POST = "Failed to delete post!";

    // LIKE POST SUCCESS & FAILED MESSAGE
    public static final String FAILED_LIKER_ID = "Failed liker id!";
    public static final String FAILED_LIKER_TYPE = "Failed liker type!";
    public static final String LIKE_POST = "Post like successfully!";
    public static final String UNLIKE_POST = "Post unlike successfully!";

    // COMMENT POST SUCCESS & FAILED MESSAGE
    public static final String COMMENT_POST = "Comment post successfully!";
    public static final String FAILED_COMMENTER_ID = "Failed commenter id!";
    public static final String FAILED_COMMENTER_TYPE = "Failed commenter type!";
    public static final String FAILED_COMMENT_TEXT = "Failed comment text!";
    public static  final String GET_COMMENT_SUCCESS = "Data found successfully!";
    public static  final String FAILED_COMMENT_POST_DATA = "Sorry, no results found!";
    public static  final String UPDATE_COMMENT = "Comment updated successfully!";
    public static  final String FAILED_UPDATE_COMMENT = "Failed to updat comment!";
    public static  final String DELETE_COMMENT = "Comment deleted successfully!";
    public static  final String FAILED_TO_DELETE_COMMENT = "Failed to delete comment!";

    // NEWS FEED SUCCESS & FAILED MESSAGE
    public static final String FAILED_FOLLOWER_ID = "Failed follower id!";
    public static final String DATA_FOUND = "Data found successfully!";

    // LIKE ON COMMENT
    public static final String LIKE_ON_COMMENT = "Like on comment!";

    // FILE SIZE
    public static final String LARGE_FILE_SIZE = "File size is too large and cannot be uploaded. Please reduce the file of the size and try again!";
    public static final String INVALID_FILE = "Please upload either image or Video file at a time!";
}


