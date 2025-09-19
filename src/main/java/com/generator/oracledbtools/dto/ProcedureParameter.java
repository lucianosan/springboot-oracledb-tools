package com.generator.oracledbtools.dto;

import lombok.Data;

@Data
public class ProcedureParameter {
    private String name;
    private String direction; // IN, OUT, INOUT
    private String oracleType; // VARCHAR2, NUMBER, DATE, etc.
    private String javaType;   // String, BigDecimal, LocalDate, etc.
}