package net.hazychill.ikafs;

import java.nio.charset.Charset;

public class IkafsConstants {
	public static final String PATH_WEBHOOK_REQUEST = "/request";
	public static final String PATH_WEBHOOK_TASK_PUSH = "/task/push";
	public static final String PATH_WEBHOOK_TASK_INIT_SEND = "/task/init_send";
	public static final String PATH_WEBHOOK_TASK_SEND = "/task/send";
	public static final String PATH_WEBHOOK_ADD_TEAM = "/admin/add_team";

	public static final String PATH_WEBFEED_FIND_SOURCES = "/task/find_sources";
	public static final String PATH_WEBFEED_INIT_DOWNLOAD_FEED = "/task/init_download_feed";
	public static final String PATH_WEBFEED_DOWNLOAD_FEED = "/task/download_feed";
	public static final String PATH_WEBFEED_ADD_SOURCE = "/admin/add_source";

	public static final String REQUEST_PARAM_NAME_TEAM_NAME = "teamName";
	public static final String REQUEST_PARAM_NAME_WEBHOOK_URL = "webhookUrl";
	public static final String REQUEST_PARAM_NAME_WEBFEED_SOURCE_FILE_ID = "fileId";
	public static final String REQUEST_PARAM_NAME_WEBFEED_SOURCE_COMMENT = "comment";

	public static final int STATUS_CODE_OK = 200;
	public static final int STATUS_CODE_BAD_REQUEST = 400;
	public static final int STATUS_CODE_FORBIDDEN = 403;
	public static final int STATUS_CODE_NOT_FOUND = 404;
	public static final int STATUS_CODE_METHOD_NOT_ALLOWED = 405;
	public static final int STATUS_CODE_LENGTH_REQUIRED = 411;
	public static final int STATUS_CODE_REQUEST_ENTITY_TOO_LARGE = 413;

	public static final String HTTP_METHOD_POST = "POST";
	public static final String HTTP_HEADER_NAME_CONTENT_TYPE = "Content-Type";
	public static final String MIME_TYPE_JSON = "application/json";
	public static final String HTTP_HEADER_VALUE_CONTENT_TYPE_TEXT_HTML = "text/html";
	public static final String HTTP_HEADER_VALUE_CONTENT_TYPE_TEXT_CSV = "text/csv";

	public static final int MAX_JSON_PAYLOAD_BYTES = 3 * 1024 * 1024;

	public static final int NETWORK_READ_BUFFER_LENGTH = 8192;

	public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

	public static final String QUEUE_NAME_DEFAULT = "default";
	public static final String QUEUE_PARAM_NAME_CACHE_KEY = "cacheKey";
	public static final String QUEUE_PARAM_NAME_SEND_GROUP = "sendGroup";
	public static final String QUEUE_PARAM_NAME_MESSAGE_KEY = "messageKey";
	public static final String QUEUE_PARAM_NAME_URL = "url";

	public static final String JSON_KEY_DESCTINATION = "destination";
	public static final String JSON_KEY_MESSAGE = "message";

	public static final String LOGGER_NAME = "ikafslogger";

	public static final int MESSAGE_SPEC_SEND_STATUS_UNSENT = 0;
	public static final int MESSAGE_SPEC_SEND_STATUS_SENDING = 1;
	public static final int MESSAGE_SPEC_SEND_STATUS_SENT = 2;
	public static final int MESSAGE_SPEC_SEND_STATUS_ERROR = -1;

	public static final String SYSPROP_KEY_SERVICE_EMAIL = "ikafs.service.email";
	public static final String SYSPROP_KEY_SERVICE_P12_PATH = "ikafs.service.p12.path";

	public static final String APPLICATION_NAME = "Ingreee Keihin Applications for Slack";
}
