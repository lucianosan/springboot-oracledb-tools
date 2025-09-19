package com.generator.oracledbtools.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProcedureDefinition {
    private String name;
    private String schema;
    private List<ProcedureParameter> parameters = new ArrayList<>();
    // getters e setters
}


