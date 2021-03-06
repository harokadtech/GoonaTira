package com.harokad.goona.repository.search;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.harokad.goona.domain.EdmDocumentFile;


public interface EdmDocumentRepository extends ElasticsearchRepository<EdmDocumentFile, String> {

    @Query("{\"match\": {\"sourceId\" : \"?0\"}}")
    Page<EdmDocumentFile> findBySourceId(String sourceId, Pageable page);
    
    List<EdmDocumentFile> findByName(String name);

}
