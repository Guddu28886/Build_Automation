import java.awt.event.KeyEvent;
import java.awt.AWTException;
import java.awt.Toolkit;
import java.awt.Robot;
import java.awt.datatransfer.StringSelection;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class BASS2_Functionality {
	
	public String log_details;
	
	//For EM-Central login
	public void login_EM(WebDriver driver,String ssoid,String ssopassword,String url) throws InterruptedException
	{
		
		try{
		//Enter URL
			driver.get(url);
			driver.manage().window().maximize();
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			//'Username' field entry
			WebElement uid = driver.findElement(By.xpath("//*[@id='sso_username']"));
			uid.clear();
			uid.sendKeys(ssoid);
			//'Password' field entry
			WebElement pwd = driver.findElement(By.xpath("//*[@id='ssopassword']"));
			pwd.clear();
			pwd.sendKeys(ssopassword);
			//'Sign In' button click 
			WebElement login_btn = driver.findElement(By.className("submit_btn"));
			login_btn.click();
			Thread.sleep(5000);
			if(driver.findElements(By.id("required")).size()>0) 
			{
				System.out.println("Failed: Please update SSOID & SSO Password in SAT property file");
				System.exit(1);
			}
			else if(driver.findElements(By.id("errormsg")).size()>0)
			{
				System.out.println("Failed: Please update SSO Password in SAT property file");
				System.exit(1);
			}
			/*if(driver.findElement(By.id("required")).getText().contains("Please enter your Username and Password"))
			{
				System.out.println("Failed: Please update SSOID & SSO Password in SAT property file");
				System.exit(1);
			}
			else if(driver.findElement(By.id("errormsg")).getText().contains("Invalid login"))
			{
				System.out.println("Failed: Please update SSO Password in SAT property file");
				System.exit(1);
			} */
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		}
		
		
	//For common login
	public void login(WebDriver driver) throws Exception
	{
		driver.get("http://slcianr.us.oracle.com:8200/psp/EMHD8SPQ/?cmd=login&languageCd=ENG");
		//driver.manage().window().maximize();
		WebElement uid = driver.findElement(By.id("userid"));
		uid.sendKeys("PSQA");
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		WebElement pwd = driver.findElement(By.id("pwd"));
		pwd.sendKeys("QA");
		WebElement login_btn = driver.findElement(By.className("psloginbutton"));
		login_btn.click();
	}
	//For Logout & close browser
	public void logout(WebDriver driver)
	{
		try
		{
		driver.switchTo().defaultContent();
		//'Logout' link click
		driver.findElement(By.id("pthdr2logout")).click();
		driver.close();
		}
		catch(Exception e)
		{
			
		}
	}
	//Queue Close before the build starts
	public void queue_Close(WebDriver driver,String activity)
	{
		
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebDriverWait wait=new WebDriverWait(driver, 10);
		System.out.println("..........Queue Close process Started..........\n");
		try
		{
				//Navigate to 'BASS2'
				driver.findElement(By.xpath("//*[@id='fldra_UOW_FOLDER']")).click();
				System.out.println("Success: BASS2 navigated \n");
				//Navigate to 'Administration'
				driver.findElement(By.xpath("//*[@id='fldra_B2_ADMIN']")).click();
				System.out.println("Success: Administration navigated \n");
				//Navigate to 'Define Target Release'
				driver.findElement(By.xpath("//*[@id='crefli_ADM_TRGT_REL']/a")).click();
				System.out.println("Success: Define Target Release navigated \n");
				
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(driver.findElement(By.id("ptifrmtgtframe"))));
				//'Activity Name' field filling
				driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).sendKeys(activity);
				System.out.println("Success: 'Activity Name' set to "+activity+"\n");
				//Click on 'Search' button
				driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
				System.out.println("Success: 'Search' button clicked \n");
				//Checking the activity & then queue closed
				if(driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_RELNAME']")).getAttribute("value").equalsIgnoreCase(activity))
				{
					//Selecting Queue Status 'Closed'
					System.out.println("Success: Activity name "+driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_RELNAME']")).getAttribute("value")+" is correct \n");
					Select queue_status=new Select (driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_MSTAPPLYMODE']")));
					queue_status.selectByVisibleText("Closed");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Saved']")));
					
					driver.findElement(By.xpath("//*[@id='#ICList']")).click(); //Return to Search button
					Thread.sleep(5000);
					//To re-check the queue close status
					//----------------------------------
					System.out.println("Rechecking of queue status started....");
					driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).clear();
					driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).sendKeys(activity);
					System.out.println("Success: 'Activity Name' set to "+activity+"\n");
					//Click on 'Search' button
					driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
					Select queue_status_check=new Select (driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_MSTAPPLYMODE']")));				
					if(queue_status_check.getAllSelectedOptions().get(0).getText().equalsIgnoreCase("Closed"))
					{
						System.out.println("Success: Queue Status is changed to: Closed");
						System.out.println("\n..........Queue Close completed..........\n");
						System.out.println("Passed:");
					}
					else
						System.out.println("Failed: Queue close failed, please re-run");
					
				}
				else
				{
					System.out.println("Activity "+activity+" is different than which is present in the page:"+driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_RELNAME']")).getAttribute("value"));
					System.out.println("\nFailed: Queue close failed,activity is different.");
				}
		}
		catch(Exception e)
		{
			System.out.println("\nFailed: Queue close failed");
		}
		
	}
	//Queue Open after the build
	public void queue_Open(WebDriver driver,String activity,String tools_build) throws Exception
	{
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebDriverWait wait=new WebDriverWait(driver, 10);
		System.out.println("...........Queue Open process Started..........\n");
		try
		{
			//Navigate to 'BASS2'
			driver.findElement(By.xpath("//*[@id='fldra_UOW_FOLDER']")).click();
			System.out.println("Success: BASS2 navigated \n");
			//Navigate to 'Administration'
			driver.findElement(By.xpath("//*[@id='fldra_B2_ADMIN']")).click();
			System.out.println("Success: Administration navigated \n");
			//Navigate to 'Define Target Release'
			driver.findElement(By.xpath("//*[@id='crefli_ADM_TRGT_REL']/a")).click();
			System.out.println("Success: Define Target Release navigated \n");
			
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			//'Activity Name' field filling
			driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).sendKeys(activity);
			System.out.println("Success: 'Activity Name' set to "+activity+"\n");
			//Click on 'Search' button
			driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
			System.out.println("Success: 'Search' button clicked \n");
			Thread.sleep(5000);
			//Checking the activity & then queue open
			if(driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_RELNAME']")).getAttribute("value").equalsIgnoreCase(activity))
			{
				//Set 'Queue Status' to Open
				Thread.sleep(2000);
				Select queue_status=new Select (driver.findElement(By.id("UOW_RELDEFN_UOW_MSTAPPLYMODE")));
				queue_status.selectByVisibleText("Open");
				Thread.sleep(2000);
				//Patch Release field update
				String patch_Release=tools_build.substring(5,7);
				driver.findElement(By.id("UOW_RELDEFN_UOW_TOOLSRELMNR")).clear();
				driver.findElement(By.id("UOW_RELDEFN_UOW_TOOLSRELMNR")).sendKeys(patch_Release);
				System.out.println("Patch Realese: "+patch_Release);
				Thread.sleep(2000);
				//set 'PS_HOME' drop down
				String ps_home="\\"+"\\psbldfs\\dfs\\build\\pt\\ptship\\"+tools_build+"\\"+"install_Windows.ora";
				Select ps_home_select=new Select (driver.findElement(By.id("UOW_RELDEFN_UOW_PSHOME")));
				ps_home_select.selectByVisibleText(ps_home);
				Thread.sleep(2000);
				System.out.println("PS Home selected as: '"+ps_home+"'");
				System.out.println("Queue Status is selected to: 'Open'");
				//Save button clicked
				driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
				System.out.println("'Save' button clicked");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Saved']")));
				//'Return to Search' button click
				driver.findElement(By.xpath("//*[@id='#ICList']")).click(); 
				Thread.sleep(3000);
				//To Validate the queue close status
				//----------------------------------
				System.out.println("...Validating the queue status...");
				driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).clear();
				driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).sendKeys(activity);
				System.out.println("Success: 'Activity Name' set to "+activity+"\n");
				//Click on 'Search' button
				driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
				Thread.sleep(5000);
				
				//if(driver.findElement(By.id("UOW_RELDEFN_UOW_MSTAPPLYMODE")).getAttribute("value").equalsIgnoreCase("Open"))
				Select queue_status_check=new Select (driver.findElement(By.id("UOW_RELDEFN_UOW_MSTAPPLYMODE")));
				Select ps_home_select_check=new Select (driver.findElement(By.id("UOW_RELDEFN_UOW_PSHOME")));
				
				if(queue_status_check.getAllSelectedOptions().get(0).getText().equalsIgnoreCase("Open"))
				{
					System.out.println("Queue Status verified as: "+queue_status_check.getAllSelectedOptions().get(0).getText());
					System.out.println("PS Home verified as: "+ps_home_select_check.getAllSelectedOptions().get(0).getText());
					System.out.println("Patch Realese verified as: "+driver.findElement(By.id("UOW_RELDEFN_UOW_TOOLSRELMNR")).getAttribute("value"));
					System.out.println("\n..........Queue Open completed..........\n");
					System.out.println("Passed:");
				}
				else
				{
					System.out.println("Queue Status verified wrong as: "+queue_status_check.getAllSelectedOptions().get(0).getText());
					System.out.println("PS Home verified as: "+ps_home_select_check.getAllSelectedOptions().get(0).getText());
					System.out.println("Patch Realese verified as: "+driver.findElement(By.id("UOW_RELDEFN_UOW_TOOLSRELMNR")).getAttribute("value"));
					System.out.println("Failed: Queue open failed, please re-run");
				}
			}
			else
			{
				System.out.println("Activity "+activity+" is different than which is present in the page:"+driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_UOW_RELNAME']")).getAttribute("value"));
				System.out.println("\nFailed:");
			}
		}
		catch(Exception e)
		{
			System.out.println("\nFailed: Queue open failed");
		}
	}
	//For Environment Sync Process
	public void env_Sync(WebDriver driver,String environment,String activity,String server_name) throws Exception
	{
		try
		{
			//Navigating to env_sync page
			System.out.println("..........Environment Sync Process Started...........\n");
			driver.switchTo().defaultContent();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			WebDriverWait wait = new WebDriverWait(driver, 15);
			driver.findElement(By.xpath("//*[@id='fldra_UOW_FOLDER']")).click();
			driver.findElement(By.xpath("//*[@id='fldra_B2_ADMIN']")).click();
			driver.findElement(By.xpath("//*[@id='crefli_B2_SYNC_ENV_GBL']/a")).click();
			System.out.println("Navigated Environment Sync page\n");
						
			//searching for the "Run Control ID"
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			Select run_control_id_select=new Select (driver.findElement(By.xpath("//*[@id='#ICKeySelect']")));
			run_control_id_select.selectByVisibleText("Run Control ID");
			System.out.println("Search criteria set to 'Run Control ID' \n");
			//Set 'Run Control ID' field value
			WebElement run_control_id = driver.findElement(By.xpath("//*[@id='B2_SYNC_ENV_RUN_CNTL_ID']"));
			String run_control_id_value=activity+"_Major";
			run_control_id.sendKeys(run_control_id_value);
			driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
			System.out.println("Run Control ID: "+run_control_id_value+" searched\n");
			Thread.sleep(2000);
			
			//Before clicking Run button verifying Environment,Using Current Image Number checked & Skip Component Relationship unchecked
			if(driver.findElement(By.id("B2_SYNC_ENV_ED")).getAttribute("value").equalsIgnoreCase(environment))
			{
				if(driver.findElement(By.id("B2_SYNC_ENV_UOW_CURRENT_IMG_FG")).isSelected()!=true)
				{
					Thread.sleep(2000);
					driver.findElement(By.id("B2_SYNC_ENV_UOW_CURRENT_IMG_FG")).click();
					Thread.sleep(3000);
					System.out.println("Current Image number checkbox was not selected, so selected\n");
				}
				if(driver.findElement(By.id("B2_SYNC_ENV_UOW_SKIP_CRS")).isSelected()==true)
				{
					Thread.sleep(2000);
					driver.findElement(By.id("B2_SYNC_ENV_UOW_SKIP_CRS")).click();
					System.out.println("Skip component relationship checkbox was selected, so de-selected\n");
					
				}
				//Click on 'Save' button
				driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
				System.out.println("Current Image number checkbox selected & Skip component relationship checkbox unchecked");
				Thread.sleep(3000);
				//Click on 'Run' button
				driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_LOADPRCSRQSTDLGPB']")).click();
				System.out.println("'Run' button clicked");
				Thread.sleep(3000);
				
				//Before submitting the process verifying the Run Control Id 
				driver.switchTo().defaultContent();
				driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				if(driver.findElement(By.id("PRCSRQSTDLG_WRK_RUN_CNTL_ID")).getText().equalsIgnoreCase(run_control_id_value))
				{
					System.out.println("Run Control Id in the page is: "+driver.findElement(By.id("PRCSRQSTDLG_WRK_RUN_CNTL_ID")).getText()+" correct\n");
					driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
					Select select_server=new Select (driver.findElement(By.id("PRCSRQSTDLG_WRK_SERVERNAME")));
					select_server.selectByVisibleText(server_name);
					Thread.sleep(2000);
					System.out.println("Server selected as: "+select_server.getFirstSelectedOption().getText()+"\n");
					//Click 'OK' to submit
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					System.out.println("Process Submitted\n");
					Thread.sleep(3000);
					//Get the process instance number
					String instance=driver.findElement(By.id("PRCSRQSTDLG_WRK_DESCR100")).getText();
					System.out.println(instance+"\n");
					String prc_Instance="";
					prc_Instance=instance.substring(instance.indexOf(":") + 1);
					//Clicking on process monitor link
					driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_LOADPRCSMONITORPB']")).click(); 
					System.out.println("Process Monitor link clicked\n");
					//Sending process instance number to "Instance" field
					driver.findElement(By.xpath("//*[@id='PMN_DERIVED_PRCSINSTANCE']")).clear();
					driver.findElement(By.xpath("//*[@id='PMN_DERIVED_PRCSINSTANCE']")).sendKeys(prc_Instance);
					
					//Clicking "Refresh" button
					driver.findElement(By.xpath("//*[@id='REFRESH_BTN']")).click();
					Thread.sleep(2000);
					System.out.println("Instance: "+prc_Instance+" is searched\n");
					//Checking the Environment sync status continuously
					System.out.println("Refreshing..........\n");
					while(true)
					{
						Thread.sleep(20000);
						driver.findElement(By.xpath("//*[@id='REFRESH_BTN']")).click();
						Thread.sleep(5000);
						if(driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText().equalsIgnoreCase("Success")==true)
						{
							System.out.println("Environment Run Status is Success for the process: "+prc_Instance+"\n");
							if((driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_DISTSTATUS$0']")).getText().equalsIgnoreCase("Posted")))
								
							{
								System.out.println("Distribution Status is 'Posted'");
								break;
							}
							else
							{
								continue;
							}
						}
						else if((driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText().equalsIgnoreCase("No Success")==true))
						{
							System.out.println("Failed: Environment Sync process is 'No Success' for process: "+prc_Instance+", so check & re-run\n");
							break;
						}
						
					  }//while loop ends 
					//checking UOWs processing for any error
					//--------------------------------------
					if((driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText().equalsIgnoreCase("Success"))
							&(driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_DISTSTATUS$0']")).getText().equalsIgnoreCase("Posted")))
					{
						//Checking for UOW processing status
						System.out.println("Checking UOWs processing for any errors..........");
						driver.findElement(By.id("PRCSDETAIL_BTN$0")).click();
						System.out.println("Details link clicked.....");
						driver.switchTo().defaultContent();
						driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
						driver.findElement(By.id("PMN_DERIVED_MESSAGELOG_BTN")).click();
						System.out.println("Message log clicked");
						//Thread.sleep(5000);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("PMN_PRCSLIST_PRCSINSTANCE")));
						String msgText="";
						int error_counter=0;
						
						//For loop is to check UOW processing table
						for(int i=0;i<=50;i++)
						{
							if(i<=49)
							{
								msgText=driver.findElement(By.id("PMN_DERIVED_MESSAGE_DESCR$"+i+"")).getText();
								if(msgText.contains("error"))
								{
									System.out.println("Errors:\n"+msgText);
									error_counter=error_counter+1;
								}
								else if(msgText.contains("Successfully posted generated files to the report repository"))
								{
									break;
								}
							}
							else
							{
								driver.findElement(By.xpath("//*[@id='PMN_MSGLOG_VW$hdown$0']/img")).click();
								Thread.sleep(5000);
								i=0;
								continue;
							}
						} //for loop ends here for UOW processing table
						
						//Error counter check
						//------------------
						if(error_counter==0)
						{
							System.out.println("UOW processing has no errors, so proceed.");
							System.out.println(".....Environment Sync Process Completed.....");
							System.out.println("Passed:");
						}
						else
						{
							System.out.println("UOW processing has "+error_counter+" errors, so need to be reported");
							System.out.println("Failed:");
						}//Error counter check ends here
						//------------------
						
					} //-----------------------------------------------------------------------
					//if ends here,checking UOWs processing for any error process completed
					
				 }
				
				
				else //else for if, Run control id check
				{
					System.out.println("Failed: Run control id is wrong: "+driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_RUN_CNTL_ID']")).getText()+"\n");
				}
				
			}
			else //else for if, to ckeck environment
			{
				System.out.println("Environment present: "+driver.findElement(By.id("B2_SYNC_ENV_ED")).getAttribute("value")+" is different than: "+environment+"\n");
				System.out.println("Failed:Yes \n");
			}
		}
		catch(Exception e)
		{
			System.out.println("Failed: Environment Sync Process failed/n");
			e.printStackTrace();
		}
		
	}
	
	//search for unit of work, if no uow pop-up comes then return true
	public void search_UOW(WebDriver driver,String activity) throws Exception
	{
		System.out.println("..........Search UOWs process started..........");
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebDriverWait wait=new WebDriverWait(driver, 10);
		try
		{
			//BASS2 navigation
			driver.findElement(By.xpath("//*[@id='fldra_UOW_FOLDER']")).click();
			//Search unit of work navigation
			driver.findElement(By.xpath("//*[@id='crefli_UOW_SEARCH']/a")).click();
			System.out.println("Success: Navigated to Search unit of work \n");
		}
		catch(Exception e)
		{
			System.out.println("Failed: Navigation failed to Search unit of work \n");
			System.exit(1);
		}
		
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(driver.findElement(By.id("ptifrmtgtframe"))));
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		//Insert 'Release' field value as activity
		driver.findElement(By.xpath("//*[@id='DERIVED_UOW_UOW_SRCHUOWREL']")).sendKeys(activity);
		System.out.println("Realease field set as: "+driver.findElement(By.xpath("//*[@id='DERIVED_UOW_UOW_SRCHUOWREL']")).getAttribute("value")+"\n");
		
		driver.findElement(By.id("DERIVED_UOW_UOW_SRCHUOWBUG")).click();
		Thread.sleep(3000);
		Select status=new Select(driver.findElement(By.xpath("//*[@id='DERIVED_UOW_UOW_SRCHUOWSTATUS']")));
		status.selectByVisibleText("Approved");
		System.out.println("Status field set as: Approved");
		Select bug_Type=new Select(driver.findElement(By.id("DERIVED_UOW_UOW_SRCHUOWTYPE")));
		bug_Type.selectByValue("");
		System.out.println("Bug Type set as blank \n");
		Thread.sleep(3000);
		//search button click
		driver.findElement(By.id("DERIVED_UOW_UOW_SEARCH_BTN")).click();
		System.out.println("Search button clicked");
		Thread.sleep(2000);
		
			try
			{
				//Check for UOWs are present
				driver.findElement(By.className("PSLEVEL1GRIDLABEL")).getText().contains("Select Unit of Work");
				System.out.println("Failed: UOWs are present for the activity: "+activity+", so need to be reported \n");
				System.exit(1);
			}
			catch (Exception e) 
			{
				driver.switchTo().defaultContent();
				//Check for UOWs not present
				if((driver.findElement(By.xpath("//*[@id='alertmsg']/span")).getText().equalsIgnoreCase("Error: No Unit Of Work found.  Please enter different search criteria. (0,0)")))
				{
					driver.findElement(By.xpath("//*[@id='#ICOK']")).click();
					System.out.println("UOWs are not present for the activity "+activity+", so proceed. \n");
					System.out.println("UOWs search process completed");
					System.out.println("Passed:");
				}
		}
					
	}
	//Hold the nightly Job
	public void hold_Nightlty_Job(WebDriver driver,String activity) throws Exception
	{
		
		String process_instance;
		String run_status;
		String run_control_id;
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		WebDriverWait wait=new WebDriverWait(driver, 10);
		System.out.println("..........Nightly Hold Process Started..........\n");
		try
			{
				//Click "People Tools" navigation
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				driver.findElement(By.xpath("//*[@id='fldra_PT_PEOPLETOOLS']")).click();
				//Click "People Scheduler" navigation
				driver.findElement(By.xpath("//*[@id='fldra_PT_PROCESS_SCHEDULER']")).click();
				//Click "Process Monitor" navigation
				driver.findElement(By.xpath("//*[@id='crefli_PT_PROCESSMONITOR_GBL']/a")).click();
				System.out.println("Navigated to 'Process Monitor' page \n");
				
				wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(driver.findElement(By.id("ptifrmtgtframe"))));
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				//Run Control ID field set
				driver.findElement(By.xpath("//*[@id='PMN_DERIVED_RUN_CNTL_ID']")).sendKeys(activity);
				Thread.sleep(2000);
				System.out.println("Run Control ID is set to: "+activity+"\n");
				//Name field set to 'B2_NIGHTLY'
				driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_PRCSNAME']")).clear();
				driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_PRCSNAME']")).sendKeys("B2_NIGHTLY");
				System.out.println("Run Control ID is set to: 'B2_NIGHTLY' \n");
				Thread.sleep(5000);
				driver.switchTo().defaultContent();
				driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				//Clear User Id field
				driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_WS_OPRID']")).clear();
				System.out.println("Cleared User Id field \n");
				//Set '1' to days field
				driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_PT_FILTERVALUE']")).clear();
				driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_PT_FILTERVALUE']")).sendKeys("1");
				Select days_dropdown=new Select(driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_PT_FILTERUNIT']")));
				days_dropdown.selectByVisibleText("Days");
				System.out.println("Set days to '1' \n");
				Thread.sleep(2000);				
				//Refresh button click
				driver.findElement(By.xpath("//*[@id='REFRESH_BTN']")).click();
				System.out.println("Refresh Button clicked \n");
				driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
				Thread.sleep(10000);
				process_instance=driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_PRCSINSTANCE$0']")).getText();
				run_status=driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText();
				run_control_id=driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNCNTLID$0']")).getText();
				System.out.println("Process Instace: "+process_instance+",Run Control Id: "+run_control_id+" & Run Status is: "+run_status+ "\n");
				
				if (run_control_id.equalsIgnoreCase(activity) && run_status.equalsIgnoreCase("Queued"))
				{
					driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
					driver.findElement(By.xpath("//*[@id='PRCSDETAIL_BTN$0']")).click();
					System.out.println("'Details' link is clicked\n");
					driver.findElement(By.xpath("//*[@id='PMN_DERIVED_HOLDREQUEST']")).click();
					System.out.println("Hold Process radio button selected");
					Thread.sleep(2000);
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click(); 
					Thread.sleep(3000);
					System.out.println("Nightly hold done for the activity: "+activity+"\n");
					System.out.println("Nightly Process Hold completed \n");
					System.out.println("Passed:");
				}
				else
				{
					System.out.println("Nightly hold process failed\n");
					System.out.println("Failed:");
				}
			}
		catch(Exception e)
			{
			System.out.println("Nightly hold process not started \n");
			System.out.println("Failed:");
			}
		
		
	}
	
	//Restarting the Nightly Job
	public void restart_Nightly_Job(WebDriver driver,String activity) throws Exception
	{
		System.out.println("..........Restart Nightly Process Started..........");
		String prc_instance;
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try
		{	
				//Navigate to Process Monitor page
			    driver.findElement(By.xpath("//*[@id='fldra_PT_PEOPLETOOLS']")).click();
			    System.out.println("Navigated to 'People Tools' \n");
				driver.findElement(By.xpath("//*[@id='fldra_PT_PROCESS_SCHEDULER']")).click();
				System.out.println("Navigated to 'Process Scheduler' \n");
				driver.findElement(By.xpath("//*[@id='crefli_PT_PROCESSMONITOR_GBL']/a")).click();
				System.out.println("Navigated to 'Process Monitor' \n");
				driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
											
				//Run Control ID field set
				driver.findElement(By.id("PMN_DERIVED_RUN_CNTL_ID")).clear();
				driver.findElement(By.id("PMN_DERIVED_RUN_CNTL_ID")).sendKeys(activity);
				System.out.println("Run Control Id set to: "+activity+"\n");
				Thread.sleep(3000);
				
				//Set Run Status to Hold
				Select run_status_dropdown=new Select(driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_RUNSTATUS']")));
				run_status_dropdown.selectByVisibleText("Hold");
				System.out.println("Run Status set to: Hold \n");
				//run_status_dropdown.selectByValue("4");
				Thread.sleep(2000);
				//Name field set to 'B2_NIGHTLY'
				driver.findElement(By.id("PMN_FILTER_WRK_PRCSNAME")).clear();
				driver.findElement(By.id("PMN_FILTER_WRK_PRCSNAME")).sendKeys("B2_NIGHTLY");
				System.out.println("Name field set to: B2_NIGHTLY \n");
				Thread.sleep(3000);
				driver.switchTo().defaultContent();
				driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				//Clear User Id field
				driver.findElement(By.id("PMN_FILTER_WRK_WS_OPRID")).clear();
				//Set '2' to days field
				driver.findElement(By.id("PMN_FILTER_WRK_PT_FILTERVALUE")).clear();
				driver.findElement(By.id("PMN_FILTER_WRK_PT_FILTERVALUE")).sendKeys("2");
				Select days_dropdown=new Select(driver.findElement(By.xpath("//*[@id='PMN_FILTER_WRK_PT_FILTERUNIT']")));
				days_dropdown.selectByVisibleText("Days");
				System.out.println("Days field set to: 2 \n");
				Thread.sleep(3000);
				//Save on Refresh checkbox unchecking
				if(driver.findElement(By.id("PMN_FILTER_WRK_PMN_SAVE_FLAG")).isSelected()==true)
				{
					driver.findElement(By.id("PMN_FILTER_WRK_PMN_SAVE_FLAG")).click();
					System.out.println("Save on Refresh checkbox uncheked\n");
					Thread.sleep(3000);
				}
				//Refresh button click				
				driver.findElement(By.id("REFRESH_BTN")).click();
				System.out.println("Refreshed\n");
				Thread.sleep(10000);
				//driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
				prc_instance=driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_PRCSINSTANCE$0']")).getText();
				System.out.println("Prcesss instance "+prc_instance+" for activity "+activity+" is on "+driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText()+"\n");
				
				if (driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText().equalsIgnoreCase("Hold"))
				{
					driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
					driver.findElement(By.xpath("//*[@id='PRCSDETAIL_BTN$0']")).click();
					System.out.println("Details link clicked\n");
					driver.findElement(By.xpath("//*[@id='PMN_DERIVED_RESTARTREQUEST']")).click();
					System.out.println("Restart Process radio button selected\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
					System.out.println("Nightly process: "+prc_instance+" restarted successfully\n");
					System.out.println("Nightly Restart Completed\n");
					System.out.println("Passed:");
					
				}
				else
				{
					System.out.println("Failed: There is no hold job to restart or Nightly Restart unsuccessful\n");
					System.out.println("Failed:");
					
				}
				
		}
		catch(Exception e)
		{
			System.out.println("Failed: Some issue so re-run the step. \n");
			System.out.println("Failed:");
			e.printStackTrace();
		}
		
	}
	
	//For sending full DB export complete notification 
	public void full_DB_Export_Notification(WebDriver driver,String activity,String build_Id)
	{
		System.out.println("..........Full DB Export Notification Started...........\n");
		driver.switchTo().defaultContent();
		WebDriverWait wait = new WebDriverWait(driver, 15);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try
		{
			driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
			driver.findElement(By.xpath("//*[@id='fldra_AE_BS_BUILD_NOTIFICATION_GBL']")).click();
			driver.findElement(By.xpath("//*[@id='crefli_AE_BS_BLD_NOTIFY_GBL']/a")).click();
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ptifrmtgtframe")));
			//driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			//driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			System.out.println("Success: Naviagted to Build Notification page \n");
		}
		catch(Exception e)
		{
			System.out.println("Failed: Naviagted to Build Notification page failed \n");
			System.out.println("Failed:");
			System.exit(1);
		}
		//Checking for Advanced Search link, if present click it
		try
		{
			driver.switchTo().defaultContent();
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			if(driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).getText().equalsIgnoreCase("Advanced Search"))
			{
			driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).click();
			System.out.println("Advanced Search link is available & clicked,proceeding.....\n");
			}
		}	
		catch(Exception e)
		{
			System.out.println("Already in Advanced Search mode, so proceeding.....\n");
		}
		try
		{
			
			driver.switchTo().defaultContent();
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			Thread.sleep(3000);
			driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_SR_BUILD']")).sendKeys(activity);
			driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_SR_BUILD_ID']")).sendKeys(build_Id);
			driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
			System.out.println("Activity: "+activity+" & Build Id: "+build_Id+" is searched"+"\n");
			//Clicking on 'DB Status' tab
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Build Notif.']")));
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[5]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='DB Status']")));
			System.out.println("Navigated to 'DB Status' tab \n");
			//Thread.sleep(3000);
			//Selecting Full DB Export Status to 'Completed'
			Select status=new Select(driver.findElement(By.xpath("//*[@id='BS_NTF_DSS_BS_EXP_STATUS']")));
			status.selectByVisibleText("Completed");
			Thread.sleep(3000);
			System.out.println("Full DB Export Status selected as: "+driver.findElement(By.xpath("//*[@id='BS_NTF_DSS_BS_EXP_STATUS']")).getAttribute("value")+"\n");
			//Click on image to send build complete notification
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_BLD_EXP_PB']/img")).click();  //Clicking the image for sending notification
			Thread.sleep(7000);
			if(driver.findElement(By.xpath("//*[@id='win0divBS_NTF_DSS_BS_EXP_NTF_OPRIDlbl']/span")).getText().contains("Full Export file Completed"))
			{
				driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Saved']")));
				System.out.println("Full DB export notification sent completed\n");
				System.out.println("Passed:");
			}
			else
			{
				System.out.println("Full DB export notification sent failed\n");
				System.out.println("Failed:");
			}
		}
		catch(Exception e)
		{
			System.out.println("Failed: After search\n");
			System.out.println("Failed:");
		}
		
	}
	
	//For sending DB build completion notification
	public void build_Complete_Notify(WebDriver driver,String activity,String build_Id,String back_up_filename,String db_stamp_file) throws Exception
	{
		System.out.println("..........Build Completion Notification process started..........\n");
		WebDriverWait wait = new WebDriverWait(driver, 30);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.switchTo().defaultContent();
		driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
		System.out.println("Navigated to BASS\n");
		driver.findElement(By.xpath("//*[@id='fldra_AE_BS_BUILD_NOTIFICATION_GBL']")).click();
		System.out.println("Navigated to Build Notification\n");
		driver.findElement(By.xpath("//*[@id='crefli_AE_BS_BLD_NOTIFY_GBL']/a")).click();
		System.out.println("Navigated to Build Notification\n");
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id("ptifrmtgtframe")));
		
		try
		{
			//Check for Advanced search if present click else it's already in Advanced search mode
			if(driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).getText().equalsIgnoreCase("Advanced Search"))
			{
				//clicking on Advanced search mode
				driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).click();
				System.out.println("Page changed to Advanced Search mode....so proceeding.....\n");
				
			}
		}
		catch (Exception e)
		{
			System.out.println("Page is in Advanced Search mode....so proceeding.....\n");
		}
		//Thread.sleep(3000);
		//Search by Activity & Build Id
		//driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_SR_BUILD']")).sendKeys(activity,Keys.ENTER);
		driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_SR_BUILD']")).sendKeys(activity);
		//Thread.sleep(5000);
		driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_SR_BUILD_ID']")).sendKeys(build_Id);
		//Thread.sleep(2000);
		driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
		System.out.println("Searched by Activity: "+activity+" & Build Id: "+build_Id+"\n");
		//Thread.sleep(3000);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='BS_NTF_GEN_DESCR200']")));
		//Updating the "Comments" section
		String comment=driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_DESCR200']")).getText();
		driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_DESCR200']")).clear();
		driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_DESCR200']")).sendKeys(comment+"\n"+"Dump File:"+back_up_filename);
		System.out.println("Comments: "+comment+"\n"+"Dump File: "+back_up_filename);
		
		//selecting the Single User DB Status drop down
		Select single_user_db_status=new Select(driver.findElement(By.xpath("//*[@id='BS_NTF_GEN_BS_SNGL_USR_STATUS']")));
		single_user_db_status.selectByVisibleText("Completed");
		System.out.println("Single User Status set to: Completed");
		
		
		//Click "Copy logs to FTP server" button
		driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_NTF_CPY_RPTS_PB']")).click();
		Thread.sleep(25000);
		System.out.println("'Copy logs to FTP server' button clicked");
		//Click on the pop-up after above button press
		driver.switchTo().defaultContent();
		driver.findElement(By.xpath("//*[@id='#ICOK']")).click();
		System.out.println("Clicked on 'OK' for the pop-up after 'Copy logs to FTP server'");
			
		//Add Database Stamp operations
		System.out.println(".....Add Database Stamp started.....\n");
		driver.switchTo().defaultContent();
		driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_NTF_DB_STAMP_PB']")).click();
		System.out.println("Add Database Stamp button clicked\n");
		driver.switchTo().defaultContent();
		driver.switchTo().frame(driver.findElement(By.id("ptModFrame_1")));
		//Browse button click
		driver.findElement(By.name("#ICOrigFileName")).click();
		System.out.println("Browse button clicked\n");
		String filePath=db_stamp_file;
		file_Upload(filePath);
		System.out.println("Screenshot selected as: "+filePath);
		//Click on Upload button
		driver.findElement(By.xpath("//*[@id='Left']/span/input")).click();
		//Thread.sleep(20000);
		System.out.println("Upload button clicked & uploaded");
		
		try{
				driver.switchTo().defaultContent();
				driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='win0divBS_NTF_GEN_BS_DB_STAMP$25$']/img")));
			    System.out.println("Database Stamp uploaded successfully\n");
			    //Focus back to frame
				
				//Clicking the image for sending notification
				driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_BLD_NOTIF_PB']/img")).click();
				Thread.sleep(3000);
				//"Build Notification Update Information" section
		 		if(driver.findElement(By.xpath("//*[@id='win0divPSOPRDEFN_OPRDEFNDESC$33$']")).getText()!=null)
				{
					//System.out.println("Build Notification Sent");
		 			Thread.sleep(2000);
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(3000);
					System.out.println("Build Complete Notification sent successfully\n");
					System.out.println("Passed:");
					
				}
				else
				{
					System.out.println("Build Complete Notification not sent successfully\n");
					System.out.println("Failed:");
				} 
			}
		catch(Exception e)
		{
			System.out.println("Database Stamp upload unsuccessful.\n");
			System.out.println("Failed:");
		}
		
	}

