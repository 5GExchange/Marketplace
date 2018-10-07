/**
* Copyright 2017 Atos
* Contact: Atos <javier.melian@atos.net>
*
*    Licensed under the Apache License, Version 2.0 (the "License");
*    you may not use this file except in compliance with the License.
*    You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS,
*    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*    See the License for the specific language governing permissions and
*    limitations under the License.
*/


package eu.atos.sla.monitoring.simple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import antlr.Utils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
//import eu.atos.sla.evaluation.guarantee.PoliciedServiceLevelEvaluator;
import eu.atos.sla.monitoring.IMetricsRetriever;
import eu.atos.sla.monitoring.IMonitoringMetric;

/**
 * metrics retriever from an external monitor
 * 
 * @author jmelian
 *
 */
public class TNovaMetricsRetriever implements IMetricsRetriever {

	public TNovaMetricsRetriever() {
	}
	
	private static Logger logger = LoggerFactory.getLogger(TNovaMetricsRetriever.class);
        private static final String NETWORK_SERVICE = "ns";
        private static final String VNF = "vnf";
        //private static final String ORCHESTRATOR_URL = "http://10.10.1.61:4000/instances/";

        //private String monitoringUrl="http://accounting:8000/aggregate";
        private String monitoringUrl = System.getenv("MONITORING_URL") != null ? System.getenv("MONITORING_URL") : "http://172.16.0.20:8000/aggregate";
        private long maxValues = 10;
        //private String timeUnit = "ns";
        private String timeUnit = System.getenv("DB_TIME_UNIT") !=  null ? System.getenv("DB_TIME_UNIT") : "ms";

	
        private static Date UnixTimeToDate(long time, String unit) {
            Date date = null;
            //unixtime needs to be in milliseconds
            unit = unit.toLowerCase();
            if (new String("s").equals(unit))
                date = new java.util.Date(time*1000L);
            if (new String("ns").equals(unit))
                date = new java.util.Date(time/1000000L);
            else //milliseconds by default
                date = new java.util.Date(time);


            /*
            switch (unit.toLowerCase()) {
                case "s": date = new java.util.Date(time*1000L); break; //seconds
                case "ns": date = new java.util.Date(time/1000000L); break; //nanoseconds
                default: date = new java.util.Date(time); break; //miliseconds
            }
            */

            return date;
        }

        private static long DateToUnixtime(Date time, String unit, long secsDelay) {
            //DateFormat dfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");

            long unixtime = 0;
            //unix time in miliseconds
            unixtime = time.getTime();  

            unit = unit.toLowerCase();
            if (new String("s").equals(unit))
                unixtime=(unixtime/1000)-secsDelay;
            if (new String("ns").equals(unit))
                unixtime=(unixtime*1000000)-(secsDelay*1000000000);
            else //milliseconds by default
                unixtime=unixtime-(secsDelay*1000);

            /*
            switch (timeUnit.toLowerCase()) {
                case "s": unixtime=(unixtime/1000)-secsDelay; break; //seconds
                case "ns": unixtime=(unixtime*1000000)-(secsDelay*1000000000); break; //nanoseconds
                default: unixtime=unixtime-(secsDelay*1000); break; //milliseconds
            }
            */
            return unixtime;
        }

	@Override
	public List<IMonitoringMetric> getMetrics(String agreementId, String formula, final String variable, Date begin, final Date end, int maxResults) {
                String instanceId; String instanceType;

		logger.debug("5GEX: agreementId: {}, metric: {}, begin: {}, end: {} -  {} unit: {} - {}",  agreementId, variable, begin, end, maxResults, timeUnit, monitoringUrl);
		if (begin == null) {
			
			begin = new Date(end.getTime() - 1000);
		}

		//Change the format of the dates to unix timestamp
                //DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
                Long dateBegin = DateToUnixtime(begin, timeUnit, 30); //move the monitoring requests 30secs back
                Long dateEnd = DateToUnixtime(end, timeUnit, 30);
                
		List<IMonitoringMetric> values = new ArrayList<IMonitoringMetric>();
		try 
		{

		    JSONParser jParser = new JSONParser();
                    formula = URLEncoder.encode(formula, "UTF-8");
		    //String url = URLEncoder.encode(monitoringUrl +"/?agreementId=" + agreementId + "&kpi=" + variable +"&formula="+ formula +"&start=" + dateBegin + "&end=" + dateEnd +"&max_values="+ maxValues);
    		    String url = monitoringUrl +"/?agreementId=" + agreementId + "&kpi=" + variable +"&formula="+ formula +"&start=" + dateBegin + "&end=" + dateEnd +"&max_values="+ maxValues;
		    /*//Header for the API call 
		    String token = Utils.getToken(source);

		    List<NameValuePair> headerParams = new ArrayList<NameValuePair>(1);
		    headerParams.add(new BasicNameValuePair("Authorization", "Token " + token));
		    */
		
		    logger.debug("5GEX: agreementId: {}, metric: {}, begin: {}:{}, end: {}:{}",  agreementId, variable, dateBegin, begin, dateEnd, end);
		    logger.debug("5GEX: URL - {}",  url);
		
                    JSONArray results = jParser.getJSONArrayFromUrl(url);

		    logger.debug("5GEX: Metrics- {}, length: {}",  results, results.length());
		    logger.debug("5GEX: URL- {}",  url);

		    //Obtention of the array of json objects (each one containing a metric value) from the monitor
		    //JSONArray results = json.getJSONArray("results");
		    for(int i = 0, size = results.length(); i < size; i++)
		    {
			JSONObject jsonMetric = results.getJSONObject(i);
			values.add(getMetric(jsonMetric, variable));
			//IMonitoringMetric test = getMetric(jsonMetric);
			//logger.debug("5GEX: monitoringMetric {} = metric: {}, value: {}, date: {}", i, test.getMetricKey(), test.getMetricValue(), test.getDate());
		    }
		}
		catch (JSONException e)
		{
			logger.debug("5GEX: ERROR- {}", e);
		}
                catch (UnsupportedEncodingException e) 
                {
			logger.debug("5GEX: ERROR- {}", e);
                }
		return values;
	}
	
	/**
	 * Converts a json object retrieved from the monitoring system to a IMonitoringMetric type: name, value, date.
	 * 
	 * @author jmelian
	 *
	 */
	public IMonitoringMetric getMetric(JSONObject metric, String name) {
		Date date = null; Double value = null;
		try {
			//value = Double.parseDouble(metric.getString("value"));
			value = metric.getDouble("value");
			//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//timestamp = metric.getDouble("date");
                        //date = new java.util.Date((long)timeStamp*1000);

                        //Convert the dates from unix time to regular dates
			date = UnixTimeToDate((Long)metric.getLong("time"), timeUnit);
		} catch (JSONException e) {
			logger.debug("5GEX: ERROR- {}", e.toString());
		}
		
		return new MonitoringMetric(name, value, date);
	}
}

