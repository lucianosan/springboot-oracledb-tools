package com.generator.oracledbtools.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QueryGeneratorService {

    private static final String BASE_PACKAGE = "com.exemplo.generated";
    private static final String SRC_PATH = "src/main/java/";

    public String generateFromQuery(String sql, String className) {
        List<String> fields = extractFields(sql);

        String packagePath = BASE_PACKAGE.replace(".", "/");
        String targetDir = SRC_PATH + packagePath;
        new File(targetDir).mkdirs();

        generateFile(className + "ResultDto.java", generateDto(className, fields));
        generateFile(className + "Repository.java", generateRepository(className, sql, fields));
        generateFile(className + "Service.java", generateService(className));
        generateFile(className + "Controller.java", generateController(className));

        return "Classes geradas em: " + targetDir;
    }

    private void generateFile(String fileName, String content) {
        try {
            String packagePath = BASE_PACKAGE.replace(".", "/");
            File file = new File(SRC_PATH + packagePath + "/" + fileName);
            try (FileWriter fw = new FileWriter(file)) {
                fw.write("package " + BASE_PACKAGE + ";\n\n");
                fw.write(content);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar arquivo " + fileName, e);
        }
    }

    private List<String> extractFields(String sql) {
        // pega apenas parte do SELECT até o FROM
        String regex = "(?i)select\\s+(.*?)\\s+from";
        Matcher matcher = Pattern.compile(regex).matcher(sql);
        if (matcher.find()) {
            String[] cols = matcher.group(1).split(",");
            List<String> fields = new ArrayList<>();
            for (String col : cols) {
                String clean = col.trim().split("\\s+")[0];
                clean = clean.replaceAll("[^a-zA-Z0-9_]", "");
                fields.add(clean.toLowerCase());
            }
            return fields;
        }
        throw new IllegalArgumentException("SQL inválido, não foi possível extrair campos do SELECT.");
    }

    private String generateDto(String className, List<String> fields) {
        StringBuilder code = new StringBuilder();
        code.append("import lombok.Data;\n\n");
        code.append("@Data\n");
        code.append("public class ").append(className).append("ResultDto {\n");
        for (String f : fields) {
            code.append("  private String ").append(f).append(";\n"); // tudo String por simplicidade
        }
        code.append("}\n");
        return code.toString();
    }

    private String generateRepository(String className, String sql, List<String> fields) {
        StringBuilder code = new StringBuilder();
        code.append("import jakarta.persistence.*;\n");
        code.append("import org.springframework.stereotype.Repository;\n");
        code.append("import java.util.*;\n\n");

        code.append("@Repository\n");
        code.append("public class ").append(className).append("Repository {\n");
        code.append("  @PersistenceContext EntityManager em;\n\n");
        code.append("  public List<").append(className).append("ResultDto> executeQuery() {\n");
        code.append("    List<Object[]> result = em.createNativeQuery(\"").append(sql.replace("\"", "\\\"")).append("\").getResultList();\n");
        code.append("    List<").append(className).append("ResultDto> list = new ArrayList<>();\n");
        code.append("    for(Object[] row : result){\n");
        code.append("      ").append(className).append("ResultDto dto = new ").append(className).append("ResultDto();\n");
        for (int i = 0; i < fields.size(); i++) {
            String f = fields.get(i);
            code.append("      dto.set").append(capitalize(f)).append("(row[").append(i).append("] != null ? row[").append(i).append("].toString() : null);\n");
        }
        code.append("      list.add(dto);\n");
        code.append("    }\n");
        code.append("    return list;\n");
        code.append("  }\n");
        code.append("}\n");
        return code.toString();
    }

    private String generateService(String className) {
        return "import org.springframework.stereotype.Service;\n" +
               "import java.util.*;\n\n" +
               "@Service\n" +
               "public class " + className + "Service {\n" +
               "  private final " + className + "Repository repo;\n\n" +
               "  public " + className + "Service(" + className + "Repository repo) {\n" +
               "    this.repo = repo;\n" +
               "  }\n\n" +
               "  public List<" + className + "ResultDto> execute() {\n" +
               "    return repo.executeQuery();\n" +
               "  }\n" +
               "}\n";
    }

    private String generateController(String className) {
        return "import org.springframework.http.ResponseEntity;\n" +
               "import org.springframework.web.bind.annotation.*;\n" +
               "import java.util.*;\n\n" +
               "@RestController\n" +
               "@RequestMapping(\"/query/" + className.toLowerCase() + "\")\n" +
               "public class " + className + "Controller {\n\n" +
               "  private final " + className + "Service service;\n\n" +
               "  public " + className + "Controller(" + className + "Service service) {\n" +
               "    this.service = service;\n" +
               "  }\n\n" +
               "  @PostMapping(\"/execute\")\n" +
               "  public ResponseEntity<List<" + className + "ResultDto>> execute() {\n" +
               "    return ResponseEntity.ok(service.execute());\n" +
               "  }\n" +
               "}\n";
    }

    private String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