//To check the build form is locked
	public void build_Form_Locked(WebDriver driver,String activity,String build_id) throws Exception 
	{
		System.out.println("..........Checking Build form is locked, process started..........");
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
		System.out.println("Navigated to BASS2 \n");
		driver.findElement(By.xpath("//*[@id='fldra_AE_BS_BUILDER_PROC_GBL']")).click();
		System.out.println("Navigated to Bass Builder Process \n");
		driver.findElement(By.xpath("//*[@id='crefli_AE_BS_SETUP_BLD_GBL']/a")).click();
		System.out.println("Navigated to Administer Build Form page \n");
		driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try
		{
			if(driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).getText().equalsIgnoreCase("Advanced Search"))
			{
				//Clicking on Advanced search link
				driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).click(); 
				System.out.println("Advanced search link is present, so clicked & proceeding......\n");
				
			}
		}
		catch(Exception e)
		{
			System.out.println("Already in Advanced search mode, so proceeding......\n");
		}
		Thread.sleep(3000);
		driver.findElement(By.xpath("//*[@id='BS_BLD_TBSCH_VW_SR_BUILD']")).sendKeys(activity);
		System.out.println("Set Activity as: "+activity+"\n");
		driver.findElement(By.xpath("//*[@id='BS_BLD_TBSCH_VW_SR_BUILD_ID']")).sendKeys(build_id);
		System.out.println("Set Build Id as: "+build_id+"\n");
		driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
		System.out.println("Above criteria searched");
		Thread.sleep(3000);
		//Checking 'Build Status Information' as locked
		if(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).getAttribute("value").equalsIgnoreCase("LOCK"))
		{
			System.out.println("Build Status Information: "+driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).getAttribute("value"));
			System.out.println("\nBuild form is Locked\n");
			//Checking Pum Build checkbox check status
			if(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_PUM_BLD_FLAG']")).isSelected()!=true)
			{
				driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_PUM_BLD_FLAG']")).click();
				driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
				System.out.println("'Pum Build' checkbox was not checked, so checked & saved.");
				System.out.println("Passed:");
			}
			else
			{
				System.out.println("Passed:");
			}
		}
		else
		{
			System.out.println("Build form is not locked. Please check.\n");
			System.out.println("Failed:");
		}
	}
	
