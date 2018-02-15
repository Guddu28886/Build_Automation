import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class BASS2_Files {
	// Not required now if required use it to read & write log
	public static String error_path;
	public static String db_logs_path;
	
	public static void logFileWriter(String buildLogFolder,String logFilename,String build_log_details) throws Exception
	{
		db_logs_path=buildLogFolder+"DB_Logs";
		File file=new File(db_logs_path+"\\"+logFilename+".txt");
		System.out.println("Log File Name: "+db_logs_path+"\\"+logFilename+".txt");
		Thread.sleep(5000);
		if(!(file.exists()))
				{
				System.out.println("Log file is not created by SAT in the path "+db_logs_path+". Hence exiting");
				System.exit(1);
				}
		FileWriter fw = new FileWriter(db_logs_path+"\\"+logFilename+".txt",true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(build_log_details);
		bw.close();
		logFileReader(buildLogFolder, logFilename);
		
	}
	public static void logFileReader(String buildLogFolder,String logFilename) throws Exception
	{
		error_path=buildLogFolder+"Errors";
		db_logs_path=buildLogFolder+"DB_Logs";
		String currentDate=BASS2_Functionality.getCurrentDateTime("yyyyMMddHHmm").toString();
		InputStream inStream = null;
    	OutputStream outStream = null;
		BufferedReader in = new BufferedReader(new FileReader(db_logs_path+"\\"+logFilename+".txt"));
        String str;
        while ((str = in.readLine()) != null) 
        {
        	if (str.contains("Error")||str.contains("Failed")||str.contains("error")||str.contains("failed"))
        		{
		        		
		        		try{
		        			System.out.println(str);
		            	    File log_file =new File(db_logs_path+"\\"+logFilename+".txt");
		            	    File error_file=new File(error_path+"\\"+logFilename+".error."+currentDate+".Validated");
		            	    inStream = new FileInputStream(log_file);
		            	    outStream = new FileOutputStream(error_file);
		                	
		            	    byte[] buffer = new byte[1024];
		            		
		            	    int length;
		            	    //copy the file content in bytes 
		            	    while ((length = inStream.read(buffer)) > 0)
		            	    {
		            	      	outStream.write(buffer, 0, length);
		            	    }
		            	    
		               	    inStream.close();
		            	    outStream.close();
		            	    in.close();
		            	    //delete the original file
		            	    log_file.delete();
		            	    }
		            	catch(IOException e)
			            	{
			            	    e.printStackTrace();						
			            	}
        		System.exit(1);
        		}
          }
       in.close();
       
	}   

}
