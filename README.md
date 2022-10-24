# satbot 💧

Básicamente, mete el input que normalmente harías en un navegador para descargar los CFDIs recibidos en un mes. 

Requiere el certificado y llave privada (e-firma) y la contraseña de la e-firma. Como bien puedes imaginar, esto hay que manejarlo con sumo cuidado.

El método de empleo que pretendo es ~~mediante REST dentro de una misma computadora, que realmente solo ejecutará una parte de otro proceso más largo y complejo~~ como libreria, de manera que se podrá inyectar el ``TalkerService`` para Firefox o Chrome según se necesite y controlar mediante las propiedades en ``Props``/``satbot.properties`` la ubicación donde caerán las facturas para su siguiente empleo. Aún me debato sobre lo que debería responder la llamada a los métodos ``TalkerService#oneByOne`` y ``TalkerService#zip`` para siguientes operaciones. Podría ser el UUID de la operaicón, o la ruta final, o un boolean indicando si se descargó o no algo, o incluso la cantidad de facturas existentes... Hay opciones. 
El bot por sí solo es útil (Viendo que, aparentemente, el WS del SAT no está tan documentado y trabajar con SOAP tiene una buena curva de aprendizaje IMO) y
de allí que lo comporta. Futuras iteraciones recibiran los bytes de los archivos y la contraseña encriptada, cuya llave sería seteada en un ``.properties``,
variable de entorno o ambas, idealmente.
