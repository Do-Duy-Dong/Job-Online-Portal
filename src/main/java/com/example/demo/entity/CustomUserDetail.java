package com.example.demo.entity;


import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

public class CustomUserDetail extends org.springframework.security.core.userdetails.User {
    private String companyId;

    public CustomUserDetail(String email,
                            String password,
                            Collection<GrantedAuthority> authorities,
                            String companyId) {
        super(email, password,authorities );
        this.companyId = companyId;
    }

    public String getCompanyId() {
        return companyId;
    }
}
