package com.udemy.webFlux.mvc.models;

import com.udemy.webFlux.mvc.core.constant.CollectionNames;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.Date;
import java.util.List;

@Document(collection = CollectionNames.CATEGORY_COLLECTION) // Anotación para definir la clase como un documento de mongo
@NoArgsConstructor // Anotación de lombook para definir un constructor sin argumentos
@AllArgsConstructor // Anotación de lombook para definir un constructor con argumentos
@Data // Anotación de lombook para definir los getters y setters
@Builder // Anotación de lombook para implementar el patrón builder en la clase
public class Category {
    @Id // Anotación de spring boot para identificar un elemento como llave primaria
    private String _id;

    private String name;
    @CreatedDate // Anotación para definir que al campo de abajo se le debe asignar en creación la fecha actual
    private Date createdAt;
    @LastModifiedDate
    private Date lastModifiedAt;

    /** FK */
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'category':?#{#self._id}}")
    private List<Product> productList;
}
