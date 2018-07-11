package net.hazychill.ikafs.misc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.slim3.datastore.Datastore;

import com.google.appengine.api.datastore.Key;

import net.hazychill.ikafs.IkafsConstants;
import net.hazychill.ikafs.IkafsRequestHandler;
import net.hazychill.ikafs.IkafsServletException;
import net.hazychill.ikafs.models.ConfigEntry;

public class ConfigHandler implements IkafsRequestHandler {

	@Override
	public void handle(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet) throws IkafsServletException {
		Logger logger = Logger.getLogger(IkafsConstants.LOGGER_NAME);
		try {
			String method = req.getMethod();
			List<ConfigEntry> configEntries = new ArrayList<ConfigEntry>();
			if (IkafsConstants.HTTP_METHOD_POST.equals(method)) {
				for (String configKey : IkafsConstants.CONFIG_KEYS) {
					String configValue = req.getParameter(configKey);
					if (configValue != null && configValue.length() > 0) {
						ConfigEntry entry = new ConfigEntry();
						Key key = Datastore.createKey(ConfigEntry.class, configKey);
						entry.setKey(key);
						entry.setValue(configValue);
						configEntries.add(entry);
					}
				}
			}

			boolean updated;
			ConfigManager configManager = new ConfigManager();
			if (configEntries.size() > 0) {
				configManager.updateAll(configEntries);
				updated = true;
			}
			else {
				updated = false;
			}

			resp.setStatus(IkafsConstants.STATUS_CODE_OK);
			resp.setContentType(IkafsConstants.HTTP_HEADER_VALUE_CONTENT_TYPE_TEXT_HTML);

			PrintWriter writer = resp.getWriter();
			writer.write("<html><head><title>IKAfS Config</title><style type=\"text/css\">table {border-collapse: collapse;} td {border: 1px solid #999999;}</style></head><body>");
			if (updated) {
				writer.write("<div>Config value successfully updated.</div>");
			}
			writer.write("<form method=\"POST\">");
			writer.write("<table>");
			writer.write("<tr><td>Name</td><td>Current value</td><td>New value</td></tr>");
			List<ConfigEntry> entries = configManager.listConfigs();
			if (entries.size() > 0) {
				for (ConfigEntry entry : entries) {
					writer.write("<tr>");
					String configKey = entry.getKey().getName();
					String configValue = entry.getValue();
					writer.write("<td>");
					writer.write(StringEscapeUtils.escapeHtml4(configKey));
					writer.write("</td>");
					writer.write("<td><input type=\"text\" readonly=\"true\" style=\"width:300px;\" value=\"");
					writer.write(configValue.replaceAll("\"", "&quot;").replaceAll("'", "&#x27;"));
					writer.write("\" />");
					writer.write("</td>");
					writer.write("<td><input type=\"text\" style=\"width:300px;\" name=\"");
					writer.write(configKey);
					writer.write("\" /></td>");
					writer.write("</tr>");
				}
			}
			writer.write("</table>");
			writer.write("<input type=\"submit\" value=\"update\" />");
			;
			writer.write("</form>");
			writer.write("</body>");
			writer.write("</html>");
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IkafsServletException(null, e);
		}
	}
}
