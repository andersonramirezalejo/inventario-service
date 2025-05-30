# Usa una imagen base oficial de OpenJDK para Java 17
FROM openjdk:17-jdk-slim

# Establece el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copia el archivo JAR de tu aplicación al contenedor
COPY target/Inventario-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto en el que se ejecuta tu aplicación Spring Boot (por defecto 8081 para inventario)
EXPOSE 8081

# Comando para ejecutar la aplicación Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://productos-service:5432/inventariodb
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=user
ENV PRODUCTO_SERVICE_URL=http://localhost:8080
ENV PRODUCTO_SERVICE_API_KEY=productos_secreta_12345