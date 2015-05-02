package net.hazychill.ikafs;

import java.nio.charset.Charset;

public class IkafsConstants {
	public static final String PATH_SERVLET_CONTEXT_WEBHOOK = "/webhook";
	public static final String PATH_WEBHOOK_REQUEST = "/request";
	public static final String PATH_WEBHOOK_TASK_PUSH = "/task/push";
	public static final String PATH_WEBHOOK_TASK_INIT_SEND = "/task/init_send";
	public static final String PATH_WEBHOOK_TASK_SEND = "/task/send";
	public static final String PATH_WEBHOOK_ADD_TEAM = "/admin/add_team";

	public static final String PATH_WEBFEED_FIND_SOURCES = "/task/find_sources";
	public static final String PATH_WEBFEED_INIT_DOWNLOAD_FEED = "/task/init_download_feed";
	public static final String PATH_WEBFEED_DOWNLOAD_FEED = "/task/download_feed";
	public static final String PATH_WEBFEED_REQUEST_POST_MESSAGE = "/task/request_post_message";
	public static final String PATH_WEBFEED_ADD_SOURCE = "/admin/add_source";

	public static final String PATH_MISC_CONFIG = "/config";
	public static final String PATH_MISC_BATCH_DELETE = "/batch_delete";
	public static final String PATH_MISC_EXEC_DELETE = "/task/exec_delete";
	public static final String PATH_MISC_ANALYZE_LOG = "/analyze_log";
	public static final String PATH_MISC_TASK_ANALYZE_LOG = "/task/analyze_log";

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
	public static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;

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

	public static final String SEND_GROUP_FEED_ENTRIES = "1F373A72-A740-4A61-723A-371F40A7614A";

	public static final String JSON_KEY_DESCTINATION = "destination";
	public static final String JSON_KEY_MESSAGE = "message";
	public static final String JSON_KEY_CHANNEL = "channel";
	public static final String JSON_KEY_USERNAME = "username";
	public static final String JSON_KEY_ATTACHMENTS = "attachments";
	public static final String JSON_KEY_FALLBACK = "fallback";
	public static final String JSON_KEY_TITLE = "title";
	public static final String JSON_KEY_TITLELINK = "title_link";
	public static final String JSON_KEY_TEXT = "text";
	public static final String JSON_KEY_IMAGEURL = "image_url";
	public static final String JSON_KEY_ICONEMOJI = "icon_emoji";

	public static final String JSON_VALUE_ICONEMOJI_WEBFEED = ":webfeed_blue:";

	public static final String MESSAGE_FORMAT_FALLBACK = "<{0}|{1}>";

	public static final String LOGGER_NAME = "ikafslogger";

	public static final int MESSAGE_SPEC_SEND_STATUS_UNSENT = 0;
	public static final int MESSAGE_SPEC_SEND_STATUS_SENDING = 1;
	public static final int MESSAGE_SPEC_SEND_STATUS_SENT = 2;
	public static final int MESSAGE_SPEC_SEND_STATUS_ERROR = -1;

	public static final String SYSPROP_KEY_SERVICE_EMAIL = "ikafs.service.email";
	public static final String SYSPROP_KEY_SERVICE_P12_PATH = "ikafs.service.p12.path";

	public static final String APPLICATION_NAME = "Ingreee Keihin Applications for Slack";

	public static final String CONFIG_KEY_FEED_ENTRY_EXPIRE_DAYS = "CONFIG_KEY_FEED_ENTRY_EXPIRE_DAYS";
	public static final String CONFIG_KEY_MESSAGE_SPEC_EXPIRE_DAYS = "CONFIG_KEY_MESSAGE_SPEC_EXPIRE_DAYS";
	public static final String CONFIG_KEY_LAST_ANALYZED_LOG_TIME = "CONFIG_KEY_LAST_ANALYZED_LOG_TIME";
	public static final String CONFIG_KEY_MAX_LOG_ENTRIES_IN_REPORT = "CONFIG_KEY_MAX_LOG_ENTRIES_IN_REPORT";
	public static final String CONFIG_KEY_CALENDAR_TIMEZONE = "CONFIG_KEY_CALENDAR_TIMEZONE";
	public static final String CONFIG_KEY_CONSOLE_URL_LOG_LIST = "CONFIG_KEY_CONSOLE_URL_LOG_LIST";

	public static final String[] CONFIG_KEYS = new String[] { CONFIG_KEY_FEED_ENTRY_EXPIRE_DAYS, CONFIG_KEY_MESSAGE_SPEC_EXPIRE_DAYS, CONFIG_KEY_LAST_ANALYZED_LOG_TIME,
			CONFIG_KEY_MAX_LOG_ENTRIES_IN_REPORT, CONFIG_KEY_CALENDAR_TIMEZONE, CONFIG_KEY_CONSOLE_URL_LOG_LIST };

	public static final String DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ssZ";
}
