import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;


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
		
		// set chart parameters
		chart.setTitle("Clustering Tests");
		
		String datapoints = "C:\\Users\\Laura\\Documents\\Uni\\Bachelorarbeit\\Data\\pollutionData-0.csv";
		String cluster = "C:\\Users\\Laura\\Documents\\Uni\\Bachelorarbeit\\Data\\pollutionData-cabirch-result-0.csv";
        BufferedReader br_data = null;
        BufferedReader br_cluster = null;
        String data_line = "";
        String cluster_line = "";
        String csvSplitBy = ",";
        HashMap<String, ArrayList<String[]>> data_map = new HashMap<String, ArrayList<String[]>>();
        
        try {

            br_data = new BufferedReader(new FileReader(datapoints));
            br_cluster = new BufferedReader(new FileReader(cluster));
            
            int count = 0;
            // idee: map erstellen mit Datum als key 
            while ((data_line = br_data.readLine()) != null && (cluster_line = br_cluster.readLine()) != null ) {
            	
            	if(count == 100) break;
            	
                String[] data = data_line.split(csvSplitBy);
                String[] cluster_data = cluster_line.split(csvSplitBy);

                String date = data[0];
                data[0] = "point";
            	ArrayList<String[]> current_points = new ArrayList<String[]>();

                if(data_map.get(date) != null) {
                	current_points = data_map.get(date);
                }
                current_points.add(data);
                data_map.put(date, current_points);
 
                String cluster_date = cluster_data[0];
                cluster_data[0] = "cluster";
                
            	ArrayList<String[]> current_cl_points = new ArrayList<String[]>();
                if(data_map.get(cluster_date) != null) {
                	current_cl_points = data_map.get(cluster_date);
                }
                current_cl_points.add(cluster_data);
                data_map.put(cluster_date, current_cl_points);

                count++;
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
        
//        Set<Map.Entry<String, ArrayList<String[]>>> entrySet = data_map.entrySet();
//        Iterator<Map.Entry<String, ArrayList<String[]>>> it = entrySet.iterator();
//        
        
        Float max_width = 0.00f;
        Float max_height = 0.00f;

        ArrayList<DataSet> dataSet = new ArrayList<>();
        String EXAMPLE_KEY = 	"2019-03-31 18:25:12.942";
        
        ArrayList<String[]> exampleData = data_map.get(EXAMPLE_KEY);
        
//        while(it.hasNext()) {
//        	Map.Entry me = (Map.Entry)it.next();
//        	current_cluster.clear();
        	for(int i = 0; i <exampleData.size(); i++) {
        		String[] value = exampleData.get(i);
        		Float X = Float.parseFloat(value[2]);
        		Float Y = Float.parseFloat(value[3]);
        		
        		if(X>max_width) max_width = X;
        		if(Y>max_height) max_height = Y;
        		
        		if(value[0] == "point") {
        			DataSet currentPoint = new DataSet();
        			currentPoint.setX(X);
        			currentPoint.setY(Y);
        			currentPoint.setRadius(0.1f);
                	dataSet.add(currentPoint);
        		}else {
        			DataSet currentPoint = new DataSet();
        			currentPoint.setX(X);
        			currentPoint.setY(Y);
        			String[] radius = value[1].split("=");
        			currentPoint.setRadius(Float.parseFloat(radius[1]));
        			
                	dataSet.add(currentPoint);
        		}
//        	}
        }
    		chart.setData(dataSet);
    		chart.setWidth(Math.round(max_width)+10);
    		chart.setHeight(Math.round(max_height)+10);
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
        			System.out.print(response);
	    		}catch(IOException e) {
	    			System.out.printf("could not get response, Error: %s", e);
	            }
    		}catch(UnsupportedEncodingException e) {
    			System.out.println(e);
    		}	

	}
	
}
