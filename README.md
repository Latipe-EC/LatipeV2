# Latipe Version 2 (Migration from [monolith](https://github.com/tdatIT/latipe-web-project) to microservices)

## Key members

[Tran Tien Dat](https://github.com/tdatIT)

[Cozark](https://github.com/longho2002)

## Tentative technologies and frameworks

- Java 17
- Spring boot 3
- ReactJS
- Keycloak
- RabbitMQ
- Elasticsearch
- K8s
- GitHub Actions
- OpenTelemetry
- Grafana, Loki, Prometheus, Tempo, Redis

## Run this project

### Build this service:

`mvn clean package`

## Start docker and services:

#### Also, make sure that the `install.sh` file has execute permissions. You can set the execute permission using the `chmod` command:

`chmod +x install.sh`

```bash
docker-compose -f ./docker/docker-compose.yml -p docker up -d

./install.sh
```

## Stop services:

#### Also, make sure that the `terminate.sh` file has execute permissions. You can set the execute permission using the `chmod` command:

`chmod +x terminate.sh`

```bash
./terminate.sh
```

## Contributing

- Give us a star
- Reporting a bug
- Participate discussions
- Propose new features
- Submit pull requests. If you are new to GitHub, consider
  to [learn how to contribute to a project through forking](https://docs.github.com/en/get-started/quickstart/contributing-to-projects)