//Make the build form 'AVAL' from 'LOCK'
	public void build_Form_Available(WebDriver driver,String activity,String build_id) throws Exception
	{
		System.out.println("..........Build Form changing status from LOCK to AVAL process started.........\n");
		driver.switchTo().defaultContent();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
		System.out.println("Navigated to BASS2 \n");
		driver.findElement(By.xpath("//*[@id='fldra_AE_BS_BUILDER_PROC_GBL']")).click();
		System.out.println("Navigated to Bass Builder Process \n");
		driver.findElement(By.xpath("//*[@id='crefli_AE_BS_SETUP_BLD_GBL']/a")).click();
		System.out.println("Navigated to Administer Build Form page \n");
		driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		//Checking Advanced Search link or Basic Search link is avalaible
		try
		{
			if(driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).getText().equalsIgnoreCase("Advanced Search"))
			{
				//Clicking on Advanced search link
				driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).click();  
				System.out.println("Advanced search link is present, so clicked & proceeding......\n");
			}
		}
		catch(Exception e)
		{
			System.out.println("Already in Advanced search mode, so proceeding......\n");
		}
		Thread.sleep(2000);
		driver.findElement(By.xpath("//*[@id='BS_BLD_TBSCH_VW_SR_BUILD']")).sendKeys(activity);
		System.out.println("Set Activity as: "+activity+"\n");
		Thread.sleep(2000);
		driver.findElement(By.xpath("//*[@id='BS_BLD_TBSCH_VW_SR_BUILD_ID']")).sendKeys(build_id);
		System.out.println("Set Build Id as: "+build_id+"\n");
		driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
		System.out.println("Above criteria searched");
		Thread.sleep(3000);
		//Checking the build status as "LOCK", then make it to "AVAL"
		if(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).getAttribute("value").equalsIgnoreCase("LOCK"))
		{
			System.out.println("Build form status is: "+driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).getAttribute("value"));
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).clear();
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).sendKeys("AVAL");
			Thread.sleep(2000);
			driver.findElement(By.xpath("//*[@id='OptionsList_0']/span")).click();
			System.out.println("Build form set to: AVAL\n");
			//If PUM build check box is not checked then select it 
			if(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_PUM_BLD_FLAG']")).isSelected()!=true)
			{
				driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_PUM_BLD_FLAG']")).click();
				System.out.println("Pum Build checkbox was not selected, but now selected\n");
			}
			else
			{
				System.out.println("Pum Build checkbox is already selected,so proceeding.....");
			}
			driver.findElement(By.xpath("//*[@id='#ICSave']")).click();   //Save button
			System.out.println("Build form set to 'AVAL' done\n");
			System.out.println("Passed:");
			Thread.sleep(3000);
		}
		else
		{
			System.out.println("Build form status is not 'LOCK'");
			System.out.println("Failed:");
		}
			
	}
