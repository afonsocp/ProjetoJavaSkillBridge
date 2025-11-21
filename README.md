# SkillBridge API - Backend Java Spring Boot

API REST desenvolvida em Java Spring Boot que serve como backend principal da plataforma SkillBridge. A aplicação conecta talentos a oportunidades de cursos e vagas sustentáveis, utilizando Inteligência Artificial para fornecer recomendações personalizadas e integração com módulo IoT para geração de planos de estudos.

## Sobre a API

A SkillBridge API é uma aplicação robusta construída com Spring Boot que gerencia toda a lógica de negócio da plataforma. Ela fornece endpoints RESTful para autenticação, gerenciamento de usuários, vagas, cursos, recomendações com IA e integração com serviços externos.

### Funcionalidades Principais

- Autenticação e autorização com JWT (JSON Web Tokens)
- Gerenciamento completo de usuários com perfil profissional
- CRUD de vagas e cursos
- Sistema de candidaturas a vagas
- Cálculo automático de compatibilidade entre usuário e vaga
- Recomendações personalizadas usando Google Gemini AI
- Integração com módulo IoT para geração de planos de estudos
- Sistema de auditoria com triggers Oracle
- Cache de dados para melhor performance
- Documentação automática com Swagger/OpenAPI

## Stack Tecnológica

- **Java 21** - Linguagem de programação
- **Maven 3.9+** - Gerenciador de dependências
- **Spring Boot 3.5.7** - Framework principal
  - Spring Web - REST APIs
  - Spring Data JPA - Persistência de dados
  - Spring Validation - Validação de dados
  - Spring Security - Segurança e autenticação
  - Spring Cache - Cache de dados
  - Spring Actuator - Monitoramento e health checks
- **Springdoc OpenAPI** - Documentação Swagger
- **Oracle Database 19c** - Banco de dados relacional
- **JWT** - Autenticação stateless

## Arquitetura da API

A API segue uma arquitetura em camadas:

```
Controllers (REST Endpoints)
    ↓
Services (Lógica de Negócio)
    ↓
Repositories (Acesso a Dados)
    ↓
Oracle Database (PL/SQL Packages)
```

### Integrações

- **Oracle Database**: Utiliza packages PL/SQL para operações complexas
- **Google Gemini API**: Para recomendações inteligentes com IA
- **API IoT Python**: Para geração de planos de estudos personalizados

## Pré-requisitos

1. **Java 21** instalado e configurado
2. **Maven 3.9+** instalado
3. **Oracle Database** acessível
   - Scripts assumem usuário `RM557863`
   - Ver instruções em `../bancodedados/README.md`
4. **Servidor IOT Python** rodando (para planos de estudos)
   - Ver instruções em `../IOT/ProjetoIOTSkillBridge/README.md`
