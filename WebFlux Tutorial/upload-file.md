 # Upload File   
   
En este apartado hablaremos de como subir archivos haciendo uso de las facilidades de webFlux. Comencemos diciendo que para subir un archivo utilizando un acercamiento no reactivo, debemos utilizar um multipart/form-data; en webFlux este acercamiento no es distinto, pero en vez de utilizar el tipo de dato Multipart, como haríamos en Spring Boot, estaremos utilizando otro tipo de dato.   
Iniciemos la explicación del código viendo las configuraciones que necesitamos añadir para especificar algunas restricciones para la subida de archivos.   
``` YAML
servlet:
  multipart:
     enabled: true
     max-file-size: 5MB
     max-request-size: 5MB
upload:
  dir: ./src/main/resources/upload

```
Como se puede observar, cambiamos la forma de trabajar nuestro archivo `**application.propperties**`.  Ahora, pasa a llamarse `application.yml` y nos permite utilizar las bondades de los archivos en formato YAML; y como un extra, muy bien recibido por parte del equipo, una mejor organización de las propiedades.    
En este caso estamos agregando las configuraciones correspondientes a `servlet` y `upload`; esta última siendo definida por los propios desarrolladores y no es parte del ecosistema de spring boot. De la primera, configuramos directamente la opción de multipart que es la encargada de almacenar los valores de configuración de la subida de archivos:   
- `enabled`: nos permite habilitar o deshabilitar la capacidad de subir archivos a nuestra aplicación.   
- `max-file-size`: define el tamaño máximo que deben tener los archivos a subir.   
- `max-request-size`: define el tamaño máximo de la petición que viaja por `multipar/form-data`.   
   
Si hubiésemos mantenido la estructura antigua del archivo application.propperties, la anterior configuración se observaría de la siguiente forma:   
``` java
servlet.multipart.enabled=true
servlet.multipart.max-file-size=5MB
servlet.multipart.max-request-size=5MB
upload.dir=./src/main/resources/upload

```
Como se mencionó anteriormente, la capacidad de poseer una agrupación de las configuraciones y escribir un documento más legible; se decidió quedarse con una configuración de spring boot en formato YAML.   
Habiendo terminado el apartado de configuración, podemos dar el salto al formulario encargado de subir la imagen y posteriormente, al controlador encargado de manejar la operación de crear el producto:   
``` html
<form action="#" method="post" th:object="${product}" th:action="@{/product/create}" enctype="multipart/form-data">
      <div class="mb-3">
        <label for="nombre" class="form-label">Nombre</label>
        <input type="text" class="form-control" th:field="*{name}" id="nombre" placeholder="name@example.com">
      </div>
      <div class="mb-3">
        <label for="price" class="form-label">Precio</label>
        <input type="text" class="form-control" th:field="*{price}"  id="price" placeholder="name@example.com">
      </div>
      <select class="form-select" th:field="*{categoryId}" aria-label="Default select example">
        <option th:each="cate: ${categories}" th:text="${cate.name}" th:value="${cate._id}"></option>
      </select>
      <div class="mb-3">
        <label for="image" class="form-label">Seleccionar imagen</label>
        <input class="form-control" type="file" id="image" name="image">
      </div>
      <div class="btn-group form-buttons" role="group" aria-label="Basic outlined example">
          <button type="submit" class="btn btn-outline-primary"><i class="bi bi-check"></i> Aceptar</button>
          <a th:href="@{/product}" type="button" class="btn btn-outline-danger"><i class="bi bi-x"></i> Cancelar</a>
        </div>
    </form>
```
Esto no dista mucho del formulario que vimos en [Trabajando con Thymeleaf](trabajando-con-thymeleaf.md), los cambios más evidentes son el input de cargar imagen añadido y el enctype del formulario. El primero se explica por si solo, es el básico ejemplo de subida de archivos que brinda el *Framework* **Boostrap**. Lo segundo nos permite definir el tipo de petición que manejará el formulario; por defecto es **application/json**. Para poder trabajar con imágenes, debemos definirlo como `multipart/form-data` y de esta forma, el mismo spring boot se encargará de armar los datos a enviar hacia el controlador.   
Hablando de controladores, veamos ahora que debemos cambiar en nuestro código para manejar el archivo enviado desde el front:   
``` java
    @PostMapping("/create")
    public String createProduct(@ModelAttribute("product") ProductDTO productDTO, @RequestPart("image") FilePart image) {
        Product newProduct = ProductConverter.productDtoToProduct(productDTO); 
        productService.createProduct(newProduct, image).subscribe(data -> log.info("Data saved")); 
		return LayoutNames.REDIRECT_PRODUCT;
    }
```
Aparte del ya conocido @ModelAttribute("product") para obtener el objeto utilizado en el formulario mediante `th:object`; tenemos `@RequestPart("image")` que permite capturar el valor de un campo del formulario en específico. Este nuevo decorador (estereotipo) nos permite definir que campo capturar mediante un parámetro. Para obtener el campo en específico del formulario debemos apoyarnos en el valor del atributo name del `input`; en este caso estamos utilizando el valor **image**.   
Para poder guardar la imagen en el proyecto, tuvimos que modificar el servicio de crear producto para aceptar un parámetro extra. Importante resaltar que este parámetro es de tipo `FilePart`, de la misma forma que lo es en los parámetros del controlador.    
``` java
@Override
    public Mono<Product> createProduct(Product product, FilePart file) {
        product.setImage(file.filename());
           return productRepository
                            .save(product)
                .flatMap(data -> file
                        .transferTo(root.resolve(file.filename()))
                        .thenReturn(data)
                );
    }
```
Los cambios aquí son sencillos. Primero, antes de comenzar el proceso de guardado, añadimos al producto el nombre de la imagen mediante `product.setImage(file.filename())`. Posteriormente comenzamos el proceso de salvado; pero, para el guardado de archivo en la computadora (`.transferTo`) retorna un dato `Mono` vacío. Debido a esto, es necesario encadenar el este proceso con el de salvado y esto lo hacemos mediante un `flatMap`. Para mantener el mismo tipo de dato a devolver por la función de crear producto, hacemos uso del `.thenReturn(data)` que permitirá después de guardar en la computadora, devolver el dato almacenado.   
De esta forma podemos subir archivos a sprinboot con webFlux; pero no hemos visto como mostrarlo. Para ello se creó en el mismo controlador un método que se encargue de buscar el archivo y enviarlo hacia el cliente:   
``` java
@GetMapping("/uploads/{name:.+}")
    public Mono<ResponseEntity<Resource>> getImage(@PathVariable("name") String name) throws MalformedURLException {
        Path imageRoute = Paths.get("./src/main/resources/upload").resolve(name).toAbsolutePath();
        Resource image = new UrlResource(imageRoute.toUri());
        return Mono.just(ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + image.getFilename() + "\" ")
                .body(image));
    }
```
El tipo de dato a devolver por este método es `ResponseEntity<Resource>`   
- `Resource` debido a que no devolveremos un objeto sino un recurso cargado desde la propia computadora.   
- `ResponseEntity` este tipo de dato es brindado por spring boot y nos permite controlar cada aspecto que da respuesta a enviar al frontend. En este caso hizo falta debido a que debemos modificar el header que utilizaremos para enviar la imagen.   
   
Por su parte, el front utiliza la siguiente línea de código para hacer la petición al método anterior:   
``` html
 <img th:src="@{/product/uploads/{name}(name=${product.image})}" width="200" height="200">
```
