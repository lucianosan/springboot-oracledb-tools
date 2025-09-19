package com.generator.oracledbtools.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EntityGeneratorService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CodeFileGeneratorService fileGenerator;

    public EntityGeneratorService(CodeFileGeneratorService fileGenerator) {
        this.fileGenerator = fileGenerator;
    }

    @SuppressWarnings("unchecked")
    public void generateEntity(String schema, String table) {
        String sql = """
                SELECT column_name, data_type
                FROM all_tab_columns
                WHERE owner = :schema AND table_name = :table
                ORDER BY column_id
                """;

        List<?> colsRaw = entityManager.createNativeQuery(sql)
                .setParameter("schema", schema.toUpperCase())
                .setParameter("table", table.toUpperCase())
                .getResultList();

        List<Object[]> columns = new ArrayList<>();
        for (Object r : colsRaw) {
            if (r instanceof Object[]) {
                columns.add((Object[]) r);
            } else {
                // protecão: se vier um único valor, colocamos em array
                columns.add(new Object[]{r});
            }
        }

        // pegar chave primária
        String pkSql = """
                SELECT cols.column_name
                FROM all_constraints cons, all_cons_columns cols
                WHERE cons.constraint_type = 'P'
                  AND cons.constraint_name = cols.constraint_name
                  AND cons.owner = :schema
                  AND cols.table_name = :table
                """;

        List<?> pkRaw = entityManager.createNativeQuery(pkSql)
                .setParameter("schema", schema.toUpperCase())
                .setParameter("table", table.toUpperCase())
                .getResultList();

        List<String> pkColumns = pkRaw.stream()
                .map(Object::toString)
                .collect(Collectors.toList());

        String className = toPascalCase(table) + "Entity";
        String packageName = "com.qintess.generated.entity";

        StringBuilder fields = new StringBuilder();
        StringBuilder getters = new StringBuilder();

        boolean compositePk = pkColumns.size() > 1;

        if (compositePk) {
            // Criar classe embeddable
            String embeddableName = toPascalCase(table) + "Id";
            String embeddablePackage = packageName;

            StringBuilder embeddableFields = new StringBuilder();
            StringBuilder embeddableGetters = new StringBuilder();

            for (Object[] col : columns) {
                String colName = (String) col[0];
                String javaType = mapType((String) col[1]);

                if (pkColumns.contains(colName)) {
                    String fieldName = toCamelCase(colName);
                    embeddableFields.append("    private ")
                            .append(javaType).append(" ")
                            .append(fieldName).append(";\n");
                    embeddableGetters.append(generateGetterSetter(javaType, fieldName));
                }
            }

            String embeddableTemplate = """
                    import jakarta.persistence.Embeddable;
                    import java.io.Serializable;
                    
                    @Embeddable
                    public class %s implements Serializable {
                    %s
                    
                    %s
                    }
                    """;

            String embeddableCode = String.format(
                    embeddableTemplate,
                    embeddableName,
                    embeddableFields.toString(),
                    embeddableGetters.toString()
            );

            fileGenerator.generateJavaFile(embeddablePackage, embeddableName, embeddableCode);

            // entidade: @EmbeddedId + os demais campos (não-PK)
            fields.append("    @EmbeddedId\n    private ").append(embeddableName).append(" id;\n\n");
            getters.append("    public ").append(embeddableName).append(" getId() { return this.id; }\n");
            getters.append("    public void setId(").append(embeddableName).append(" id) { this.id = id; }\n\n");

            for (Object[] col : columns) {
                String colName = (String) col[0];
                if (pkColumns.contains(colName)) continue;
                String javaType = mapType((String) col[1]);
                String fieldName = toCamelCase(colName);
                fields.append("    @Column(name = \"").append(colName).append("\")\n");
                fields.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
                getters.append(generateGetterSetter(javaType, fieldName));
            }

        } else {
            for (Object[] col : columns) {
                String colName = (String) col[0];
                String javaType = mapType((String) col[1]);
                String fieldName = toCamelCase(colName);

                if (pkColumns.contains(colName)) {
                    fields.append("    @Id\n");
                }
                fields.append("    @Column(name = \"").append(colName).append("\")\n");
                fields.append("    private ").append(javaType).append(" ").append(fieldName).append(";\n\n");
                getters.append(generateGetterSetter(javaType, fieldName));
            }
        }

        String entityTemplate = """
                import jakarta.persistence.*;
                import java.io.Serializable;
                
                @Entity
                @Table(name = "%s", schema = "%s")
                public class %s implements Serializable {
                %s
                
                %s
                }
                """;

        String entityCode = String.format(
                entityTemplate,
                table,
                schema,
                className,
                fields.toString(),
                getters.toString()
        );

        fileGenerator.generateJavaFile(packageName, className, entityCode);
    }

    private String mapType(String oracleType) {
        return switch (oracleType) {
            case "VARCHAR2", "CHAR", "CLOB" -> "String";
            case "NUMBER" -> "java.math.BigDecimal";
            case "DATE" -> "java.time.LocalDate";
            case "TIMESTAMP" -> "java.time.LocalDateTime";
            default -> "String";
        };
    }

    private String generateGetterSetter(String type, String name) {
        String camel = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        String template = """
                public %s get%s() { return this.%s; }
                public void set%s(%s %s) { this.%s = %s; }
                """;

        return String.format(template, type, camel, name, camel, type, name, name, name);
    }

    private String toPascalCase(String text) {
        if (text == null || text.isEmpty()) return text;
        String camel = toCamelCase(text);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private String toCamelCase(String colName) {
        if (colName == null || colName.isEmpty()) return colName == null ? null : colName;
        String lower = colName.toLowerCase();
        Pattern p = Pattern.compile("_(.)");
        Matcher m = p.matcher(lower);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            // group 1 -> single char after underscore
            m.appendReplacement(sb, m.group(1).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
