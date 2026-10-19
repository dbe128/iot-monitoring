# IoT Monitoring System

We are building an **IoT monitoring system** to collect, process, and analyze device data.  
The project has already been started, and we are looking for it to be completed.

---

## 📋 Project Tasks

A detailed project description and the remaining tasks can be found in the [TASKS.md](./TASKS.md) file located in the root directory.

---

## 🚀 Assignment Instructions

1. Clone this repository
2. Review the [TASKS.md](./TASKS.md) file.
3. Complete the remaining implementation tasks.
4. Ensure the project is working end-to-end.
5. Upload the finished project into your own **GitHub repository**.
5. Share the repository link with us.

---

## ⏰ Timeline

Please complete the assignment **within one week**.

---

## Running

```bash
docker compose up -d      # PostgreSQL + Kafka
./mvnw spring-boot:run    # http://localhost:8080
./mvnw test               # needs Docker (Testcontainers)
```

Kafka topics: `sensor-reading` and `sensor-alerts` (in), `alerts` (out), JSON payloads.

Notes:
- `AlertService.saveAlert` is not transactional on purpose: the alert is committed before it is published to Kafka.
- Testcontainers is bumped to 1.21.4, as 1.21.3 does not work with Docker Engine 29+.

