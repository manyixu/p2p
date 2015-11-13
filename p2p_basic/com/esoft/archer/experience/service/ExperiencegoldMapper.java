package com.esoft.archer.experience.service;

import java.util.List;

import com.esoft.archer.experience.model.Experiencegold;

public interface ExperiencegoldMapper {
    
    int save(Experiencegold record);

    List find(String userid);

    int update(Experiencegold record);
    
    void timeupdate();
}