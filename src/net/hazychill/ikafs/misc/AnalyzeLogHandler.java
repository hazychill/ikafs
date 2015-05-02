package net.hazychill.ikafs.misc;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.AppEventReport;

import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.log.AppLogLine;
import com.google.appengine.api.log.LogQuery;
import com.google.appengine.api.log.LogService;
import com.google.appengine.api.log.LogService.LogLevel;
import com.google.appengine.api.log.LogServiceFactory;
import com.google.appengine.api.log.RequestLogs;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class AnalyzeLogHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);

		String path = req.getPathInfo();
		if (IkafsConstants.PATH_MISC_ANALYZE_LOG.equals(path)) {
			queue(req, resp, logger);
		}
		else if (IkafsConstants.PATH_MISC_TASK_ANALYZE_LOG.equals(path)) {
			analyzeLog(resp, logger);
		}
		else {
			resp.setStatus(IkafsConstants.STATUS_CODE_NOT_FOUND);
		}
	}

	private void analyzeLog(HttpServletResponse resp, Logger logger) throws IkafsServletException {
		ConfigManager configManager = new ConfigManager();
		long lastAnalyzedLogTime = configManager.getLong(IkafsConstants.CONFIG_KEY_LAST_ANALYZED_LOG_TIME);
		LogService service = LogServiceFactory.getLogService();
		LogQuery query = LogQuery.Builder.withIncludeAppLogs(true).startTimeUsec(lastAnalyzedLogTime + 10000).minLogLevel(LogLevel.ERROR);
		int maxLogEntriesInReport = configManager.getInt(IkafsConstants.CONFIG_KEY_MAX_LOG_ENTRIES_IN_REPORT);
		List<String> reportLogs = new ArrayList<String>();
		for (RequestLogs log : service.fetch(query)) {
			if (log.getStartTimeUsec() > lastAnalyzedLogTime) {
				lastAnalyzedLogTime = log.getStartTimeUsec();
			}

			if (reportLogs.size() < maxLogEntriesInReport) {
				String calendarTimeZone = configManager.get(IkafsConstants.CONFIG_KEY_CALENDAR_TIMEZONE);
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(calendarTimeZone));
				cal.clear();
				cal.setTimeInMillis(log.getStartTimeUsec() / 1000);
				String appLogMessage = getAppLogMessage(log);
				String logSummary = MessageFormat.format("{0,number,0000}-{1,number,00}-{2,number,00}T{3,number,00}:{4,number,00}:{5,number,00}.{6,number} {7} {8} {9}", cal.get(Calendar.YEAR),
						cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND), cal.get(Calendar.MILLISECOND),
						log.getStatus(), log.getResource(), appLogMessage);
				reportLogs.add(logSummary);
			}
			else if (reportLogs.size() == maxLogEntriesInReport) {
				reportLogs.add("(and more...)");
				break;
			}
		}

		if (reportLogs.size() > 0) {
			StringBuilder reportText = new StringBuilder();
			for (String logSummary : reportLogs) {
				reportText.append(logSummary).append("\r\n");
			}
			reportText.append("\r\n");
			reportText.append("for more details, visit: ");
			reportText.append(configManager.get(IkafsConstants.CONFIG_KEY_CONSOLE_URL_LOG_LIST));
			AppEventReport report = new AppEventReport();
			report.setCreated(new Date());
			report.setTitle("");
			report.setReported(false);
			report.setText(new Text(reportText.toString()));
			Datastore.put(report);
		}

		configManager.setLong(IkafsConstants.CONFIG_KEY_LAST_ANALYZED_LOG_TIME, lastAnalyzedLogTime);

		resp.setStatus(200);
		try {
			resp.getWriter().write("OK");
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}

	private String getAppLogMessage(RequestLogs log) {
		List<AppLogLine> appLogLines = log.getAppLogLines();
		String appLogMessage = "";
		for (AppLogLine appLog : appLogLines) {
			if (appLog.getLogLevel() == LogLevel.WARN || appLog.getLogLevel() == LogLevel.ERROR || appLog.getLogLevel() == LogLevel.FATAL) {
				appLogMessage = appLog.getLogMessage();
				break;
			}
		}

		return appLogMessage;
	}

	private void queue(HttpServletRequest req, HttpServletResponse resp, Logger logger) throws IkafsServletException {
		Queue queue = QueueFactory.getQueue(IkafsConstants.QUEUE_NAME_DEFAULT);
		String queueUrl = req.getServletPath() + IkafsConstants.PATH_MISC_TASK_ANALYZE_LOG;
		queue.add(TaskOptions.Builder.withUrl(queueUrl).method(Method.POST));
		logger.info("exec delete task queued");

		resp.setStatus(200);
		try {
			resp.getWriter().write("OK");
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}

}
