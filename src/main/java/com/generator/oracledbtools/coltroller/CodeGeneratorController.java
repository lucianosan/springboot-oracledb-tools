package com.generator.oracledbtools.coltroller;


import com.generator.oracledbtools.service.EntityGeneratorService;
import com.generator.oracledbtools.service.FunctionCodeGeneratorService;
import com.generator.oracledbtools.service.ProcedureCodeGeneratorService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/generator")
public class CodeGeneratorController {

    private final ProcedureCodeGeneratorService procedureGenerator;
    private final EntityGeneratorService entityGenerator;
    private final FunctionCodeGeneratorService functionGenerator;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public CodeGeneratorController(ProcedureCodeGeneratorService procedureGenerator, EntityGeneratorService entityGenerator, FunctionCodeGeneratorService functionGenerator) {
        this.procedureGenerator = procedureGenerator;
        this.entityGenerator = entityGenerator;
        this.functionGenerator = functionGenerator;
    }

    // --------------------------
    // Endpoints existentes
    // --------------------------
    @PostMapping("/full/{schema}/{procedure}")
    public String generateProcedureStack(@PathVariable String schema,
                                         @PathVariable String procedure) {
        procedureGenerator.generateAll(schema, procedure);
        return "Repository + Service + Controller gerados para " + schema + "." + procedure;
    }

    @PostMapping("/entity/{schema}/{table}")
    public String generateEntity(@PathVariable String schema,
                                 @PathVariable String table) {
        entityGenerator.generateEntity(schema, table);
        return "Entity gerada para " + schema + "." + table;
    }

    // --------------------------
    // Novos endpoints
    // --------------------------

    // Listar tabelas de um schema
    @GetMapping("/tables/{schema}")
    public List<String> listTables(@PathVariable String schema) {
        String sql = "SELECT table_name FROM all_tables WHERE owner = :schema ORDER BY table_name";
        return entityManager.createNativeQuery(sql)
                .setParameter("schema", schema.toUpperCase())
                .getResultList();
    }

    // Listar procedures de um schema
    @GetMapping("/procedures/{schema}")
    public List<String> listProcedures(@PathVariable String schema) {
        String sql = """
            SELECT object_name
            FROM all_objects
            WHERE owner = :schema
              AND object_type = 'PROCEDURE'
            ORDER BY object_name
            """;
        return entityManager.createNativeQuery(sql)
                .setParameter("schema", schema.toUpperCase())
                .getResultList();
    }

    // Listar functions de um schema
    @GetMapping("/functions/{schema}")
    public List<String> listFunctions(@PathVariable String schema) {
        String sql = """
            SELECT object_name
            FROM all_objects
            WHERE owner = :schema
              AND object_type = 'FUNCTION'
            ORDER BY object_name
            """;
        return entityManager.createNativeQuery(sql)
                .setParameter("schema", schema.toUpperCase())
                .getResultList();
    }

    // Gerar Repository + Service para chamar uma function
    @PostMapping("/function/{schema}/{functionName}")
    public String generateFunctionCall(@PathVariable String schema,
                                       @PathVariable String functionName) {
        functionGenerator.generateFunctionRepositoryService(schema, functionName);
        return "Repository + Service gerados para function " + schema + "." + functionName;
    }


}
