# 📦 Modernizzazione E4-Collab
> Progetto nato dalla volontà di modernizzare un applicativo sviluppato in Thymeleaf ed MVC.

## Tecnologie attuali del progetto:
- Vue JS + Vue Router + Pinia -> Frontend
- Spring boot 3 in Java 21 -> Server Backend e Server Rest

## Diagramma strutturale del progetto
![Diagramma senza titolo drawio](https://github.com/user-attachments/assets/9b7ae4a3-edaf-4f84-9c62-c2282f0e1aa7)

---

## ✅ Prerequisiti

- Docker
- Docker Compose
- Java JDK 21+
- Node.js

---

## Avvio dei container Docker
Possibile eseguire i 4 container eseguedo il Docker Compose all'interno della cartella docker del progetto dove sono presenti i DockerFile.Una volta eseguito il compose troveremo all'interno dell'applicazione Docker il container principale al cui interno troviamo i 4 container che compongono il progetto.

** N.B. Il container mysql al suo spegnimento potrebbe mostrare una perdita di dati, se sono stati inseriti sul database dati importanti eseguite un backup della cartella docker/static del progetto che contiene un bind con il volume di mysql **

## Porte di riferimento dei container:
- mysql: 3306
- backend: 8081
- rest: 8090
- frontend: 4000


## Dipendenze dei vari progetti:
### Frontend:
- @aurium/vue-plotly: "^2.0.0-rc5",
- @fortawesome/fontawesome-free: "^6.7.2",
    "axios": "^1.10.0",
    "bootstrap": "^5.3.7",
    "bootstrap-icons": "^1.13.1",
    "chart.js": "^4.5.0",
    "crypto-js": "^4.2.0",
    "d3": "^7.9.0",
    "datatables": "^1.10.18",
    "datatables.net-dt": "^2.3.2",
    "datatables.net-vue3": "^3.0.4",
    "jquery": "^3.7.1",
    "pinia": "^3.0.1",
    "startbootstrap-sb-admin-2": "^4.1.4",
    "svg-path-properties": "^1.3.0",
    "vanillajs-datepicker": "^1.3.4",
    "vue": "^3.5.13",
    "vue-data-ui": "^2.12.6",
    "vue-router": "^4.5.0"



# 📚 Autore

**Matteo Passaro**

[PROGETTO ORIGINALE](https://github.com/collab-uniba/E4-Collab)

*Esame Evoluzione del Software – Università degli Studi di Bari (2025)*
