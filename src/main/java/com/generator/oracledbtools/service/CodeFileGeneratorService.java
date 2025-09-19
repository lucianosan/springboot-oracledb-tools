package com.generator.oracledbtools.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class CodeFileGeneratorService {
    private static final String BASE_PATH = "generated-src"; // pasta de saída

    public void generateJavaFile(String packageName, String className, String sourceCode) {
        try {
            String path = BASE_PATH + "/" + packageName.replace(".", "/");
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, className + ".java");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("package " + packageName + ";\n\n");
                writer.write(sourceCode);
            }
            System.out.println("✅ Arquivo gerado: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar arquivo Java", e);
        }
    }
}
