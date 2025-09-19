# springboot-oracledb-tools
A spring boot application that helps generate classes (service, repository, Controller) for oracle database.

Este projeto demonstra como gerar dinamicamente classes Java (Repository, Service, Controller, DTOs e Entidades) a partir de:

Stored Procedures (Oracle DB)

Native Queries SQL

Schemas de Banco de Dados → Entidades JPA com @Embedded

O sistema recebe código ou consultas via endpoints REST, processa parâmetros/campos, e cria automaticamente os arquivos necessários para expor endpoints ou mapear entidades.

🚀 Funcionalidades
🔹 1. Stored Procedure

Recebe o código fonte da procedure via endpoint.

Gera automaticamente:

InDto (parâmetros de entrada).

OutDto (parâmetros de saída).

Repository com chamada via EntityManager.createStoredProcedureQuery.

Service para encapsular a lógica.

Controller com endpoint POST /{procedure}/execute.

🔹 2. Native Query

Recebe a query SQL (SELECT ... FROM ...) via endpoint.

Extrai os campos da cláusula SELECT.

Gera automaticamente:

ResultDto com os campos da query.

Repository usando EntityManager.createNativeQuery.

Service para executar a query.

Controller com endpoint POST /query/{className}/execute.

🔹 3. Entidades JPA com suporte a @Embedded

Recebe um schema ou metadados de tabelas.

Gera automaticamente:

Entidades JPA com @Entity, @Table, @Id etc.

Campos simples mapeados para colunas (@Column).

Objetos compostos mapeados com @Embedded + @Embeddable.

Exemplo:

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Embedded
    private Endereco endereco;
}

@Embeddable
public class Endereco {
    private String rua;
    private String cidade;
    private String cep;
}


Útil quando a tabela contém grupos de colunas que representam um objeto coeso (ex: endereço, documento, etc.).

📂 Estrutura das Classes Geradas

Exemplo de geração para uma procedure MY_PROC:

src/main/java/com/exemplo/generated/
├── MyProcInDto.java
├── MyProcOutDto.java
├── MyProcRepository.java
├── MyProcService.java
└── MyProcController.java


Exemplo de geração para uma query SELECT id, nome FROM usuarios:

src/main/java/com/exemplo/generated/
├── DynamicQuery1668891234567ResultDto.java
├── DynamicQuery1668891234567Repository.java
├── DynamicQuery1668891234567Service.java
└── DynamicQuery1668891234567Controller.java


Exemplo de geração para uma tabela clientes com campos de endereço:

src/main/java/com/exemplo/generated/
├── Cliente.java          (entidade principal com @Entity)
└── Endereco.java         (objeto embutido com @Embeddable)

⚙️ Dependências Importantes

No pom.xml:

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.34</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc11</artifactId>
    <version>21.9.0.0</version>
</dependency>


⚠️ Ajuste o driver do banco conforme necessário.

📌 Endpoints
1. Gerar classes a partir de Stored Procedure
POST /generator/procedure

Request Body:
{
  "procedureCode": "CREATE OR REPLACE PROCEDURE MY_PROC (p_id IN NUMBER, p_nome OUT VARCHAR2) AS ..."
}

2. Gerar classes a partir de Native Query
POST /generator/query

Request Body:
{
  "sql": "SELECT id, nome, email FROM usuarios"
}

3. Gerar entidades a partir de schema
POST /generator/entities

Request Body (exemplo simplificado):
{
  "table": "clientes",
  "columns": [
    { "name": "id", "type": "NUMBER", "pk": true },
    { "name": "nome", "type": "VARCHAR" },
    { "name": "rua", "type": "VARCHAR", "embedded": "endereco" },
    { "name": "cidade", "type": "VARCHAR", "embedded": "endereco" },
    { "name": "cep", "type": "VARCHAR", "embedded": "endereco" }
  ]
}


Resultado:

Cliente.java com campo @Embedded Endereco endereco;

Endereco.java com @Embeddable e os atributos rua, cidade, cep.

🔧 Como funciona internamente

Parsing:

Procedures: leitura de parâmetros IN/OUT e tipos Oracle → mapeados para tipos Java.

Queries: regex extrai colunas do SELECT.

Entidades: leitura de metadados de tabelas → gera classes com @Entity, @Embeddable conforme os grupos.

Gerador:

Cria classes .java em src/main/java/com/exemplo/generated.

Usa Lombok (@Data) para DTOs e entidades.

Execução:

Procedures: EntityManager.createStoredProcedureQuery.

Queries: EntityManager.createNativeQuery.

Entidades: classes JPA utilizáveis diretamente no EntityManager/Spring Data JPA.

🛠️ Melhorias futuras

Mapear tipos SQL → tipos Java mais precisos (NUMBER → BigDecimal, DATE → LocalDateTime, etc.).

Suporte a alias em queries (SELECT u.id AS userId).

Reconhecimento automático de agrupamentos para @Embedded.

Cache das classes geradas para evitar recriação desnecessária.

Compilar dinamicamente sem reiniciar a aplicação.
