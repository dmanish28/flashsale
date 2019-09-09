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
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth = true
spring.mail.properties.mail.smtp.starttls.enable = true

API's: com.turvo.flashSaleDemo.controller.FlashController 
Two API's are exposed :
1. To register  : http://localhost:8080/demo/v1/flashsale/{cutomerID}/{flashId}/register
2. To purchase : http://localhost:8080/demo/v1/flashsale/{cutomerID}/{flashId}/purchase



ClassDiagram: com.turvo.flashSaleDemo.model.FSClassDiagram.jpg

ER Diagram: com.turvo.flashSaleDemo.model.ERDiagram.pdf

