 # Upload File Api   
   
En [Upload File](upload-file.md) vimos como subir archivos utilizando la arquitectura MVC. Pero cuando estamos trabajando con APIs, la forma de subir archivos difiere un poco. A continuación mostraremos la forma que tenemos para hacer esto en el proyecto.    
Comencemos con los códigos a utilizar.:   
``` java
// Router
.POST(RequestPredicates.contentType(MediaType.MULTIPART_FORM_DATA), productApiHandler::createProduct))

```
``` java
// Handler
public Mono<ServerResponse> createProduct(ServerRequest req){
        return req.body(BodyExtractors.toMultipartData()).flatMap(
                data -> {
                    Map<String, Part> map = data.toSingleValueMap();
                    Part filePart = map.get("file");

                    FormFieldPart name = (FormFieldPart) data.getFirst("name");
                    FormFieldPart price = (FormFieldPart) data.getFirst("price");
                    FormFieldPart categoryId = (FormFieldPart) data.getFirst("categoryId");
                    FormFieldPart test = (FormFieldPart) data.getFirst("test");

                    Product p = Product
                            .builder()
                            .name(name.value())
                            .price(Double.parseDouble(price.value()))
                            .categoryId(categoryId.value())
                            .build();
                    return ServerResponse.ok().body(productService.createProduct(p,(FilePart)filePart), ProductDTO.class);
                }
        );
    }
```
El primer fragmento de código pertenece al manejador de rutas funcionales. Para saber más sobre esto vea [Creando API](creando-api.md). En esta función de rutas utilizamos `RequestPredicates` para definir que el servidor esta esperando una petición que contenga en el cuerpo un multipart/form-data. Posteriormente le pasamos el handler o la función que se encargará de manejar los datos recibidos desde el front.   
El segundo código representa dicha función. Todas las funciones que sean llamadas mediante las rutas funcionales deben recibir como parámetro un `ServerRequest`. Mediante este podemos acceder al cuerpo de la petición y extraer el **multipart form** que fue enviado desde el front. Para extraer estos datos utilizamos el siguinete fragmento de código:   
```java
req.body(BodyExtractors.toMultipartData())
```
- req.body: contiene todos los datos del cuerpo de la petición   
- BodyExtractors.toMultipartData(): los `bodyExtractors` nos permite extraer información en específico del cuerpo de una petición. En este caso le pedimos que extraiga los datos del multipart-form.   
   
Posteriormente nos encargamos de extraer los datos que necesitamos del multipar y se lo pasamos al servicio que se encargará de almacenarlo en base de datos. Tengan en cuenta que en todo momento estamos trabajando con funciones reactivas.   
   
