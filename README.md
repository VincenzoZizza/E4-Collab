# 📦 E4-Collab – Dockerized Spring Boot (Backend & Frontend) + MySQL

> Guida passo-passo per clonare, configurare e far partire il progetto con Docker Compose.

---

## ✅ Prerequisiti

- [Git](https://git-scm.com/)  
- [Docker](https://www.docker.com/)  
- [Docker Compose](https://docs.docker.com/compose/)

---

## 🧾 Clonazione del repository
Apri il terminale e clona il progetto:

```bash
git clone https://github.com/matteopassaro/Evoluzione_Software/
cd Evoluzione_Software/docker
```

# 🚀 Avvia il progetto
Lancia l'app con Docker Compose:

```bash
docker-compose up --build
```

Questo comando:

⚒️ Costruisce le immagini Docker per backend e frontend

⚒️ Avvia i container:
- mysql
- backend (porta 8081)
- frontend (porta 8080)

# 🧼 Spegnere e pulire 
```bash
docker-compose down -v
```

# 🔍 Spiegazione Backend Dockerfile
**💡Stage 1 (builder)**

```FROM gradle:8.0-jdk17```: immagine con Gradle + JDK17

```COPY . .```: copia sorgenti e wrapper

```chmod +x ./gradlew```: abilita esecuzione

```./gradlew clean bootJar -x test```: compila e genera JAR

**💡Stage 2 (runtime)**

```FROM eclipse-temurin:17-jre-jammy```: immagine JRE17 leggera

```COPY --from=builder ...```: prende il JAR preparato

```ARG/ENV SPRING_PROFILES_ACTIVE```: profili Spring (backend,mysql)

```EXPOSE 8081```: espone la porta

```ENTRYPOINT```: comando di avvio

# 🔍 Spiegazione Frontend Dockerfile
Identico al backend, ma:

Usa ```SPRING_PROFILES_ACTIVE=frontend,mysql```

Espone la porta 8080 per il rendering Thymeleaf/static

# 🧩 Cosa fa docker-compose.yml

```mysql```: avvia MySQL con healthcheck

```backend```: costruisce il servizio API spring su porta 8081, aspetta MySQL

```frontend```: costruisce il servizio MVC spring su porta 8080, aspetta backend & MySQL

```volumes```: persistono i dati di backend e MySQL

```networks```: tutti i container comunicano su prog_evoluzione





# 📚 Autore

**Matteo Passaro**

*Esame Evoluzione del Software – Università degli Studi di Bari (2025)*
