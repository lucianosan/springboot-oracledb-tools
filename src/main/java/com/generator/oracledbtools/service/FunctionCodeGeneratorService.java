package com.generator.oracledbtools.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FunctionCodeGeneratorService {
    private final CodeFileGeneratorService fileGenerator;

    @Autowired
    public FunctionCodeGeneratorService(CodeFileGeneratorService fileGenerator) {
        this.fileGenerator = fileGenerator;
    }

    /**
     * Gera Repository + Service para chamar uma function Oracle
     *
     * @param schema       schema da função
     * @param functionName nome da função
     */
    public void generateFunctionRepositoryService(String schema, String functionName) {
        generateRepository(schema, functionName);
        generateService(functionName);
    }

    private void generateRepository(String schema, String functionName) {
        String repoClass = capitalize(functionName) + "Repository";
        String repoPackage = "com.example.generated.repository";

        String template = """
                import jakarta.persistence.EntityManager;
                import jakarta.persistence.PersistenceContext;
                import jakarta.persistence.StoredProcedureQuery;
                import jakarta.persistence.ParameterMode;
                import org.springframework.stereotype.Repository;
                
                @Repository
                public class %s {
                
                    @PersistenceContext
                    private EntityManager entityManager;
                
                    public Object call(Object... params) {
                        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("%s.%s");
                        query.registerStoredProcedureParameter(1, Object.class, ParameterMode.OUT);
                
                        for (int i = 0; i < params.length; i++) {
                            query.registerStoredProcedureParameter(i + 2, params[i].getClass(), ParameterMode.IN);
                            query.setParameter(i + 2, params[i]);
                        }
                
                        query.execute();
                        return query.getOutputParameterValue(1);
                    }
                }
                """;

        String repoCode = String.format(template, repoClass, schema, functionName);
        fileGenerator.generateJavaFile(repoPackage, repoClass, repoCode);
    }

    private void generateService(String functionName) {
        String serviceClass = capitalize(functionName) + "Service";
        String servicePackage = "com.example.generated.service";
        String repoClass = capitalize(functionName) + "Repository";

        String template = """
                import org.springframework.stereotype.Service;
                import com.example.generated.repository.%s;
                
                @Service
                public class %s {
                
                    private final %s repository;
                
                    public %s(%s repository) {
                        this.repository = repository;
                    }
                
                    public Object execute(Object... params) {
                        return repository.call(params);
                    }
                }
                """;

        String serviceCode = String.format(template,
                repoClass, serviceClass, repoClass, serviceClass, repoClass);

        fileGenerator.generateJavaFile(servicePackage, serviceClass, serviceCode);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}

