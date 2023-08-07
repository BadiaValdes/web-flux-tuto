 # Conectando Spring Boot a Mongo DB   
   
Primero comencemos instalando las dependencias para poder trabajar con MongoDB. En [start.spring.io](https://start.spring.io/) existen dos dependencias que permiten la conexión a esta base de datos SQL:   
- Spring Data MongoDB → Esta dependencia permite la conexión a la base de datos de mongo DB y brinda funcionalidades ya predefinidas para realizar un CRUD. Básicamente es un ORM que permite el trabajo con la base de datos Mongo.   
- Spring Reactive Data MongoDB → Lo mismo que lo anterior, pero en este caso está diseñado para trabajar con webFlux.   
   
Conociendo esto, solo nos falta ver como realizar la conexión a nuestra base de datos. Para ello nos dirigimos a `aplications.properties` donde definiremos todas las propiedades de nuestra aplicación y añadimos:   
```java
spring.data.mongodb.username=root // Database username
spring.data.mongodb.password=root // Database password
spring.data.mongodb.database=user_db // Collection name
spring.data.mongodb.port=27017 // DB port
spring.data.mongodb.host=localhost // DB ip
```
Y de esta forma ya tenemos implementada la conexión a la base de dato.   
