# flashsale

Pre-requisites:

A running postgresql instance.
A running redis instance.

Configuration:

Java properties file in src/main/resources/application.properties.

spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username= postgres
spring.datasource.password=qazxsw
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto = update

spring.redis.host=localhost
spring.redis.password=password
spring.redis.port=6379

spring.mail.protocol=smtp
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=manishwin4@gmail.com
spring.mail.password=qwerty4$
spring.mail.properties.mail.smtp.auth = true
spring.mail.
spring.mail.properties.mail.smtp.starttls.enable = true

API's:




ClassDiagram:

ER Diagram:

