package Chart;

import java.util.ArrayList;

import Chart.DataSet;

public class Chart {
	
	int height;
	int width;
	int minX;
	int minY;
	int maxX;
	int maxY;
	int dataRefreshInterval;
	String title;
	ArrayList<DataSet> data = new ArrayList<>();
	
	public void setHeight(int height) {
		this.height = height;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setMinWidth(int minWidth) {
		this.minX = minWidth;
	}
	public void setMinHeight(int minHeight) {
		this.minY = minHeight;
	}
	public void setMaxWidth(int maxWidth) {
		this.maxX = maxWidth;
	}
	public void setMaxHeight(int maxHeight) {
		this.maxY = maxHeight;
	}
	public void setDataRefreshInterval(int interval) {
		this.dataRefreshInterval = interval;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setData(ArrayList<DataSet> data) {
		this.data = data;
	}
	
}