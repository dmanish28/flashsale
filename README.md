# Flashsale

### Pre-requisites:

* A running postgresql instance.
* A running redis instance.

### Configuration:

Java properties file in src/main/resources/application.properties.

* spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
* spring.datasource.username= postgres
* spring.datasource.password=qazxsw
* spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
* spring.jpa.hibernate.ddl-auto = update

* spring.redis.host=localhost
* spring.redis.password=password
* spring.redis.port=6379

* spring.mail.protocol=smtp
* spring.mail.host=smtp.gmail.com
* spring.mail.port=587
* spring.mail.username=
* spring.mail.password=
* spring.mail.properties.mail.smtp.auth = true
* spring.mail.properties.mail.smtp.starttls.enable = true

### API's: com.turvo.flashSaleDemo.controller.FlashController 
* Two API's are exposed :
  1. To register  POST : http://localhost:8080/demo/v1/flashsale/{cutomerID}/{flashId}/register
  2. To purchase POST : http://localhost:8080/demo/v1/flashsale/{cutomerID}/{flashId}/purchase


### ClassDiagram:
![FSClassDiagram](https://user-images.githubusercontent.com/54669278/65026103-7fbc3880-d955-11e9-92f7-a7536d85bec6.jpg)
### ER Diagram: 
![ER](https://user-images.githubusercontent.com/54669278/65026765-cfe7ca80-d956-11e9-97af-41ba18a936a9.png)

### Business Logic: 

* A flashsale is created with an existing product and the registration is opened.
* Customers are notified in emails about the flash sale
* Customers register for the given flash sale
* Operator starts the flashsale and new regitration for the given flash sale is stopped.Product stock and customer registration info are cached during this operation.
* Customer can purchase an order based on the following conditions:
    - If stock of flashsale product > 0
    - If customer is already registered.
    - if the customer hasn't already purchased the product.

* Reduced stock number is stored back in cache and the customer registration info is removed from cache as one customer is allowed to purchase only once.
* Persist order information in database

