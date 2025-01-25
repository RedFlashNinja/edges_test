### edges-test
./mvnw clean compile

./mvnw clean install -T2C

### Docker-compose
Running services will create instances:
- postgre db
- edge app
- prometheus
- grafana

### Edge-app
- actuator http://localhost:9080/actuator
- metrics http://localhost:9080/actuator/metrics
- prometheus http://localhost:9080/actuator/prometheus

### API Docs
- http://localhost:9080/api-docs
- http://localhost:9080/swagger-ui/index.html

### Prometheus
- http://localhost:9090/query

### Grafana
- http://localhost:10000
- use prometheus urls for data resources - http://host.docker.internal:9090

### Postman Collection
- [Postman-Collection](./src/main/resources/Edge.postman_collection.json)