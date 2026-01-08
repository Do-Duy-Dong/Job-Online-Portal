package com.example.demo.service;

import com.example.demo.entity.Company;
import com.example.demo.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyService {
    private CompanyRepository companyRepository;
    public CompanyService(CompanyRepository companyRepository){
        this.companyRepository=companyRepository;
    }
    public Company findByTitle(String title){
        return companyRepository.findByTitle(title)
                .orElseThrow(()-> new RuntimeException("Company not found"));
    }
    public Company createCompany(String title,String address,String phone,String email){
        Optional<Company> record = companyRepository.findByTitle(title);
        if(record.isPresent()){
            throw new RuntimeException("Company already exists");
        }
        Company company= new Company();
        company.setTitle(title);
        company.setAddress(address);
        company.setPhone(phone);
        company.setEmail(email);
        return companyRepository.save(company);
    }
}
