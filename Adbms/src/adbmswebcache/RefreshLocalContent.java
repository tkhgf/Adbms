package adbmswebcache;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ArrayListMultimap;

public class RefreshLocalContent {
	public static void refresh() throws SQLException, IOException, InterruptedException
	{
		long startTime = System.currentTimeMillis();
		System.out.println("\nUpdating Web Cache Local File Contents will start in 2 secs....");
		//TimeUnit.SECONDS.sleep(2);
		try
		{
			
			ExecutionPanel ex=new ExecutionPanel();
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
		
        Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","system","tiger");
		Statement stmt = con.createStatement();
		ResultSet result = null;
		String sql = "SELECT * FROM cachestorage order by url"; 
       result = stmt.executeQuery(sql);
       String retETag=null;
       Long retTimems =(long) 0;
       int stalecheck = 0;
      
       String retFilename=null;
       String retLast=null;
       String retExpiry=null;
       String retCurrtime=null;
		
      //Retrieve by column name
       int count=0;
       
       final SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
       f.setTimeZone(TimeZone.getTimeZone("UTC"));
       String present=f.format(new Date());
       System.out.println("\ncurrent date : "+present);
     //  int mm = Integer.parseInt(present.substring(5,7));
       int d = Integer.parseInt(present.substring(8,10));
       int h = Integer.parseInt(present.substring(11,13));
       int m = Integer.parseInt(present.substring(14,16));
       int pres_mins=m+(h*60)+(d*60*24);
       System.out.println("\nPresent Index : "+pres_mins); 
    // create multimap to store key and values
       	ArrayListMultimap<String,Integer> map = ArrayListMultimap.create();
      // Map<String, List<Integer>> map = new HashMap();
       List<Integer> values=new ArrayList();
        while(result.next()){
        	count++;
        	System.out.println("\nRetrieved URL : "+result.getString("URL")+"\tRetrieved Time : "+result.getLong("rettime")+"\tWeb hit at time :"+result.getString("currtime"));
        	retETag = result.getString("ETag");
       	 System.out.print("Retrieved ETag : " + retETag);
       	 retCurrtime = result.getString("currtime");
       	 
       	if((retCurrtime!=null)&&(retCurrtime.length()>25)){
       		String dd=retCurrtime.substring(5,7);
    		String h1=retCurrtime.substring(17,19);
    		String m1=retCurrtime.substring(20,22);	
    		String s1=retCurrtime.substring(23,25);			    		
    		//Check whether daily index or weekly index corresponds to less than 180 min i.e., 3 hours
    		//if both daily index and weekly index is less than 180 min , the web hit corresponds to be one of daily hit or week hit that needs 
    		//be update now.
    		
    		//if daily index is less than 180 min and the difference index is varied with the difference index of previous web hit, that web hit is 
    		// considered as daily web hit
    		
    		//if daily index is less than 180 min and the difference index is not varying and not less than 180 min, it is a web hit no need to 
    		//update as it is not considered to open in next 180 min
    		int web_hit_mins=(Integer.parseInt(h1)*60+Integer.parseInt(m1)+(Integer.parseInt(dd)*24*60));
    		int daily_stale_check_index = (pres_mins-web_hit_mins)%(24*60);
    		int weekly_stale_check_index = (pres_mins-web_hit_mins)%(7*24*60);
    		System.out.println("\nStale check Index for this web hit : "+result.getString("URL")+" Daily index : "+daily_stale_check_index+ " Weekly index :"+weekly_stale_check_index);
    		
    		if((daily_stale_check_index<180)||(weekly_stale_check_index<180)){
    		map.put(result.getString("URL"),(daily_stale_check_index-weekly_stale_check_index));
    		//count++;
    		}
       	}
       	else
       	{
       		System.out.println("Current time not found");
       	}
       	 
       	 
      retTimems = result.getLong("curtimems");
      stalecheck = result.getInt("stalenesscheck");
      retFilename=result.getString("filename");
      retLast = result.getString("LastModify");
      retExpiry = result.getString("Expiry");
      
        	
          }
        result.close();
        System.out.println("Count of Records : "+count);
        Iterator keys=null;
       	keys=map.keySet().iterator();
       	//count=0;
       	String key=null;
       	//values=null;
       	int update_url_count=0;
        while(keys.hasNext())
        {	
        	key=(String) keys.next();
        	System.out.println("\n********************************\n");
        	System.out.println("Key Running : "+key+"\n");
        	values = map.get(key);
        	int update_daily = 0,update_weekly = 0,no_update_weekly=0;
        	for (int i=0;i<values.size()-1;i++){
        	System.out.println("values list size : "+values.size());
        		//Check whether daily index or weekly index corresponds to less than 180 min i.e., 3 hours
        		//if both daily index and weekly index is less than 180 min , the web hit corresponds to be one of daily hit or week hit that needs 
        		//be update now.
        		
        		//if daily index is less than 180 min and the difference index is varied with the difference index of previous web hit, that web hit is 
        		// considered as daily web hit
        		
        		//if daily index is less than 180 min and the difference index is not varying and not less than 180 min, it is a web hit no need to 
        		//update as it is not considered to open in next 180 min
        		if(i==0){
        			if(values.get(i)==0){
        				//considered for both weekly and daily update
        				update_daily++;		
        				update_weekly++;
        			}
        			else if(values.get(i)!=values.get(i+1)){
        				//update daily only
        				update_daily++;
        			}
        			else if(values.get(i)==values.get(i+1))
        			{
        				//updated weekly but not going to open in next 180 mins
        				no_update_weekly++;
        			}
        		}
        		else{
        			if(values.get(i)==0){
        				//considered for both weekly and daily update
        				update_daily++;		
        				update_weekly++;
        			}
        			else if(values.get(i)!=values.get(i-1)){
        				//update daily only
        				update_daily++;
        			}
        			else if(values.get(i)==values.get(i-1))
        			{
        				//It is weekly web hit record but the difference between
        				//updated weekly but not going to open in next 180 mins
        				no_update_weekly++;
        			}
        		}
        		
        		System.out.println("\nKey : "+key+"\tDifference Index : "+values.get(i));
        		//count++;
        	//	System.out.println("Count of Records : "+count);
               	
        	}
        	if( ( ((update_weekly>=update_daily)?update_weekly:update_daily) >= no_update_weekly ) ? true : false ){
        		System.err.println("\nWeb site Need to be updated : "+key.substring(0,4)+"://"+key.substring(4,7)+"."+key.substring(7,key.length()-3)+"."+key.substring(key.length()-3,key.length()));
        		//TimeUnit.SECONDS.sleep(1);
        		String update_url=key.substring(0,4)+"://"+key.substring(4,7)+"."+key.substring(7,key.length()-3)+"."+key.substring(key.length()-3,key.length());
        		ex.downloadFile(update_url, key);
        		update_url_count++;
        		//ex.downloadFile(key.substring(0,4)+"://"+key.substring(4,7)+"."+key.substring(7,key.lastIndexOf(key)-3)+"."+key.substring(key.lastIndexOf(key)-3,key.lastIndexOf(key)),key);
        	} 
        	else{
        		System.err.println("\nWeb site Not Needed to be updated : "+key);
        		//TimeUnit.MILLISECONDS.sleep(300);
            }
         }
        System.err.println("\nCount of Web Hit History Records in database : "+count);
       	System.err.println("\nTotal Number of Websites updated are "+update_url_count+ " in "+(System.currentTimeMillis()-startTime)+" ms");
   
        return;
	
	} catch (InstantiationException | IllegalAccessException
			| ClassNotFoundException e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
	}

}
}