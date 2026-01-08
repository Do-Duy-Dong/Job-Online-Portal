package com.example.demo.utils;

import com.example.demo.entity.Job;
import org.springframework.data.jpa.domain.Specification;

public class FilterJob {
    private static String[] rangeSalary={
            "0-10000000",
            "10000000-20000000",
            "20000000-30000000",
            "30000000-40000000",
            "40000000-50000000",
            "50000000-1000000000"
    };
    private static String[] positionEnum={
            "Internship",
            "Fresher",
            "Junior",
            "Senior",
            "Lead",
            "Manager"
    };
    public static Specification<Job> filter(Integer salary, Integer postion, String keyword){
        Specification<Job> spec= Specification.where(null);
//        Filter by salary,position
        if( salary != null && salary>=0 && salary<rangeSalary.length){
            String[] rangeSalarys= rangeSalary[salary].split("-");
            int startRange= Integer.parseInt(rangeSalarys[0]);
            int endRange= Integer.parseInt(rangeSalarys[1]);
            spec= spec.and(JobSpecification.hasSalary(startRange,endRange));
        }
        if(postion != null && postion>=0 && postion< positionEnum.length){
            spec= spec.and(JobSpecification.hasPosition(positionEnum[postion]));
        }
        if(keyword !=null && !keyword.isEmpty()){
            spec= spec.and(JobSpecification.hasTitle(keyword));
        }
        spec= spec.and(JobSpecification.isNotExpired());
        return spec;
    }
}
