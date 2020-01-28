package com.harokad.goona.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.harokad.goona.domain.EdmSource;
import com.harokad.goona.repository.search.EdmSourceRepository;

@Service
public class EdmSourceService {

    @Inject
    private EdmSourceRepository edmSourceRepository;

    public EdmSource findOne(String id) {
        return edmSourceRepository.findOne(id);
    }

    public EdmSource save(EdmSource edmSource) {
        return edmSourceRepository.index(edmSource);
    }
    
    public List<EdmSource> findByName(String name) {
        return edmSourceRepository.findByName(name);
    }

    public void delete(EdmSource edmSource) {
        edmSourceRepository.delete(edmSource);
    }

    public EdmSource findOneByName(String sourceName) {
        return edmSourceRepository.findByName(sourceName).stream()
                .findFirst()
                .orElse(new EdmSource());
    }

    public List<EdmSource> findAll() {
        List<EdmSource> edmSources = new ArrayList<>();
        edmSourceRepository.findAll().forEach(edmSources::add);
        return edmSources;
    }
}
