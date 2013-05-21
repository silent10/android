package com.evature.search.controllers.web_services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.evaapis.EvaApiReply;
import com.evature.search.MyApplication;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.models.travelport.AirLowFareSearchRsp;
import com.evature.search.models.travelport.XMLParser;

public class SearchTravelportTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "SearchTravelportTask";
	MainActivity mMainActivity;
	private EvaApiReply mApiReply;

	public SearchTravelportTask(MainActivity mainActivity, EvaApiReply apiReply) {
		mMainActivity = mainActivity;
		mApiReply = apiReply;
	}

	public static String buildXml(String targetBranch, String origin, String destination, String PreferredTime,
			boolean trainOnly) {
		StringWriter writer = new StringWriter();
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document doc = docBuilder.newDocument();// root element
			Element rootElement = doc.createElement("s:Envelope");
			rootElement.setAttribute("xmlns:s", "http://schemas.xmlsoap.org/soap/envelope/");
			rootElement.setAttribute("xmlns:air", "http://www.travelport.com/schema/air_v16_0");
			doc.appendChild(rootElement);

			Element header = doc.createElement("s:Header");
			rootElement.appendChild(header);

			Element body = doc.createElement("s:Body"); // body element
			rootElement.appendChild(body);

			Element request = doc.createElement("air:LowFareSearchReq");
			request.setAttribute("xmlns:air", "http://www.travelport.com/schema/air_v19_0");
			request.setAttribute("xmlns:com", "http://www.travelport.com/schema/common_v16_0");
			request.setAttribute("TargetBranch", targetBranch);
			body.appendChild(request);

			Element billing = doc.createElement("com:BillingPointOfSaleInfo");
			billing.setAttribute("OriginApplication", "UAPI");
			request.appendChild(billing);

			Element searchAirLeg = doc.createElement("air:SearchAirLeg");
			request.appendChild(searchAirLeg);

			Element searchOrigin = doc.createElement("air:SearchOrigin");

			Element cityOrAirport;
			cityOrAirport = doc.createElement("com:CityOrAirport");
			cityOrAirport.setAttribute("Code", origin);
			searchOrigin.appendChild(cityOrAirport);

			Element searchDestination = doc.createElement("air:SearchDestination");

			cityOrAirport = doc.createElement("com:CityOrAirport");
			cityOrAirport.setAttribute("Code", destination);
			searchDestination.appendChild(cityOrAirport);

			Element searchDepTime = doc.createElement("air:SearchDepTime");
			searchDepTime.setAttribute("PreferredTime", PreferredTime);

			searchAirLeg.appendChild(searchOrigin);
			searchAirLeg.appendChild(searchDestination);
			searchAirLeg.appendChild(searchDepTime);

			if (trainOnly) { // Optimize by searching faster if specifically requested trains:
				Element AirSearchModifiers = doc.createElement("air:AirSearchModifiers");
				Element PreferredProviders = doc.createElement("air:PreferredProviders");
				AirSearchModifiers.appendChild(PreferredProviders);
				Element Provider = doc.createElement("com:Provider");
				Provider.setAttribute("Code", "RCH");
				PreferredProviders.appendChild(Provider);
				request.appendChild(AirSearchModifiers);
			}
			// Passengers:
			Element SearchPassenger = doc.createElement("com:SearchPassenger");
			SearchPassenger.setAttribute("Code", "ADT");
			request.appendChild(SearchPassenger);
			// write the content into a string
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(writer);
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // just for debug
			transformer.transform(source, result);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
		return writer.toString();
	}

	
	boolean checkApiReply(EvaApiReply reply)
	{
		if(reply==null) return false;
		
		if(reply.locations==null) return false;
		
		if(reply.locations.length==0) return false;
		
		if(reply.locations[0]==null) return false;
		
		return true;
	}
	
	@Override
	protected String doInBackground(String... unusedParams) {
		String airport_code0 = "";
		String airport_code1 = "";
		
		if(!checkApiReply(mApiReply))
		{
			return null;
		}
		
		if (mApiReply.locations.length >= 2) {
			Log.i(TAG, "Eva returned 2 locations!");
			if (mApiReply.locations[0].allAirportCode != null)
				airport_code0 = mApiReply.locations[0].allAirportCode;
			else
				airport_code0 = mApiReply.locations[0].airports.get(0);
			Log.i(TAG, "airport_code0 = " + airport_code0);

			if (mApiReply.locations[1].allAirportCode != null)
				airport_code1 = mApiReply.locations[1].allAirportCode;
			else {
				if (mApiReply.locations[1].airports != null && mApiReply.locations[1].airports.size() > 0) {
					airport_code1 = mApiReply.locations[1].airports.get(0);
				}
			}
			Log.i(TAG, "airport_code1 = " + airport_code1);
		}
		if (!airport_code0.equals("") && !airport_code1.equals("")) {
			String departureDate;
			if (mApiReply.locations[0].Departure != null && mApiReply.locations[0].Departure.Date != null) {
				departureDate = new String(mApiReply.locations[0].Departure.Date);
			} else {
				Calendar cal = Calendar.getInstance(); // now
				cal.add(Calendar.HOUR, 24); // tomorrow
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US); // format Travelport like
				departureDate = sdf.format(cal.getTime());
			}

			try {
				final String targetBranch = "P7002731";
				String theXml = buildXml(targetBranch, airport_code0, airport_code1, departureDate,
						mApiReply.isTrainSearch());
				// return callApi(String.format(data, targetBranch, airport_code0, airport_code1, departureDate));
				return callApi(theXml);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// Call the Travelport uAPI, sending the XML request and receiving the XML reply.
	private String callApi(String data) throws IOException {
		Log.d(TAG, "Travelport XML = " + data);
		final String travelport_url = "https://americas.copy-webservices.travelport.com/B2BGateway/connect/uAPI/AirService";
		URL url = new URL(travelport_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(50000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/xml");
		conn.setRequestProperty("Accept", "application/xml");
		String USER_ID = "Universal API/uAPI7662861526-8cd988d4";// Universal API User ID
		String PASSWORD = "pYbd9pPrPJhXdxXyZ38AnGKB6"; // Universal API Password
		String b64 = "Basic " + Base64.encodeToString((USER_ID + ":" + PASSWORD).getBytes("UTF-8"), Base64.NO_WRAP);
		conn.setRequestProperty("Authorization", b64);
		conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
		conn.getOutputStream().write(data.getBytes());
		conn.getOutputStream().flush();
		Log.d(TAG, "Starts the query");
		conn.connect(); // Starts the query
		int response = conn.getResponseCode();
		Log.d(TAG, "response = " + String.valueOf(response));
		// Read from web: http://stackoverflow.com/a/1381784/78234
		Reader r = new InputStreamReader(conn.getInputStream(), "UTF-8");
		StringBuilder buf = new StringBuilder();
		while (true) {
			int ch = r.read();
			if (ch < 0)
				break;
			buf.append((char) ch);
		}
		String xml = buf.toString();
		XMLParser parser = new XMLParser();
		Document document = parser.getDomElement(xml);
		MyApplication.getDb().airLowFareSearchRsp = new AirLowFareSearchRsp(document);
		Log.d(TAG, "Travelport returned " + MyApplication.getDb().airLowFareSearchRsp.railJourneyList.size()
				+ " Rail Journeys");
		return xml;
	}

	@Override
	protected void onPostExecute(String result) { // onPostExecute displays the results of the AsyncTask.

		if (result != null) {
			Log.d(TAG, "Got Travelport Response!");
			if (mMainActivity != null) {
				mMainActivity.setTravelportReply(mApiReply.isTrainSearch());
			}
		} else {
			Log.w(TAG, "Did NOT get Travelport Response!");
			Toast.makeText(mMainActivity, "Did NOT get Travelport Response!", Toast.LENGTH_LONG).show();
		}

	}
}
