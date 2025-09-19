package com.generator.oracledbtools.service;

import com.generator.oracledbtools.repository.MeuSchemaProcedureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MeuSchemaProcedureService {
    private final MeuSchemaProcedureRepository repository;


    @Autowired
    public MeuSchemaProcedureService(MeuSchemaProcedureRepository repository) {
        this.repository = repository;
    }


    public Object executarProcedure(String schema, String procedure, Object... params) {
        return repository.callMinhaProcedure(schema, procedure, params);
    }


    public Object executarFunction(String schema, String function, Class<?> returnType, Object... params) {
        return repository.callMinhaFunction(schema, function, returnType, params);
    }
}
