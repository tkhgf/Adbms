
package adbmswebcache;

import java.net.*;
import java.awt.Desktop;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.lang.*;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
 

public class DescriptionPanel {
	public static long startTime=0;
	public static long checkStartTime=System.currentTimeMillis();

	public static ArrayList<String> total_urls = new ArrayList<String>();
	public static ArrayList<Long> ret_times = new ArrayList<Long>();
	
 
    public static void main(String[] args) throws Exception {

    	/*String url = "jdbc:mysql://localhost:3306/";// ( MySQL server in xampp is installed in port 3306)*/
    	 
    	  /*String userName = "root";
    	  String password = "";*/
    	  ExecutionPanel w=new ExecutionPanel();
    	  RefreshLocalContent r=new RefreshLocalContent();
    	  try {
    		  
    		  //Oracle Db connection 
    		  String dbName = "webcache";
        	  String driver = "oracle.jdbc.driver.OracleDriver";
    		  Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
          	  Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","tiger");
          	
          	//Demo Starts Here
          	System.err.println("\n   ***###$$$   DEMO BEGINS   $$$###***    \n");
          	TimeUnit.SECONDS.sleep(2);
          	int c=0;
          	while(System.currentTimeMillis()-checkStartTime>(180*60*1000)||c==0){
          		
          checkStartTime=System.currentTimeMillis();
          c=1;
          	System.err.println("\nFirst Step : Check Network Traffic and Bandwidth Availability\n");
          	TimeUnit.SECONDS.sleep(1);
          	long beginTime = System.currentTimeMillis();
          	String[] urls = {"http://www.flipkart.com","http://www.indiatimes.com","http://www.newyorktimes.com","http://www.weeklystandard.com"};
          	for(int i=0;i<urls.length;i++){
          	w.downloadFile(urls[i],urls[i].replaceAll("[^a-zA-Z0-9]",""));
          	}
          	long endTime=System.currentTimeMillis();
          	System.err.println("\nAverage Retrieving Time : "+(endTime-beginTime)/urls.length+"ms");
          	if((endTime-beginTime)/urls.length<1000){
          		// Network Traffic Status Low
          		System.err.println("\nNetwork Traffic Status : Low\n");
          		System.out.println("\nUpdate Local Cache Web Contents in 2 sec ... \n");
          		TimeUnit.SECONDS.sleep(2);
          			r.refresh();
          		//	System.out.println("Control Came Back");
          	}
          	else{
          		//Network Traffic Status High
          		System.err.println("\nNetwork Traffic Status : High\n");
          		System.out.println("\nBrowser Menu Will be displayed in  2 sec ... \n");
          		TimeUnit.SECONDS.sleep(2);
          	}
          	
          	
         //Menu
        System.err.println("\n***Menu***\n 1. Enter to browse url \n 2. To refresh local webcontents \n 3. Plot Web Cache Performance Graph\n"
        		+ " Enter your Choice : ");
    	Scanner scanner = new Scanner(System.in);
    	int choice = scanner.nextInt();    	
    	while(choice == 1 || choice ==2 || choice ==3){
    		if(choice ==1){
    	System.err.println("Enter Url Here :");
    	Scanner u = new Scanner(System.in);
    	//Take Input web url
    	String weburl = u.nextLine();
    	startTime=System.currentTimeMillis();
        String fname= weburl.replaceAll("[^a-zA-Z0-9]","");
     	//Browse url
    	w.downloadFile(weburl, fname);      
    	}
    	else if (choice ==2) {
    			//Refresh Web Contents
    			r.refresh();
    	}
    	else if (choice==3){  
    		//Plot web cache performance graph
    			sampleGraph s = new sampleGraph();
    			s.main(args);
    	}
    		
    		
    		
    		 System.err.println("\n***Menu***\n 1. Enter to browse url \n 2. To refresh local webcontents \n 3. Plot Web Cache Performance Graph\n"
    	        		+ " Enter your Choice : ");
    		 choice = scanner.nextInt();
    	}
          	}
    	  }
          	
    	  catch(Exception Ex)
    	  {
    		  System.out.println(Ex);
    	  }
    }

	
}