# satbot 💧

Básicamente, mete el input que normalmente harías en un navegador para descargar los CFDIs recibidos en un mes. 

Requiere el certificado y llave privada (e-firma) y la contraseña de la e-firma. Como bien puedes imaginar, esto hay que manejarlo con sumo cuidado.

El método de empleo que pretendo es mediante REST dentro de una misma computadora, que realmente solo ejecutará una parte de otro proceso más largo y complejo. 
Pero el bot por sí solo es útil (Viendo que, aparentemente, el WS del SAT no está tan documentado y trabajar con SOAP tiene una buena curva de aprendizaje IMO).
De alli que lo comporta. Futuras iteraciones recibiran los bytes de los archivos y la contraseña encriptada, cuya llave sería seteada en el application.properties,
variable de entorno o ambas, idealmente.
