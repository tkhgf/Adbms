package adbmswebcache;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
public class ExecutionPanel {

	
	 
	/**
	 * A utility that downloads a file from a URL.
	 * @author www.codejava.net
	 *
	 */

	    private static final int BUFFER_SIZE = 4096;
	    public static void browseurl(String URL)
	    {
	    	try {
	    	
	        if (Desktop.isDesktopSupported()) {
                // Windows
	        	File url1=new File(URL+".html");
	        	
	        	
	        	//Uncomment below to display web contents  in browser
            //  	Desktop.getDesktop().open(url1);
	        	
	        	
			 } else {
                // Ubuntu
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("/usr/bin/firefox -new-window " + URL);
            }
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	 
	    /**
	     * Downloads a file from a URL
	     * @param fileURL HTTP URL of the file to be downloaded
	     * @param saveDir path of the directory to save the file
	     * @throws IOExceptionxa
	     */
	    public static void downloadFile(String fileURL, String saveDir)
	            throws IOException {
	    	
	        URL url = new URL(fileURL);
	       long stopTime=0;
	        ResultSet rs = null;
	        String retETag=null;
	        Long retTimems =(long) 0;
	        int stalecheck = 0;
	        File folder = new File(saveDir+".html");
	        String retFilename=null;
	        String retLast=null;
	        String retExpiry=null;
	        String retCurrtime=null;
	        long startTime=System.currentTimeMillis();
				try {
					
				  Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		            Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","tiger");
					Statement stmt = con.createStatement();
					
					String sql = "SELECT * FROM cachestorage where URL='"+saveDir+ "'ORDER BY curtimems DESC";
		           rs = stmt.executeQuery(sql);
		         //Retrieve by column name
		            if(rs.next()){
		            	retETag = rs.getString("ETag");
		            	 System.out.print("Retrieved ETag : " + retETag);
		            	 retCurrtime = rs.getString("currtime");
		           retTimems = rs.getLong("curtimems");
		           stalecheck = rs.getInt("stalenesscheck");
		           retFilename=rs.getString("filename");
		           retLast = rs.getString("LastModify");
		           retExpiry = rs.getString("Expiry");
		           System.out.println("\nRetrieved timestamp : "+retTimems+"\nStaleness check time : "+stalecheck);
		           rs.close();
		            }
		            else
		            {
		            	System.out.println("\nNot found in database");
		            }
		      //    System.out.print("Retrieved Expiry : " + rs.getString("EXPIRY")+"\n Expiry : "+ Expiry);
		       //   System.out.print("Retrieved Last Modified : " + rs.getString("LASTMODIFY")+"\n ETag : "+ Last);
		         
		            
		            //Display values
		           
		          stopTime=System.currentTimeMillis();
		          
		            System.out.println("\nRetrieved timestamp : "+retTimems+"\nCurrent timestamp : "+stopTime);
		            if((retTimems != 0)&&(stopTime-retTimems < stalecheck)){         		//validating staleness with staletimecheck of particular website
		            	String query="INSERT INTO CACHESTORAGE VALUES (?,?,?,?,?,?,?,?,?,?)";
		            	//Connection con=null;
		            	System.err.println("\nRetrieving from local storage because of hit within stale check time "+ (stalecheck/1000)+" secs\n");
		            	//TimeUnit.SECONDS.sleep(1);
		            	System.err.println("\nRetrieved timestamp : "+retTimems+"\nCurrent timestamp : "+stopTime+"\nTime Difference between Web hit and Last Updated Time : "+(stopTime-retTimems)/1000+" sec");
		            	PreparedStatement ps=con.prepareStatement(query);
					//	System.out.println("check3");
		            	ps.setString(1, saveDir);
		            	ps.setString(2, saveDir);
		            	ps.setString(3, retFilename);
		            	ps.setString(4, retETag);
		            	ps.setString(5, retCurrtime);
		            	ps.setString(6, retLast);
		            	ps.setString(7, retExpiry);
		            	ps.setLong(8,stopTime-startTime);
		            	//System.out.println("check1");
		            	ps.setLong(9,retTimems);
		            	ps.setLong(10, stalecheck);
		            	//System.out.println("check2");
		            	ps.execute();
		            	con.close(); 
		            	System.err.println("\nRetrieving Time : "+(stopTime-startTime)+" ms");
		            	//Display web contents
		            	browseurl(saveDir);
		            	
		            /*	if (Desktop.isDesktopSupported()) {
		                    // Windows
		                  //  Desktop.getDesktop().browse(new URI(saveDir+".html"));
		                    
		                    
		                } else 	{
		                    // Ubuntu
		                    Runtime runtime = Runtime.getRuntime();
		                    runtime.exec("/usr/bin/firefox -new-window " + fileURL);
		                }*/
		        		return;
		            }
		            
		           
		            System.err.println("\nNot opened in Staleness Check Time");
		            System.err.println("\nRequest server for web page validators...");
		            System.out.println("Printing Web attributes provided by server ...\n");
		            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			        int responseCode = httpConn.getResponseCode();
			          String dbName = "webcache";
			    	  String driver = "oracle.jdbc.driver.OracleDriver";
			    	  
			        // always check HTTP response code first
			        if (responseCode == HttpURLConnection.HTTP_OK) {
			            String fileName = "dummy";
			            String disposition = "dummy";
			            String ETag = "dummy";
			            
			    		String Last = "dummy";
			    		String Cache = "dummy";
			    		String currTime = "dummy";
			    		String Expiry = "dummy";
			    		
			    		
			    		disposition = httpConn.getHeaderField("Content-Disposition");
			            ETag = httpConn.getHeaderField("ETag");
			           // ETag = ETag.replaceAll("\\\"[-+.^:,]/","");
			    		Last = httpConn.getHeaderField("Last-Modified");
			    		
			    		Cache = httpConn.getHeaderField("Cache-Control");
			    		currTime = httpConn.getHeaderField("Date");
			    		Expiry = httpConn.getHeaderField("Expires");
			    		Long currTimems = System.currentTimeMillis();
			    		Long timedif = (long) 120000;
			    	if((Last!=null)&&(currTime!=null)&&(currTime.length()>25)&&(Last.length()>25)){
			    		String h1=currTime.substring(17,19);
			    		String m1=currTime.substring(20,22);	
			    		String s1=currTime.substring(23,25);			    		
			    		String h2=Last.substring(17,19);
			    		String m2=Last.substring(20,22);	
			    		String s2=Last.substring(23,25);
			    		int t1=(Integer.parseInt(h1)*3600+Integer.parseInt(m1)*60+Integer.parseInt(s1));
			    		int t2=(Integer.parseInt(h2)*3600+Integer.parseInt(m2)*60+Integer.parseInt(s2));
			    		int dif = t1 - t2;
			    	
			    		//Calculating staleness check time
			    		if(dif > 43200)   //if last modified before 12 hours
			    		{
			    			timedif= (long) 1800000;	//stale check diff of the website is set to 30 min
			    		}
			    		else if(dif > 10800)  //if last modified before 3 hours
			    		{
			    			timedif = (long) 600000;	//stale check diff of the website is set to 10 min
			    		}
			    		else if(dif > 3600)  //if last modified before 1 hours
			    		{
			    			timedif = (long) 300000;	//stale check diff of the website is set to 5 min
			    		}
			    		else if(dif > 1800)  //if last modified before 30 min
			    		{
			    			timedif = (long) 120000;	//stale check diff of the website is set to 2 min
			    		}
			    		else				//if last modified within last 30 min
			    		{
			    			timedif = (long) 60000;		//stale check diff of the website is set to 1 min
			    		}
			    		System.out.println("Time Difference : "+ t1+" "+ t2+" "+ dif+ "secs");
			    	 	
			    	}
			    	
			    		//System.out.println("retrieved numbers : " +Integer.parseInt(a)+ " "+ Integer.parseInt(b));
			    		
			    		Map<String, List<String>> map = httpConn.getHeaderFields();
			    		
			    	 
			    		
			    	 
			    		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			    			System.out.println("Key : " + entry.getKey() 
			    	                           + " ,Value : " + entry.getValue());
			    			//System.out.println("check3");
			    		}
			    		System.err.println("\nURL : "+fileURL);
			    		System.err.println("ETag  : "+ETag);
			    		System.err.print("Web Hit Time  :" +currTime);
			    		System.err.println("\nLast Modification Time  : "+Last);
			   // 		System.out.println("Etag :" +ETag+"Last : "+Last+"Cache : "+Cache);
			            String contentType = httpConn.getContentType();
			            int contentLength = httpConn.getContentLength();
			 
			            if (disposition != null) {
			                // extracts file name from header field
			                int index = disposition.indexOf("filename=");
			                if (index > 0) {
			                    fileName = disposition.substring(index + 10,
			                            disposition.length() - 1);
			                    System.out.println("2 : "+ fileName);
			                }
			            } else {
			                // extracts file name from URL
			                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
			                        fileURL.length());
			                System.out.println("3 : "+ fileName);
			            }
			 
			            System.out.println("Content-Type = " + contentType);
			            System.out.println("Content-Disposition = " + disposition);
			            System.out.println("Content-Length = " + contentLength);
			            System.out.println("fileName = " + fileName);
			           
			            // opens input stream from the HTTP connection
			            InputStream inputStream = httpConn.getInputStream();
			            String saveFilePath = fileURL.replaceAll("[^a-zA-Z0-9]","");
			          
			           
			             
			            // opens an output stream to save into file
			            FileOutputStream outputStream = new FileOutputStream(saveFilePath+".html");
			         // select etag sort by currtimems where url = fileurl
			          
			            
			            
			            System.out.println("Creating statement..."+"\nRetrieved Etag :"+retETag+"\nFolder : "+folder+"\n"+"ETag : "+ETag+"\n");
		            
		            
		            
		            
		            
		            if((retETag!=null)&&(folder.isFile())&&(ETag.equals(retETag)))
		        	{
		            	 stopTime=System.currentTimeMillis();
		        	//	System.out.println("Entered into if loop");
		        		System.err.println("Retrieving web contents from local storage because of ETAG match");
		        		//TimeUnit.SECONDS.sleep(1);
		        		String query="INSERT INTO CACHESTORAGE VALUES (?,?,?,?,?,?,?,?,?,?)";
		            	//Connection con=null;
						PreparedStatement ps=con.prepareStatement(query);
					//	System.out.println("check3");
		            	ps.setString(1, saveDir);
		            	ps.setString(2, saveDir);
		            	ps.setString(3, fileName);
		            	ps.setString(4, ETag);
		            	ps.setString(5, currTime);
		            	ps.setString(6, Last);
		            	ps.setString(7, Expiry);
		            	
		            	ps.setLong(8,stopTime-startTime);
		            	
		            //	System.out.println("check1");
		            	ps.setLong(9,System.currentTimeMillis());
		            	
		            	ps.setLong(10,timedif);
		            //	System.out.println("check2");
		            	ps.execute();
		            	con.close();
		            	httpConn.disconnect();
		            	System.err.println("\nSet Stale Time Check : "+timedif+"ms"+" i.e., "+(timedif/(60*1000))+"mins");
		            	System.err.println("\nRetrieving Time : "+(stopTime-startTime)+" ms");
		            	//Display web contents
		            	browseurl(saveDir);
		        	/*	if (Desktop.isDesktopSupported()) {
		                    // Windows
		                   // Desktop.getDesktop().browse(new URI(saveDir+".html"));
		                    
		            //        System.out.println("Retrieving Time : "+(stopTime-DescriptionPanel.startTime));
		                } else 	{
		                    // Ubuntu
		                    Runtime runtime = Runtime.getRuntime();
		                    runtime.exec("/usr/bin/firefox -new-window " + fileURL);
		                }*/
		        		return;
		        	}
		         
				
				 System.err.println("Etags not matched or null, so local file contents are stale");
				 System.err.println("Retrieving web contents from server");
				 //TimeUnit.MILLISECONDS.sleep(200);
	            
	            // match both etags from db and http request
	            //if matched contents
	            //retrieve file from local disk , use code in description panel
				
				
	            
	            //if etag not match 
	            int bytesRead = -1;
	            byte[] buffer = new byte[BUFFER_SIZE];
	            while ((bytesRead = inputStream.read(buffer)) != -1) {
	                outputStream.write(buffer, 0, bytesRead);
	             //   System.out.println(bytesRead);
	            }
	           
	            outputStream.close();
	            inputStream.close();
	 
	            System.out.println("File downloaded");
	           // browseurl(fileURL);
	            
	         
	            	
		           /* Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		            	Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","tiger");*/
		            	
		            	//String query="CREATE TABLE NEWCACHESTORAGE (URL VARCHAR(300),FOLDER VARCHAR(200), FILENAME VARCHAR(200), ETAG VARCHAR(100), CURRTIME VARCHAR(150), LASTMODIFY VARCHAR(150), EXPIRY VARCHAR(150), RETTIME INT(10000))";
		            	//Statement stmt=con.createStatement();
		            	
		            	//stmt.execute(query);
		            	
		            	String url2=fileURL.replaceAll("[-+.\"\'\\^:,/]","");
		            	//stmt.executeUpdate("INSERT INTO CACHESTORAGE (URL,FOLDER,FILENAME,ETAG,CURRTIME,LASTMODIFY,EXPIRY)" + 
		            	//"VALUES ("+url2+","+saveDir+","+fileName+","+ETag+","+currTime/+","+Last+","+Expiry+")");
		            	
		            	Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			            //Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","tiger");
		            	
		            	String query="INSERT INTO CACHESTORAGE VALUES (?,?,?,?,?,?,?,?,?,?)";
		            	//Connection con=null;
						PreparedStatement ps=con.prepareStatement(query);
						ps.setString(1, saveDir);
		            	ps.setString(2, saveDir);
		            	ps.setString(3, fileName);
		            	ps.setString(4, ETag);
		            	ps.setString(5, currTime);
		            	ps.setString(6, Last);
		            	ps.setString(7, Expiry);
		            	stopTime=System.currentTimeMillis();
		               	ps.setLong(8,stopTime-startTime);
		            	ps.setLong(9,System.currentTimeMillis());
		            	System.err.println("\nSet Stale Time Check : "+timedif+"ms"+" i.e., "+(timedif/(60*1000))+"mins");
		            	ps.setLong(10,timedif);
		            	ps.execute();
		            	con.close();
		            	
		            	System.err.println("\nRetrieving Time : "+(stopTime-startTime)+" ms");
			            httpConn.disconnect();
			        	//Display web contents
		            	browseurl(saveDir);
			        }
			        /*			Statement stmt = con.createStatement();
	    			ResultSet result = null;
	    			System.out.println("Enter the time in 'x' mins to view the cache perfomance in last 'x' mins :");
	    			Scanner min=new Scanner(System.in);
	    			int mins=min.nextInt();
	    		//	String query="select url,retTime from cachestorage where curTimems >= "+(System.currentTimeMillis()-600000)+" and retTime < 60000 order by url";
	    			String query="select url,retTime from cachestorage where curTimems >= "+(System.currentTimeMillis()-(mins*60*60))+" order by url";
	        	    	 ResultSet res = stmt.executeQuery(query);
	    	    	  String url=null;
	       	       int retTime = 0;
	    	    	 String dummy_url=null;
	    	         int dummy_count=0;
	    	    	int total_count=0;
	    	         while(res.next()){
	    	      // 	 System.out.println(total_count+"string : "+total_urls[total_count]);
	    	        	 Long retrieval_time=res.getLong("rettime");
					   System.out.println("Key : "+res.getString("URL")+" Ret Time : "+retrieval_time );
					    
					   total_urls.add(res.getString("URL"));
				ret_times.add( retrieval_time);
				    	total_count++;
				    	System.out.println(total_count);
	    	         }

	    	    	   	 
	    	    res.close();
	    	*/
					
	         else {
	            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
	        }
				}
	        catch(Exception Ex)
	    	  {
	    		  System.out.println(Ex);
	    	  }
	        
	    }


}
