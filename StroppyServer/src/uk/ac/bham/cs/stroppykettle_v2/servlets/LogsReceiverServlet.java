package uk.ac.bham.cs.stroppykettle_v2.servlets;

import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import uk.ac.bham.cs.stroppykettle_v2.models.Log;
import uk.ac.bham.cs.stroppykettle_v2.protocols.JSONParams;

@SuppressWarnings("serial")
public class LogsReceiverServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(req.getInputStream(), writer, "UTF-8");
			String logString = writer.toString();
	
			JSONObject json = (JSONObject) JSONValue.parse(logString);
			if(json == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			String deviceId = (String) json.get(JSONParams.DEVICE_ID);
			JSONArray logs = (JSONArray) json.get(JSONParams.LOG_LIST);
			if(logs == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			for(int i = 0; i < logs.size(); i++) {
				Log l = new Log(deviceId, (JSONObject) logs.get(i));
				System.out.println(l.toString());
				// TODO Insert in DB
			}
			
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch(Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
