import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

import Chart.Chart;
import Chart.DataSet;

public class DataReader {

	public static void main(String[] args) throws InterruptedException {

		Chart chart = new Chart(); 
		
		/*
		 * set chart parameters
		 */
		
		String datapoints = "C:\\Users\\Laura\\Documents\\Uni\\Bachelorarbeit\\Data\\pollutionData-0.csv";
		String cluster = "C:\\Users\\Laura\\Documents\\Uni\\Bachelorarbeit\\Data\\pollutionData-cabirch-result-0.csv";
        BufferedReader br_data = null;
        BufferedReader br_cluster = null;
        String data_line = "";
        String cluster_line = "";
        String csvSplitBy = ",";
        
        Float max_width = 0.00f;
      	Float max_height = 0.00f;
      	Float min_width = Float.POSITIVE_INFINITY;
      	Float min_height= Float.POSITIVE_INFINITY;
        String location = "";
        /*
         *  save all datapoints in a long list and save all cluster in a Map with date as key
         */
        ArrayList<String[]> all_datapoints = new ArrayList();
        HashMap<String, ArrayList<String[]>> cluster_map = new HashMap<String, ArrayList<String[]>>();
        
        try {

            br_data = new BufferedReader(new FileReader(datapoints));
            br_cluster = new BufferedReader(new FileReader(cluster));
            
            // read the datapoints into a list
            int count = 0;
            while ((data_line = br_data.readLine()) != null) {        
            	count++;
            	if(count == 1) continue;
            	if(count == 50) break;
                String[] data = data_line.split(csvSplitBy);
            	Float X = Float.parseFloat(data[2]);
            	Float Y = Float.parseFloat(data[3]);
            	// search for the max width to set
            	if(X>max_width) max_width = X;
            	if(Y>max_height) max_height = Y;
            	if(X<min_width) min_width = X;
            	if(Y<min_height) min_height = Y;
            	
                // add the current datapoint to list
                all_datapoints.add(data);
            }

            // read the cluster into a Map
            count = 0;
            while((cluster_line = br_cluster.readLine()) != null ) {
                count++;
            	if(count == 1) continue;
            	if(count == 500) break;
                String[] cluster_data = cluster_line.split(csvSplitBy);
                String cluster_date = cluster_data[0];
            	ArrayList<String[]> current_cl_points = new ArrayList<String[]>();

            	if(cluster_map.get(cluster_date) != null) {
                	current_cl_points = cluster_map.get(cluster_date);
                }
            	Float X = Float.parseFloat(cluster_data[2]);
            	Float Y = Float.parseFloat(cluster_data[3]);
            	// search for the max width to set
            	if(X>max_width) max_width = X;
            	if(Y>max_height) max_height = Y;
            	if(X<min_width) min_width = X;
            	if(Y<min_height) min_height = Y;
            	
                current_cl_points.add(cluster_data);
                cluster_map.put(cluster_date, current_cl_points);

            }
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br_data != null) {
                try {
                    br_data.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // set all the params for the chart
		chart.setMaxWidth(Math.round(max_width)+10);
		chart.setMaxHeight(Math.round(max_height)+10);
		chart.setMinWidth(Math.round(min_width)-10);
		chart.setMinHeight(Math.round(min_height)-10);
		chart.setWidth(300);
		chart.setHeight(130);
		chart.setDataRefreshInterval(500);
		
		// make a request to initialize the chart
		String       postUrl       = "http://localhost:8000/chart";// put in your url
		Gson         gson          = new Gson();
		HttpClient   httpClient    = HttpClientBuilder.create().build();
		HttpPost     post          = new HttpPost(postUrl);
		StringEntity postingString;//gson.tojson() converts your pojo to json

		try {
    		postingString = new StringEntity(gson.toJson(chart));//gson.tojson() converts your pojo to json
    		
    		post.setEntity(postingString);
    		post.setHeader("Content-type", "application/json");
    		try {
    			HttpResponse  response = httpClient.execute(post);
    			location = response.getHeaders("Location")[0].getValue().toString();
    			System.out.printf("Your Chart is at Location: %s", location);
    		}catch(IOException e) {
    			System.out.printf("could not get response, Error: %s", e);
            }
		}catch(UnsupportedEncodingException e) {
			System.out.println(e);
		}	
		
        /*
         * read dataSet, means:
         * get list of datapoints( current and up to 20 in the past)
         * get list of cluster (matching the timestamp of last datapoint)
         * 
         * for the beginning: 
         * 	- points from 0-20
         * 	- cluster of 20th datapoint
         */
        
        // das muss jetzt automatisch immer wieder passieren
        // cp: index of current_point
        // fp: index of first point from sublist
        
        for(int cp = 0; cp < all_datapoints.size(); cp++) {
        	int fp = 0;
        	if(cp > 19) fp = cp - 19;
        	// get the sublist of 20 last points
            List<String[]> sub_points = all_datapoints.subList(fp, cp+1);
            String last_point_date = sub_points.get(sub_points.size()-1)[0];
            // get all the cluster for the current date
            ArrayList<String[]> current_cluster = cluster_map.get(last_point_date);

            // create a new dataset that will be send to the Server
            ArrayList<DataSet> dataSet = new ArrayList<>();
          
            // iterate through all the points and add them to the dataSet
            for(int i = 0; i <sub_points.size(); i++) {
	          	String[] value = sub_points.get(i);
	          	
	      		DataSet currentPoint = new DataSet();
	      		currentPoint.setX(Float.parseFloat(value[2]));
	      		currentPoint.setY(Float.parseFloat(value[3]));
	      		currentPoint.setRadius(0);
	      		dataSet.add(currentPoint);
            }
            
            // iterate through all the cluster and add them to the dataSet
            for(int i = 0; i <current_cluster.size(); i++) {
          	
	          	String[] value = current_cluster.get(i);
	          	
	      		DataSet currentPoint = new DataSet();
	      		currentPoint.setX(Float.parseFloat(value[2]));
	      		currentPoint.setY(Float.parseFloat(value[3]));
	      		
	      		String[] radius = value[1].split("=");
	      		currentPoint.setRadius(Float.parseFloat(radius[1]));
	      		dataSet.add(currentPoint);
            }
            
            /*
             * add chart data
             */
    		chart.setTitle(last_point_date);
    		chart.setData(dataSet);

    		
    		/*
    		 * post request to server and update data
    		 * get back chart Id and open manually to see result
    		 */
    		
        	String       updateUrl       = "http://localhost:8000/chart/" + location;// put in your url
    		gson          = new Gson();
    		httpClient    = HttpClientBuilder.create().build();
    		post          = new HttpPost(updateUrl);

    		try {
        		postingString = new StringEntity(gson.toJson(chart));//gson.tojson() converts your pojo to json
        		
        		post.setEntity(postingString);
        		post.setHeader("Content-type", "application/json");
        		try {
        			HttpResponse  response = httpClient.execute(post);
        			System.out.print(response);
        		}catch(IOException e) {
        			System.out.printf("could not get response, Error: %s", e);
                }
    		}catch(UnsupportedEncodingException e) {
    			System.out.println(e);
    		}
    		Thread.sleep(500);
        }
	}	
}



/*
 *  reading datapoints into the map
 */

//String date = data[0];
//ArrayList<String[]> current_points = new ArrayList<String[]>();
//
//if(data_map.get(date) != null) {
//	current_points = data_map.get(date);
//}
//current_points.add(data);
//data_map.put(date, current_points);

/*
 * reading current points out of map
 */


//Set<Map.Entry<String, ArrayList<String[]>>> entrySet = data_map.entrySet();
//Iterator<Map.Entry<String, ArrayList<String[]>>> it = entrySet.iterator();
//
//ArrayList<String[]> current_cluster = new ArrayList();
//ArrayList<String[]> current_poits = new ArrayList();
//Float max_width = 0.00f;
//Float max_height = 0.00f;
//
//ArrayList<DataSet> dataSet = new ArrayList<>();
//String EXAMPLE_KEY = 	"2019-03-31 18:25:12.942";
//
//ArrayList<String[]> exampleData = data_map.get(EXAMPLE_KEY);
//int count = 0;
//
//while(it.hasNext()) {
//	if(count == 20) break;
//	count++;
//	
//	Map.Entry me = (Map.Entry)it.next();
//	current_cluster.clear();
//	String[] value = current_cluster.get(me.getValue());
//	Float X = Float.parseFloat(value[2]);
//	Float Y = Float.parseFloat(value[3]);
//	
//	if(X>max_width) max_width = X;
//	if(Y>max_height) max_height = Y;
//	
//	if(value[0] == "point") {
//		DataSet currentPoint = new DataSet();
//		currentPoint.setX(X);
//		currentPoint.setY(Y);
//		currentPoint.setRadius(0.1f);
//  	dataSet.add(currentPoint);
//	}else {
//		DataSet currentPoint = new DataSet();
//		currentPoint.setX(X);
//		currentPoint.setY(Y);
//		String[] radius = value[1].split("=");
//		currentPoint.setRadius(Float.parseFloat(radius[1]));
//		
//  	dataSet.add(currentPoint);
//	}
//	
//}
//
//current_cluster.addAll(current_poits);
//
//for(int i = 0; i <current_cluster.size(); i++) {
//
//}