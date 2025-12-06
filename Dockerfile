# -------------------------------------------------------------
# STAGE 1: Builder (Extraer capas del JAR)
# -------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app

# Copiamos primero la definición del proyecto para aprovechar caché de Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Descarga dependencias (esto se cacheará si no cambias el pom.xml)
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente y construimos
COPY src ./src
RUN ./mvnw package -DskipTests

# Extraemos las capas del JAR usando Spring Boot Layer Tools
# Ajusta el nombre del jar si es necesario, aquí asumo el estándar
RUN java -Djarmode=layertools -jar target/CITASaludApplication-0.0.1-SNAPSHOT.jar extract

# -------------------------------------------------------------
# STAGE 2: Runtime (Imagen final ligera y segura)
# -------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# SEGURIDAD: Crear usuario 'spring' para no correr como root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos las capas extraídas en el orden correcto
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Puerto de la aplicación
EXPOSE 8080

# Healthcheck interno (seguridad extra por si K8s falla)
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Usamos JarLauncher para iniciar la app optimizada
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]