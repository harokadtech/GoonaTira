package com.harokad.goona.repository.search;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.harokad.goona.domain.EdmSource;


public interface EdmSourceRepository extends ElasticsearchRepository<EdmSource, String> {
    
    List<EdmSource> findByName(String name);
    
}
