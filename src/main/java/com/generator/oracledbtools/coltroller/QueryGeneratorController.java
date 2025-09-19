package com.generator.oracledbtools.coltroller;

import com.generator.oracledbtools.dto.QueryRequestDto;
import com.generator.oracledbtools.service.QueryGeneratorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/generator/query")
public class QueryGeneratorController {

    private final QueryGeneratorService generatorService;

    public QueryGeneratorController(QueryGeneratorService generatorService) {
        this.generatorService = generatorService;
    }

    @PostMapping
    public String generateFromQuery(@RequestBody QueryRequestDto request) {
        String className = "DynamicQuery" + System.currentTimeMillis();
        return generatorService.generateFromQuery(request.getSql(), className);
    }
}

