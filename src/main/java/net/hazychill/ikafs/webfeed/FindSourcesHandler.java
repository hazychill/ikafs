package net.hazychill.ikafs.webfeed;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slim3.datastore.Datastore;
import org.slim3.datastore.EntityNotFoundRuntimeException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Export;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.utils.SystemProperty;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.FeedChannelRelation;
import net.hazychill.ikafs.models.FeedSourceSheet;
import net.hazychill.ikafs.models.FeedUrl;

public class FindSourcesHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			List<FeedSourceSheet> sourceSheets = Datastore.query(FeedSourceSheet.class).asList();

			if (sourceSheets.size() == 0) {
				logger.info("no source sheets registered");
				resp.setStatus(200);
				return;
			}

			Drive service = getDriveService(servlet, logger);

			Map<String, List<String>> feedMap = new HashMap<String, List<String>>();

			for (FeedSourceSheet sourceSheet : sourceSheets) {
				String id = sourceSheet.getKey().getName();
				try {
					File file = service.files().get(id).execute();
					String csvContent = downloadCsv(service, file, logger);
					parseCsv(csvContent, feedMap);
				}
				catch (IOException e) {
					e.printStackTrace();
					logger.warning(e.toString());
					throw new IkafsServletException(null, e);
				}
			}

			List<Object> models = new ArrayList<Object>();
			for (String url : feedMap.keySet()) {
				FeedUrl feedUrl;
				Key feedUrlKey = Datastore.createKey(FeedUrl.class, url);
				try {
					feedUrl = Datastore.get(FeedUrl.class, feedUrlKey);
				}
				catch (EntityNotFoundRuntimeException e) {
					feedUrl = new FeedUrl();
					feedUrl.setKey(feedUrlKey);
					feedUrl.setUpdated(new Date());
				}
				feedUrl.setActive(true);
				models.add(feedUrl);
				List<String> teamChannelList = feedMap.get(url);
				for (String teamChannel : teamChannelList) {
					FeedChannelRelation feedChannelRelation;
					Key fcrKey = Datastore.createKey(feedUrlKey, FeedChannelRelation.class, teamChannel);
					try {
						feedChannelRelation = Datastore.get(FeedChannelRelation.class, fcrKey);
					}
					catch (EntityNotFoundRuntimeException e) {
						feedChannelRelation = new FeedChannelRelation();
						feedChannelRelation.setKey(fcrKey);
						feedChannelRelation.setUpdated(new Date());
					}
					feedChannelRelation.setActive(true);
					models.add(feedChannelRelation);
				}
			}

			Datastore.put(models);

			resp.setStatus(200);
			resp.getWriter().write("OK");
		}
		catch (Exception e1) {
			e1.printStackTrace();
			logger.severe(e1.toString());
			throw new IkafsServletException(null, e1);
		}
	}

	private void parseCsv(String csvContent, Map<String, List<String>> feedMap) {
		String[] lines = csvContent.split("[\r\n]+");

		String[] firstLine = lines[0].split(",");
		String team = firstLine[1];

		for (int i = 2; i < lines.length; i++) {
			String[] line = lines[i].split(",");
			if (line.length < 3) {
				continue;
			}

			String channel = line[1];
			String url = line[2];
			if (!feedMap.containsKey(url)) {
				List<String> channels = new ArrayList<String>();
				feedMap.put(url, channels);
			}
			if (!channel.startsWith("#")) {
				channel = "#" + channel;
			}
			String teamChannel = team + channel;
			feedMap.get(url).add(teamChannel);
		}
	}

	private String downloadCsv(Drive service, File file, Logger logger) throws IkafsServletException, IOException {
		Export export = service.files().export(file.getId(), IkafsConstants.HTTP_HEADER_VALUE_CONTENT_TYPE_TEXT_CSV);
		HttpResponse response = export.executeMedia();

		InputStream responseInput = null;
		Reader responseReader = null;
		try {
			responseInput = response.getContent();
			responseReader = new InputStreamReader(responseInput);
			StringBuilder sb = new StringBuilder();
			char[] buffer = new char[IkafsConstants.NETWORK_READ_BUFFER_LENGTH];
			int readCount;
			while (true) {
				readCount = responseReader.read(buffer, 0, buffer.length);
				if (readCount == -1) {
					break;
				}
				sb.append(buffer, 0, readCount);
			}
			return sb.toString();
		}
		finally {
			if (responseReader != null) {
				try {
					responseReader.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (responseInput != null) {
				try {
					responseInput.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Drive getDriveService(HttpServlet servlet, Logger logger) throws GeneralSecurityException, IOException {
		Drive service = null;
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
			AppIdentityCredential credential = new AppIdentityCredential(Collections.singleton(DriveScopes.DRIVE));
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			JsonFactory jsonFactory = new JacksonFactory();
			service = new Drive.Builder(httpTransport, jsonFactory, null).setHttpRequestInitializer(credential).setApplicationName(IkafsConstants.APPLICATION_NAME).build();
		}
		else if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
			URL url = servlet.getServletContext().getResource(System.getProperty(IkafsConstants.SYSPROP_KEY_SERVICE_P12_PATH));
			String emailAddress = System.getProperty(IkafsConstants.SYSPROP_KEY_SERVICE_EMAIL);
			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
			HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setServiceAccountId(emailAddress)
					.setServiceAccountPrivateKeyFromP12File(new java.io.File(url.getFile())).setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE)).build();
			service = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName(IkafsConstants.APPLICATION_NAME).build();
		}

		return service;
	}

}