//Create the next build form			
	public void build_Form_Next(WebDriver driver,String activity) throws Exception
	{
		System.out.println("..........Next Build Form available process started..........\n");
		try
		{
			driver.switchTo().defaultContent();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
			System.out.println("Navigated to BASS2 \n");
			driver.findElement(By.xpath("//*[@id='fldra_AE_BS_BUILDER_PROC_GBL']")).click();
			System.out.println("Navigated to Bass Builder Process \n");
			driver.findElement(By.xpath("//*[@id='crefli_AE_BS_SETUP_BLD_GBL']/a")).click();
			System.out.println("Navigated to Administer Build Form page \n");
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			//Click on "Add New Value" tab
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[2]/a/span")).click();
			Thread.sleep(2000);
			System.out.println(driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[2]/a/span")).getText()+" tab selected\n");
			//License Group assignment depending on activity
			String license_Group="";
			if (activity.equalsIgnoreCase("HR92"))
				license_Group="HRMS";
			else if(activity.equalsIgnoreCase("FSCM92"))
				license_Group="FIN/SCM";
			else if(activity.equalsIgnoreCase("CRM92"))
				license_Group="CRM";
			else if(activity.equalsIgnoreCase("ELS92"))
				license_Group="EL";
			else if(activity.equalsIgnoreCase("CS92"))
				license_Group="CAMPUS SOLUTIONS";
			else if(activity.equalsIgnoreCase("PS91"))
				license_Group="PORTAL SOLUTIONS";
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			//Input "License Group" field
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_VW_SR_LICENSE_GRP']")).clear();
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_VW_SR_LICENSE_GRP']")).sendKeys(license_Group);
			Thread.sleep(2000);
			System.out.println("License Group set to: "+license_Group);
			//Input "Activity" field
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_VW_SR_BUILD']")).clear();
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_VW_SR_BUILD']")).sendKeys(activity);
			Thread.sleep(2000);
			System.out.println("Activity set to: "+activity);
			//Input "Build Id" field
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_VW_SR_BUILD_ID']")).clear();
			driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_VW_SR_BUILD_ID$prompt']/img")).click(); //Clicking on search icon
			Thread.sleep(2000);
			System.out.println("Search icon of 'Build Id' is clicked");
			driver.switchTo().defaultContent();
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driver.switchTo().frame(driver.findElement(By.id("ptModFrame_0")));
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			String next_build_id=driver.findElement(By.xpath("//*[@id='SEARCH_RESULT1']")).getText();
			driver.findElement(By.xpath("//*[@id='SEARCH_RESULT1']")).click();
			System.out.println("Next build Id selected: "+next_build_id);
			driver.switchTo().defaultContent();
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			//Click "Add" button
			Thread.sleep(3000);
			driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
			System.out.println("Add button clicked\n");
			//Checking after add in the next page new build id is present
			if(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_SR_BUILD_ID']")).getText().equalsIgnoreCase(next_build_id))
			{	
				System.out.println("Next page new Build Id is present as: "+next_build_id);
				//Checking "PUM Build" checkbox, it should be checked & Build form should not be AVAL
				if(!(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).getAttribute("value").equalsIgnoreCase("AVAL")))
				{
					
					if(driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_PUM_BLD_FLAG']")).isSelected()!=true)
					{
						driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_PUM_BLD_FLAG']")).click();
						System.out.println("Pum Build checkbox was not selected, now it's selected\n");
					}
					//Clicking "Save" button
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
					System.out.println("Next build form "+next_build_id+" is created successfully\n");
					System.out.println("Passed:");
				}
				else
				{
					System.out.println("Build Form is '"+driver.findElement(By.xpath("//*[@id='BS_BLD_TABLE_BS_BLD_STATUS$3$']")).getAttribute("value")+"' which need to be changed \n");
					System.out.println("Failed:");
				}
			}
			else
			{
				System.out.println("New build id: "+next_build_id+" is not present in the page");
				System.out.println("Failed:");
			}
		}
		catch(Exception e)
		{
			System.out.println("Next build form creation failed\n");
			System.out.println("Failed:");
		}
	}
	
//Incrementing Image number for GLD builds
	public void image_Number_Increment(WebDriver driver,String activity,String build_id)
	{
		System.out.println("Image Number increment process started for activity : "+activity+" & Build: "+build_id);
		System.out.println("------------------------------------------------------------------------------------");		
		if (build_id.contains("GLD"))
		{
			System.out.println("Build ID: "+build_id+" is a gold build, so proceeding...............");
			int build_Number=Integer.parseInt((build_id.substring(1,4)));
			//If the build number is a multiple of 10
			if((build_Number%10)==0)
			{
				
				try
				{
					driver.switchTo().defaultContent();
					driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
					WebDriverWait wait=new WebDriverWait(driver, 30);
					//Navigate to 'BASS2'
					driver.findElement(By.xpath("//*[@id='fldra_UOW_FOLDER']")).click();
					System.out.println("Success: BASS2 navigated \n");
					//Navigate to 'Administration'
					driver.findElement(By.xpath("//*[@id='fldra_B2_ADMIN']")).click();
					System.out.println("Success: Administration navigated \n");
					//Navigate to 'Define Target Release'
					driver.findElement(By.xpath("//*[@id='crefli_ADM_TRGT_REL']/a")).click();
					System.out.println("Success: Define Target Release navigated \n");
					
					wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(driver.findElement(By.id("ptifrmtgtframe"))));
					//'Activity Name' field filling
					driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).sendKeys(activity);
					System.out.println("Success: 'Activity Name' set to "+activity+"\n");
					//Click on 'Search' button
					driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
					System.out.println("Success: 'Search' button clicked \n");
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("UOW_RELDEFN_UOW_SPIVERS")));
					int current_image_number=Integer.parseInt(driver.findElement(By.id("UOW_RELDEFN_UOW_SPIVERS")).getText());
					System.out.println("CURRENT IMAGE NUMBER is: "+current_image_number+" before increament");
					//Calculating Image number based on Build Id
					int next_image_number;
					if (activity.contains("PS"))
					{
						next_image_number=(build_Number/10)-39;
					}
					else
					{
						next_image_number=(build_Number/10)-29;
					}
					
					//code is to avoid 2 times increment of image number
					//String msg="This will increment Image Number from "+current_image_number+" to "+next_image_number+" and insert Applied Units of Work/Bugs to the PUM log target table of your target release";
					
					if((next_image_number-current_image_number)==1)
					{
						//Click on "Increment Image" button
						driver.findElement(By.id("DERIVED_UOW_ADM_INCREMENT_NUM")).click();
						driver.switchTo().defaultContent();
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='alertmsg']/span")));
						System.out.println("Pop-up with text: '"+driver.findElement(By.xpath("//*[@id='alertmsg']/span")).getText()+"'");
						//driver.findElement(By.id("#ICCancel")).click();
						driver.findElement(By.id("#ICOK")).click();
						driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
						//wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(driver.findElement(By.id("ptifrmtgtframe"))));
						//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Saved']")));
						Thread.sleep(5000);
						//Click on Save button
						driver.findElement(By.id("#ICSave")).click();
						//wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Saved']")));
						Thread.sleep(4000);
						System.out.println("Rechecking the image number after increment.....................");
						//Click on return to search button
						driver.findElement(By.xpath("//*[@id='#ICList']")).click(); 
						Thread.sleep(5000);
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("UOW_RELDEFN_SRC_UOW_RELNAME")));
						driver.findElement(By.id("UOW_RELDEFN_SRC_UOW_RELNAME")).clear();
						driver.findElement(By.xpath("//*[@id='UOW_RELDEFN_SRC_UOW_RELNAME']")).sendKeys(activity);
						System.out.println("Success: 'Activity Name' set to "+activity+"\n");
						//Click on 'Search' button
						driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
						System.out.println("Success: 'Search' button clicked \n");
						wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("UOW_RELDEFN_UOW_SPIVERS")));
						int updated_image_number=Integer.parseInt(driver.findElement(By.id("UOW_RELDEFN_UOW_SPIVERS")).getText());
						if(next_image_number==updated_image_number)
						{
							System.out.println("Image Number is incremented from "+current_image_number+" to "+next_image_number+" for the build "+build_id);
							System.out.println("Image Number Incremented successfully");
							System.out.println("------------------------------------------------------------------------------");
							System.out.println("Passed:");
						}
						else
						{
							System.out.println("Expected Image number: "+next_image_number+" is not equal to current image number: "+updated_image_number);
							System.out.println("Failed:");
							System.exit(1);
						}
						
						
					}
					else if((next_image_number-current_image_number)==0)
					{
						System.out.println("Image Number already incremented, this step is running 2nd time which is not allowed");
						System.exit(1);
					}
					else
					{
						System.out.println("This is not relavant build with image number");
						System.exit(1);
					}
					
					//End of code is to avoid 2 times increment of image number
				} //End of try block
				
				catch(ElementNotFoundException ele)
				{
					ele.printStackTrace();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				System.out.println("Build: "+build_id+"  contains GLD but it's not a gold build");
				System.out.println("Failed:");
			}
			
		}
		else
		{
			System.out.println("For Activity: "+activity+" Build is: "+build_id+" which is not a Gold build, this step is only for Gold build");
			System.out.println("Failed:");
		}
	}
	
	
