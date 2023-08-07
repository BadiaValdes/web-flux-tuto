 # Trabajando con Thymeleaf   
   
Iniciemos este apartado hablando un poco de Thymeleaf:   
  Thymeleaf surge como motor de plantillas para el renderizado de páginas HTML del lado del servidor. Nos permite embeber código Java dentro de archivos HTML y posteriormente mostrarle los datos correspondientes al usuario.   
Para este proyecto decidimos utilizar este motor de plantillas para mostrar datos al usuario. Esta decision esta respaldada en el hecho que el proyecto que realizamos está basado en programación reactiva y este motor de plantillas se integra perfectamente con esta forma de trabajo. Comencemos mostrando la estructura de carpetas que estamos utilizando para las plantillas:   
![image.png](files\image_o.png)    
Hablemos un poco de la estructura. Spring boot espera que dentro de la carpeta **resources **exista una carpeta llamada **templates **donde se encontrarán todos los archivo HTML de la aplicación. Dentro, iremos declarando nuestros archivos HTML. En nuestro caso seguimos la siguiente estructura:   
- **Layout.html → **Será el archivo principal de nuestra plantilla. Aquí declararemos las importaciones de CSS y JS y el diseño (layout) de nuestra intefaz de usuario.   
- **Part** → Para mantener un layout.html limpio y sin carga de elementos, estaremos dividiendo los diferentes componentes que componen nuestro maquetado en archivos aparte. Esto es posible gracias a la propiedad `**th:repleace**` de thymeleaf. Esta nos permite sustituir un código de un archivo HTML trayendolo de otro archivo con la misma extensión.   
-  **Pages** → Esta carpeta nos permitirá ordenar los diferentes archivos HTML que pertenecen a cada una de las vistas de nuestro proyecto.   
- **Error **→ Almacena las páginas HTML de los errores que serán manejados en la aplicación. De esta forma, le estaremos mostrando una interfaz agradable al usuario cuando no encuentre un elemento (404).   
   
Comencemos mostrando como logramos crear la estructura del html **Layout**:   

## Uso de archivos Static   

```html
 <link th:href="@{/css/page.css}" rel="stylesheet" type="text/css">
```
Al igual que las plantillas deben estar en una carpeta llamada template, los archivos **css **y **js **deben estar contenidos dentro de **static**. Para integrarlos con nuestros html, debemos utilizar el código anterior, que a primera vista parece la importación de un archivo CSS. Y es verdad, no cambia mucho con respecto a la que conocemos en HTML puro; pero en este caso sustituimos `href` por `th:href`.    
La diferencia entre los dos está que el primero espera una dirección `URL` o `URI` completa; mientras que el segundo espera un código propio de la plantilla que apunte hacia una dirección interna.    
El que hace esto posible es el selector `@` en thymeleaf. Este hace referencia a una dirección URL dentro de la misma aplicación. Más adelante veremos su uso dentro de la etiqueta `<a></a>`.   

## Remplazando componentes internos   

```html
<div th:replace="part/nav::nav"></div>
<div class="container_body">
    <div th:replace="${page}::content"> </div>
</div>
<footer class="footer" th:replace="part/footer::footer"></footer>

```
Este es el código que hace funcionar nuestra plantilla. Aquí vemos la división de la misma en 3 partes:   
- Menú de navegación.   
- Contenido.   
- Pie de página.   
   
