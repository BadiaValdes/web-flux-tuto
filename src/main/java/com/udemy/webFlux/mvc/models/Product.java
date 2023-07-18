package com.udemy.webFlux.mvc.models;

import com.udemy.webFlux.mvc.core.constant.CollectionNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = CollectionNames.PRODUCT_COLLECTION) // Anotación para definir la clase como un documento de mongo
@NoArgsConstructor // Anotación de lombook para definir un constructor sin argumentos
@AllArgsConstructor // Anotación de lombook para definir un constructor con argumentos
@Data // Anotación de lombook para definir los getters y setters
@Builder // Anotación de lombook para implementar el patrón builder en la clase
public class Product {
    @Id // Anotación de spring boot para identificar un elemento como llave primaria
    private String id;

    private String name;
    private Double price;
    @CreatedDate // Anotación para definir que al campo de abajo se le debe asignar en creación la fecha actual
    private Date createdAt;
}