//End of Incrementing Image number for GLD builds
//Sync Packaging Table operations	
	public void sync_Packaging_Tables(WebDriver driver,String activity,String sever_name) throws Exception
	{
		System.out.println("..........Sync Packaging Tables process started..........\n");
		String run_control_id=activity+"_Major";
		//String run_control_id=activity;
		String instance;
		String prc_Instance;
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.switchTo().defaultContent();
		try
		{
			driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
			System.out.println("Naviagted to BASS\n");
			driver.findElement(By.xpath("//*[@id='fldra_AE_BS_ADMINISTRATION_GBL']")).click();
			System.out.println("Naviagted to BASS Administration\n");
			driver.findElement(By.xpath("//*[@id='crefli_AE_BS_SETUP_PKG_RUN_GBL']/a")).click();
			System.out.println("Naviagted to Sync Packaging tables\n");
		}
		catch(Exception e)
		{
			System.out.println("Naviagtion failed\n");
			System.out.println("Failed");
			System.exit(1);
		}
		//If Basic Search link is avalaible click it
		driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		try{
			if(driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[3]")).getText().equalsIgnoreCase("Basic Search"))
			{
				//Clicking on Basic search link
				driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[3]")).click();
				System.out.println("Moved to Basic search mode, so proceeding......");
			}
		    }
		catch(Exception e)
			{
			System.out.println("Already in Basic search mode, so proceeding......");
			}
		try
		{
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			//set "Run Control ID" field value
			driver.findElement(By.xpath("//*[@id='PRCSRUNCNTL_RUN_CNTL_ID']")).sendKeys(run_control_id);
			System.out.println("Run Control Id set as: "+run_control_id);
			//click on "Search" button
			driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
			System.out.println("Search button clicked\n");
			//click on "Run" button
			driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_LOADPRCSRQSTDLGPB']")).click();
			System.out.println("'Run' button clicked");
			
			//Before submitting the process verifying the Run Control Id 
			if(driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_RUN_CNTL_ID']")).getText().equalsIgnoreCase(run_control_id))
			{
				System.out.println("Run control Id is correct: "+driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_RUN_CNTL_ID']")).getText());
				driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
				//Select the "Server Name"
				Select select=new Select(driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_SERVERNAME']")));
				select.selectByVisibleText(sever_name);
				System.out.println("Server selected as: "+select.getFirstSelectedOption().getText());
				//click on "Ok" button
				driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
				System.out.println("Ok clicked to submit the process\n");
				//Get the process instance number with text
				instance=driver.findElement(By.id("PRCSRQSTDLG_WRK_DESCR100")).getText();
				System.out.println(instance);
				//Get the process instance
				prc_Instance=instance.substring(instance.indexOf(":") + 1);
				//System.out.println("Process Instance: "+prc_Instance);
				//Clicking on process monitor link
				driver.findElement(By.xpath("//*[@id='PRCSRQSTDLG_WRK_LOADPRCSMONITORPB']")).click();
				System.out.println("Process monitor link clciked\n");
				driver.findElement(By.xpath("//*[@id='PMN_DERIVED_PRCSINSTANCE']")).clear();
				driver.findElement(By.xpath("//*[@id='PMN_DERIVED_PRCSINSTANCE']")).sendKeys(prc_Instance);
				System.out.println("Process Instance searched as: "+prc_Instance);
				driver.findElement(By.xpath("//*[@id='REFRESH_BTN']")).click();
				Thread.sleep(6000);
				//Checking the Environment sync status
				while(true)
				{
					driver.findElement(By.xpath("//*[@id='REFRESH_BTN']")).click();
					System.out.println("Sync packaging process running.....");
					Thread.sleep(15000);
					if(driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText().equalsIgnoreCase("Success"))
					{
						System.out.println("Sync Packaging Tables is successful\n");
						System.out.println("Passed:");
						break;
					}
					else if(driver.findElement(By.xpath("//*[@id='PMN_PRCSLIST_RUNSTATUSDESCR$0']")).getText().equalsIgnoreCase("No Success"))
					{
						System.out.println("Sync Packaging Tables is 'No Success'");
						System.out.println("Failed:");
						break;
					}
				 }
				
			}
		}
		catch(Exception e)
		{
			System.out.println("Sync Packahing process failed, check & re-run");
			System.out.println("Failed:");
		}
		
	}
	
//Generation packaging scripts in Packaging options-NOT Complete
	public void generate_Pkg_Scripts(WebDriver driver,String activity) throws Exception
	{
		WebDriverWait wait = new WebDriverWait(driver, 180);
		try
		{
			driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
			driver.switchTo().defaultContent();
			driver.findElement(By.xpath("//*[@id='fldra_AE_BASS_GBL']")).click();
			System.out.println("Navigated to BASS\n");
			driver.findElement(By.xpath("//*[@id='fldra_AE_BS_PACKAGING_OPTION_GBL']")).click();
			System.out.println("Navigated to Packaging Options\n");
			driver.findElement(By.xpath("//*[@id='crefli_AE_BS_PKG_GBL']/a")).click();
			System.out.println("Navigated to Packaging Options page\n");
		}
		catch(Exception e)
		{
			System.out.println("Navigation failed.");
			System.out.println("Failed:");
			System.exit(1);
		}
		driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		//License Group assignment depending on activity
		String license_Group="";
		if (activity.equalsIgnoreCase("HR92"))
			license_Group="HRMS";
		else if(activity.equalsIgnoreCase("FSCM92"))
			license_Group="FIN/SCM";
		else if(activity.equalsIgnoreCase("CRM92"))
			license_Group="CRM";
		else if(activity.equalsIgnoreCase("ELS92"))
			license_Group="EL";
		else if(activity.equalsIgnoreCase("CS92"))
			license_Group="CAMPUS SOLUTIONS";
		else if(activity.equalsIgnoreCase("PS91"))
			license_Group="PORTAL SOLUTIONS";
		try
		{
			//Checking Advanced Search link or Basic Search link is avalaible
			if(driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).getText().equalsIgnoreCase("Advanced Search"))
			{
				//Clicking on Advanced search link
				driver.findElement(By.xpath("//*[@id='win0divSEARCHBELOW']/a[2]")).click();  
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Basic Search")));
				//Thread.sleep(3000);
				System.out.println("Changed to advanced search mode, hence proceeding....");
			}
		}
		catch(Exception e)
		{
			System.out.println("Already in advanced search mode, hence proceeding....");
		}
		//Search the packaging options
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.findElement(By.xpath("//*[@id='BS_PKG_STAT_SR_LICENSE_GRP']")).sendKeys(license_Group);
		System.out.println("License group set as: "+license_Group);
		driver.findElement(By.xpath("//*[@id='BS_PKG_STAT_SR_BUILD']")).sendKeys(activity);
		System.out.println("Activity set as: "+activity);
		driver.findElement(By.xpath("//*[@id='#ICSearch']")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Packaging']")));
		System.out.println("Activity:"+activity+" & License:"+license_Group+" Successfully searched\n");
		//Thread.sleep(5000);
		try
		{
			//'Projects' tab operations
			driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[2]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Projects']")));
			System.out.println("Moved to tab: "+driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[2]/a/span")).getText());
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")));
			//Thread.sleep(10000);
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("Projects script generated successfully\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("Projects script not generated successfully\n");
					System.out.println("Failed:");
					
				}
			
			//'User Profiles' tab operations
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[3]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='User Profiles']")));
			//Thread.sleep(50000);
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")));
			//Thread.sleep(30000);
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("User profile script generated successfully\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(15000);
				}
				else
				{
					System.out.println("User profile scripts are not generated successfully\n");
					System.out.println("Failed:");
					
				}
				
			//'Perm. Lists' tab operations
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[4]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Perm. Lists']")));
			//Thread.sleep(50000);
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")));
			//Thread.sleep(30000);
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("Perm. lists script generated successfully\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("Perm. List scripts are not generated successfully\n");
					System.out.println("Failed:");
					
				}
				
			//'Sys. Settings' tab operations
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[5]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Sys. Settings']")));
			//Thread.sleep(10000);
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			//Thread.sleep(10000);
			driver.switchTo().defaultContent();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[contains(text(),'Message')]")));
			driver.findElement(By.xpath("//*[@id='#ICOK']")).click();
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("Sys. Setting script generated successfully\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("Sys. Settings scripts are not generated successfully\n");
					System.out.println("Failed:");
					
				}
				
			//'Demo Settings' tab operations
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[6]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Demo Setting']")));
			//Thread.sleep(10000);
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			//Thread.sleep(3000);
			driver.switchTo().defaultContent();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='#ICOK']")));
			driver.findElement(By.xpath("//*[@id='#ICOK']")).click();
			driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("Demo Setting script generated successfully\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("Demo Settings scripts are not generated successfully\n");
					System.out.println("Failed:");
					
				}
			//Click 'next' page arrow
			//driver.switchTo().frame(driver.findElement(By.id("ptifrmtgtframe")));
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[7]/a/img")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//abbr[text()='M']")));
			//Thread.sleep(15000);
			
			//'Message Defns' tab operations
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[6]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Message Defns']")));
			//Thread.sleep(10000);
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")));
			//Thread.sleep(10000);
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("Demo Setting script generated successfully\n");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("Message Defns scripts are not generated successfully\n");
					System.out.println("Failed:");
					
				}
			//'Message Trans' tab operations
			driver.findElement(By.xpath("//*[@id='PSTAB']/table/tbody/tr/td[7]/a/span")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//span[text()='Message Trans.']")));
			//Thread.sleep(10000);
			driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_GEN_SCR_PB']/img")).click();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")));
			//Thread.sleep(10000);
				if(driver.findElement(By.xpath("//*[@id='BS_DERIVED_WRK_BS_PKG_VIEW_SCR_PB']")).getText().equalsIgnoreCase("View Script"))
				{
					System.out.println("Message defns script generated successfully\n");
					System.out.println("Script Generation completed");
					driver.findElement(By.xpath("//*[@id='#ICSave']")).click();
					Thread.sleep(5000);
				}
				else
				{
					System.out.println("Message Trans scripts are not generated successfully\n");
					System.out.println("Failed:");
					
				}
				System.out.println("Passed:");	
		}
		catch(Exception e)
		{
			System.out.println("Failed:");
			e.printStackTrace();
		}
		
	
	}
	
