# 🚀 Deploy da API no Render - Guia Passo a Passo

Este guia mostra como fazer deploy da API Java Spring Boot no Render.

---

## 📋 Pré-requisitos

- Conta no Render (https://render.com) - pode usar GitHub para login
- Repositório Git com o código da API
- Acesso ao banco de dados Oracle da FIAP

---

## 🎯 Passo a Passo

### 1. Preparar o Repositório

Certifique-se de que o código está no Git e no GitHub/GitLab/Bitbucket.

### 2. Criar Novo Web Service no Render

1. **Acesse:** https://dashboard.render.com
2. **Clique em:** "New +" → "Web Service"
3. **Conecte seu repositório:**
   - Se ainda não conectou, clique em "Connect account" e autorize o Render
   - Selecione seu repositório
   - Clique em "Connect"

### 3. Configurar o Serviço

Preencha os campos:

- **Name:** `skillbridge-api`
- **Root Directory:** `api` ⚠️ **IMPORTANTE:** Se seu repositório tem a pasta `api/`, coloque `api` aqui
- **Environment:** `Java`
- **Build Command:** `mvn clean install -DskipTests`
- **Start Command:** `java -jar target/skillbridge-api-0.0.1-SNAPSHOT.jar`
- **Plan:** Escolha o plano (Free funciona para testes)

### 4. Configurar Variáveis de Ambiente

Na seção **Environment Variables**, adicione:

| Key | Value |
|-----|-------|
| `DB_USERNAME` | `RM557863` |
| `DB_PASSWORD` | `091105` ⚠️ **Altere se necessário** |
| `SPRING_DATASOURCE_URL` | `jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL` |
| `SECURITY_JWT_SECRET` | `seu-secret-seguro-minimo-32-caracteres-aqui` ⚠️ **Altere!** |
| `SPRING_AI_GEMINI_API_KEY` | `AIzaSyCuOLheV5Rc6C0V_yJArFjDFQxjyzN971s` |
| `SPRING_AI_GEMINI_MODEL` | `gemini-2.0-flash-exp` |
| `IOT_SERVICE_URL` | `http://localhost:8000` ⚠️ **Configure depois com URL real do IoT** |

**⚠️ IMPORTANTE:**
- `SECURITY_JWT_SECRET`: Use um secret seguro com pelo menos 32 caracteres
- `IOT_SERVICE_URL`: Configure depois que fizer deploy do serviço IoT
- `PORT`: Não precisa configurar, o Render define automaticamente

### 5. Deploy

1. **Clique em:** "Create Web Service"
2. **Aguarde o build** (pode levar alguns minutos)
3. **Verifique os logs** para ver se está tudo ok

### 6. Verificar se Funcionou

Após o deploy, você receberá uma URL como:
```
https://skillbridge-api.onrender.com
```

**Teste o health check:**
```bash
curl https://skillbridge-api.onrender.com/actuator/health
```

**Resposta esperada:**
```json
{
  "status": "UP"
}
```

**Acesse o Swagger:**
```
https://skillbridge-api.onrender.com/swagger-ui.html
```

---

## 🔧 Configurações Importantes

### Porta Dinâmica

O Render define a porta automaticamente via variável `PORT`. O `application.properties` já está configurado para usar isso:

```properties
server.port=${PORT:8080}
```

### Banco de Dados Oracle

⚠️ **ATENÇÃO:** O Oracle da FIAP (`oracle.fiap.com.br`) pode ter firewall bloqueando conexões externas.

**Se não conseguir conectar:**
- Verifique se o IP do Render está liberado no firewall da FIAP
- Considere usar um banco Oracle em nuvem (Oracle Cloud, AWS RDS)
- Ou use um túnel SSH se disponível

### Logs

Para ver os logs do deploy:
1. No dashboard do Render, vá para seu serviço
2. Clique na aba "Logs"
3. Veja os logs em tempo real

---

## 🐛 Troubleshooting

### Erro: "Cannot connect to database"

**Problema:** Render não consegue acessar `oracle.fiap.com.br:1521`

**Soluções:**
1. Verifique se o banco Oracle permite conexões externas
2. Verifique as credenciais (`DB_USERNAME` e `DB_PASSWORD`)
3. Teste a conexão localmente primeiro
4. Considere usar um banco em nuvem

### Erro: "Build failed"

**Possíveis causas:**
- Dependências não encontradas
- Erro de compilação
- Java version incompatível

**Solução:**
- Verifique os logs do build
- Teste localmente: `mvn clean install`
- Certifique-se de que o Java 21 está disponível no Render

### Erro: "Application failed to start"

**Possíveis causas:**
- Variáveis de ambiente faltando
- Porta incorreta
- Erro na configuração do Spring Boot

**Solução:**
- Verifique todos os logs
- Confirme que todas as variáveis de ambiente estão configuradas
- Teste localmente com as mesmas variáveis

### Erro 404 ao acessar endpoints

**Problema:** Endpoints não encontrados

**Solução:**
- Verifique se a aplicação iniciou corretamente
- Confirme que o contexto path está correto
- Teste o health check primeiro: `/actuator/health`

---

## ✅ Checklist Pós-Deploy

- [ ] Health check funcionando (`/actuator/health`)
- [ ] Swagger acessível (`/swagger-ui.html`)
- [ ] Teste de registro de usuário (`POST /auth/register`)
- [ ] Teste de login (`POST /auth/login`)
- [ ] Teste de endpoints protegidos com JWT
- [ ] Logs sem erros críticos
- [ ] URL anotada para atualizar no `IOT/DEPLOY.md`

---

## 🔗 Próximos Passos

1. **Anote a URL da API:** `https://skillbridge-api.onrender.com`
2. **Faça deploy do serviço IoT** (Python)
3. **Configure `IOT_SERVICE_URL`** na API com a URL do IoT
4. **Atualize `IOT/DEPLOY.md`** com as URLs reais
5. **Teste a integração completa**

---

## 📝 Notas

- O Render pode "dormir" serviços gratuitos após 15 minutos de inatividade
- O primeiro acesso após dormir pode demorar ~30 segundos
- Para produção, considere um plano pago para evitar o "sleep"
- Mantenha as credenciais seguras e não commite no Git

---

**Boa sorte com o deploy! 🚀**

