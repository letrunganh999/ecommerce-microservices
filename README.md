# Ecommerce Microservices System

## Tech Stack
- Java 17
- Spring Boot 2.7.x
- Spring Cloud Netflix (Eureka, Gateway)
- MySQL
- Docker
- Kafka (optional)
- ELK (optional)

## Services
| Service | Port |
|------|------|
| Discovery Service | 8761 |
| API Gateway | 8080 |
| User Service | 8088 |
| Product Service | 8086 |
| Order Service | 8084 |
| Inventory Service | 8082 |

## How to run
1. Start infrastructure (MySQL, Kafka, ELK)
2. Start discovery-service
3. Start api-gateway
4. Start other services

## Notes
- Each service has its own database
- Payment service is optional (future work)
