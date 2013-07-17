package uk.ac.bham.cs.stroppykettle_v2.servlets;

import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import uk.ac.bham.cs.stroppykettle_v2.BaseServlet;
import uk.ac.bham.cs.stroppykettle_v2.protocols.JSONParams;

@SuppressWarnings("serial")
public class LogsReceiverServlet extends BaseServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException {

		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(req.getInputStream(), writer, "UTF-8");
			String theString = writer.toString();
	
			JSONObject json = (JSONObject) JSONValue.parse(theString);
			
			if(json == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			System.out.println("Device Id : " + json.get(JSONParams.DEVICE_ID));
			
			JSONArray logs = (JSONArray) json.get(JSONParams.LOG_LIST);
			if(logs == null) {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
			
			for(int i = 0; i < logs.size(); i++) {
				JSONObject log = (JSONObject) logs.get(i);
				
				if(log != null) {
					System.out.println(i + " : " + log.get(JSONParams.LOG_DATETIME) + " - " + log.get(JSONParams.LOG_PREVIOUS_WEIGHT) + " - " + log.get(JSONParams.LOG_WEIGHT));
				}
			}
			
			resp.setStatus(HttpServletResponse.SC_OK);
		} catch(Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
}
