package adbmswebcache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
 
public class sampleGraph extends Application {
       

	@Override
	public void start(Stage stage) throws Exception {
		
		 String dbName = "webcache";
			 ArrayList<String> total_urls = new ArrayList<String>();
			 ArrayList<Long> ret_times = new ArrayList<Long>();

  	  String driver = "oracle.jdbc.driver.OracleDriver";
		// TODO Auto-generated method stub
		  Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        	Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","tiger");
        	

		Statement stmt = con.createStatement();
		ResultSet result = null;
		System.err.println("Enter the time in 'x' mins to view the cache perfomance in last 'x' mins :");
		Scanner min=new Scanner(System.in);
	
		int mins=min.nextInt();
	//	String query="select url,retTime from cachestorage where curTimems >= "+(System.currentTimeMillis()-600000)+" and retTime < 60000 order by url";
		String query="select url,retTime from cachestorage where curTimems >= "+(System.currentTimeMillis()-(mins*60*1000))+" order by url";
	    	 ResultSet res = stmt.executeQuery(query);
    	  String url=null;
	       int retTime = 0;
    	 String dummy_url="";
         int dummy_count=0;
    	int total_count=0;
    	
         while(res.next()){
        	 Long retrieval_time=res.getLong("rettime");
      // 	 System.out.println(total_count+"string : "+total_urls[total_count]);
		   System.out.println("Key : "+res.getString("URL")+" Ret Time : "+retrieval_time );
		    
total_urls.add(res.getString("URL"));
ret_times.add( retrieval_time);
	    	total_count++;
	    	System.out.println(total_count);
         }


    	   	 
     res.close();

		
		stage.setTitle("Web Cache Performance Chart");
         final CategoryAxis xAxis = new CategoryAxis();
         final NumberAxis yAxis = new NumberAxis();
         final BarChart<String,Number> bc =  new BarChart<String,Number>(xAxis,yAxis);
         bc.setTitle("Web Cache Performace");
         xAxis.setLabel("Web Sites");       
         yAxis.setLabel("Retrieval Time");
         XYChart.Series series1 = new XYChart.Series();
         XYChart.Series series2 = new XYChart.Series();
         XYChart.Series series3 = new XYChart.Series();
         XYChart.Series series4 = new XYChart.Series();
         series1.setName("Hit 1"); 
         series2.setName("Hit 2"); 
         series3.setName("Hit 3"); 
         series4.setName("Hit 4"); 
        

         int i=0;
         int plots=0;
         
	        while((i<total_urls.size()-1)&&(plots<10)){
	        	 System.out.println("\nCount : "+i);
	    		 System.out.println("Key : "+total_urls.get(i)+" Ret Time : "+ret_times.get(i) );
	    	if(dummy_url.equals(total_urls.get(i))){
	    		dummy_count++;
	    		}else{
	    		dummy_count=0;
	    		plots++;
	    	}
	    	dummy_url=total_urls.get(i);
	    	if(ret_times.get(i)<120000){
	    		
	        if(dummy_count==0)
	        {
	        	 	series1.getData().add(new XYChart.Data(dummy_url,ret_times.get(i)));
	        }
	        else  if(dummy_count==1)
	        {
	        	series2.getData().add(new XYChart.Data(dummy_url,ret_times.get(i)));
	        }
	        else  if(dummy_count==2)
	        {
	        	series3.getData().add(new XYChart.Data(dummy_url,ret_times.get(i)));
	        }
	        else  if(dummy_count==3)
	        {
	        	series4.getData().add(new XYChart.Data(dummy_url,ret_times.get(i)));;
	        }
	        else
	        {
	        	i++;
	        	continue;
	        }
	        i++;
	         }
	    	  else{
	    		  i++;
		        	continue;
		        }
	        }
	      
Scene scene  = new Scene(bc,800,600);
//bc.getData().addAll(series4, series3, series2,series1);
System.err.println("\nPlotting Graph Now ...");
bc.getData().addAll(series1, series2, series3,series4);
stage.setScene(scene);
stage.show();

	} 
    public static void main(String[] args) {
        launch(args);
        return;
    }
    
  
}