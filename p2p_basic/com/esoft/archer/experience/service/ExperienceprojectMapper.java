package com.esoft.archer.experience.service;

import java.util.List;

import com.esoft.archer.experience.model.Experienceproject;

public interface ExperienceprojectMapper {

    //int deleteByPrimaryKey(Integer id);

    int save(Experienceproject record);

    List find(String esql);

    int update(Experienceproject record);
}