package com.ak.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ak.beans.Keys;
import com.ak.modals.EM;
import com.ak.modals.EM2;
import com.ak.modals.EM3;
import com.ak.modals.Law;
import com.ak.modals.Planning;
import com.ak.modals.Planning2;
import com.ak.modals.ProjectTech;
import com.ak.modals.Systems;
import com.ak.modals.UE;
import com.ak.services.CommonService;
import com.ak.services.EMLawService;
import com.ak.utils.FileUtils;
import com.ak.utils.Utils;

/*
 *	@Author
 *	Swapril Tyagi 
 *
 *  @Update
 *	Preeti Rani 
*/

@Controller
public class EMLController
{
	@Autowired
	private EMLawService emlService;
	@Autowired
	private CommonService commonService;
	@Autowired
	private ModelInitializer modelInitializer;
	@Autowired
	private Utils utils;
	@Autowired
	private Keys keys;
	
	@RequestMapping(value="/retrieveEM",method=RequestMethod.GET)
	public String retrieveEM(ModelMap model,HttpServletRequest request,@ModelAttribute("emForm")EM em)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		model=modelInitializer.initializeModel(model,request);
		ArrayList<String> params=utils.generateEMParams(em);
		
		for(String p:params) {
			System.out.println("p:"+p);
		}
		ArrayList<EM> records=emlService.retrieveEMRecords(params);
		if(records.isEmpty())
			model.addAttribute("msg","Sorry No Record Found!");
		else
		{
			request.getSession(true).setAttribute("params",params);
			model.addAttribute("records",records);
			modelInitializer.getRights(model,request);
		}
		return "departments/EM/retrieve";
	}
	

	@RequestMapping(value="/retrieveEM3",method=RequestMethod.GET)
	public String retrieveEM3(ModelMap model,HttpServletRequest request,@ModelAttribute("em3Form")EM3 em)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		model=modelInitializer.initializeModel(model,request);
		model.addAttribute("department",em.getDepartment());
		System.out.println("----------depart--------"+em.getDepartment());
		ArrayList<String> params=utils.generateEM3Params(em);
		
		for(String p:params) {
			System.out.println("p:"+p);
		}
		ArrayList<EM3> records=emlService.retrieveEM3Records(params);
		if(records.isEmpty())
			model.addAttribute("msg","Sorry No Record Found!");
		else
		{
			request.getSession(true).setAttribute("params",params);
			model.addAttribute("records",records);
			modelInitializer.getRights(model,request);
		}
		return "departments/EM3/retrieve";
	}
	
	@RequestMapping(value="/retrieveSystem",method=RequestMethod.GET)
	 public String retrieveSystem(ModelMap model,HttpServletRequest req,@ModelAttribute("SystemForm")Systems sys)	{
			String uId=modelInitializer.getId(req);
			if(uId==null)
				return "error";
			model=modelInitializer.initializeModel(model,req);
			
			ArrayList<String> params=utils.generateSystemParams(sys);
			
		ArrayList<Systems> records=emlService.retrieveSystemRecords(params);
			if(records.isEmpty())
				model.addAttribute("msg","Sorry No Record Found!");
			else
			{
				req.getSession(false).setAttribute("params",params);
				model.addAttribute("records",records);
				modelInitializer.getRights(model,req);
			}
			return "departments/Systems/retrieve";
		}
	
	@RequestMapping(value="/retrievePT",method=RequestMethod.GET)
	public String retrieveProjectTech(ModelMap model,HttpServletRequest request,@ModelAttribute("ProjectTechForm")ProjectTech pt)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		model=modelInitializer.initializeModel(model,request);
		ArrayList<String> params=utils.generateProjectTechParams(pt);
		ArrayList<ProjectTech> records=emlService.retrieveProjectTechRecords(params);
		if(records.isEmpty())
			model.addAttribute("msg","Sorry No Record Found!");
		else
		{
			request.getSession(false).setAttribute("params",params);
			model.addAttribute("records",records);
			modelInitializer.getRights(model,request);
		}
		return "departments/ProjectTech/retrieve";
	}
	
	@RequestMapping(value="/retrieveUE",method=RequestMethod.GET)
	 public String retrieveUE(ModelMap model,HttpServletRequest req,@ModelAttribute("UEForm")UE ue)	{
			String uId=modelInitializer.getId(req);
			if(uId==null)
				return "error";
			model=modelInitializer.initializeModel(model,req);
			
			
			ArrayList<String> params=utils.generateUEParams(ue);
			System.out.println("parms"+params);
			ArrayList<UE> records=emlService.retrieveUERecords(params);
			
			if(records.isEmpty())
				model.addAttribute("msg","Sorry No Record was Found!");
			else
			{
				req.getSession(false).setAttribute("params",params);
				model.addAttribute("records",records);
				modelInitializer.getRights(model,req);
			}
			return "departments/UE/retrieve";
		}
	
	@RequestMapping(value="/updateEM",method=RequestMethod.POST)
	public String updateEM(HttpServletRequest request,@ModelAttribute("emForm")EM em,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(em.getOpaFts()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as OPA/FTSNoL.pdf");
				return "redirect:/updateFile?department=Project&sno="+em.getSno();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(em.getOpaFts()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as OPA/FTSNoR.pdf");
				return "redirect:/updateFile?department=Project&sno="+em.getSno();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpaFts()+"L.pdf").renameTo(new File("C:/Resources/"+em.getOpaFts()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpaFts()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpaFts()+"L.pdf",keys.getRepository()+em.getOpaFts()+"L.pdf",em.getLocation()+em.getOpaFts()+"L.pdf");
			new File("C:/Resources/"+em.getOpaFts()+"L.pdf").delete();new File(keys.getRepository()+em.getOpaFts()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpaFts()+"R.pdf").renameTo(new File("C:/Resources/"+em.getOpaFts()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpaFts()+"R.pdf"),correspondence.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpaFts()+"R.pdf",keys.getRepository()+em.getOpaFts()+"R.pdf",em.getLocation()+em.getOpaFts()+"R.pdf");
			new File("C:/Resources/"+em.getOpaFts()+"R.pdf").delete();new File(keys.getRepository()+em.getOpaFts()+"R.pdf").delete();
		}
		emlService.insertOrUpdateEMRecord(em);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of EM with Id:"+em.getOpaFts()+".");
		return "redirect:/updateFile?department=EM&sno="+em.getSno();
	}
	
	@RequestMapping(value="/updateEM2",method=RequestMethod.POST)
	public String updateEM2(HttpServletRequest request,@ModelAttribute("emForm")EM2 em,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(em.getOpaFts()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as OPA/FTSNoL.pdf");
				return "redirect:/updateFile?department=EM2&sno="+em.getSno();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(em.getOpaFts()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as OPA/FTSNoR.pdf");
				return "redirect:/updateFile?department=EM2&sno="+em.getSno();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpaFts()+"L.pdf").renameTo(new File("C:/Resources/"+em.getOpaFts()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpaFts()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpaFts()+"L.pdf",keys.getRepository()+em.getOpaFts()+"L.pdf",em.getLocation()+em.getOpaFts()+"L.pdf");
			new File("C:/Resources/"+em.getOpaFts()+"L.pdf").delete();new File(keys.getRepository()+em.getOpaFts()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpaFts()+"R.pdf").renameTo(new File("C:/Resources/"+em.getOpaFts()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpaFts()+"R.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpaFts()+"R.pdf",keys.getRepository()+em.getOpaFts()+"R.pdf",em.getLocation()+em.getOpaFts()+"R.pdf");
			new File("C:/Resources/"+em.getOpaFts()+"R.pdf").delete();new File(keys.getRepository()+em.getOpaFts()+"R.pdf").delete();
		}
		emlService.insertOrUpdateEM2Record(em);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of EM2 with Id:"+em.getOpaFts()+".");
		return "redirect:/updateFile?department=EM2&sno="+em.getSno();
	}
	
	
	
	@RequestMapping(value="/updateSys",method=RequestMethod.POST)
	public String updateSys(HttpServletRequest request,@ModelAttribute("SystemForm")Systems em,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(em.getFile_No()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as FileL.pdf");
				return "redirect:/updateFile?department=Systems&sno="+em.getSno();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(em.getFile_No()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as FileL/FTSNoR.pdf");
				return "redirect:/updateFile?department=Systems&sno="+em.getSno();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getFile_No()+"L.pdf").renameTo(new File("C:/Resources/"+em.getFile_No()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getFile_No()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getFile_No()+"L.pdf",keys.getRepository()+em.getFile_No()+"L.pdf",em.getLocation()+em.getFile_No()+"L.pdf");
			new File("C:/Resources/"+em.getFile_No()+"L.pdf").delete();new File(keys.getRepository()+em.getFile_No()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getFile_No()+"R.pdf").renameTo(new File("C:/Resources/"+em.getFile_No()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getFile_No()+"R.pdf"),correspondence.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getFile_No()+"R.pdf",keys.getRepository()+em.getFile_No()+"R.pdf",em.getLocation()+em.getFile_No()+"R.pdf");
			new File("C:/Resources/"+em.getFile_No()+"R.pdf").delete();new File(keys.getRepository()+em.getFile_No()+"R.pdf").delete();
		}
		emlService.insertOrUpdateSystemRecord(em);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of EM with Id:"+em.getFile_No()+".");
		return "redirect:/updateFile?department=Systems&sno="+em.getSno();
	}
	
	@RequestMapping(value="/updateUE",method=RequestMethod.POST)
	public String updateUE(HttpServletRequest request,@ModelAttribute("UEForm")UE em,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(em.getOpa_fts()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as FileL.pdf");
				return "redirect:/updateFile?department=UE&sno="+em.getOpa_fts();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(em.getOpa_fts()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as FileL/FTSNoR.pdf");
				return "redirect:/updateFile?department=UE&sno="+em.getOpa_fts();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpa_fts()+"L.pdf").renameTo(new File("C:/Resources/"+em.getOpa_fts()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpa_fts()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpa_fts()+"L.pdf",keys.getRepository()+em.getOpa_fts()+"L.pdf",em.getLocation()+em.getOpa_fts()+"L.pdf");
			new File("C:/Resources/"+em.getOpa_fts()+"L.pdf").delete();new File(keys.getRepository()+em.getOpa_fts()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpa_fts()+"R.pdf").renameTo(new File("C:/Resources/"+em.getOpa_fts()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpa_fts()+"R.pdf"),correspondence.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpa_fts()+"R.pdf",keys.getRepository()+em.getOpa_fts()+"R.pdf",em.getLocation()+em.getOpa_fts()+"R.pdf");
			new File("C:/Resources/"+em.getOpa_fts()+"R.pdf").delete();new File(keys.getRepository()+em.getOpa_fts()+"R.pdf").delete();
		}
		emlService.insertOrUpdateUERecord(em);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of EM with Id:"+em.getOpa_fts()+".");
		return "redirect:/updateFile?department=UE&sno="+em.getSno();
	}
	
	@RequestMapping(value="/updateEM3",method=RequestMethod.POST)
	public String updateEM3(HttpServletRequest request,@ModelAttribute("em3Form")EM3 em,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(em.getOpa_Fts()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as OPA/FTSNoL.pdf");
				return "redirect:/updateFile?department=EM3&sno="+em.getSno();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(em.getOpa_Fts()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as OPA/FTSNoR.pdf");
				return "redirect:/updateFile?department=EM3&sno="+em.getSno();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpa_Fts()+"L.pdf").renameTo(new File("C:/Resources/"+em.getOpa_Fts()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpa_Fts()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpa_Fts()+"L.pdf",keys.getRepository()+em.getOpa_Fts()+"L.pdf",em.getLocation()+em.getOpa_Fts()+"L.pdf");
			new File("C:/Resources/"+em.getOpa_Fts()+"L.pdf").delete();new File(keys.getRepository()+em.getOpa_Fts()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(em.getLocation()+em.getOpa_Fts()+"R.pdf").renameTo(new File("C:/Resources/"+em.getOpa_Fts()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+em.getOpa_Fts()+"R.pdf"),correspondence.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+em.getOpa_Fts()+"R.pdf",keys.getRepository()+em.getOpa_Fts()+"R.pdf",em.getLocation()+em.getOpa_Fts()+"R.pdf");
			new File("C:/Resources/"+em.getOpa_Fts()+"R.pdf").delete();new File(keys.getRepository()+em.getOpa_Fts()+"R.pdf").delete();
		}
		emlService.insertOrUpdateEM3Record(em);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of EM3 with Id:"+em.getOpa_Fts	()+".");
		return "redirect:/updateFile?department=EM3&sno="+em.getSno();
	}
	
	@RequestMapping(value="/retrieveLaw",method=RequestMethod.GET)
	public String retrieveLaw(ModelMap model,HttpServletRequest request,@ModelAttribute("lawForm")Law law)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		System.out.println("law1");
		model=modelInitializer.initializeModel(model,request);
		System.out.println("law2");
		ArrayList<String> params=utils.generateLawParams(law);
		System.out.println("law3");
		ArrayList<Law> records=emlService.retrieveLawRecords(params);
		System.out.println("law4");
		if(records.isEmpty())
			model.addAttribute("msg","No Record Found Of Law!");
		else
		{
			request.getSession(false).setAttribute("params",params);
			model.addAttribute("records",records);
			modelInitializer.getRights(model,request);
		}
		return "departments/Law/retrieve";
	}
	@RequestMapping(value="/retrieveEM2",method=RequestMethod.GET)
	 public String retrieveEM2(ModelMap model,HttpServletRequest req,@ModelAttribute("EM2Form")EM2 em2)	{
			String uId=modelInitializer.getId(req);
			if(uId==null)
				return "error";
			model=modelInitializer.initializeModel(model,req);
			System.out.println("HR Department");
			
			ArrayList<String> params=utils.generateEM2Params(em2);
			for(String p:params) {
				System.out.println(p);
			}
			
			ArrayList<EM2> records=emlService.retrieveEM2Records(params);
			if(records.isEmpty())
				model.addAttribute("msg","Sorry No Record was Found!");
			else
			{
				req.getSession(false).setAttribute("params",params);
				model.addAttribute("records",records);
				modelInitializer.getRights(model,req);
			}
			return "departments/EM2/retrieve";
		}
		
	@RequestMapping(value="/retrievePlan2",method=RequestMethod.GET)
	public String retrievePlan(ModelMap model,HttpServletRequest request,@ModelAttribute("planning2Form")Planning2 plan)
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		model=modelInitializer.initializeModel(model,request);
		ArrayList<String> params=utils.generatePlanning2Params(plan);
		ArrayList<Planning2> records=emlService.retrievePlanning2Records(params);
		if(records.isEmpty())
			model.addAttribute("msg","No Record Found!");
		else
		{
			model=modelInitializer.getRights(model,request);
			request.getSession(false).setAttribute("params",params);
			model.addAttribute("records",records);
		}
		return "departments/Planning2/retrieve";
	}	
	

	@RequestMapping(value="/updateLaw",method=RequestMethod.POST)
	public String updateLaw(HttpServletRequest request,@ModelAttribute("lawForm")Law law,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(law.getPetitionNo()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as AllotmentNoL.pdf");
				return "redirect:/updateFile?department=law&sno="+law.getSno();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(law.getPetitionNo()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as AllotmentNoR.pdf");
				return "redirect:/updateFile?department=law&sno="+law.getSno();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(law.getLocation()+law.getPetitionNo()+"L.pdf").renameTo(new File("C:/Resources/"+law.getPetitionNo()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+law.getPetitionNo()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+law.getPetitionNo()+"L.pdf",keys.getRepository()+law.getPetitionNo()+"L.pdf",law.getLocation()+law.getPetitionNo()+"L.pdf");
			new File("C:/Resources/"+law.getPetitionNo()+"L.pdf").delete();new File(keys.getRepository()+law.getPetitionNo()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(law.getLocation()+law.getPetitionNo()+"R.pdf").renameTo(new File("C:/Resources/"+law.getPetitionNo()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+law.getPetitionNo()+"R.pdf"),correspondence.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+law.getPetitionNo()+"R.pdf",keys.getRepository()+law.getPetitionNo()+"R.pdf",law.getLocation()+law.getPetitionNo()+"R.pdf");
			new File("C:/Resources/"+law.getPetitionNo()+"R.pdf").delete();new File(keys.getRepository()+law.getPetitionNo()+"R.pdf").delete();
		}
		emlService.insertOrUpdateLawRecord(law);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of Law with Id:"+law.getPetitionNo()+".");
		return "redirect:/updateFile?department=Law&sno="+law.getSno();
	}
}