package com.generator.oracledbtools.service;

import com.generator.oracledbtools.dto.ProcedureDefinition;
import com.generator.oracledbtools.dto.ProcedureParameter;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProcedureParserService {

    public ProcedureDefinition parse(String sql) {
        ProcedureDefinition def = new ProcedureDefinition();

        // Regex simples para capturar nome da procedure
        Pattern pName = Pattern.compile("PROCEDURE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher mName = pName.matcher(sql);
        if (mName.find()) {
            def.setName(mName.group(1));
        }

        // Regex para parâmetros
        Pattern pParam = Pattern.compile("(\\w+)\\s+(IN|OUT|IN OUT)\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher mParam = pParam.matcher(sql);
        while (mParam.find()) {
            ProcedureParameter param = new ProcedureParameter();
            param.setName(mParam.group(1));
            param.setDirection(mParam.group(2).toUpperCase());
            param.setOracleType(mParam.group(3).toUpperCase());
            param.setJavaType(mapToJavaType(param.getOracleType()));
            def.getParameters().add(param);
        }

        return def;
    }

    private String mapToJavaType(String oracleType) {
        return switch (oracleType) {
            case "VARCHAR2", "CHAR", "CLOB" -> "String";
            case "NUMBER", "DECIMAL", "INTEGER" -> "BigDecimal";
            case "DATE", "TIMESTAMP" -> "LocalDateTime";
            default -> "Object";
        };
    }
}
