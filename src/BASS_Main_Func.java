import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BASS_Main_Func {
	
	public static String url="http://em-central.oraclecorp.com/";
	//public static String browserDriverPath="\\"+"\\psbldfs\\dfs\\relops\\em-staging\\Utilities\\Selenium_Add_Ons\\geckodriver.exe";
	public static String browserDriverPath="\\"+"\\psbldfs\\dfs\\relops\\em-staging\\Utilities\\SAT-Staging_Automation_Tool\\Production\\scripts\\BASS_PIA_Process\\geckodriver.exe";
	public static String ssoId;
	public static String ssoPassword;
	public static String LINUX_DPK_SERVER;
	public static String LINUX_APP_USER;
	public static String LINUX_APP_PWD;
	public static String password_Lnx;
	public static String encryptedPassword_Lnx;
	public static String emailPassword;
	public static String encryptedEmailPassword;
	public static String activity;
	public static String tools_build;
	public static String build_id;
	public static String db_name;
	public static String server_name;
	public static String back_up_filename;
	public static String db_stamp_file;
	//public static String satPropertiesFilePath="C:\\SAT-Staging_Automation_Tool_v7.7\\properties\\SAT.properties";
	public static String satPropertiesFilePath=System.getProperty("user.dir")+"\\Properties\\SAT.properties";
	
	public static void main(String[] args) throws Exception {
		
		//To read sat properties
		File file=new File(satPropertiesFilePath);
		if(file.exists())
		{
			Properties prop=new Properties();
			FileInputStream input=new FileInputStream(file);
			prop.load(input);
			ssoId=prop.getProperty("MailTo");
			ssoPassword=prop.getProperty("emailPassword");
			LINUX_DPK_SERVER=prop.getProperty("LINUX_DPK_SERVER");
			LINUX_APP_USER=prop.getProperty("LINUX_APP_USER");
			LINUX_APP_PWD=prop.getProperty("LINUX_APP_PWD");
			emailPassword=prop.getProperty("emailPassword");
			encryptedEmailPassword=prop.getProperty("encryptedEmailPassword");
			encryptedPassword_Lnx=prop.getProperty("encryptedPassword_Lnx");
			password_Lnx=prop.getProperty("password_Lnx");
			activity=prop.getProperty("ACTIVITY");
			tools_build=prop.getProperty("TOOLSBUILD");
			build_id=prop.getProperty("BUILDID");
			db_name=prop.getProperty("DB_PKG");
			server_name=prop.getProperty("SERVER_NAME");
			db_stamp_file="C:\\"+db_name+"_"+build_id+"_BUILD\\DB_STAMP.BMP";
			input.close();
			
			//Encrypting,decrypting & Getting SSO password
			if(encryptedEmailPassword.equals("")){
				encryptedEmailPassword=AESCryptUtils.encrypt(emailPassword);
				FileOutputStream output=new FileOutputStream(file);
				prop.setProperty("encryptedEmailPassword", encryptedEmailPassword);
				prop.setProperty("emailPassword", "*****");
				prop.store(output, null);
				output.close();
			}
			else if(emailPassword.equals("*****"))
				ssoPassword=AESCryptUtils.decrypt(encryptedEmailPassword);
			//Encrypting, decrypting & Getting linux password
			if(encryptedPassword_Lnx.equals("")){ 
				encryptedPassword_Lnx=AESCryptUtils.encrypt(LINUX_APP_PWD);
				FileOutputStream output=new FileOutputStream(file);
				prop.setProperty("encryptedPassword_Lnx", encryptedPassword_Lnx);
				prop.setProperty("password_Lnx", "*****");
				prop.store(output, null);
				output.close();
			}
			else if(password_Lnx.equals("*****"))
				LINUX_APP_PWD=AESCryptUtils.decrypt(encryptedPassword_Lnx);
			

		}
		else
		{
			System.out.println("Failed: SAT propeties file not found in the path: "+satPropertiesFilePath);
			System.exit(1);
		}   
				
		System.setProperty("webdriver.firefox.marionette", browserDriverPath);
		WebDriver driver=new FirefoxDriver();
		BASS2_Functionality bf=new BASS2_Functionality();
		Thread.sleep(5000);
			
		if (args[0].equalsIgnoreCase("hold_nightly"))
		
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.hold_Nightlty_Job(driver, activity);
			bf.logout(driver);
		} 
		else if (args[0].equalsIgnoreCase("queue_close"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.queue_Close(driver, activity);
			bf.logout(driver);
		} 
		else if (args[0].equalsIgnoreCase("search_UOWs"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.search_UOW(driver, activity);
			bf.logout(driver);
		}
		else if (args[0].equalsIgnoreCase("env_sync"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.env_Sync(driver, db_name, activity, server_name);
			bf.logout(driver);
		}
		else if (args[0].equalsIgnoreCase("build_form_locked"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.build_Form_Locked(driver, activity, build_id);
			bf.logout(driver);
		}
		else if(args[0].equalsIgnoreCase("full_db_complete"))
		{
			bf.login_EM(driver,ssoId,ssoPassword, url);
			bf.full_DB_Export_Notification(driver, activity, build_id);
			bf.logout(driver);
		}
		else if (args[0].equalsIgnoreCase("increment_image_number"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.image_Number_Increment(driver, activity, build_id);
			bf.logout(driver);
		}
		else if (args[0].equalsIgnoreCase("queue_open"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			tools_build=tools_build.toLowerCase();
			bf.queue_Open(driver, activity, tools_build);
			bf.logout(driver);
		}
		else if (args[0].equalsIgnoreCase("restart_nightly"))
		{
			bf.login_EM(driver,ssoId,ssoPassword,url);
			bf.restart_Nightly_Job(driver, activity);
			bf.logout(driver);
		}  
		else if(args[0].equalsIgnoreCase("build_complete_notify"))
		{
			bf.login_EM(driver,ssoId,ssoPassword, url);
			back_up_filename=bf.getBkpFileName(LINUX_DPK_SERVER, LINUX_APP_USER, LINUX_APP_PWD, activity, build_id);
			if (back_up_filename==null)
				System.out.println("Failed: Back file name is null, so can't be proceed....");
			else if(back_up_filename.contains("No such file or directory"))
				System.out.println("Failed: Wrong command executed.....");
			else
			{
				bf.build_Complete_Notify(driver, activity, build_id, back_up_filename,db_stamp_file);
			}
			bf.logout(driver);
		}
		else if(args[0].equalsIgnoreCase("build_form_aval"))
		{
			bf.login_EM(driver,ssoId,ssoPassword, url);
			bf.build_Form_Available(driver, activity, build_id);
			bf.logout(driver);
		}
		else if(args[0].equalsIgnoreCase("build_form_next"))
		{
			bf.login_EM(driver,ssoId,ssoPassword, url);
			bf.build_Form_Next(driver, activity);
			bf.logout(driver);
		}
		else if(args[0].equalsIgnoreCase("sync_packaging_tables"))
		{
			bf.login_EM(driver,ssoId,ssoPassword, url);
			bf.sync_Packaging_Tables(driver, activity, server_name);
			bf.logout(driver);
		}
		else if(args[0].equalsIgnoreCase("generate_pkg_scripts"))
		{
			bf.login_EM(driver,ssoId,ssoPassword, url);
			bf.generate_Pkg_Scripts(driver,activity );
			bf.logout(driver);
		}
		else
		{
			System.out.println("Failed: Wrong input step name");
		}
	}

}
