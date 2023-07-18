# Proyecto de estudio de WebFlux

Este proyecto surge como parte del tutorial de trabajo con programación reactiva en spring boot. El tutorial lo pueden encontrar en Udemy y al final del readme se les dejará el link hacia el mismo.

La idea básica es crear un proyecto para probar las diferentes formas de trabajar con WebFlux. Comenzaremos craendo un proyecto clásico MVC para ver como el mismo spring boot mediante thymeleaf manejan las llamadas asíncronas a la base de datos. Posteriormente, se añadirá una api para manejar los endpoints de nuestro sistema.

---

## Librerías y Herramientas

A continuación estaremos listando las librerías y tecnologías que estaremos utilizando en el proyecto. Además, se valido decir que el cascarón para el mismo fue elaborado gracias al sitio web start.spring; al final del readme le dejaremos el link.

Tecnologías:
- Spring Boot: 3.1.1
- JDK 17
- Visual Studio Code 1.77

Librerías:
- Spring Reactive Web
- Spring Data Reactive MongoDB
- Lombok
- Thymeleaf
- Spring Boot Dev Tools

Plugins:
- husky -> Para crear hooks de git

---

## Prerequisitos para probar el proyecto

- Tener instalado MongoDB en la PC o mediante Docker
- Los datos de conexión pueden ser cambiados en el archivo applications.properties dentro de src.

---

## Comandos para probar el proyecto

> Instalar dependencias
> - mvn clean install --DskipTests

> Ejecutar la aplicación
> - mvn spring-boot:run

---

## Como usar el archivo docker

> docker compose up

El archivo de docker está dividido en 3 stage. 
El primero (builder) se encarga de isntalar las dependencias necesarias del proyecto.
El segundo (develop) es un stage intermedio para los desarrolladores que no le gusten instalar dependencias directamentes en la PC y mediante el plugin Remote Explorer de Visual Studio Code se conecten al contenedor. Cualquier cambio en el contenedor se verá reflejado en el de la aplicación.
Por último, pero no menos importante el production; encargado de preparar la aplicación para trabajar en entorno de producción.

---

## Sitios de interés

- https://start.spring.io/ -> Para crear el cascarón del proyecto