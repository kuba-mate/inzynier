# Użycie obrazu Amazon Corretto 17 (Java 17)
FROM amazoncorretto:17

# Ustawienie katalogu roboczego
WORKDIR /app

# Kopiowanie pliku JAR do kontenera
COPY target/inzynier-0.0.6-SNAPSHOT.jar app.jar

# Expose port aplikacji (Spring Boot domyślnie używa portu 8080)
EXPOSE 8080

# Uruchomienie aplikacji
ENTRYPOINT ["java", "-jar", "app.jar"]
