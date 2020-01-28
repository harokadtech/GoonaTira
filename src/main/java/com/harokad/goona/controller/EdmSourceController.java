package com.harokad.goona.controller;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.harokad.goona.domain.EdmSource;
import com.harokad.goona.service.EdmSourceService;

@RestController
@RequestMapping("/api")
public class EdmSourceController {

    @Inject
    private EdmSourceService edmSourceService;
    
    @RequestMapping(value = "/source", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<EdmSource> list() {
        return edmSourceService.findAll();
    }
    
    @RequestMapping(method=RequestMethod.POST, value="/source", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EdmSource create(@RequestBody EdmSource edmDirectory) {
        return edmSourceService.save(edmDirectory);
    }
    
    @RequestMapping(value = "/source/name/{sourceName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public EdmSource getOneByName(@PathVariable String sourceName) {
        return edmSourceService.findOneByName(sourceName);
    }
    
}
