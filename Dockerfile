# Use imagem oficial do OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine

# Instalar Maven
RUN apk add --no-cache maven

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Baixar dependências (cache layer)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Expor porta
EXPOSE 8080

# Variável de ambiente para porta dinâmica
ENV PORT=8080

# Comando para iniciar a aplicação
CMD java -jar target/skillbridge-api-0.0.1-SNAPSHOT.jar --server.port=${PORT}

