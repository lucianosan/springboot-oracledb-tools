package com.generator.oracledbtools.coltroller;

import com.generator.oracledbtools.service.MeuSchemaProcedureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/procedures")
public class ProcedureController {

    private final MeuSchemaProcedureService service;


    @Autowired
    public ProcedureController(MeuSchemaProcedureService service) {
        this.service = service;
    }


    @PostMapping("/{schema}/{procedure}")
    public Object callProcedure(
            @PathVariable String schema,
            @PathVariable String procedure,
            @RequestBody Object[] params) {
        return service.executarProcedure(schema, procedure, params);
    }


    @PostMapping("/function/{schema}/{function}")
    public Object callFunction(
            @PathVariable String schema,
            @PathVariable String function,
            @RequestParam Class<?> returnType,
            @RequestBody Object[] params) {
        return service.executarFunction(schema, function, returnType, params);
    }
}
