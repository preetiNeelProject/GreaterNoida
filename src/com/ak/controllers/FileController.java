package com.ak.controllers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ak.beans.Keys;
import com.ak.daos.OracleData;
import com.ak.modals.EM;
import com.ak.modals.EM2;
import com.ak.modals.EM3;
import com.ak.modals.Finance;
import com.ak.modals.General;
import com.ak.modals.HR;
import com.ak.modals.Health;
import com.ak.modals.Land;
import com.ak.modals.Law;
import com.ak.modals.Marketing;
import com.ak.modals.Planning;
import com.ak.modals.Planning2;
import com.ak.modals.Project;
import com.ak.modals.ProjectTech;
import com.ak.modals.Systems;
import com.ak.modals.UE;
import com.ak.services.CommonService;
import com.ak.services.EMLawService;
import com.ak.services.GenAgePlanService;
import com.ak.services.HRService;
import com.ak.services.HealthService;
import com.ak.services.LandService;
import com.ak.services.MarketingService;
import com.ak.services.ProFinService;
import com.ak.utils.FileUtils;
import com.ak.utils.Utils;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

/*
 *	@Author
 *	Swapril Tyagi 
 *  Updated By:Ashok Khatri
 *  Started 02-Jan-2020 to 21-Jan-2020
 *  Updated By:Preeti Rani
 *  Started 20-Jun-2020 to till
 *  
*/

@Controller
public class FileController
{
	@Autowired
	private ModelInitializer modelInitializer;
	@Autowired
	private Utils utils;
	@Autowired
	private static Keys keys;
	@Autowired
	private GenAgePlanService genAgePlanService;
	@Autowired
	private ProFinService proFinService;
	@Autowired
	private EMLawService emlService;
	@Autowired
	private CommonService commonService;
	@Autowired
	private MarketingService marketingService;
	
	@Autowired
	private HealthService healthService;
	
	@Autowired
	private HRService hrs;
	@Autowired
	private LandService landService;
	
	
	
	@RequestMapping(value="/uploadBulk",method=RequestMethod.POST)
	public String uploadBulk(HttpServletRequest request,@RequestParam("csv")MultipartFile csv,@RequestParam("department")String department,@RequestParam("pdfLocation")String pdfLocation,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(csv.getOriginalFilename().indexOf(".csv")==-1)
			flashAttributes.addFlashAttribute("msg","Select a valid csv file.");
		else if(!new File(pdfLocation).exists())
			flashAttributes.addFlashAttribute("msg","Enter a valid file location.");
		else
		{
			boolean oracleCheck=false;
			if(request.getParameter("oracle")!=null && request.getParameter("oracle").equals("Yes"))
				oracleCheck=true;
			
			String flage=uploadFiles(request,csv,department,pdfLocation,oracleCheck);
			
			if(flage.equals("Success"))
				flashAttributes.addFlashAttribute("msg","All files have been uploaded successfully.");
			else
			{
				flashAttributes.addFlashAttribute("err","1");
				if(flage.equals("Failed"))
					flashAttributes.addFlashAttribute("msg","Files couldn't be uploaded!");
				else
					flashAttributes.addFlashAttribute("msg","All files can't be uploaded successfully!");
			}
		}
		return "redirect:/uploadBulk";
	}
	
	@RequestMapping(value="/errFile",method=RequestMethod.GET)
	public String downloadErrorFile(HttpServletRequest request,HttpServletResponse response)
	{
		if(modelInitializer.getId(request)==null)
			return "error";
		FileUtils.downloadFile(response,"ErrorFile.txt","C:/Resources/",false,"","");
		return null;
	}
	
