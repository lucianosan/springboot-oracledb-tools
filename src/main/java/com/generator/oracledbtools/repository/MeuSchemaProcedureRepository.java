package com.generator.oracledbtools.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;


@Repository
public class MeuSchemaProcedureRepository {


    @PersistenceContext
    private EntityManager entityManager;


    public Object callMinhaProcedure(String schema, String procedureName, Object... params) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(schema + "." + procedureName);


// registrar parâmetros dinamicamente
        for (int i = 0; i < params.length; i++) {
            query.registerStoredProcedureParameter(i + 1, params[i].getClass(), ParameterMode.IN);
            query.setParameter(i + 1, params[i]);
        }


        query.execute();
        return query.getResultList();
    }


    public Object callMinhaFunction(String schema, String functionName, Class<?> returnType, Object... params) {
        String sql = "SELECT " + schema + "." + functionName + "(?" + ", ?".repeat(params.length - 1) + ") FROM dual";
        var query = entityManager.createNativeQuery(sql, returnType);
        for (int i = 0; i < params.length; i++) {
            query.setParameter(i + 1, params[i]);
        }
        return query.getSingleResult();
    }
}
