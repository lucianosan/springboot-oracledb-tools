package com.generator.oracledbtools.service;

import com.generator.oracledbtools.dto.ProcedureDefinition;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;

@Service
public class ClassGeneratorService {

    private static final String BASE_PACKAGE = "com.exemplo.generated";
    private static final String SRC_PATH = "src/main/java/";

    public String generate(ProcedureDefinition def) {
        String packagePath = BASE_PACKAGE.replace(".", "/");
        String targetDir = SRC_PATH + packagePath;

        new File(targetDir).mkdirs();

        // Gerar DTOs, Repository, Service e Controller
        generateFile(def, def.getName() + "InDto.java", generateInDto(def));
        generateFile(def, def.getName() + "OutDto.java", generateOutDto(def));
        generateFile(def, def.getName() + "Repository.java", generateRepository(def));
        generateFile(def, def.getName() + "Service.java", generateService(def));
        generateFile(def, def.getName() + "Controller.java", generateController(def));

        return "Classes geradas em: " + targetDir;
    }

    private void generateFile(ProcedureDefinition def, String fileName, String content) {
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

    private String generateInDto(ProcedureDefinition def) {
        StringBuilder code = new StringBuilder();
        code.append("import lombok.Data;\n\n");
        code.append("@Data\n");
        code.append("public class ").append(def.getName()).append("InDto {\n");
        def.getParameters().stream()
                .filter(p -> p.getDirection().contains("IN"))
                .forEach(p -> code.append("  private ").append(p.getJavaType()).append(" ").append(p.getName()).append(";\n"));
        code.append("}\n");
        return code.toString();
    }

    private String generateOutDto(ProcedureDefinition def) {
        StringBuilder code = new StringBuilder();
        code.append("import lombok.Data;\n\n");
        code.append("@Data\n");
        code.append("public class ").append(def.getName()).append("OutDto {\n");
        def.getParameters().stream()
                .filter(p -> p.getDirection().contains("OUT"))
                .forEach(p -> code.append("  private ").append(p.getJavaType()).append(" ").append(p.getName()).append(";\n"));
        code.append("}\n");
        return code.toString();
    }

    private String generateRepository(ProcedureDefinition def) {
        StringBuilder code = new StringBuilder();
        code.append("import jakarta.persistence.*;\n");
        code.append("import org.springframework.stereotype.Repository;\n\n");

        code.append("@Repository\npublic class ").append(def.getName()).append("Repository {\n")
                .append("  @PersistenceContext EntityManager em;\n\n")
                .append("  public ").append(def.getName()).append("OutDto call(")
                .append(def.getName()).append("InDto in) {\n")
                .append("    StoredProcedureQuery query = em.createStoredProcedureQuery(")
                .append("\"").append(def.getName()).append("\");\n");

        def.getParameters().forEach(p -> {
            if (p.getDirection().contains("IN")) {
                code.append("    query.registerStoredProcedureParameter(\"")
                        .append(p.getName()).append("\", ").append(p.getJavaType()).append(".class, ParameterMode.IN);\n");
                code.append("    query.setParameter(\"").append(p.getName()).append("\", in.get")
                        .append(capitalize(p.getName())).append("());\n");
            }
            if (p.getDirection().contains("OUT")) {
                code.append("    query.registerStoredProcedureParameter(\"")
                        .append(p.getName()).append("\", ").append(p.getJavaType()).append(".class, ParameterMode.OUT);\n");
            }
        });

        code.append("    query.execute();\n");
        code.append("    ").append(def.getName()).append("OutDto out = new ").append(def.getName()).append("OutDto();\n");

        def.getParameters().stream().filter(p -> p.getDirection().contains("OUT")).forEach(p -> {
            code.append("    out.set").append(capitalize(p.getName()))
                    .append("((").append(p.getJavaType()).append(") query.getOutputParameterValue(\"").append(p.getName()).append("\"));\n");
        });

        code.append("    return out;\n  }\n}\n");
        return code.toString();
    }

    private String generateService(ProcedureDefinition def) {
        return """
            import org.springframework.stereotype.Service;

            @Service
            public class """ + def.getName() + "Service {\n" +
               "  private final " + def.getName() + "Repository repo;\n\n" +
               "  public " + def.getName() + "Service(" + def.getName() + "Repository repo) {\n" +
               "    this.repo = repo;\n  }\n\n" +
               "  public " + def.getName() + "OutDto execute(" + def.getName() + "InDto in) {\n" +
               "    return repo.call(in);\n" +
               "  }\n" +
               "}\n";
    }

    private String generateController(ProcedureDefinition def) {
        return "import org.springframework.http.ResponseEntity;\n" +
               "import org.springframework.web.bind.annotation.*;\n\n" +
               "@RestController\n" +
               "@RequestMapping(\"/" + def.getName().toLowerCase() + "\")\n" +
               "public class " + def.getName() + "Controller {\n\n" +
               "  private final " + def.getName() + "Service service;\n\n" +
               "  public " + def.getName() + "Controller(" + def.getName() + "Service service) {\n" +
               "    this.service = service;\n" +
               "  }\n\n" +
               "  @PostMapping(\"/execute\")\n" +
               "  public ResponseEntity<" + def.getName() + "OutDto> execute(@RequestBody " + def.getName() + "InDto inDto) {\n" +
               "    return ResponseEntity.ok(service.execute(inDto));\n" +
               "  }\n" +
               "}\n";
    }


    private String capitalize(String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}

