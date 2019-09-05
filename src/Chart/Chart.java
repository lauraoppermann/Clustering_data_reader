package Chart;

import java.util.ArrayList;

import Chart.DataSet;

public class Chart {
	
	int height;
	int width;
	String title;
	ArrayList<DataSet> data = new ArrayList<>();
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setData(ArrayList<DataSet> data) {
		this.data = data;
	}
	
}