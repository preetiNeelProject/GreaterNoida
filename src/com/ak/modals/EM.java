package com.ak.modals;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name="EM")
public class EM
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="Sno")
	private int sno;
	
	@Column(name="Sector")
	private String sector;
	
	@Column(name="Category")
	private String category;
	
	@Column(name="Opa_Fts")
	private String opaFts;
	
	@Column(name="WorkName")
	private String workName;
	
	@Column(name="ContractorName")
	private String contractorName;
	
	@Column(name="Department")
	private String department;
	
	@Column(name="FileNo")
	private String fileNo;
	
	@Column(name="Year")
	private String year;
	
	@Column(name="Location")
	private String location;
	
	public int getSno()
	{
		return sno;
	}
	public void setSno(int sno)
	{
		this.sno=sno;
	}
	
	public void setSector(String sector)
	{
		this.sector=sector;
	}
	public String getSector()
	{
		return sector;
	}
	
	public void setCategory(String category)
	{
		this.category=category;
	}
	public String getCategory()
	{
		return category;
	}
	
	public void setOpaFts(String opaFts)
	{
		this.opaFts=opaFts;
	}
	public String getOpaFts()
	{
		return opaFts;
	}
	
	public void setWorkName(String workName)
	{
		this.workName=workName;
	}
	public String getWorkName()
	{
		return workName;
	}
	
	public void setContractorName(String contractorName)
	{
		this.contractorName=contractorName;
	}
	public String getContractorName()
	{
		return contractorName;
	}
	
	public void setFileNo(String fileNo)
	{
		this.fileNo=fileNo;
	}
	public String getFileNo()
	{
		return fileNo;
	}
	
	public void setDepartment(String department)
	{
		this.department=department;
	}
	public String getDepartment()
	{
		return department;
	}
	
	public void setYear(String year)
	{
		this.year=year;
	}
	public String getYear()
	{
		return year;
	}
	
	public void setLocation(String location)
	{
		this.location=location;
	}
	public String getLocation()
	{
		return location;
	}
}