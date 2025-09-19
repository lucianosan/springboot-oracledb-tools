package com.generator.oracledbtools.coltroller;

import com.generator.oracledbtools.dto.ProcedureDefinition;
import com.generator.oracledbtools.service.ClassGeneratorService;
import com.generator.oracledbtools.service.ProcedureParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/procedure")
public class ProcedureGeneratorController {

    private final ProcedureParserService parserService;
    private final ClassGeneratorService generatorService;

    @Autowired
    public ProcedureGeneratorController(ProcedureParserService parserService, ClassGeneratorService generatorService) {
        this.parserService = parserService;
        this.generatorService = generatorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generate(@RequestBody String procedureSource) {
        // 1. Parse
        ProcedureDefinition definition = parserService.parse(procedureSource);

        // 2. Gerar classes
        String generatedCode = generatorService.generate(definition);

        // Retorna o código gerado (poderia salvar em disco, ou compilar em runtime)
        return ResponseEntity.ok(generatedCode);
    }
}
