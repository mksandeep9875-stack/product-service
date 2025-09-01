# Product Service

**Port:** 8094  

---

## Overview
`product-service` manages product-related operations for the Ecommerce platform.  
It allows creating, updating, retrieving, and deleting products.

As of now the products along with inventory can only be managed by vendor type profiles.

The product service will communicate with the customer service for client evaluation using reactive webflux interaction using spring cloud webflux dependency.

---

## Endpoints

| Endpoint                   | Method | Description                        |
|-----------------------------|--------|------------------------------------|
| /product/v1/create          | POST   | Create a new product               |
| /product/v1/update/{id}     | POST   | Update details of an existing product |
| /product/v1/active          | GET    | Retrieve all active products       |
| /product/v1/delete/{id}     | DELETE | Delete a product by ID             |

---
## Configuration

Configuration file: src/main/resources/application*.properties or application.yml.

The application properties will be taken from the profile from https://github.com/mksandeep9875-stack/config-server-properties.git using spring cloud config server

---
## Dependencies

-Spring Boot Starter Web

-Spring Boot Starter Actuator

-Spring Boot Starter MongoDB (depending on your database)

-Spring Cloud Config Client

-Eureka Client

-Spring cloud Webflux


---

## How to Run

```bash
git clone <your-repo-url>
cd customer-service
mvn clean install
mvn spring-boot:run
