package com.harokad.goona.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.harokad.goona.domain.EdmCategory;
import com.harokad.goona.repository.search.EdmCategoryRepository;
import lombok.Setter;

@Service
public class EdmCategoryService {

    @Inject
    @Setter
    private EdmCategoryRepository edmCategoryRepository;

    public EdmCategory findOne(String id) {
        return edmCategoryRepository.findOne(id);
    }

    public List<EdmCategory> findAll() {
        List<EdmCategory> edmLibraries = new ArrayList<>();
        edmCategoryRepository.findAll().forEach(edmLibraries::add);
        return edmLibraries;
    }

    public EdmCategory save(EdmCategory edmCategory) {
    	if(StringUtils.isEmpty(edmCategory.getBackgroundColor())){
       		String[] bgColors = {"#7CB5EC",  "#F45B5B", "#90ED7D", "#F7A35C", "#E4D354", "#8085E9", 
            "#2B908F"};
    		String selectedColor = bgColors[0];
    		for (String color : bgColors) {
    			List<EdmCategory> cats = edmCategoryRepository.findByBackgroundColor(color);
    			if(cats.isEmpty()){
    				selectedColor = color;
    				break;
    			}
    		}
       		edmCategory.setBackgroundColor(selectedColor);	
    	}
        return edmCategoryRepository.index(edmCategory);
    }

    public List<EdmCategory> findByName(String name) {
        return edmCategoryRepository.findByName(name);
    }

    public void delete(EdmCategory edmCategory) {
        edmCategoryRepository.delete(edmCategory);
    }

    public EdmCategory findOneByName(String sourceName) {
        return edmCategoryRepository.findByName(sourceName).stream()
                .findFirst()
                .orElse(new EdmCategory());
    }
}
