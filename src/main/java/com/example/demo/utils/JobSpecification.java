package com.example.demo.utils;

import com.example.demo.entity.Job;
import org.springframework.data.jpa.domain.Specification;

public class JobSpecification {
    public static Specification<Job> hasPosition(String position){
        return ((root, query, criteriaBuilder) -> {
//            So sanh "="
            return criteriaBuilder.equal(root.get("position"),position);
        });
    }

    public static Specification<Job> hasSalary(int startRange,int endRange){
        return (
                ((root, query, criteriaBuilder) -> {
//                    So sanh trong range
                    return criteriaBuilder.not(
                            criteriaBuilder.or(
                                    criteriaBuilder.greaterThan(root.get("startRange"),endRange),
                                    criteriaBuilder.lessThan(root.get("endRange"),startRange)
                            )
                    );
                })
                );
    }
    public static Specification<Job> hasTitle(String title){
        return((root,query, cb)->{
            return cb.like(cb.lower(root.get("title")),"%"+title.toLowerCase()+"%");
        });
    }
    public static Specification<Job> isNotExpired(){
        return((root,query, cb)->{
            return cb.equal(root.get("status"),"OPEN");
        });
    }
}
