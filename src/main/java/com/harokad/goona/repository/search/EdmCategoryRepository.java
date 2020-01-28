package com.harokad.goona.repository.search;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.harokad.goona.domain.EdmCategory;


public interface EdmCategoryRepository extends ElasticsearchRepository<EdmCategory, String> {

    List<EdmCategory> findByName(String name);
    
    List<EdmCategory> findByBackgroundColor(String backgroundColor);
}