	private String uploadFiles(HttpServletRequest request,MultipartFile csv,String department,String pdfLocation,boolean oracleCheck)throws IOException
	{   
		System.out.println("uploadFiles:1");
		String flage="Success";
		System.out.println("2");
		
		String[] csvHeader=utils.getHeader(department);
		
		System.out.println("3");
		BufferedReader csvReader=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csv.getBytes())));
		
		System.out.println("4");
		String line="";
		System.out.println("5");
		int countLine=0;
		System.out.println("6a");
		boolean invalidLine=false;
		StringBuffer errors=new StringBuffer("Errors: \n");
		System.out.println("7");
		List<Integer> lines=new ArrayList<Integer>();
		System.out.println("8");
		outer:while((line=csvReader.readLine())!=null)
		{
			if(csvHeader==null)
			{
				flage="Failed";
				errors.append("\tDepartment "+department+" can't be instantiated!");
				break;
			}
			String[] data=line.split(",");
	        invalidLine=false;
	        if(data.length!=csvHeader.length)
	        {
	        	if(countLine==0)
	        	{
	        		flage="Failed";
	        		errors.append("\tInvalid Csv Header!\n");
	        		break outer;
	        	}
	        	else
	        	{
	        		flage="";
	        		errors.append("\tInvalid Line No.:"+countLine+".\n");
	        		lines.add(countLine);
	        		invalidLine=true;
	        	}
	        }
	        else
	        {
	        	if(countLine==0)
	        	{
	        		for(int i=0;i<csvHeader.length;i++)
	        		{
	        			if(!csvHeader[i].trim().equalsIgnoreCase(data[i].trim()))
	        			{
	        				flage="Failed";
	        				errors.append("\tInvalid Header "+data[i].trim()+". It should be as "+csvHeader[i].trim()+".\n");
	        				invalidLine=true;
	        			}
	        		}
	        		if(invalidLine)
	        			break outer;
	        	}
	        	else
	        	{
	        		if(!invalidLine)
	        		{
	        			for(int i=0;i<data.length;i++)
		        		{
		        			if(data[i].trim().length()==0)
		        			{
		        				flage="";
		        				errors.append("\tLine No.:"+countLine+" contain empty entries.\n");
		        				invalidLine=true;
		        				lines.add(countLine);
		        				break;
		        			}
		        			else if(data[i].trim().indexOf(",")!=-1)
		        				data[i]=data[i].trim().replace("@",",");
		        		}
	        			
	        			if(!invalidLine)
	        			{   System.out.println("invalid line:1");
	        				boolean duplicateFlage=false;
	        				System.out.println("duplicateFlage:"+duplicateFlage);
	        				System.out.println("2");
	        				
	        				if(department.equals("Residential") || department.equals("Abadi 6%") || department.equals("Institutional") || department.equals("IT") || department.equals("Group Housing") || department.equals("Builder") || department.equals("Commercial") || department.equals("Industry"))
	        				{
	        					if(genAgePlanService.isAllotmentNoExists(data[7].trim(),"AllotmentNo"))
	        						duplicateFlage=true;
	        					else
	        					{
	        						if(new File(pdfLocation+"/"+data[7].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[7].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[7].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[7].trim()+" R.pdf").exists())
	        							uploadGeneralFiles(data,pdfLocation,oracleCheck,department);
	        						else
	        						{
	        							flage="";
	        							invalidLine=true;
	        						}
	        					}
	        				}
							/*
							 * if(department.equals("Finance")) {
							 * if(proFinService.isFinExists(data[5].trim())) duplicateFlage=true; else {
							 * Finance finance=new Finance(); String subd=finance.getSubdepartment();
							 * if(subd.equals("Bank Loan")) {} if(new
							 * File(pdfLocation+"/"+data[5].trim()+".pdf").exists())
							 * uploadFinanceFiles(data,pdfLocation); else { flage=""; invalidLine=true; } }
							 * }
							 */
	        				String subDepartment = null;
	        				if(department.contains("_")) {
	        					String[] departmentArray = department.split("_");
	        					//String departmentName = departmentArray[0];
	        					if(departmentArray.length == 3) {
	        						subDepartment = departmentArray[1]+" "+departmentArray[2];
	        					}else {
	        						subDepartment = departmentArray[1];
	        					}
	        					
		        				if(subDepartment.equals("Bank Statement") || subDepartment.equals("Loan") || subDepartment.equals("Direct Salary") || subDepartment.equals("Deputation Salary")|| subDepartment.equals("TDS") || subDepartment.equals("Labor Cass") || subDepartment.equals("Costing") || subDepartment.equals("tax"))
		        				{
		        					if(subDepartment.equals("Bank Statement"))
		        					{
		        						if(proFinService.isFinExists(data[11].trim(),"AccountNo") || proFinService.isFinExists(data[4].trim(),"PeriodOfStatement")) {
		        							duplicateFlage=true;
		        						}
			        					else
			        					{
		        						System.out.println(pdfLocation+"/"+data[11]+".pdf");
		        						System.out.println(pdfLocation+"/"+data[4]+".pdf");
		        						if(new File(pdfLocation+"/"+data[11]+".pdf").exists()){
		        							uploadFinanceFiles(subDepartment,data,pdfLocation);
		        						} else 
		        							if(new File(pdfLocation+"/"+data[4]+".pdf").exists()){
		        								System.out.println(pdfLocation+"/"+data[4]+"pdf");
		        								uploadFinanceaFiles(subDepartment,data,pdfLocation);
		        						}
		        						else
		        						{
		        							flage="";
		        							invalidLine=true;
		        						}
		        					}
		        					}
		        					if(subDepartment.equals("Loan"))
		        					{
		        						if(proFinService.isFinExists(data[11].trim(),"AccountNo")) {
		        							duplicateFlage=true;
		        						}
			        					else
			        					{
		        						System.out.println(pdfLocation+"/"+data[11].trim()+"L.pdf");
		        						if(new File(pdfLocation+"/"+data[11].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[11].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[11].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[11].trim()+" R.pdf").exists())
		        							uploadFinance1Files(subDepartment,data,pdfLocation);
		        						else
		        						{
		        							flage="";
		        							invalidLine=true;
		        						}
		        					}
		        					}
		        					else if(subDepartment.equals("Direct Salary") || subDepartment.equals("Deputation Salary")|| subDepartment.equals("TDS") || subDepartment.equals("Labor Cass") || subDepartment.equals("Costing") || subDepartment.equals("tax"))
		        					{
		        						if(proFinService.isFinExists(data[2].trim(),"CodeNo"))
			        						duplicateFlage=true;
			        					else
			        					{
		        						
		        						if(new File(pdfLocation+"/"+data[2].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[2].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[2].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[2].trim()+" R.pdf").exists())
		        							uploadFinance2Files(subDepartment,data,pdfLocation);
		        						else
		        						{
		        							flage="";
		        							invalidLine=true;
		        						}
		        					}
		        				}
		        				}
	        				}
	        				if(department.equals("Project") || department.equals("Sewage") || department.equals("Water"))
	        				{
	        					if(proFinService.isProExists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					if(new File(pdfLocation+"/"+data[3].trim()+"L.pdf").exists()  || new File(pdfLocation+"/"+data[3].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" R.pdf").exists())
        							uploadProjectFiles(data,pdfLocation);
        						else
        						{
        							flage="";
        							invalidLine=true;
        						}
	        					}
	        				}
	        				if(department.equals("Planning(Residential)") || department.equals("Planning(Industry)") || department.equals("Planning(Building NOC)") || department.equals("Planning(Policies)") || department.equals("Planning(Institutional)"))
	        				{
	        					if(department.equals("Planning(Residential)"))
	        					{
	        						if(genAgePlanService.isAllotmentNo1Exists(data[7].trim(),"Residential"))
		        						duplicateFlage=true;
		        					else
		        					{
	        						if(new File(pdfLocation+"/"+data[7].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[7].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[7].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[7].trim()+" R.pdf").exists())
	        							uploadPlanningFiles("Residential",data,pdfLocation);
	        						else
	        						{
	        							flage="";
	        							invalidLine=true;
	        						}
	        					}
	        					}
	        					else if(department.equals("Planning(Industry)"))
	        					{
	        						if(genAgePlanService.isAllotmentNo2Exists(data[3].trim(),"Industry"))
		        						duplicateFlage=true;
		        					else
		        					{
	        						if(new File(pdfLocation+"/"+data[3].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" R.pdf").exists())
	        							uploadPlanningFiles("Industry",data,pdfLocation);
	        						else
	        						{
	        							flage="";
	        							invalidLine=true;
	        						}
	        					}
	        					}
	        					else if(department.equals("Planning(Group Housing)"))
	        					{
	        						if(genAgePlanService.isAllotmentNo2Exists(data[3].trim(),"Group Housing"))
		        						duplicateFlage=true;
		        					else
		        					{
	        						if(new File(pdfLocation+"/"+data[3].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" R.pdf").exists())
	        							uploadPlanningFiles("Industry",data,pdfLocation);
	        						else
	        						{
	        							flage="";
	        							invalidLine=true;
	        						}
	        					}
	        					}
	        					else if(department.equals("Planning(Institutional)"))
	        					{
	        						if(genAgePlanService.isAllotmentNo3Exists(data[6].trim(),"Institutional"))
		        						duplicateFlage=true;
		        					else
		        					{
	        						if(new File(pdfLocation+"/"+data[6].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[6].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[6].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[6].trim()+" R.pdf").exists())
	        							uploadPlanningFiles("Institutional",data,pdfLocation);
	        						else
	        						{
	        							flage="";
	        							invalidLine=true;
	        						}
	        					}
	        					}
	        					else if(department.equals("Planning(Building NOC)"))
	        					{
	        						if(genAgePlanService.isAllotmentNo4Exists(data[5].trim(),"Building NOC"))
		        						duplicateFlage=true;
		        					else
		        					{
	        						if(new File(pdfLocation+"/"+data[5].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[5].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[5].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[5].trim()+" R.pdf").exists())
	        							uploadPlanningFiles("Building NOC",data,pdfLocation);
	        						else
	        						{
	        							flage="";
	        							invalidLine=true;
	        						}
	        					}
	        					}
	        				}
	        				if(department.equals("EM") )
	        				{
	        					if(emlService.isEMExists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					if(new File(pdfLocation+"/"+data[3].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" R.pdf").exists())
	        						uploadEMFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				}
	        				if(department.equals("Land") )
	        				{
	        					if(new File(pdfLocation+"/"+data[8].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[8].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[8].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[8].trim()+" R.pdf").exists())
	        						uploadLandFiles(data,pdfLocation);
	        				}
	        				
	        				if(department.equals("EM3") || department.equals("HortiCulture") || department.equals("Urban"))
	        				{
	        					if(emlService.isEHUExists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					if(new File(pdfLocation+"/"+data[3].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+" R.pdf").exists())
	        						uploadEM3Files(data,pdfLocation,department);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				}
	        				if(department.equals("Law"))
	        				{
	        					if(emlService.isPetitionNoexists(data[7].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					if(new File(pdfLocation+"/"+data[2].trim()+"L.pdf").exists() || new File(pdfLocation+"/"+data[2].trim()+" L.pdf").exists() || new File(pdfLocation+"/"+data[2].trim()+"R.pdf").exists() || new File(pdfLocation+"/"+data[2].trim()+" R.pdf").exists())
	        						uploadLawFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				}
	        			
	      /*--------------------------New Departments Added----------------------------*/  				
	        				if(department.equals("Systems"))
	        				{
	        					if(emlService.isSysexists(data[4].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					System.out.println("file path :  "+pdfLocation+"/"+data[4].trim()+".pdf");
	        					if(new File(pdfLocation+"/"+data[4].trim()+"L"+".pdf").exists() || new File(pdfLocation+"/"+data[4].trim()+"R"+".pdf").exists())
	        						uploadSystemFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        					
	        				} 
	        				if(department.equals("Marketing"))
	        				{
	        					if(marketingService.isAllotmentNoExists(data[6].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					System.out.println("file path :  "+pdfLocation+"/"+data[6].trim()+".pdf");
	        					if(new File(pdfLocation+"/"+data[6].trim()+"L"+".pdf").exists() || new File(pdfLocation+"/"+data[6].trim()+"R"+".pdf").exists())
	        						uploadMarketingFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        					
	        				}
	        				
	        				if(department.equals("Health"))
	        				{
	        					if(healthService.isAllotmentNoExists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					System.out.println("file path :  "+pdfLocation+"/"+data[3].trim()+".pdf");
	        					if(new File(pdfLocation+"/"+data[3].trim()+"L"+".pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R"+".pdf").exists())
	        						uploadHealthFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				}
	        				
	        				if(department.equals("HR"))
	        				{
	        					if(hrs.isHRExists(data[4].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					System.out.println("file path :  "+pdfLocation+"/"+data[4].trim()+".pdf");
	        					if(new File(pdfLocation+"/"+data[4].trim()+"L"+".pdf").exists() || new File(pdfLocation+"/"+data[4].trim()+"R"+".pdf").exists())
	        						uploadHRFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				} 
	        				if(department.equals("UE"))
	        				{
	        					if(emlService.isUEExists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					System.out.println("file path :  "+pdfLocation+"/"+data[3].trim()+".pdf");
	        					if(new File(pdfLocation+"/"+data[3].trim()+"L"+".pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R"+".pdf").exists())
	        						uploadUEFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				} 
	        				
	        				if(department.equals("EM2"))
	        				{
	        					if(emlService.isEM2Exists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					System.out.println("file path :  "+pdfLocation+"/"+data[3].trim()+".pdf");
	        					if(new File(pdfLocation+"/"+data[3].trim()+"L"+".pdf").exists() || new File(pdfLocation+"/"+data[3].trim()+"R"+".pdf").exists())
	        						uploadEM2Files(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				} 
	        				
	        				if(department.equals("ProjectTech"))
	        				{
	        					if(emlService.isPTExists(data[3].trim()))
	        						duplicateFlage=true;
	        					else
	        					{
	        					if(new File(pdfLocation+"/"+data[3].trim()+".pdf").exists())
	        						uploadProjectTechFiles(data,pdfLocation);
	        					else
        						{
        							flage="";
        							invalidLine=true;
        						}
        					}
	        				} 
	        				
	        				if(duplicateFlage)
	        					errors.append("\tDuplicate entry for line no.:"+countLine+".\n");
	        				if(invalidLine)
	        				{
	        					flage="";
	        					errors.append("\tPdf File not found for line no.:"+countLine+".\n");
	        					lines.add(countLine);
	        				}
	        			}
	        		}
	        	}
	        }
	        countLine++;
		}
		if(!flage.equals("Success"))
			FileUtils.createFailedFile(errors);
		if(!flage.equals("Failed"))
		{
			File file=File.createTempFile(csv.getOriginalFilename(),"");
			csv.transferTo(file);
			FileUtils.createErrorFile(file,lines);
		}
		return flage;
	}
	
	private void uploadGeneralFiles(String[] data,String pdfLocation,boolean oracleCheck,String department)
	{
		General general=new General();
		general.setDepartment(department);
		if(oracleCheck)
			general=OracleData.getGen(data[7].trim(),department);
		else
		{
			general.setSector(data[4]);
			general.setScheme(data[6]);
			general.setAllotmentNo(data[7].trim());
			general.setAlloteeName(data[8]);
			general.setPlotNo(data[9]);
			general.setPlotSize(data[10]);
			general.setBlock(data[11]);
		}
		general.setMasterPlot(data[15]);
		general.setFts(data[16]);
		general.setOfficeOrder(data[17]);
		general.setOrderDate(data[18]);
		general.setAgenda(data[19]);
		general.setBoardMeeting(data[20]);
		general.setMeetingDate(data[21]);
		general.setLocation(utils.generateFilePath());
		new File(pdfLocation+"/"+data[7].trim()+"L.pdf").renameTo(new File(general.getLocation()+data[7].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[7].trim()+"R.pdf").renameTo(new File(general.getLocation()+data[7].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[7].trim()+" L.pdf").renameTo(new File(general.getLocation()+data[7].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[7].trim()+" R.pdf").renameTo(new File(general.getLocation()+data[7].trim()+"R.pdf"));
		genAgePlanService.insertOrUpdateGen(general);
	}
	
	private void uploadFinanceFiles(String subDepartment,String[] data,String pdfLocation)
	{
		Finance fc=new Finance();
		fc.setBankName(data[1]);
		fc.setBranchName(data[2]);
		fc.setFileNo(data[3]);
		fc.setStatement(data[4]);
		fc.setPeriodOfYear(data[5]);
		fc.setPatrawaliSankya(data[6]);
		fc.setSubject(data[7]);
		fc.setClerkName(data[8]);
		fc.setRegisterName(data[9]);
		fc.setSubdepartment(subDepartment);
		fc.setAccountNo(data[11].trim());
		fc.setLocation(utils.generateFilePath());
		new File(pdfLocation+"/"+data[11].trim()+".pdf").renameTo(new File(fc.getLocation()+data[11].trim()+".pdf"));
		proFinService.insertOrUpdateFin(fc);
	}
	
	private void uploadFinanceaFiles(String subDepartment,String[] data,String pdfLocation)
	{
		Finance fc=new Finance();
		fc.setBankName(data[1]);
		fc.setBranchName(data[2]);
		fc.setFileNo(data[3]);
		fc.setStatement(data[4].trim());
		fc.setPeriodOfYear(data[5]);
		fc.setPatrawaliSankya(data[6]);
		fc.setSubject(data[7]);
		fc.setClerkName(data[8]);
		fc.setRegisterName(data[9]);
		fc.setSubdepartment(subDepartment);
		fc.setAccountNo(data[11]);
		fc.setLocation(utils.generateFilePath());
		new File(pdfLocation+"/"+data[4].trim()+".pdf").renameTo(new File(fc.getLocation()+data[4].trim()+".pdf"));
		proFinService.insertOrUpdateFin(fc);
	}
	
	private void uploadFinance1Files(String subDepartment,String[] data,String pdfLocation)
	{
		Finance fc=new Finance();
		fc.setBankName(data[1]);
		fc.setBranchName(data[2]);
		fc.setFileNo(data[3]);
		fc.setStatement(data[4]);
		fc.setPeriodOfYear(data[5]);
		fc.setPatrawaliSankya(data[6]);
		fc.setSubject(data[7]);
		fc.setClerkName(data[8]);
		fc.setRegisterName(data[9]);
		fc.setSubdepartment(subDepartment);
		fc.setAccountNo(data[11].trim());
		fc.setLocation(utils.generateFilePath());
		new File(pdfLocation+"/"+data[11].trim()+"L.pdf").renameTo(new File(fc.getLocation()+data[11].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[11].trim()+"R.pdf").renameTo(new File(fc.getLocation()+data[11].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[11].trim()+" L.pdf").renameTo(new File(fc.getLocation()+data[11].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[11].trim()+" R.pdf").renameTo(new File(fc.getLocation()+data[11].trim()+"R.pdf"));
		proFinService.insertOrUpdateFin(fc);
	}
	
	private void uploadFinance2Files(String subDepartment,String[] data,String pdfLocation)
	{
		Finance fc=new Finance();
		fc.setFileNo(data[1]);
		fc.setCodeNo(data[2].trim());
		fc.setYear(data[3]);
		fc.setSubject(data[4]);
		fc.setDesignation(data[5]);
		fc.setFileType(data[6]);
		fc.setSubdepartment(data[7]);
		fc.setLocation(utils.generateFilePath());
		new File(pdfLocation+"/"+data[2].trim()+"L.pdf").renameTo(new File(fc.getLocation()+data[2].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[2].trim()+"R.pdf").renameTo(new File(fc.getLocation()+data[2].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[2].trim()+" L.pdf").renameTo(new File(fc.getLocation()+data[2].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[2].trim()+" R.pdf").renameTo(new File(fc.getLocation()+data[2].trim()+"R.pdf"));
		proFinService.insertOrUpdateFin(fc);
	}
	
	private void uploadProjectFiles(String[] data,String pdfLocation)
	{
		Project project=new Project();
		project.setSector(data[1]);
		project.setCategory(data[2]);
		project.setOpaFts(data[3].trim());
		project.setWorkName(data[4]);
		project.setContractorName(data[5]);
		project.setDepartment(data[6]);
		project.setFileNo(data[7]);
		project.setYear(data[8]);
		project.setLocation(utils.generateFilePath());
		project.setWorkCircle(data[9]);
		System.out.println("Bean Settings");
		new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(project.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+"R.pdf").renameTo(new File(project.getLocation()+data[3].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" L.pdf").renameTo(new File(project.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" R.pdf").renameTo(new File(project.getLocation()+data[3].trim()+"R.pdf"));
		proFinService.insertOrUpdatePro(project);
	}
	
	private void uploadPlanningFiles(String subDepartment,String[] data,String pdfLocation)
	{
		Planning planning=new Planning();
		planning.setSubDepartment(subDepartment);
		if(!subDepartment.equals("Building NOC")) {
		planning.setFileType(data[4]);
		planning.setCategory(data[5]);
		planning.setBpNo(data[6].trim());
		planning.setAllotmentNo(data[7].trim());
		planning.setApplicantName(data[8]);
		planning.setPlotNo(data[9]);
		planning.setPlotSize(data[10]);
		planning.setBlockNo(data[11]);
		planning.setSector(data[12]);
		planning.setFts(data[13]);
		}
		planning.setLocation(utils.generateFilePath());
		if(subDepartment.equals("Industry") || subDepartment.equals("Institutional"))
		{
			planning.setDoa(data[14]);
			planning.setDoc(data[15]);
			new File(pdfLocation+"/"+data[6].trim()+"L.pdf").renameTo(new File(planning.getLocation()+data[6].trim()+"L.pdf"));
			new File(pdfLocation+"/"+data[6].trim()+" L.pdf").renameTo(new File(planning.getLocation()+data[6].trim()+"L.pdf"));
			new File(pdfLocation+"/"+data[6].trim()+"R.pdf").renameTo(new File(planning.getLocation()+data[6].trim()+"R.pdf"));
			new File(pdfLocation+"/"+data[6].trim()+" R.pdf").renameTo(new File(planning.getLocation()+data[6].trim()+"R.pdf"));
		}
		else if(subDepartment.equals("Planning(Industry)") || subDepartment.equals("Residential") || subDepartment.equals("Planning(Group Housing)"))
		{
			planning.setDoa("NA");
			planning.setDoc("NA");
			new File(pdfLocation+"/"+data[7].trim()+"L.pdf").renameTo(new File(planning.getLocation()+data[7].trim()+"L.pdf"));
			new File(pdfLocation+"/"+data[7].trim()+" L.pdf").renameTo(new File(planning.getLocation()+data[7].trim()+"L.pdf"));
			new File(pdfLocation+"/"+data[7].trim()+"R.pdf").renameTo(new File(planning.getLocation()+data[7].trim()+"R.pdf"));
			new File(pdfLocation+"/"+data[7].trim()+" R.pdf").renameTo(new File(planning.getLocation()+data[7].trim()+"R.pdf"));
		}
		else if(subDepartment.equals("Building NOC"))
		{
			planning.setClerk_Name(data[1]);
			planning.setFileType(data[2]);
			planning.setCategory(data[3]);
			planning.setDepartment(data[4]);
			planning.setBn_no(data[5].trim());
			planning.setAllotmentNo(data[6]);
			planning.setApplicantName(data[7]);
			planning.setYear(data[8]);
			planning.setFileNo(data[9]);
			planning.setPlotNo(data[10]);
			planning.setPlotSize(data[11]);
			planning.setBlockNo(data[12]);
			planning.setSector(data[13]);
			planning.setFts(data[14]);
			new File(pdfLocation+"/"+data[5].trim()+"L.pdf").renameTo(new File(planning.getLocation()+data[5].trim()+"L.pdf"));
			new File(pdfLocation+"/"+data[5].trim()+" L.pdf").renameTo(new File(planning.getLocation()+data[5].trim()+"L.pdf"));
			new File(pdfLocation+"/"+data[5].trim()+"R.pdf").renameTo(new File(planning.getLocation()+data[5].trim()+"R.pdf"));
			new File(pdfLocation+"/"+data[5].trim()+" R.pdf").renameTo(new File(planning.getLocation()+data[5].trim()+"R.pdf"));
		}
		
		
		
		genAgePlanService.insertOrUpdatePlan(planning);
	}
	
	private void uploadEMFiles(String[] data,String pdfLocation)
	{
		EM em=new EM();
		em.setSector(data[1]);
		em.setCategory(data[2]);
		em.setOpaFts(data[3].trim());
		em.setWorkName(data[4]);
		em.setContractorName(data[5]);
		em.setDepartment(data[6]);
		em.setFileNo(data[7]);
		em.setYear(data[8]);
		em.setLocation(utils.generateFilePath());
		emlService.insertOrUpdateEMRecord(em);
		new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" L.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+"R.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" R.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"R.pdf"));
	}
	
	
	private void uploadLandFiles(String[] data,String pdfLocation)
	{
		Land em=new Land();
		em.setDate(data[1]);
		em.setLine(data[2]);
		em.setFileType(data[3]);
		em.setFileNo(data[4]);
		em.setAccountNo(data[5]);
		em.setFilesub(data[6]);
		em.setVillage(data[7]);
		em.setOpaFts(data[8].trim());
		em.setYear(data[9]);
		em.setNo_of_notsheet(data[10]);
		em.setNo_of_cos(data[11]);
		em.setNo_of_a3(data[12]);
		em.setNo_of_a2(data[13]);
		em.setNo_of_a1(data[14]);
		em.setNo_of_a0(data[15]);
		em.setTotal_pages(data[16]);
		em.setServey_letter(data[17]);
		em.setNotifection(data[18]);
		em.setMorgeg_letter(data[19]);
		em.setMap_11(data[20]);
		em.setLocation(utils.generateFilePath());
		landService.insertOrUpdateLand(em);
		new File(pdfLocation+"/"+data[8].trim()+"L.pdf").renameTo(new File(em.getLocation()+data[8].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[8].trim()+" L.pdf").renameTo(new File(em.getLocation()+data[8].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[8].trim()+"R.pdf").renameTo(new File(em.getLocation()+data[8].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[8].trim()+" R.pdf").renameTo(new File(em.getLocation()+data[8].trim()+"R.pdf"));
	}
	
	private void uploadEM3Files(String[] data,String pdfLocation,String department)
	{
		EM3 em=new EM3();
		em.setSector(data[1]);
		em.setCategory(data[2]);
		em.setOpa_Fts(data[3].trim());
		em.setWorkName(data[4]);
		em.setContractorName(data[5]);
		em.setDepartment(department);
		em.setFileNo(data[7]);
		em.setYear(data[8]);
		em.setLocation(utils.generateFilePath());
		emlService.insertOrUpdateEM3Record(em);
		new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" L.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+"R.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" R.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"R.pdf"));
	}
	
	private void uploadLawFiles(String[] data,String pdfLocation)
	{
		Law law=new Law();
		System.out.println("uploadLawFiles:1");
		law.setCourtName(data[1]);
		System.out.println("2");
		law.setPetitionNo(data[2].trim());
		System.out.println("3");
		law.setYear(data[3]);
		System.out.println("4");
		law.setPartyName(data[4]);
		System.out.println("5");
		law.setPetitonerAdvocate(data[5]);
		System.out.println("6");
		law.setRespondentAdvocate(data[6]);
		System.out.println("7");
		law.setRelatedDepartment(data[7]);
		System.out.println("8");		
		law.setLocation(utils.generateFilePath());//????????		
		System.out.println("9");
		
		emlService.insertOrUpdateLawRecord(law);
		
		System.out.println("10");
		new File(pdfLocation+"/"+data[2].trim()+"L.pdf").renameTo(new File(law.getLocation()+data[2].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[2].trim()+" L.pdf").renameTo(new File(law.getLocation()+data[2].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[2].trim()+"R.pdf").renameTo(new File(law.getLocation()+data[2].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[2].trim()+" R.pdf").renameTo(new File(law.getLocation()+data[2].trim()+"R.pdf"));
	}
	
	
	private void uploadProjectTechFiles(String[] data,String pdfLocation)
	{
		ProjectTech pt=new ProjectTech();
		System.out.println("uploadProjectTechFiles:1");
		pt.setSector(data[1]);
		
		pt.setFileNumber(data[3].trim());
	//	pt.setPetitionNo(data[2].trim());
		pt.setCategory(data[2]);
		
		
		pt.setName_Of_Work(data[4]);
		
		pt.setContractor_Name(data[5]);
		
		pt.setDepartment(data[6]);
		
		
		pt.setLocation(utils.generateFilePath());//????????		
		System.out.println("9");
		
		emlService.insertOrUpdateProjectTechRecord(pt);
	
		System.out.println("10");
		new File(pdfLocation+"/"+data[2].trim()+".pdf").renameTo(new File(pt.getLocation()+data[2].trim()+".pdf"));
	}
	
	private void uploadSystemFiles(String[] data,String pdfLocation)
	{
		System.out.println("uploadSystemFiles:1");
		Systems sy=new Systems();
		
		System.out.println("1");
		sy.setDate(data[1]);
		
		System.out.println("2");
		sy.setLotNo(data[2]);
		
		System.out.println("3");
		sy.setClerk_Name(data[3]);
		
		System.out.println("4");
		sy.setFile_No(data[4].trim());
		
		System.out.println("5");
		sy.setDepartment(data[5]);
		
		System.out.println("6");
		sy.setOptFts(data[6]);
		
		sy.setSubjectName(data[7]);
		System.out.println("6");
		
		sy.setLocation(utils.generateFilePath());
		emlService.insertOrUpdateSystemRecord(sy);
		System.out.println("uploadSystemFiles:7");
		//new File(pdfLocation+"/"+data[4].trim()+".pdf").renameTo(new File(sy.getLocation()+data[4].trim()+".pdf"));
		
		new File(pdfLocation+"/"+data[4].trim()+"L.pdf").renameTo(new File(sy.getLocation()+data[4].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[4].trim()+"R.pdf").renameTo(new File(sy.getLocation()+data[4].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[4].trim()+" L.pdf").renameTo(new File(sy.getLocation()+data[4].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[4].trim()+" R.pdf").renameTo(new File(sy.getLocation()+data[4].trim()+"R.pdf"));
	}
	
	private void uploadMarketingFiles(String[] data,String pdfLocation)
	{
		Marketing makt=new Marketing();
		
		makt.setDate(data[1]);
		makt.setLot_No(data[2]);
		makt.setClerk_Name(data[3]);
		makt.setFile_No(data[4].trim());
		makt.setDepartment(data[5]);
		makt.setFts_No_Opa_No(data[6]);
		makt.setApplicant_Name(data[7]);
		makt.setYear(data[8]);
		makt.setNo_Of_NoteSheet(data[9]);
		makt.setNo_Of_Cros(data[10]);
		makt.setTotal_No_Of_Pages(data[11]);
		makt.setLocation(utils.generateFilePath());
		marketingService.insertOrUpdateMarketing(makt);
		System.out.println(pdfLocation+"/"+data[6].trim()+"L.pdf");
		System.out.println(makt.getLocation()+data[6].trim()+".pdf");
		//new File(pdfLocation+"/"+data[6].trim()+"L.pdf").renameTo(new File(makt.getLocation()+data[6].trim()+"L.pdf"));
		
		new File(pdfLocation+"/"+data[6].trim()+"L.pdf").renameTo(new File(makt.getLocation()+data[6].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[6].trim()+"R.pdf").renameTo(new File(makt.getLocation()+data[6].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[6].trim()+" L.pdf").renameTo(new File(makt.getLocation()+data[6].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[6].trim()+" R.pdf").renameTo(new File(makt.getLocation()+data[6].trim()+"R.pdf"));
	}
	
	private void uploadHealthFiles(String[] data,String pdfLocation)
	{
		Health health=new Health();
		
		health.setSector(data[1]);
		health.setCategory(data[2]);
		health.setOpa_fts(data[3].trim());
		health.setWorkName(data[4]);
		health.setContractorName(data[5]);
		health.setDepartment(data[6]);
		health.setFileNo(data[7]);
		health.setYear(data[8]);
		health.setLocation(utils.generateFilePath());
		healthService.insertOrUpdateHealth(health);
		System.out.println(pdfLocation+"/"+data[3].trim()+"L.pdf");
		System.out.println(health.getLocation()+data[3].trim()+".pdf");
		//new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(makt.getLocation()+data[3].trim()+"L.pdf"));
		
		new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(health.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+"R.pdf").renameTo(new File(health.getLocation()+data[3].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" L.pdf").renameTo(new File(health.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" R.pdf").renameTo(new File(health.getLocation()+data[3].trim()+"R.pdf"));
	}
	/*----------------------------------------------------------------*/
	private void uploadHRFiles(String[] data,String pdfLocation)
	{
		HR hr=new HR();
		hr.setLine_No(data[1]);
		hr.setDate(data[2]);
		hr.setFileNo(data[3]);
		hr.setFileCode(data[4].trim());
		hr.setYear(data[5]);
		hr.setFile_Subject(data[6]);
		hr.setOpaFts(data[7]);
		hr.setNo_Of_Noteeing(data[8]);
		hr.setNop_Of_Coss(data[9]);
		hr.setTotal_No_Of_Pages(data[10]);
		hr.setLocation(utils.generateFilePath());
		hrs.insertOrUpdateHRRecord(hr);
		new File(pdfLocation+"/"+data[4].trim()+"L.pdf").renameTo(new File(hr.getLocation()+data[4].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[4].trim()+"R.pdf").renameTo(new File(hr.getLocation()+data[4].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[4].trim()+" L.pdf").renameTo(new File(hr.getLocation()+data[4].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[4].trim()+" R.pdf").renameTo(new File(hr.getLocation()+data[4].trim()+"R.pdf"));
		
	}
	
	
	private void uploadEM2Files(String[] data,String pdfLocation)
	{
		EM2 em=new EM2();
		em.setSector(data[1]);
		em.setCategory(data[2]);
		em.setOpaFts(data[3]);
		em.setName_Of_Work(data[4]);
		em.setContractor_Name(data[5]);
		em.setDepartment(data[6]);
		em.setFile_number(data[7]);
		em.setYear(data[8]);
		em.setLocation(utils.generateFilePath());
	    emlService.insertOrUpdateEM2Record(em);
	   
		new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+"R.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" L.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" R.pdf").renameTo(new File(em.getLocation()+data[3].trim()+"R.pdf"));
	}
	
	
	
	
	
	private void uploadUEFiles(String[] data,String pdfLocation)
	{
		UE ue=new UE();
		ue.setSector(data[1]);
		ue.setCategory(data[2]);
		ue.setOpa_fts(data[3].trim());
		ue.setName_Of_Work(data[4]);
		ue.setContractor_Name(data[5]);
		ue.setDepartment(data[6]);
		ue.setFile_Number(data[7]);
		ue.setYear(data[8]);
		ue.setLocation(utils.generateFilePath());
		
		
		emlService.insertOrUpdateUERecord(ue);
		new File(pdfLocation+"/"+data[3].trim()+"L.pdf").renameTo(new File(ue.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+"R.pdf").renameTo(new File(ue.getLocation()+data[3].trim()+"R.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" L.pdf").renameTo(new File(ue.getLocation()+data[3].trim()+"L.pdf"));
		new File(pdfLocation+"/"+data[3].trim()+" R.pdf").renameTo(new File(ue.getLocation()+data[3].trim()+"R.pdf"));
		
	}
	
	
	private void uploadPlanning2Files(String[] data,String pdfLocation)
	{
		Planning2 p=new Planning2();
		p.setLot_No(data[1]);
		p.setClerk_Name(data[2].trim());
		p.setCategory(data[3]);
		
		p.setAllotmentNo(data[4]);
		p.setApplicantName(data[5]);
		p.setOpaFts(data[6]);
		
	    emlService.insertOrUpdatePlanning2Record(p);
		new File(pdfLocation+"/"+data[2].trim()+".pdf").renameTo(new File(p.getLocation()+data[2].trim()+".pdf"));	
	}
	
	
	
	
	
	
	
	
	
	/*----------------------------------------------------------------*/
	
	
	@RequestMapping(value="/viewFile",method=RequestMethod.GET)
	public String viewFile(HttpServletRequest request,HttpServletResponse response,@RequestParam("department")String department,@RequestParam("id")String id,@RequestParam("sno")String sno,@RequestParam("prFlage")String prFlage,@RequestParam("name")String alloteeName)throws IOException
	{
		
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		PrintWriter out=response.getWriter();
		String count=null;
		System.out.println("Name:Passed:"+alloteeName);
		String location=null;
		System.out.println("REal Path : "+request.getServletContext().getRealPath("/"));
		String webLocation=request.getServletContext().getRealPath("/")+"staticResources/pdfs/";
		System.out.println("webLocation:"+"E://New Folder/"+"staticResources/pdfs/");
		if(prFlage!=null && prFlage.equals("null"))
		{
			if(department.equals("General")||department.equals("Planning"))
			{
				location=genAgePlanService.getLocation(department,Integer.parseInt(sno));
				count=FileUtils.viewFile(id+"L.pdf", webLocation,location,modelInitializer.getId(request)+",v",false);
				count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
			}
			if(department.equals("Finance") || department.equals("Project"))
			{
				location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
				
				if(department.equals("Finance")) {
					if(new File(location+"/"+alloteeName+".pdf").exists()) {
						count=FileUtils.viewFile(alloteeName+".pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
						commonService.insertLogs(uId,"Viewed file of "+department+" with Id:"+id+" & name:"+alloteeName);
						out.println(alloteeName+"<@>"+count);
						
						return null;
					} 
					else if(new File(location+"/"+id+".pdf").exists()) {
						count=FileUtils.viewFile(id+".pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
						commonService.insertLogs(uId,"Viewed file of "+department+" with Id:"+id+" & name:"+alloteeName);
						out.println(id+"<@>"+count);
						return null;
					}
					else {
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				     count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					}
					
				}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			if(department.equals("EM") || department.equals("Law") || department.equals("EM2"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:"+location);
				if(department.equals("Law")) {
					
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				System.out.println("path:"+count);}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			if(department.equals("EM3") || department.equals("HortiCulture") || department.equals("Urban"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:"+location);
				if(department.equals("Law")) {
					
					count=FileUtils.viewFile(id+".pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				System.out.println("path:"+count);}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			if(department.equals("Systems") || department.equals("UE"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:systems:"+location);
				System.out.println("view:systems");
				if(department.equals("Systems")) {
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
				else
				{
                     count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			if(department.equals("Marketing"))
			{
				location=marketingService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:Marketing:"+location);
				System.out.println("Marketing");
				if(department.equals("Marketing")) {
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
						
					
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			if(department.equals("Health"))
			{
				location=healthService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:Health:"+location);
				System.out.println("Health");
				if(department.equals("Health")) {
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			
			
			
			
			if(department.equals("Land"))
			{
				location=landService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:Land:"+location);
				System.out.println("Land");
				if(department.equals("Land")) {
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			if(department.equals("HR"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
				System.out.println("view:1:HR:"+location);
				System.out.println("view:HR");
				if(department.equals("HR")) {
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
				else
				{
					count=FileUtils.viewFile(id+"L.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
					count=count+"<@>"+FileUtils.viewFile(id+"R.pdf",webLocation,location,modelInitializer.getId(request)+",v",false);
				}
			}
			
			
			commonService.insertLogs(uId,"Viewed file of "+department+" with Id:"+id+" & name:"+alloteeName);
			out.println(id+"<@>"+count);
		}
		else
		{
			if(department.equals("General")||department.equals("Planning"))
			{
				location=genAgePlanService.getLocation(department,Integer.parseInt(sno));
				FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			if(department.equals("Land"))
			{
				location=landService.getLocation(Integer.parseInt(sno),department);
				FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			if(department.equals("Finance"))
			{
				location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
				    if(request.getParameterValues("subdepartment")[0].equals("Bank Statement"))
				    if(new File(location+"/"+id.trim()+".pdf").exists()) {
				    	id=id+"_"+request.getParameterValues("subdepartment")[0];
					   FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
					   	out.print(id);
				    	return null;
				    }
				    else {
				    	alloteeName=alloteeName+"_"+request.getParameterValues("subdepartment")[0];
				    	FileUtils.viewFile(alloteeName,webLocation,location,modelInitializer.getId(request),false);
				    	out.print(alloteeName);
				    	return null;
				    }
				    FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			if(department.equals("Project"))
			{
				location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			if(department.equals("EM") || department.equals("Law"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			if(department.equals("EM3") || department.equals("HortiCulture") || department.equals("Urban"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			if(department.equals("Systems") || department.equals("UE"))
				{
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);

			}
			
			if(department.equals("Marketing") )
			{    
				location=marketingService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			if(department.equals("Health") )
			{    
				location=healthService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			if(department.equals("HR"))
			{    System.out.println("view:HR");
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			
			
			if(department.equals("UE"))
			{    System.out.println("view:UE");
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			if(department.equals("EM2") || department.equals("Planning2"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			if(department.equals("ProjectTech") || department.equals("HR"))
			{
				location=emlService.getLocation(Integer.parseInt(sno),department);
					FileUtils.viewFile(id,webLocation,location,modelInitializer.getId(request),false);
			}
			
			
			
			
			commonService.insertLogs(uId,"Printed file of "+department+" with Id:"+id+" & name:"+alloteeName);
			out.print(id);
		}
		return null;
	}
	
	@RequestMapping(value="/downloadFile",method=RequestMethod.GET)
	public String downloadFile(ModelMap model,HttpServletRequest request,HttpServletResponse response,@RequestParam("department")String department,@RequestParam("id")String id,@RequestParam("sno")String sno)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		String location=null;
		//System.out.println("------"+request.getServletContext().getRealPath("/")+"staticResources/pdfs/");
		commonService.insertLogs(uId,"Downloaded File of "+department+" with Id:"+id+".");
		if(department.equals("General") || department.equals("Planning"))
		{
			location=genAgePlanService.getLocation(department,Integer.parseInt(sno));
			FileUtils.downloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		if(department.equals("Finance") || department.equals("Project"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if (department.equals("Finance")) {
				String statement = request.getParameterValues("statement")[0];
				String subdepartment = request.getParameterValues("subdepartment")[0];
				if (statement != "undefined" && subdepartment.equals("Bank Statement")) {
					if (new File(location + "/" + statement.trim() + ".pdf").exists()) {
						FileUtils.downloadFile(response, statement, location, false,request.getServletContext().getRealPath("/") + "staticResources/pdfs/",modelInitializer.getId(request));
					} else
						FileUtils.downloadFile(response, id, location, false,request.getServletContext().getRealPath("/") + "staticResources/pdfs/",modelInitializer.getId(request));
				} else {
					FileUtils.downloadFile(response, id, location, true,request.getServletContext().getRealPath("/") + "staticResources/pdfs/",modelInitializer.getId(request));
				}

			}
			else {
				FileUtils.downloadFile(response, id, location, true,request.getServletContext().getRealPath("/") + "staticResources/pdfs/",modelInitializer.getId(request));
			}
			
		}
		if(department.equals("EM") || department.equals("Law") || department.equals("EM2"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("Law"))
				FileUtils.downloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.downloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		if(department.equals("EM3") || department.equals("HortiCulture") || department.equals("Urban"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("Law"))
				FileUtils.downloadFile(response,id+".pdf",location,false,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.downloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		
		if(department.equals("Systems") || department.equals("UE"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("Systems"))
				FileUtils.newDownloadFile(response,id+".pdf",location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.newDownloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		
		if(department.equals("Marketing"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("Marketing"))
				FileUtils.newDownloadFile(response,id+".pdf",location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.newDownloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		
		if(department.equals("Health"))
		{
			location=healthService.getHealthLocation(department,Integer.parseInt(sno));
			if(department.equals("Health"))
				FileUtils.newDownloadFile(response,id+".pdf",location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.newDownloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		
		if(department.equals("Land"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("Land"))
				FileUtils.newDownloadFile(response,id+".pdf",location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.newDownloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		
		
		if(department.equals("UE"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("UE"))
				FileUtils.downloadFile(response,id+".pdf",location,false,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.downloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
		}
		
		if(department.equals("HR"))
		{
			location=proFinService.getProFinLocation(department,Integer.parseInt(sno));
			if(department.equals("Marketing"))
				FileUtils.newDownloadFile(response,id+".pdf",location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
			else
				FileUtils.newDownloadFile(response,id,location,true,request.getServletContext().getRealPath("/")+"staticResources/pdfs/",modelInitializer.getId(request));
	}
		
		System.out.println("======location"+location);
		return null;
	}
	
	@RequestMapping(value="/updateFile",method=RequestMethod.GET)
	public String updateFile(ModelMap model,HttpServletRequest request,@RequestParam("department")String department,@RequestParam("sno")String sno)
	{
 		model=modelInitializer.initializeModel(model, request);
		if(model==null)
			return "error";
		if(department.equals("General"))
			model.addAttribute("generalForm",genAgePlanService.retrieveGen(Integer.parseInt(sno)));
		if(department.equals("Finance")) {
			model.addAttribute("financeForm",proFinService.retrieveFin(Integer.parseInt(sno)));
		}
		if(department.equals("Project"))
			model.addAttribute("projectForm",proFinService.getPro(Integer.parseInt(sno)));
		if(department.equals("Planning"))
			model.addAttribute("planningForm",genAgePlanService.retrievePlanRecord(Integer.parseInt(sno)));
		if(department.equals("EM"))
			model.addAttribute("emForm",emlService.getEMRecord(Integer.parseInt(sno)));
		if(department.equals("EM2"))
			model.addAttribute("em2Form",emlService.getEM2Record(Integer.parseInt(sno)));
		if(department.equals("EM3") || department.equals("HortiCulture") || department.equals("Urban"))
			model.addAttribute("em3Form",emlService.getEM3Record(Integer.parseInt(sno)));
		if(department.equals("Law"))
			model.addAttribute("lawForm",emlService.getLawRecord(Integer.parseInt(sno)));
		if(department.equals("Land"))
			model.addAttribute("LandForm",landService.getLandRecord(Integer.parseInt(sno)));
		if(department.equals("Marketing"))
			model.addAttribute("marketingForm",marketingService.getMarketingRecord(Integer.parseInt(sno)));
		if(department.equals("HR"))
			model.addAttribute("HRForm",hrs.getHRRecord(Integer.parseInt(sno)));
		if(department.equals("Health"))
			model.addAttribute("HealthForm",healthService.getHealthRecord(Integer.parseInt(sno)));
		if(department.equals("Systems"))
			model.addAttribute("SystemForm",emlService.getSystemsRecord(Integer.parseInt(sno)));
		if(department.equals("UE"))
			model.addAttribute("UEForm",emlService.getUERecord(Integer.parseInt(sno)));
		System.out.println("departments/"+department+"/update");
		
		return "departments/"+department+"/update";
	}
	
	@RequestMapping(value="/delView",method=RequestMethod.GET)
	public String deleteViewFile(ModelMap model,HttpServletRequest request,@RequestParam("department")String department)
	{
		for(File file:new File(request.getServletContext().getRealPath("/")+"staticResources/pdfs/").listFiles())
		{
			System.out.println("Absolute Path is "+file.getAbsolutePath());
			file.delete();
		}
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		model=modelInitializer.initializeModel(model,request);
		model=modelInitializer.getDepartmentObject(model,department);
		ArrayList<String> params=(ArrayList<String>)request.getSession(false).getAttribute("params");
		if(department.equals("General"))
		{
			ArrayList<General> records=genAgePlanService.retrieveGen(params,uId);
			model.addAttribute("department",records.get(0).getDepartment());
			if(modelInitializer.getIdModule(request).equals("Super Administrator") || modelInitializer.getIdModule(request).equals("Administrator"))
				model.addAttribute("sectors",commonService.getAllSectors(records.get(0).getDepartment()));
			else
				model.addAttribute("sectors",commonService.getAllSectors(records.get(0).getDepartment(),uId));
			model.addAttribute("records",records);
			if(modelInitializer.getIdModule(request).equals("Super Administrator") || modelInitializer.getIdModule(request).equals("Administrator"))
				model.addAttribute("sectors",commonService.getAllSectors(params.get(0).substring(0,params.get(0).indexOf("@"))));
			else
				model.addAttribute("sectors",commonService.getAllSectors(params.get(0).substring(0,params.get(0).indexOf("@")),uId));
		}
		if(department.equals("Finance"))
			model.addAttribute("records",proFinService.retrieveFin(params));
		if(department.equals("Project"))
		{
			if(modelInitializer.getIdModule(request).equals("Super Administrator") || modelInitializer.getIdModule(request).equals("Administrator"))
				model.addAttribute("sectors",commonService.getAllSectors(department));
			else
				model.addAttribute("sectors",commonService.getAllSectors(department,uId));
			//model.addAttribute("records",proFinService.retrievePro(params));
		}
		if(department.equals("Planning"))
			model.addAttribute("records",genAgePlanService.retrievePlanRecords(params));
		return "departments/"+department+"/retrieve";
	}
	
	@RequestMapping(value="/generateReport",method=RequestMethod.GET)
	public String generateReport(HttpServletRequest request,HttpServletResponse response,@RequestParam("department")String department,@RequestParam("sno")String sno)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		System.out.println("dddddddddd"+department);
		String location=request.getServletContext().getRealPath("/")+"staticResources/pdfs/";
		if(department.equals("General"))
		{
			ArrayList<General> record=new ArrayList<General>();
			record.add(genAgePlanService.retrieveGen(Integer.parseInt(sno)));
			FileUtils.generateGenReport(record,location,response);
		}
		if(department.equals("Finance"))
		{
			ArrayList<Finance> record=new ArrayList<Finance>();
			System.out.println("FinanceReport:1");
			record.add(proFinService.retrieveFin(Integer.parseInt(sno)));
			System.out.println("2");
			FileUtils.generateFinReport(record,location,response);
		}
		if(department.equals("Law"))
		{  System.out.println("LawReport:1:fileController");
			ArrayList<Law> record=new ArrayList<Law>();
			System.out.println("LawReport:2");
			
			record.add(emlService.retrieveLawRecords(Integer.parseInt(sno)));
			System.out.println("LawReport:3");
			FileUtils.generateLawReport(record,location,response);
		}
		
		if(department.equals("EM"))
		{  System.out.println("EMReport:1:fileController");
			ArrayList<EM> record=new ArrayList<EM>();
			System.out.println("EMReport:2");
			
			record.add(emlService.retrieveEMRecords(Integer.parseInt(sno)));
			System.out.println("EMReport:3");
			FileUtils.generateEMReport(record,location,response);
		}
		
		
		if(department.equals("UE"))
		{  System.out.println("UEReport:1:fileController");
			ArrayList<UE> record=new ArrayList<UE>();
			System.out.println("UEReport:2");
			
			record.add(emlService.retrieveUERecords(Integer.parseInt(sno)));
			System.out.println("LawReport:3");
			FileUtils.generateUEReport(record,location,response);
		}
		
		if(department.equals("HR"))
		{
			ArrayList<HR> record=new ArrayList<HR>();
			record.add(hrs.retrieveHRRecords(Integer.parseInt(sno)));
			System.out.println("LawReport:3");
			FileUtils.generateHRReport(record,location,response);
		}
		if(department.equals("EM2") )
		{
			ArrayList<EM2> record=new ArrayList<EM2>();
			record.add(emlService.retrieveEM2Records(Integer.parseInt(sno)));
			System.out.println("EM2Report:3");
			FileUtils.generateEM2Report(record,location,response);
		}
		if(department.equals("EM3") || department.equals("HortiCulture") || department.equals("Urban"))
		{
			ArrayList<EM3> record=new ArrayList<EM3>();
			record.add(emlService.retrieveEM3Records(Integer.parseInt(sno)));
			System.out.println("EM3Report:3");
			FileUtils.generateEM3Report(department,record,location,response);
		}
		
		if(department.equals("ProjectTech"))
		{
			ArrayList<ProjectTech> record=new ArrayList<ProjectTech>();
			record.add(emlService.retrieveProjectTechRecords(Integer.parseInt(sno)));
			System.out.println("LawReport:3");
			FileUtils.generateProjectTechReport(record,location,response);
		}
		
		
		if(department.equals("Systems"))
		{
			ArrayList<Systems> record=new ArrayList<Systems>();
			record.add(emlService.retrieveSystemRecords(Integer.parseInt(sno)));
			System.out.println("LawReport:3");
			FileUtils.generateSystemReport(record,location,response);
		}
		
		if(department.equals("Marketing"))
		{
			ArrayList<Marketing> record=new ArrayList<Marketing>();
			record.add(marketingService.retrieveMarketing(Integer.parseInt(sno)));
			System.out.println("MarketingReport:3");
			FileUtils.generateMarketingReport(record,location,response);
		}
		
		if(department.equals("Health"))
		{
			ArrayList<Health> record=new ArrayList<Health>();
			record.add(healthService.retrieveHealth(Integer.parseInt(sno)));
			System.out.println("HealthReport:3");
			FileUtils.generateHealthReport(record,location,response);
		}
		
		if(department.equals("Health"))
		{
			ArrayList<Health> record=new ArrayList<Health>();
			record.add(healthService.retrieveHealth(Integer.parseInt(sno)));
			System.out.println("HealthReport:3");
			FileUtils.generateHealthReport(record,location,response);
		}
	/*------------------------------------------------------*/	
		if(department.equals("UE"))
		{
			ArrayList<Finance> record=new ArrayList<Finance>();
			record.add(proFinService.retrieveFin(Integer.parseInt(sno)));
			FileUtils.generateFinReport(record,location,response);
		}
				
		if(department.equals("EM2"))
		{
			ArrayList<Finance> record=new ArrayList<Finance>();
			record.add(proFinService.retrieveFin(Integer.parseInt(sno)));
			FileUtils.generateFinReport(record,location,response);
		}
					
		
		if(department.equals("Planning2"))
		{
			ArrayList<Finance> record=new ArrayList<Finance>();
			record.add(proFinService.retrieveFin(Integer.parseInt(sno)));
			FileUtils.generateFinReport(record,location,response);
		}
		
		
		
		
		/*---------------------Code End---------------------------------*/		
		if(department.equals("Project") || department.equals("Sewage"))
		{
			ArrayList<Project> record=new ArrayList<Project>();
			record.add(proFinService.getPro(Integer.parseInt(sno)));
			FileUtils.generateProReport(record,location,response);
		}
		if(department.equals("Planning"))
		{
			ArrayList<Planning> record=new ArrayList<Planning>();
			record.add(genAgePlanService.retrievePlanRecord(Integer.parseInt(sno)));
			FileUtils.generatePlanReport(record,location,response);
		}
		
		
		return null;
	}
	
	@RequestMapping(value="/generateReport",method=RequestMethod.POST)
	public String generateReport(HttpServletRequest request,HttpServletResponse response,@RequestParam("department")String department)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		String[] snos=request.getParameterValues("snos");
		String location=request.getServletContext().getRealPath("/")+"staticResources/pdfs/";
		if(snos!=null && snos.length>0)
		{
			if(department.equals("General"))
				FileUtils.generateGenReport(genAgePlanService.retrieveGen(snos),location,response);
			
			if(department.equals("Finance")) {
				System.out.println("FinanceReport:post method");
				FileUtils.generateFinReport(proFinService.retrieveFin(snos),location,response);
			}
			if(department.equals("Project"))
				FileUtils.generateProReport(proFinService.retrievePro(snos),location,response);
			if(department.equals("Planning"))
				FileUtils.generatePlanReport(genAgePlanService.retrievePlan(snos),location,response);
		
			if(department.equals("Law"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generateLawReport(emlService.retrieveLawRecords(snos),location,response);
			}
			if(department.equals("Planning2"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generatePlanning2Report(emlService.retrievePlanning2Records(snos),location,response);
			}
			
			if(department.equals("UE"))
			{ 
				System.out.println("UEReport:1:post method");				
			    FileUtils.generateUEReport(emlService.retrieveUERecords(snos),location,response);
			}
			if(department.equals("HR"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generateHRReport(hrs.retrieveHRRecords(snos),location,response);
			}
			
			
			if(department.equals("EM2"))
			{ 
				System.out.println("EM2Report:1:post method");				
			    FileUtils.generateEM2Report(emlService.retrieveEM2Records(snos),location,response);
			}
			
			if(department.equals("ProjectTech"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generateProjectTechReport(emlService.retrieveProjectTechRecords(snos),location,response);
			}
			
			if(department.equals("Systems"))
			{ 
				System.out.println("SystemsReport:1:post method");				
			    FileUtils.generateSystemReport(emlService.retrieveSystemRecords(snos),location,response);
			}
			
			if(department.equals("EM3"))
			{ 
				System.out.println("EM3Report:1:post method");				
			    FileUtils.generateEM3Report(emlService.retrieveEM3Records(snos),location,response);
			}
			
			if(department.equals("Marketing"))
			{ 
				System.out.println("MarketingReport:1:post method");				
			    FileUtils.generateMarketingReport(marketingService.retrieveMarketingRecords(snos),location,response);
			}
			
			if(department.equals("Health"))
			{ 
				System.out.println("HealthReport:1:post method");				
			    FileUtils.generateHealthReport(healthService.retrieveHealthRecords(snos),location,response);
			}
			
			if(department.equals("Land"))
			{ 
				System.out.println("LandReport:1:post method");				
			    FileUtils.generateLandReport(landService.retrieveLandRecords(snos),location,response);
			}
			
			if(department.equals("EM"))
			{ 
				System.out.println("LandReport:1:post method");				
			    FileUtils.generateEMReport(emlService.retrieveEMRecords(snos),location,response);
			}
		
		}
		else
		{
			ArrayList<String> params=(ArrayList<String >)request.getSession(false).getAttribute("params");
			if(department.equals("General"))
				FileUtils.generateGenReport(genAgePlanService.retrieveGen(params,uId),location,response);
			
			if(department.equals("EM"))
				FileUtils.generateEMReport(emlService.retrieveEMRecords(params),location,response);
			
			  if(department.equals("EM3"))
			  FileUtils.generateEM3Report(emlService.retrieveEM3Records(params,uId),location,response);
			 
			if(department.equals("Finance"))
				FileUtils.generateFinReport(proFinService.retrieveFin(params),location,response);
			if(department.equals("Law"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generateLawReport(emlService.retrieveLawRecords(params,uId),location,response);
			}
			if(department.equals("Project"))
				FileUtils.generateProReport(proFinService.retrievePro(params),location,response);
			if(department.equals("Planning"))
				FileUtils.generatePlanReport(genAgePlanService.retrievePlanRecords(params),location,response);
			if(department.equals("Planning2"))
			{ 
			    FileUtils.generatePlanning2Report(emlService.retrievePlanning2Records(params),location,response);
			}
			
			if(department.equals("UE"))
			{ 
				System.out.println("UEReport:1:post method");				
			    FileUtils.generateUEReport(emlService.retrieveUERecords(params),location,response);
			}
			if(department.equals("HR"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generateHRReport(hrs.retrieveHRRecords(params),location,response);
			}
			if(department.equals("ProjectTech"))
			{ 
				System.out.println("LawReport:1:post method");				
			    FileUtils.generateProjectTechReport(emlService.retrieveProjectTechRecords(params),location,response);
			}
			
			if(department.equals("Systems"))
			{ 
				System.out.println("SystemsReport:1:post method");				
			    FileUtils.generateSystemReport(emlService.retrieveSystemRecords(params),location,response);
			}
			
			if(department.equals("Marketing"))
			{ 
				System.out.println("MarketingReport:1:post method");				
			    FileUtils.generateMarketingReport(marketingService.retrieveMarketingRecords(params),location,response);
			}
			
			if(department.equals("Health"))
			{ 
				System.out.println("HealthReport:1:post method");				
			    FileUtils.generateHealthReport(healthService.retrieveHealthRecords(params),location,response);
			}
			
			if(department.equals("Land"))
			{ 
				System.out.println("LandReport:1:post method");				
			    FileUtils.generateLandReport(landService.retrieveLandRecords(params),location,response);
			}
		}
		return null;
	}
	
	@RequestMapping(value="/csvTemplate",method=RequestMethod.GET)
	public String csvTemplate(HttpServletRequest request,@RequestParam("department")String department,HttpServletResponse response)throws IOException
	{
		if(department.equals("Abadi")){
			department="Abadi 6%";
		}
		csvTemplate(department);
		downloadFile1("C:/Resources/",department+"_Template.csv",response,false);
		return null;
	}
	
	public  void csvTemplate(String department) {
		File csvTemplate = new File("C:/Resources/" + department + "_Template.csv");
		String[] csvHeader =utils.getHeader(department);
		try {
			String data = null;
			for (int i = 0; i < csvHeader.length; i++) {
				if (data == null)
					data = csvHeader[i] + ",";
				else {
					if (i == (csvHeader.length - 1))
						data = data + csvHeader[i];
					else
						data = data + csvHeader[i] + ",";
				}
			}
			if (csvTemplate.exists())
				org.apache.commons.io.FileUtils.forceDelete(csvTemplate);
			FileWriter fileWriter = new FileWriter(csvTemplate.getAbsolutePath());
			fileWriter.write(data);
			fileWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public  static void downloadFile1(String fileLocation, String fileName, HttpServletResponse response, boolean waterFlage) {
		System.out.println("fileLocation" + fileLocation + "fileName" + fileName);
		File file = null;
		BufferedInputStream bin = null;
		FileInputStream fin = null;
		ServletOutputStream sos = null;
		try {
			sos = response.getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			if (waterFlage) {
				addWaterMark(fileLocation, fileName);
				file = new File(keys.getRepository() + fileName);
			} else
				file = new File(fileLocation + fileName);
			fin = new FileInputStream(file);
			bin = new BufferedInputStream(fin);
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "Attachment; filename=\"" + fileName + "\"");
			response.setHeader("Pragma", "public");
			response.setHeader("Cache-Control", "max-age=-1");
			response.setContentLengthLong((long) file.length());
			sos = response.getOutputStream();
			byte[] buff = new byte[8192];
			int numChars;
			while ((numChars = bin.read(buff, 0, buff.length)) != -1)
				sos.write(buff, 0, numChars);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bin != null)
					bin.close();
				if (fin != null)
					fin.close();
				if (sos != null)
					sos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}  
	
	
	private static void addWaterMark(String location, String fileId) {
		try {
			PdfReader pdfReader = new PdfReader(location + fileId);
			PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(keys.getRepository() + fileId));
			// Image image=Image.getInstance("C:/Resources/logo.jpg");
			// float width=image.getScaledWidth(),height=image.getScaledHeight();
			PdfContentByte pdfContent;
			Rectangle pagesize;
			float x, y;
			for (int i = 1; i <= pdfReader.getNumberOfPages(); i++) {
				pagesize = pdfReader.getPageSizeWithRotation(i);
				x = (pagesize.getLeft() + pagesize.getRight()) / 2;
				y = (pagesize.getTop() + pagesize.getBottom()) / 2;
				pdfContent = pdfStamper.getOverContent(i);
				pdfContent.saveState();
				PdfGState state = new PdfGState();
				state.setFillOpacity(0.4f);
				pdfContent.setGState(state);
				// pdfContent.addImage(image,width,0,0,height,x-(width/2),y-(height/2));
				pdfContent.restoreState();
			}
			pdfStamper.close();
			pdfReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}