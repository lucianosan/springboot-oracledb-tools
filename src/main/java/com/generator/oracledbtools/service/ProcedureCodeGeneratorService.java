package com.generator.oracledbtools.service;

import org.springframework.stereotype.Service;

@Service
public class ProcedureCodeGeneratorService {

    private final CodeFileGeneratorService fileGenerator;

    public ProcedureCodeGeneratorService(CodeFileGeneratorService fileGenerator) {
        this.fileGenerator = fileGenerator;
    }

    /**
     * Gera Repository, Service e Controller para uma procedure Oracle.
     *
     * @param schema        schema da procedure
     * @param procedureName nome da procedure
     */
    public void generateAll(String schema, String procedureName) {
        generateRepository(schema, procedureName);
        generateService(schema, procedureName);
    }

    private void generateRepository(String schema, String procedureName) {
        String repoClass = capitalize(procedureName) + "Repository";
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
                
                    public void call(Object... params) {
                        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("%s.%s");
                
                        for (int i = 0; i < params.length; i++) {
                            query.registerStoredProcedureParameter(i + 1, params[i].getClass(), ParameterMode.IN);
                            query.setParameter(i + 1, params[i]);
                        }
                
                        query.execute();
                    }
                }
                """;

        String repoCode = String.format(template, repoClass, schema, procedureName);
        fileGenerator.generateJavaFile(repoPackage, repoClass, repoCode);
    }

    private void generateService(String schema, String procedureName) {
        String serviceClass = capitalize(procedureName) + "Service";
        String servicePackage = "com.example.generated.service";
        String repoClass = capitalize(procedureName) + "Repository";

        String template = """
                import org.springframework.stereotype.Service;
                import com.example.generated.repository.%s;
                
                @Service
                public class %s {
                
                    private final %s repository;
                
                    public %s(%s repository) {
                        this.repository = repository;
                    }
                
                    public void execute(Object... params) {
                        repository.call(params);
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