5. **Chave API Gemini** (obtenha em: https://aistudio.google.com/apikey)

## Configuração Inicial

### 1. Configurar Banco de Dados Oracle

Execute os scripts na ordem (em `../bancodedados/sql/`):

1. `create_tables.sql` - Criação das tabelas
2. `functions.sql` - Funções PL/SQL utilitárias
3. `packages.sql` - Packages `PKG_USUARIOS` e `PKG_VAGAS`
4. `triggers.sql` - Triggers de auditoria
5. `create_recomendacao_ia_table.sql` - Tabela para recomendações com IA

Popular dados iniciais:

```sql
BEGIN
  pkg_usuarios.popular_dados_iniciais;
END;
/
COMMIT;
```

### 2. Configurar Variáveis de Ambiente

Edite `src/main/resources/application.properties`:

```properties
# Banco de Dados Oracle
spring.datasource.url=jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL
spring.datasource.username=${DB_USERNAME:RM557863}
spring.datasource.password=${DB_PASSWORD:sua-senha}

# JWT Secret (mínimo 32 caracteres)
security.jwt.secret=change-me-please-32-characters-minimum

# Gemini API (para recomendações com IA)
spring.ai.gemini.api-key=sua-chave-gemini-aqui
spring.ai.gemini.model=gemini-2.0-flash-exp

# Servidor IOT Python (para planos de estudos)
iot.service.url=http://localhost:8000
```

**Ou configure via variáveis de ambiente:**

- `DB_USERNAME` - Usuário do Oracle
- `DB_PASSWORD` - Senha do Oracle
- `GEMINI_API_KEY` - Chave da API Gemini
- `IOT_SERVICE_URL` - URL do serviço IoT Python
- `JWT_SECRET` - Secret para assinatura de tokens JWT

### 3. Instalar Dependências

```bash
cd api
mvn clean install
```

## Como Executar

### Executar Localmente

```bash
cd api
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

### Verificar Saúde da API

```bash
curl http://localhost:8080/actuator/health
```

### Acessar Documentação Swagger

Abra no navegador: `http://localhost:8080/swagger-ui.html`

A documentação Swagger fornece interface interativa para testar todos os endpoints da API.

## Endpoints Principais

### Autenticação

#### POST `/auth/register`

Registra um novo usuário no sistema e retorna token JWT.

**Request Body:**

```json
{
  "nome": "João Silva",
  "email": "joao@email.com",
  "senha": "senha123",
  "telefone": "(11) 99999-9999",
  "cidade": "São Paulo",
  "uf": "SP",
  "objetivoCarreira": "Desenvolvedor Java Sênior",
  "competencias": ["Java", "Spring Boot", "SQL"]
}
```

**Response:** Token JWT para autenticação

#### POST `/auth/login`

Autentica um usuário existente e retorna token JWT.

**Request Body:**

```json
{
  "email": "joao@email.com",
  "senha": "senha123"
}
```

**Response:** Token JWT

### Usuários

#### GET `/api/v1/usuarios`

Lista todos os usuários com paginação e cache.

**Headers:** `Authorization: Bearer <token-jwt>`

**Query Parameters:**

- `page` - Número da página (padrão: 0)
- `size` - Tamanho da página (padrão: 20)

### Vagas

#### GET `/api/v1/vagas`

Lista todas as vagas disponíveis com paginação.

**Headers:** `Authorization: Bearer <token-jwt>`

**Query Parameters:**

- `page` - Número da página
- `size` - Tamanho da página

#### GET `/api/v1/vagas/{id}`

Obtém detalhes de uma vaga específica.

**Headers:** `Authorization: Bearer <token-jwt>`

#### GET `/api/v1/vagas/{id}/compatibilidade`

Calcula a compatibilidade entre o usuário autenticado e uma vaga específica.

**Headers:** `Authorization: Bearer <token-jwt>`

**Response:**

```json
{
  "compatibilidade": 85.5,
  "competenciasMatch": ["Java", "Spring Boot"],
  "competenciasFaltantes": ["Docker", "Kubernetes"]
}
```

#### POST `/api/v1/vagas`

Cria uma nova vaga (requer permissões de administrador).

**Headers:** `Authorization: Bearer <token-jwt>`

### Aplicações

#### POST `/api/v1/aplicacoes`

Registra uma candidatura do usuário autenticado a uma vaga.

**Headers:** `Authorization: Bearer <token-jwt>`

**Request Body:**

```json
{
  "vagaId": "123",
  "mensagem": "Tenho interesse nesta vaga"
}
```

Este endpoint chama a procedure PL/SQL `PKG_VAGAS.REGISTRAR_APLICACAO`.

#### GET `/api/v1/aplicacoes`

Lista todas as candidaturas do usuário autenticado.

**Headers:** `Authorization: Bearer <token-jwt>`

### Cursos

#### GET `/api/v1/cursos`

Lista todos os cursos disponíveis com paginação.

**Headers:** `Authorization: Bearer <token-jwt>`

**Query Parameters:**

- `page` - Número da página
- `size` - Tamanho da página

#### GET `/api/v1/cursos/{id}`

Obtém detalhes de um curso específico.

**Headers:** `Authorization: Bearer <token-jwt>`

### Recomendações com IA

#### POST `/api/v1/ia/recomendacoes/{usuarioId}`

Gera recomendações personalizadas usando Google Gemini AI baseadas no perfil do usuário.

**Headers:** `Authorization: Bearer <token-jwt>`

**Response:**

```json
{
  "usuarioId": "123",
  "recomendacoes": "Baseado no seu perfil...",
  "dataGeracao": "2025-01-15T10:30:00"
}
```

#### GET `/api/v1/ia/recomendacoes/{usuarioId}`

Busca a última recomendação gerada para um usuário.

**Headers:** `Authorization: Bearer <token-jwt>`

### Planos de Estudos (Integração IOT)

#### POST `/api/v1/planos-estudos/gerar`

Gera um plano de estudos personalizado através da integração com o módulo IoT Python.

**Headers:** `Authorization: Bearer <token-jwt>`

**Request Body:**

```json
{
  "objetivoCarreira": "Tornar-me desenvolvedor Java Sênior",
  "nivelAtual": "Intermediário",
  "competenciasAtuais": ["Java", "Spring Boot", "SQL"],
  "tempoDisponivelSemana": 15,
  "prazoMeses": 6,
  "areasInteresse": ["Microservices", "Cloud"]
}
```

**Response:** Plano de estudos estruturado com etapas, recursos e métricas.

**Nota:** Requer que o servidor IoT Python esteja rodando na URL configurada em `iot.service.url`.

### Segurança

Todos os endpoints (exceto `/auth/**`, Swagger e actuator) requerem autenticação:

```
Authorization: Bearer <token-jwt>
```

O token JWT é obtido através dos endpoints de autenticação e deve ser incluído em todas as requisições subsequentes.

## Integração com Oracle Database

A API utiliza packages PL/SQL para operações complexas:

### Procedures Principais

- **`PKG_USUARIOS.INSERIR_USUARIO`** - Cadastro de usuários com validações
- **`PKG_VAGAS.REGISTRAR_APLICACAO`** - Registro de candidaturas com validações
- **`PKG_VAGAS.CALCULAR_COMPATIBILIDADE`** - Cálculo de compatibilidade entre competências

### Funções

- **`fn_gerar_json_manual`** - Gera JSON para exportação de dados
- **`fn_calcular_compatibilidade`** - Calcula compatibilidade entre competências

### Triggers de Auditoria

Triggers automáticos registram todas as operações importantes:

- INSERT em `usuario`, `vaga`, `curso`, `aplicacao`
- UPDATE em `usuario`, `vaga`
- DELETE em `vaga`, `curso`

Logs são salvos automaticamente na tabela `log_auditoria` para rastreabilidade.

## Integração com IOT (Deep Learning)

A API integra com o módulo Python FastAPI para gerar planos de estudos personalizados usando IA Generativa.

**Fluxo de Integração:**

1. Cliente faz requisição: `POST /api/v1/planos-estudos/gerar`
2. API Java valida dados e chama serviço IoT: `POST {iot.service.url}/gerar-plano-estudos`
3. Serviço IoT processa com Gemini API e retorna plano estruturado
4. API Java retorna resposta ao cliente

**Configuração:** `iot.service.url=http://localhost:8000` (ou URL de produção)

**Tratamento de Erros:** A API trata timeouts e erros do serviço IoT, retornando mensagens apropriadas ao cliente.

## Testes Automatizados

Execute os testes unitários:

```bash
mvn test
```

**Principais testes:**

- `UsuarioServiceTest` - Testa cadastro e chamada a `PKG_USUARIOS`
- `AplicacaoServiceTest` - Testa registro de candidatura via `PKG_VAGAS`
- `VagaServiceTest` - Testa cálculo de compatibilidade
- `RecommendationServiceTest` - Testa recomendações com IA

## Coleção Postman

Importe `../postman/SkillBridge.postman_collection.json` no Postman:

1. Configure `{{base_url}}` = `http://localhost:8080`
2. Execute `Auth - Registrar usuário` → `Auth - Login`
3. Copie o token para `{{auth_token}}`
4. Teste os endpoints protegidos

A coleção Postman inclui exemplos de todas as requisições principais.

## Deploy em Produção

A aplicação está disponível em produção através do Render:

- **API Java (Spring Boot):** https://projetojavaskillbridge.onrender.com
- **Swagger UI:** https://projetojavaskillbridge.onrender.com/swagger-ui.html
- **Health Check:** https://projetojavaskillbridge.onrender.com/actuator/health

### Nota sobre Hibernação do Render

O Render oferece um plano gratuito que coloca os serviços em hibernação após 15 minutos de inatividade. A primeira requisição após hibernação pode levar 30-60 segundos para "acordar" o serviço.

## Build e Deploy

### Gerar JAR

```bash
mvn clean package
```

O JAR será gerado em: `target/skillbridge-api-0.0.1-SNAPSHOT.jar`

### Executar JAR

```bash
java -jar target/skillbridge-api-0.0.1-SNAPSHOT.jar
```

### Deploy em Nuvem

1. Configure variáveis de ambiente:
   - `DB_USERNAME`, `DB_PASSWORD`
   - `GEMINI_API_KEY`
   - `IOT_SERVICE_URL`
   - `JWT_SECRET`
2. Garanta acesso ao Oracle (VPN/rede corporativa)
3. Inicie o servidor IOT Python separadamente
4. Configure health checks para manter serviço ativo

## Estrutura do Projeto

```
api/
├── src/
│   ├── main/
│   │   ├── java/br/com/skillbridge/api/
│   │   │   ├── config/          # Configurações (Security, Cache, etc)
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Tratamento de exceções
│   │   │   ├── model/           # Entidades JPA
│   │   │   ├── repository/      # Repositórios Spring Data
│   │   │   ├── security/        # JWT e autenticação
│   │   │   └── service/         # Lógica de negócio
│   │   └── resources/
│   │       ├── application.properties
│   │       └── messages*.properties  # i18n
│   └── test/                    # Testes unitários
└── pom.xml
```

## Troubleshooting

### Erro de conexão com Oracle

- Verifique credenciais em `application.properties`
- Confirme acesso à rede/VPN
- Valide scripts SQL executados
- Teste conexão manual com SQL Developer

### Erro 403 em endpoints protegidos

- Obtenha token via `/auth/login`
- Inclua header: `Authorization: Bearer <token>`
- Verifique se token não expirou (tokens JWT têm validade)

### Erro ao chamar serviço IOT

- Verifique se servidor Python está rodando na porta 8000
- Confirme `iot.service.url` em `application.properties`
- Teste endpoint diretamente: `curl http://localhost:8000/health`
- Verifique logs do servidor Python

### Erro ao gerar recomendações com IA

- Verifique se `GEMINI_API_KEY` está configurada
- Confirme quota da API Gemini
- Verifique logs da aplicação para detalhes do erro

### Problemas de cache

- Limpe cache se dados não atualizarem: `POST /actuator/caches`
- Verifique configuração de cache em `application.properties`

---

**SkillBridge API – Conectando talentos, habilidades e oportunidades no futuro da energia sustentável.**
