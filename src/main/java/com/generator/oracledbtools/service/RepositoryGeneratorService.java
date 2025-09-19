package com.generator.oracledbtools.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryGeneratorService {
    private final CodeFileGeneratorService fileGenerator;

    @Autowired
    public RepositoryGeneratorService(CodeFileGeneratorService fileGenerator) {
        this.fileGenerator = fileGenerator;
    }

    public void generateRepository(String schema, String procedureName) {
        String className = schema + "_" + procedureName + "Repository";
        String packageName = "com.example.generated.repository";

        String sourceCode = String.format(
                "import jakarta.persistence.EntityManager;%n" +
                        "import jakarta.persistence.PersistenceContext;%n" +
                        "import jakarta.persistence.ParameterMode;%n" +
                        "import jakarta.persistence.StoredProcedureQuery;%n" +
                        "import org.springframework.stereotype.Repository;%n%n" +
                        "@Repository%n" +
                        "public class %s {%n%n" +
                        "    @PersistenceContext%n" +
                        "    private EntityManager entityManager;%n%n" +
                        "    public Object call(Object... params) {%n" +
                        "        StoredProcedureQuery query = entityManager.createStoredProcedureQuery(\"%s.%s\");%n%n" +
                        "        for (int i = 0; i < params.length; i++) {%n" +
                        "            query.registerStoredProcedureParameter(i + 1, params[i].getClass(), ParameterMode.IN);%n" +
                        "            query.setParameter(i + 1, params[i]);%n" +
                        "        }%n%n" +
                        "        query.execute();%n" +
                        "        return query.getResultList();%n" +
                        "    }%n" +
                        "}%n",
                className, schema, procedureName
        );

        fileGenerator.generateJavaFile(packageName, className, sourceCode);
    }
}