Gracias al uso de `th:replace` como mencionamos anteriormente, podemos sustituir las etiquetas div por etiquetas en otros archivos html. Pongamos de ejemplo el footer que es el más corto. En el código anterior estamos llamando a la siguiente plantilla "`part/footer`" y posteriormente estamos utilizando el selector `::footer`; esto quiere decir que dentro de ese archivo `HTML` buscaremos un fragmento de código llamado `footer`:   
```html
<div class="footer" th:fragment="footer">
    Pagina web de prueba para curso de WebFlux - Udemy 2023
</div>
```
El selector de `th:repleace` apunta al nombre declarado dentro de `th:fragment="footer"`. Y de esta forma podemos organizar mejor nuestro código HTML.   
Ahora, cuando hablamos de remplazar la parte del contenido, se enreda un poco el tema. Por qué? Bueno, primero es la forma de redirigir del backend hacia el archivo HTML; si se retorna el nombre de un archivo HTML que no sea Layout, perderemos la estructura del sitio. Segundo, no queremos repetir HTML por gusto, la idea es reutilizar todo lo que se pueda. La solución?   
Utilizaremos las propias variables que pasemos del back al front:   
```html
<div th:replace="${page}::content"> </div>
```
En thymeleaf las podemos acceder a las variables que enviemos desde al back con el selector `$` y dentro su nombre. Optamos por nombrar esta variable como page y en el backend hacemos lo siguiente:   
```java
model.addAttribute("page", "pages/product/list");

```
Al objeto model le añadimos el atributo `page` y como valor la ruta dentro de `template` al html que se debe renderizar. Ahora veamos como esta compuesto se `HTML`:   
```html
<div th:fragment="content">
...
</div>
```
Dentro de todos los HTML correspondientes a las diferentes vistas se agregó un fragmento llamado `content`. Este fragmento será el seleccionado en el layout `::content` para remplazar el `div`. De esta forma estamos simulando una estructura jerárquica de plantillas donde el nodo raíz es el layout y ella posee una serie de hijos.   
> Formularios   

Los formularios son un aspecto fundamental en toda aplicación web y a continuación mostraremos como thymeleaf los maneja. Comencemos por el controller que desencadena el acceso al formulario:   
```java
@GetMapping("/create")
public String createProduct(Model model) {
    model.addAttribute("product", new ProductDTO());
    model.addAttribute("page", "pages/product/create");
    return LayoutNames.LAYOUT_NAME;
}

```
El primer dato que le pasamos al objeto model es un `ProductDTO` vacío. De esta forma, en el front podremos decirle al formulario que objeto debe utilizar pero eso lo veremos en un momento. Como segundo valor (explicado anteriormente) es la página del formulario. Ahora veamos el `HTML`:   
```html
 <form action="#" method="post" th:object="${product}" th:action="@{/product/create}">
      <div class="mb-3">
        <label for="nombre" class="form-label">Nombre</label>
        <input type="text" class="form-control" th:field="*{name}" id="nombre" placeholder="name@example.com">
      </div>
      <div class="mb-3">
        <label for="price" class="form-label">Precio</label>
        <input type="text" class="form-control" th:field="*{price}"  id="price" placeholder="name@example.com">
      </div>
        <div class="btn-group form-buttons" role="group" aria-label="Basic outlined example">
          <button type="submit" class="btn btn-outline-primary"><i class="bi bi-check"></i> Aceptar</button>
          <a th:href="@{/product}" type="button" class="btn btn-outline-danger"><i class="bi bi-x"></i> Cancelar</a>
        </div>
  </form>
```
Vamos directo al grano ya que un formulario es básico en HTML.   
- `th:action` → Definimos la dirección `URL` del controller del backend que se encargará de usar los datos del formulario. `Th` nos permite acceder a los operadores de thymeleaf; en este caso el encargado de acceder a las URL internas de la aplicación `@{/product/create}`.   
- `th:object`  → Este es muy importante para los formularios ya que nos permite definir la el objeto que estaremos utilizando como base para el formulario. De esta forma, una vez que subamos el formulario, thymeleaf podrá crear un objeto o mejor dicho, podrá completar los datos del objeto que le pasamos con anterioridad.   
- `th:field` → Hace referencia a un atributo del objeto pasado. De esta forma podemos decirle a cada input que propiedad del objeto le corresponde, haciendo la construcción del mismo mucho más sencilla.   
   
## Template engine   

- https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html    
   
   
