# Latipe v2 - Microservices Migration

> Migration from [monolith architecture](https://github.com/tdatIT/latipe-web-project) to microservices

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 👨‍💻 Key Contributors

- [Tran Tien Dat](https://github.com/tdatIT)
- [Cozark](https://github.com/longho2002)

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.x
- **Messaging**: RabbitMQ
- **Search**: Elasticsearch
- **Deployment**: Kubernetes (K8s)
- **CI/CD**: GitHub Actions
- **Observability**: OpenTelemetry, Grafana, Prometheus, Tempo, Loki
- **Cache**: Redis
- **Service Discovery**: Eureka

## 🚀 Getting Started

### Prerequisites

- JDK 17 or higher
- Maven
- Docker & Docker Compose

### Build the Services

```bash
mvn clean package -DskipTests
```

### Start Docker and Services

1. Set execute permissions for the installation script:

```bash
chmod +x install.sh
```

2. Start the Docker containers and install services:

```bash
docker-compose -f ./docker/docker-compose.yml -p docker up -d
./install.sh
```

### Stop Services

1. Set execute permissions for the termination script:

```bash
chmod +x terminate.sh
```

2. Stop all services:

```bash
./terminate.sh
```

## ⚙️ Configuration

### Using Without Eureka

If you prefer not to use Eureka for service discovery:

1. Set the `EUREKA_ENABLED` environment variable to `false` in either:
   - `application.yaml` files
   - Docker configuration files

```yaml
EUREKA_ENABLED: false
```

2. Update service URLs in the `application.yaml` file of each service:
   - Locate `service:` section
   - Replace with actual service endpoints

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

- ⭐ Give us a star
- 🐛 Report bugs and issues
- 💬 Participate in discussions
- 🔧 Propose new features
- 📝 Submit pull requests

New to open source? [Learn how to contribute through forking](https://docs.github.com/en/get-started/quickstart/contributing-to-projects)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.