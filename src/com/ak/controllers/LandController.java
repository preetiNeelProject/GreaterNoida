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
import com.ak.modals.General;
import com.ak.modals.Land;
import com.ak.modals.Systems;
import com.ak.services.CommonService;
import com.ak.services.LandService;
import com.ak.utils.FileUtils;
import com.ak.utils.Utils;

/*
 *	@Author 
 * 	Preeti Rani
*/

@Controller
public class LandController {
	@Autowired
	private ModelInitializer modelInitializer;
	@Autowired
	private Utils utils;
	@Autowired
	private LandService LandService;
	@Autowired
	private CommonService commonService;
	@Autowired
	private Keys keys;

	@RequestMapping(value = "/retrieveLand", method = RequestMethod.GET)
	public String retrieveLand(ModelMap model, HttpServletRequest request,
			@ModelAttribute("LandForm")Land Land) {
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		model=modelInitializer.initializeModel(model,request);
		model.addAttribute("department",Land.getDepartment());
		if(modelInitializer.getIdModule(request).equals("Super Administrator") || modelInitializer.getIdModule(request).equals("Administrator"))
			model.addAttribute("sectors",commonService.getAllSectors(Land.getDepartment()));
		else
			model.addAttribute("sectors",commonService.getAllSectors(Land.getDepartment(),uId));
		ArrayList<String> params=utils.generateLandParams(Land);
		ArrayList<Land> records=LandService.retrieveLandRecords(params);
		if(records.isEmpty())
			model.addAttribute("msg","No Record Found");
		else
		{
			request.getSession(false).setAttribute("params",params);
			model.addAttribute("records",records);
			model=modelInitializer.getRights(model,request);
		}
		return "departments/Land/retrieve";
		}

	@RequestMapping(value="/updateLand",method=RequestMethod.POST)
	public String updateLand(HttpServletRequest request,@ModelAttribute("LandForm")Land Land,@RequestParam("noteSheet")MultipartFile noteSheet,@RequestParam("correspondence")MultipartFile correspondence,RedirectAttributes flashAttributes)throws IOException
	{
		String uId=modelInitializer.getId(request);
		if(uId==null)
			return "error";
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			if(!noteSheet.getOriginalFilename().equals(Land.getOpaFts()+"L.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Notesheet name should be as OPA/FTSNoL.pdf");
				return "redirect:/updateFile?department=Land&sno="+Land.getSno();
			}
		}
		if(correspondence!=null && correspondence.getOriginalFilename().length()>0)
		{
			if(!correspondence.getOriginalFilename().equals(Land.getOpaFts()+"R.pdf"))
			{
				flashAttributes.addFlashAttribute("msg","Correspondence name should be as OPA/FTSNoR.pdf");
				return "redirect:/updateFile?department=Land&sno="+Land.getSno();
			}
		}
		if(noteSheet!=null && noteSheet.getOriginalFilename().trim().length()>0)
		{
			new File(Land.getLocation()+Land.getOpaFts()+"L.pdf").renameTo(new File("C:/Resources/"+Land.getOpaFts()+"L.pdf"));
			Files.write(Paths.get(keys.getRepository()+Land.getOpaFts()+"L.pdf"),noteSheet.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+Land.getOpaFts()+"L.pdf",keys.getRepository()+Land.getOpaFts()+"L.pdf",Land.getLocation()+Land.getOpaFts()+"L.pdf");
			new File("C:/Resources/"+Land.getOpaFts()+"L.pdf").delete();new File(keys.getRepository()+Land.getOpaFts()+"L.pdf").delete();
		}
		if(correspondence!=null && correspondence.getOriginalFilename().trim().length()>0)
		{
			new File(Land.getLocation()+Land.getOpaFts()+"R.pdf").renameTo(new File("C:/Resources/"+Land.getOpaFts()+"R.pdf"));
			Files.write(Paths.get(keys.getRepository()+Land.getOpaFts()+"R.pdf"),correspondence.getBytes());
			FileUtils.mergeFiles("C:/Resources/"+Land.getOpaFts()+"R.pdf",keys.getRepository()+Land.getOpaFts()+"R.pdf",Land.getLocation()+Land.getOpaFts()+"R.pdf");
			new File("C:/Resources/"+Land.getOpaFts()+"R.pdf").delete();new File(keys.getRepository()+Land.getOpaFts()+"R.pdf").delete();
		}
		LandService.insertOrUpdateLand(Land);
		flashAttributes.addFlashAttribute("msg","File has been updated successfully.");
		commonService.insertLogs(uId,"Updated File of Land with Id:"+Land.getOpaFts()+".");
		return "redirect:/updateFile?department=Land&sno="+Land.getSno();
	}

}