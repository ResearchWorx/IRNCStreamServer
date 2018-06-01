package edu.uky.irnc.streamserver.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.researchworx.cresco.library.messaging.MsgEvent;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerChannel {

	private final String USER_AGENT = "Cresco-Agent-Controller-Plugin/0.5.0";
	    
	
	private String controllerUrl;

	public ControllerChannel()
	{
		/*
		if(CLI.config.getStringParam("globalcontroller","globalcontroller_host") != null)
		{
			if(CLI.config.getStringParam("globalcontroller","globalcontroller_port") != null)
			{
				controllerUrl = "http://" + CLI.config.getStringParam("globalcontroller","globalcontroller_host") + ":" + CLI.config.getStringParam("globalcontroller","globalcontroller_port") + "/API";
			}
			else
			{
				controllerUrl = "http://" + CLI.config.getStringParam("globalcontroller","globalcontroller_host") + ":32000/API";
			}
			performanceUrl = "http://" + CLI.config.getStringParam("globalcontroller","globalcontroller_host") + ":32002/API";

		}
		*/
		controllerUrl = "http://" + "127.0.0.1" + ":32000/API";
		//controllerUrl = "http://" + CLI.config.getStringParam("globalcontroller","globalcontroller_host") + ":" + CLI.config.getStringParam("globalcontroller","globalcontroller_port") + "/API";
		System.out.println("Global Controller URL: " + controllerUrl);
	}
	
	public MsgEvent sendMsgEventReturn(MsgEvent le) {

		MsgEvent me = null;
		
		try
		{
			Map<String,String> tmpMap = le.getParams();
			Map<String,String> leMap = null;
			String type = null;
			synchronized (tmpMap)
			{
				leMap = new ConcurrentHashMap<String,String>(tmpMap);
				type = le.getMsgType().toString();
			}
			String url = controllerUrl + urlFromMsg(type,leMap);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
			con.setConnectTimeout(5000);
			
			// optional default is GET
			con.setRequestMethod("GET");
 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("service_key", "123");
 
			int responseCode = con.getResponseCode();
			
			if(responseCode == 200)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));				        
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) 
				{
						response.append(inputLine);
				}
				in.close();


				
				try
				{
				    me = meFromJson(response.toString());
				}
				catch(Exception ex)
				{
					System.out.println("Controller : ControllerChannel : Error meFromJson");
				}					
				return me;
			}
			else
			{
			return me;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : sendControllerLog : " + ex.toString());
			return me;
		}
	}

	public MsgEvent sendJSONReturn(String urlString, String JSON) {
		MsgEvent me = null;
	    try
		{
			//String urlParameters  = "param1=a&param2=b&param3=c";
			byte[] postData       = JSON.getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;
			//String request        = "http://example.com/index.php";
			URL    url            = new URL( controllerUrl + urlString );
			HttpURLConnection con= (HttpURLConnection) url.openConnection();
			con.setDoOutput( true );
			con.setInstanceFollowRedirects( false );
			con.setRequestMethod( "POST" );
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("service_key", "123");
			con.setRequestProperty( "Content-Type", "application/json");
			con.setRequestProperty( "charset", "utf-8");
			con.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
			con.setUseCaches( false );
			try( DataOutputStream wr = new DataOutputStream( con.getOutputStream())) {
				wr.write( postData );
			}

			con.setConnectTimeout(5000);

			//add request header

			int responseCode = con.getResponseCode();

			if(responseCode == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				System.out.println(response.toString());
                me = meFromJson(response.toString());
				//returnString = response.toString();
			}

		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : sendControllerLog : " + ex.toString());
		}
		return me;
	}


	public boolean sendMsgEvent(MsgEvent le)
    {
		try
		{
			Map<String,String> tmpMap = le.getParams();
			Map<String,String> leMap = null;
			String type = null;
			synchronized (tmpMap)
			{
				leMap = new ConcurrentHashMap<String,String>(tmpMap);
				type = le.getMsgType().toString();
			}
			String url = controllerUrl + urlFromMsg(type,leMap);
			System.out.println("url: [" + url + "]");
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
			con.setConnectTimeout(5000);
			
			// optional default is GET
			con.setRequestMethod("GET");
 
			//add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
 
			int responseCode = con.getResponseCode();
			
			if(responseCode == 200)
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));				        
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) 
				{
						response.append(inputLine);
				}
				in.close();
			
				try
				{
					System.out.println(response);
					//ce = meFromJson(response.toString());
				}
				catch(Exception ex)
				{
					System.out.println("Controller : ControllerChannel : Error meFromJson");
				}					
				return true;
			}
			else
			{
			return false;
			}
		}
		catch(Exception ex)
		{
			System.out.println("Controller : ControllerChannel : sendControllerLog : " + ex.toString());
			return false;
		}
	}
 
    public String urlFromMsg(String type, Map<String,String> leMap)
    {
    	
    	try
    	{
    		StringBuilder sb = new StringBuilder();
    		
    		sb.append("?type=" + type);
    		
    		Iterator it = leMap.entrySet().iterator();
    		while (it.hasNext()) 
    		{
    			Map.Entry pairs = (Map.Entry)it.next();
    			sb.append("&paramkey=" + URLEncoder.encode(pairs.getKey().toString(), "UTF-8") + "&paramvalue=" + URLEncoder.encode(pairs.getValue().toString(), "UTF-8"));
    			it.remove(); // avoids a ConcurrentModificationException
    		}
    		//System.out.println(sb.toString());
        return sb.toString();
    	}
    	catch(Exception ex)
    	{
    		System.out.println("Controller : ControllerChannel : urlFromMsg :" + ex.toString());
    		return null;
    	}
    }
   
    private MsgEvent meFromJson(String jsonMe)
	{
		Gson gson = new GsonBuilder().create();
        MsgEvent me = gson.fromJson(jsonMe, MsgEvent.class);
        //System.out.println(p);
        return me;
	} 
    
    
	// HTTP GET request
	
	
}