//Browse File Upload operations
	public void file_Upload(String file)
	{
		
		try {
			Robot robot = new Robot();
			robot.setAutoDelay(2000);
			StringSelection filePath=new StringSelection(file);
			//copying the file path to clip board
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(filePath, null);
			robot.setAutoDelay(2000);
			//pressing ctrl+v i.e, paste operation
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			//releasing ctrl+v key
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_V);
			//pressing "Enter" & then releasing			
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
		} catch (AWTException e) {
			
			e.printStackTrace();
		}
		
	}
	
	//Code to get the master db back up file from linux machine
	public String getBkpFileName(String LINUX_DPK_SERVER,String LINUX_APP_USER,String LINUX_APP_PWD,String activity,String build_Id) 
	{
		String build_Number=build_Id.substring(0,4);
	    String master_DB_Name=null;
		String host=LINUX_DPK_SERVER;
	    String allBackUpFileName = null;
	    String constantPath="/apps_autofs/recovery/ENTERPRISE/opsdba/";
	    String backupFilePath=null;
	    String [] allBackUpFiles;
	    if (activity.equalsIgnoreCase("HR92"))
	    {
	    	backupFilePath=constantPath+"hrms/hrms92/zip/hc920mst*";
	    	master_DB_Name="hc920mst";
	    }
		else if(activity.equalsIgnoreCase("FSCM92"))
		{
			backupFilePath=constantPath+"fscm/fscm92/zip/ep920mst*";
			master_DB_Name="ep920mst";
		}
		else if(activity.equalsIgnoreCase("CRM92"))
		{
			backupFilePath=constantPath+"crm/crm92/zip/cr920mst*";
			master_DB_Name="cr920mst";
		}
		else if(activity.equalsIgnoreCase("ELS92"))
		{
			backupFilePath=constantPath+"els/els92/zip/lm920mst*";
			master_DB_Name="lm920mst";
		}
		else if(activity.equalsIgnoreCase("CS92"))
		{
			backupFilePath=constantPath+"cs/cs92/zip/cs920mst*";
			master_DB_Name="cs920mst";
		}
		else if(activity.equalsIgnoreCase("PS91"))
		{
			backupFilePath=constantPath+"ps/ps91/zip/pa910mst*";
			master_DB_Name="pa910mst";
		}
	    String command1="find "+backupFilePath+" -type f -mtime -1 | sort -r";
	    
	    try{
	    	
	    	java.util.Properties config = new java.util.Properties(); 
	    	config.put("StrictHostKeyChecking", "no");
	    	JSch jsch = new JSch();
	    	Session session=jsch.getSession(LINUX_APP_USER, host, 22);
	    	session.setPassword(LINUX_APP_PWD);
	    	session.setConfig(config);
	    	session.connect();
	    	System.out.println("Connected");
	    	
	    	Channel channel=session.openChannel("exec");
	        ((ChannelExec)channel).setCommand(command1);
	        channel.setInputStream(null);
	        //((ChannelExec)channel).setErrStream(System.err);
	        
	        InputStream in=channel.getInputStream();
	        channel.connect();
	        byte[] tmp=new byte[1024];
	        while(true)
	        {
	          while(in.available()>0)
	          {
	            int i=in.read(tmp, 0, 1024);
	            if(i<0)break;
	            String tmpAllBackUpFileNames=new String(tmp, 0, i);
	            allBackUpFileName=tmpAllBackUpFileNames;
	            System.out.print(new String(tmp, 0, i));
	          }
	          
	          if(channel.isEOF())
	          {
	            //System.out.println("exit-status: "+channel.getExitStatus());
	            break;
	          }
	          try{Thread.sleep(2000);}catch(Exception ee){}
	        }
	        System.out.println("Back file name retrieved as:  "+allBackUpFileName);
	        channel.disconnect();
	        session.disconnect();
	        	        
	    }
	    catch(JSchException jsch)
	    {
	    	System.out.println("Failed:Linux User Id & Password is wrong, please check in SAT properties files....");
	    }
	    catch(Exception e){
	    	e.printStackTrace();
	    }
	   //Checking for correct back up file
	    if (allBackUpFileName.contains(master_DB_Name)&allBackUpFileName.contains(build_Number))
        {
	    	allBackUpFiles=allBackUpFileName.split("\n");
	    	System.out.println("Back file name is correct:  "+allBackUpFiles[0]);
	    	return allBackUpFiles[0];
        }
        else if(allBackUpFileName.contains("No such file or directory"))
        {
        	System.out.println("wrong command:"+allBackUpFileName);
        	return "No such file or directory";
        }
        else
        {
	    	return null;
        }

	}
	//Get the current system date 
	public static String getCurrentDateTime(String format) 
	{
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date date = new Date();
        return dateFormat.format(date);
    } 
}
