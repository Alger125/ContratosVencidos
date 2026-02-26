# fidcven — Proceso Batch de Contratos Vencidos

## Tabla de Contenidos

1. [¿Qué hace este proyecto?](#1-qué-hace-este-proyecto)
2. [Conceptos clave que debes conocer antes de leer el código](#2-conceptos-clave-que-debes-conocer-antes-de-leer-el-código)
3. [Arquitectura y flujo del proceso](#3-arquitectura-y-flujo-del-proceso)
4. [Descripción detallada de cada clase](#4-descripción-detallada-de-cada-clase)
5. [Las consultas SQL explicadas](#5-las-consultas-sql-explicadas)
6. [Formato del archivo de salida](#6-formato-del-archivo-de-salida)
7. [Requisitos y dependencias](#7-requisitos-y-dependencias)
8. [Configuración y despliegue](#8-configuración-y-despliegue)
9. [Estructura del proyecto](#9-estructura-del-proyecto)
10. [Notas técnicas y advertencias](#10-notas-técnicas-y-advertencias)
11. [Preguntas frecuentes para un desarrollador nuevo](#11-preguntas-frecuentes-para-un-desarrollador-nuevo)

---

## 1. ¿Qué hace este proyecto?

### En palabras simples

Imagina que un banco tiene miles de contratos fiduciarios (fideicomisos). Algunos de esos contratos tienen una fecha de vencimiento, y cuando esa fecha ya pasó, el contrato se considera **vencido**.

Este programa es un **proceso batch** (es decir, un proceso automático que corre en segundo plano sin intervención humana, normalmente de noche o en horarios programados) que hace lo siguiente:

1. **Consulta la base de datos** buscando todos los contratos que ya vencieron.
2. **Obtiene los correos electrónicos** de las personas relacionadas a esos contratos (beneficiarios y fideicomitentes).
3. **Genera un archivo de texto** con esa información, con un formato muy específico que otro sistema del banco usará para enviar notificaciones por correo.

### ¿Qué es un fideicomiso?

Un fideicomiso es un contrato legal donde una persona (el **fideicomitente**) entrega bienes o dinero a una institución financiera (el **fiduciario**, en este caso Banco azul) para que los administre en beneficio de otra persona (el **fideicomisario** o beneficiario). Tiene una fecha de inicio y generalmente una fecha de vencimiento.

### ¿Qué es un proceso batch?

Un proceso batch es un programa que:
- **No tiene interfaz gráfica** (no hay pantallas ni botones).
- **Se ejecuta de forma programada** (por ejemplo, todos los días a las 2 AM).
- **Procesa grandes volúmenes de datos** de forma automática.
- **Comienza y termina solo**, reportando si tuvo éxito o error mediante un código de salida (`System.exit(0)` = éxito, `System.exit(1)` = error).

---

## 2. Conceptos clave que debes conocer antes de leer el código

### Spring Framework

Este proyecto usa **Spring**, que es un framework (conjunto de herramientas) muy popular en Java para construir aplicaciones empresariales. Las anotaciones más importantes que verás son:

| Anotación | ¿Qué significa? |
|---|---|
| `@Service` | Le dice a Spring que esta clase es un servicio de negocio |
| `@Repository` | Le dice a Spring que esta clase accede a la base de datos |
| `@Autowired` | Spring inyecta automáticamente una instancia de la clase indicada (no hay que hacer `new`) |
| `@Value("${propiedad}")` | Inyecta el valor de una propiedad del archivo `.properties` |

### Patrón DAO (Data Access Object)

El proyecto usa el patrón DAO, que sirve para **separar la lógica de negocio del acceso a la base de datos**. Funciona así:

```
Servicio (lógica de negocio)
    │
    └──► DAO (solo habla con la base de datos)
              │
              └──► Base de datos DB2
```

Esto es bueno porque si mañana cambia la base de datos, solo hay que modificar el DAO, no toda la aplicación.

### Patrón Interfaz + Implementación

En Java empresarial es muy común definir primero una **interfaz** (qué métodos existen) y luego una **implementación** (cómo funcionan). Por ejemplo:

- `ContratosDAO.java` → Interfaz: dice "existirá un método `getCtoFideicomisario()`"
- `ContratosDAOImpl.java` → Implementación: dice cómo funciona ese método

### DTOs (Data Transfer Objects)

Los **DTOs** son clases simples que solo sirven para transportar datos de un lugar a otro. No tienen lógica de negocio, solo atributos con sus getters y setters. En este proyecto:

- `FideicomisarioDTO` → Transporta datos de un beneficiario (correo, fecha de vencimiento, tipo de negocio).
- `FideicomitenteDTO` → Transporta datos de un fideicomitente (correo, fecha de vencimiento, tipo de negocio).

### IBM DB2

La base de datos que usa este proyecto es **IBM DB2**, que es la base de datos que Banco Azul utiliza en sus mainframes. La sintaxis SQL es muy similar a otros motores (MySQL, PostgreSQL), pero con algunas particularidades como `WITH UR` (Uncommitted Read, para leer datos sin esperar bloqueos).

---

## 3. Arquitectura y flujo del proceso

### Diagrama del flujo completo

```
╔══════════════════════════════════════════════════════════════╗
║                        INICIO                                ║
║                      App.java                                ║
║          (main method - punto de entrada)                    ║
╚══════════════════════════════╦═══════════════════════════════╝
                               │
                               ▼
╔══════════════════════════════════════════════════════════════╗
║            CatalogoContratosServiceImpl                      ║
║                  método: inicio()                            ║
║   Orquesta el proceso y atrapa errores generales             ║
╚══════════════════════════════╦═══════════════════════════════╝
                               │
                               ▼
╔══════════════════════════════════════════════════════════════╗
║               ContratosServiceImpl                           ║
║              método: createFile(rutaBase)                    ║
║   Crea el archivo de salida y coordina las llamadas al DAO   ║
╚══════════════════╦═══════════════════════╦═══════════════════╝
                   │                       │
                   ▼                       ▼
╔══════════════════════╗     ╔══════════════════════════════╗
║  getCtoFideicomisario║     ║    getCtoFideicomitente      ║
║  Beneficiarios con   ║     ║    Fideicomitentes con       ║
║  correo válido de    ║     ║    correo válido de          ║
║  contratos vencidos  ║     ║    contratos vencidos        ║
╚══════════╦═══════════╝     ╚═══════════════╦══════════════╝
           │                                 │
           └─────────────┬───────────────────┘
                         ▼
            ╔═════════════════════╗
            ║  validarCorreo()    ║
            ║  Regex de email     ║
            ╚══════════╦══════════╝
                       │
                       ▼
       ╔═══════════════════════════════╗
       ║  MMFID_D01_YYYYMMDD_          ║
       ║  GCVE_ECONTVENC.TXT           ║
       ║  (Archivo de salida)          ║
       ╚═══════════════════════════════╝
```

### Paso a paso de lo que ocurre cuando se ejecuta

1. **Se lanza el JAR** desde la línea de comandos o un scheduler (como cron o Control-M).
2. **`App.java` arranca** e inicializa el contexto de Spring (levanta todos los beans, inyecta dependencias, carga propiedades).
3. **Se obtiene el bean** `CatalogoContratosService` del contexto de Spring.
4. **Se llama a `catalogo.inicio()`**, que a su vez llama a `contratosService.createFile(resultado)`.
5. **`createFile`** hace dos cosas:
   - Crea el archivo `.TXT` vacío en la ruta configurada.
   - Llama al DAO para obtener las dos listas de datos.
6. **El DAO ejecuta dos consultas SQL** contra DB2 para obtener fideicomisarios y fideicomitentes con contratos vencidos.
7. **Cada correo es validado** con una expresión regular antes de incluirse.
8. **Cada registro se escribe** en el archivo con el formato fijo establecido.
9. **El proceso termina** con `System.exit(0)` si todo fue bien, o `System.exit(1)` si ocurrió algún error.

---

## 4. Descripción detallada de cada clase

### `App.java` — El punto de entrada
**Ubicación:** `src/main/java/com/banco azul/arqspring/App.java`

Esta es la clase con el método `main`, es decir, por donde empieza todo. Hace lo siguiente:

```java
Config.setNumberProcess(207);        // Identifica este proceso con el número 207
Config.setNoActivo(NoActiveProcess.class);  // Qué hacer si el proceso no está activo
ApplicationContext c = Config.obtenerContexto(); // Levanta Spring
```

Luego mide el tiempo de ejecución usando dos objetos `Date` (uno antes y uno después de ejecutar). Al final imprime cuántos segundos y minutos tardó el proceso.

> 💡 **Para el Junior:** Nota que `System.exit(0)` y `System.exit(1)` son la forma estándar en procesos batch de comunicar al sistema operativo si el proceso terminó bien o con error. Un scheduler como Control-M usa estos códigos para saber si relanzar el proceso o generar una alarma.

---

### `CatalogoContratosServiceImpl.java` — El orquestador
**Ubicación:** `src/main/java/cargamasiva/service/impl/CatalogoContratosServiceImpl.java`

Es el servicio principal. Su único método `inicio()` llama a `contratosService.createFile()` y atrapa cualquier excepción, relanzándola como `ExcepcionAplicacion` (excepción personalizada del framework de banco azul).

```java
@Value("${rutas.carga.masiva.result}")
private String resultado;   // Lee la ruta desde datos.properties
```

> 💡 **Para el Junior:** La anotación `@Value` inyecta automáticamente el valor de `rutas.carga.masiva.result` del archivo `datos.properties`. No hay que leer el archivo manualmente, Spring lo hace por nosotros.

---

### `ContratosServiceImpl.java` — La lógica de negocio
**Ubicación:** `src/main/java/cargamasiva/service/impl/ContratosServiceImpl.java`

Aquí está la lógica de creación del archivo. Las partes importantes:

**Construcción del nombre del archivo:**
```java
DateFormat hourdateFormat = new SimpleDateFormat("yyyyMMdd");
String fechaActual = hourdateFormat.format(date);
File file = new File(ruta + File.separator + "MMFID_D01_" + fechaActual + "_GCVE_ECONTVENC.TXT");
```
Esto genera un nombre como `MMFID_D01_20240315_GCVE_ECONTVENC.TXT`.

**Escritura del archivo:**
```java
escribir = new FileWriter(file, false);  // false = sobrescribir si ya existe
```
El `false` es importante: si el archivo ya existe de una ejecución anterior del mismo día, lo sobreescribe en lugar de agregarle contenido.

**Salto de línea Windows:**
```java
String salto = "\r\n";  // Salto de línea estilo Windows (CRLF)
```
Se usa `\r\n` en lugar de solo `\n` porque los sistemas mainframe y Windows esperan ese formato.

> ⚠️ **Para el Junior:** Nota que si `contratosDAO.getCtoFideicomisario()` lanza una excepción, el `catch` solo atrapa `ExcepcionAplicacion`, pero el `finally` siempre cierra el `FileWriter`. Esto es importante para no dejar recursos abiertos. Sin embargo, hay un bug sutil: si `escribir` es `null` cuando se llega al `finally`, se lanzará un `NullPointerException`. Esto ocurriría si la creación del `FileWriter` falla.

---

### `ContratosDAOImpl.java` — El acceso a datos
**Ubicación:** `src/main/java/cargamasiva/dao/impl/ContratosDAOImpl.java`

Esta es la clase más compleja. Tiene tres métodos:

#### Método `getCtoFideicomisario()`
Consulta la tabla `BENEFICI` de DB2 para obtener beneficiarios activos con correo electrónico de contratos vencidos en zona restringida. Por cada registro:
1. Crea un `FideicomisarioDTO`.
2. Valida el correo con `validarCorreo()`.
3. Si el correo es válido, formatea el correo a exactamente 300 caracteres (con espacios al final) y agrega el DTO a la lista.

#### Método `getCtoFideicomitente()`
Hace lo mismo pero para fideicomitentes, consultando la tabla `FIDEICOM`.

#### Método `validarCorreo(String email)`
Valida que el correo tenga un formato básico válido usando una expresión regular (regex):

```java
Pattern pattern = Pattern.compile(
    "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)@" +
    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)(\\.[A-Za-z]{2,})$"
);
```

Esta regex acepta correos como `usuario.nombre@dominio.com.mx` pero **no** acepta:
- Correos sin punto en el dominio (`usuario@dominio`)
- Correos con caracteres especiales no contemplados
- TLDs muy largos o modernos (`.academy`, `.technology`, etc.)

> 💡 **Para el Junior:** Una expresión regular (regex) es un patrón de texto que permite validar si una cadena cumple cierta estructura. `^` significa inicio, `$` significa fin, `+` significa "uno o más", `*` significa "cero o más", `[]` define un conjunto de caracteres permitidos.

---

### `FideicomisarioDTO.java` y `FideicomitenteDTO.java` — Los transportadores de datos
**Ubicación:** `src/main/java/cargamasiva/dto/`

Son clases muy simples (POJOs) con atributos privados y sus getters/setters. Lo más importante es su método `toString()`, que define exactamente cómo se escribe cada línea en el archivo de salida:

```java
@Override
public String toString() {
    return "A0000007600000000001                                   BG102@@@"
        + correo
        + "{\"sFECHAVIGENCIA\":\"" + fechaVencimiento
        + "\",\"sFIDEICOMISO\":\"" + cveTipoNegocio + "\"}";
}
```

> 💡 **Para el Junior:** El método `toString()` en Java se llama automáticamente cuando intentas convertir un objeto a String (por ejemplo, al concatenarlo o al pasarlo a `escribir.write()`). Aquí está siendo sobreescrito (`@Override`) para generar el formato específico que el sistema receptor espera.

---

### `ConstantesBatch.java` — Las constantes del proyecto
**Ubicación:** `src/main/java/cargamasiva/util/ConstantesBatch.java`

Clase utilitaria que centraliza todos los valores constantes usados en el proyecto. Al tener un constructor privado que lanza excepción, se impide que alguien instancie la clase (patrón de clase utilitaria):

```java
private ConstantesBatch() {
    throw new IllegalStateException("Utility class");
}
```

Las constantes más usadas son `QUERY` y `PARAM`, que se usan para etiquetar los logs antes de ejecutar cada consulta SQL.

---

### `NoActiveProcess.java` — El proceso inactivo
**Ubicación:** `src/main/java/cargamasiva/util/NoActiveProcess.java`

Implementa la interfaz `NoActivo` del framework de banco azul. Cuando el scheduler detecta que el proceso no debe correr (por configuración o ventana de mantenimiento), ejecuta este método en su lugar, que simplemente crea tres archivos vacíos de control. 

> ⚠️ Los archivos que crea (`concilia_admitivos.txt`, `concilia_dividendos.txt`, `concilia_acciones.txt`) parecen pertenecer a otro proceso batch y podrían ser código heredado. Ver sección de notas técnicas.

---

## 5. Las consultas SQL explicadas

### ¿Por qué las consultas son tan largas?

Porque necesitan determinar si un contrato está vencido calculando la fecha desde campos separados (`ANO_VENCIMIENTO`, `MES_VENCIMIENTO`, `DIA_VENCIMIENTO`) que están guardados como números enteros en la base de datos, no como un campo `DATE` directamente.

### Lógica central de ambas consultas

**Paso 1:** Se construye la fecha de vencimiento concatenando los tres campos numéricos:
```sql
DATE(
  LTRIM(RTRIM(CHAR(COALESCE(CTO_ANO_VENCIM,0)))) || '-' ||
  LTRIM(RTRIM(CHAR(COALESCE(CTO_MES_VENCIM,0)))) || '-' ||
  LTRIM(RTRIM(CHAR(COALESCE(CTO_DIA_VENCIM,0))))
) AS FECHA_VENCIMIENTO
```
> `COALESCE` devuelve el primer valor no nulo. Si `CTO_ANO_VENCIM` es NULL, devuelve 0. Así se evitan errores al construir la fecha.

**Paso 2:** Se determina si el contrato está ACTIVO o VENCIDO comparando año, mes y día con la fecha actual:
```sql
CASE
  WHEN (ANO_VENCIMIENTO > YEAR(CURRENT DATE)
    OR (ANO_VENCIMIENTO = YEAR(CURRENT DATE) AND MES_VENCIMIENTO > MONTH(CURRENT DATE))
    OR (ANO_VENCIMIENTO = YEAR(CURRENT DATE) AND MES_VENCIMIENTO = MONTH(CURRENT DATE) AND DIA_VENCIMIENTO > DAY(CURRENT DATE)))
  THEN 'ACTIVO'
  ELSE 'VENCIDO'
END AS ESTATUS
```

**Paso 3:** Se filtran solo los contratos VENCIDOS que:
- NO están bloqueados en `CTOBLOQU`.
- NO están en trámite de extinción (`PAC_CVE_ST_PACAHON <> 'EN TRAMITE DE EXTINCION'`).

**Paso 4:** Se hace JOIN con la tabla de beneficiarios (`BENEFICI`) o fideicomitentes (`FIDEICOM`) filtrando solo los que están `ACTIVO` y tienen un correo con `@`.

**`WITH UR` al final:** Significa _Uncommitted Read_. Le dice a DB2 que lea los datos aunque otra transacción los esté modificando. Esto mejora el rendimiento en consultas de solo lectura, pero puede traer datos "sucios" (no confirmados aún). Es un estándar en procesos batch de lectura masiva.

---

## 6. Formato del archivo de salida

### Nombre del archivo

```
MMFID_D01_YYYYMMDD_GCVE_ECONTVENC.TXT
```

Ejemplo para el 15 de marzo de 2024:
```
MMFID_D01_20240315_GCVE_ECONTVENC.TXT
```

### Estructura de cada línea

```
[CAMPO_FIJO_54_CHARS][CORREO_300_CHARS][JSON_PAYLOAD]
```

Desglose:

| Segmento | Contenido | Longitud |
|---|---|---|
| `A0000007600000000001` | Identificador fijo del proceso | 20 chars |
| `(espacios)` | Relleno fijo | 35 espacios |
| `BG102@@@` | Separador fijo | 8 chars |
| Correo electrónico | Email del participante, rellenado con espacios hasta 300 chars | 300 chars |
| JSON | `{"sFECHAVIGENCIA":"YYYY-MM-DD","sFIDEICOMISO":"TIPO"}` | Variable |

### Ejemplo de línea completa

```
A0000007600000000001                                   BG102@@@juan.perez@empresa.com.mx                      ...{"sFECHAVIGENCIA":"2023-12-31","sFIDEICOMISO":"FID"}
```

> 💡 **Para el Junior:** El correo se rellena a 300 caracteres con `String.format("%-300s", correo)`. El `%-300s` significa: alinear a la izquierda (`-`), ocupar 300 caracteres (`300`), y rellenar con espacios si el texto es más corto (`s`).

---

## 7. Requisitos y dependencias

### Entorno de ejecución

| Componente | Versión requerida | ¿Por qué esta versión? |
|---|---|---|
| Java (JDK) | 8 | El `pom.xml` especifica `maven.compiler.source=1.6`, compatible con JDK8 |
| Maven | 3.6.3 | Versión configurada en Jenkins |
| Base de datos | IBM DB2 | Base de datos corporativa de banco azul |
| Sistema Operativo | Linux (producción) | Las rutas configuradas usan `/fiduciario/...` |

### Dependencias del `pom.xml` explicadas

**Dependencias de negocio:**

| Artefacto | Versión | ¿Para qué sirve? |
|---|---|---|
| `fiduBase` | 01 | Framework base de Banco aul Fiduciario. Contiene `BaseDAO`, `ExcepcionAplicacion`, `Config`, etc. |
| `archivos` | 01 | Utilidades para manejo de archivos del área Fiduciaria |
| `commons-logging` | 1.2 | API de logging de Apache. Permite que el código use `log` sin acoplarse a Log4j directamente |

**Dependencias de pruebas (scope: test):**

| Artefacto | Versión | ¿Para qué sirve? |
|---|---|---|
| `junit` | 4.13 | Framework estándar de pruebas unitarias en Java |
| `powermock-module-junit4` | 2.0.7 | Permite mockear clases `static`, constructores y métodos `final` (cosas que Mockito solo no puede) |
| `powermock-api-mockito2` | 2.0.7 | Integración de PowerMock con la API de Mockito 2 |
| `org.jacoco.agent` | 0.8.6 | Agente de JaCoCo para medir la cobertura de código durante las pruebas |

> 💡 **Para el Junior:** Las dependencias con `<scope>test</scope>` solo existen durante la compilación y ejecución de pruebas. No se incluyen en el JAR final que va a producción.

### Plugins de Maven explicados

| Plugin | ¿Qué hace? |
|---|---|
| `jacoco-maven-plugin` | Instrumenta el código para medir qué líneas se ejecutan durante las pruebas (cobertura de código) |
| `maven-surefire-plugin` | Ejecuta las pruebas unitarias en paralelo (10 hilos) durante la fase `test` de Maven |
| `maven-dependency-plugin` | Copia todas las dependencias (JARs) a `target/lib/` al empaquetar |
| `maven-jar-plugin` | Configura el JAR final: define la clase `main`, el classpath de dependencias |

---

## 8. Configuración y despliegue

### Archivo de propiedades

**Ubicación:** `src/main/resources/properties/datos.properties`

```properties
# Ruta donde se leerán archivos de entrada (actualmente no usada activamente)
rutas.carga.masiva.kardex=/fiduciario/trns/in/batch

# Ruta donde se generará el archivo de salida TXT
rutas.carga.masiva.result=/fiduciario/trns/out/batch/
```

Para desarrollo local en Windows, puedes comentar las líneas de producción y usar:
```properties
rutas.carga.masiva.kardex=C:\\fiduciario\\trns\\in\\batch
rutas.carga.masiva.result=C:\\fiduciario\\trns\\out\\batch\\
```

### Configuración del logging

**Ubicación:** `src/main/resources/log4j.properties`

El proyecto usa **Log4j** para registrar lo que hace. La configuración actual:
- Nivel raíz: `DEBUG` (registra todo).
- Los logs de Spring se reducen a `ERROR` para no saturar la salida.
- Los logs de `JdbcTemplate` están en `DEBUG` para ver las consultas SQL ejecutadas.
- La salida va a **consola** (`stdout`), que en producción se redirige a un archivo desde el shell que lanza el proceso.

### Pasos para compilar

```bash
# 1. Clonar el repositorio
git clone <url-del-repositorio>
cd fidcven

# 2. Compilar y empaquetar (requiere acceso al Artifactory de Banco azul)
mvn clean package -s settings_artifactory

# 3. El JAR quedará en:
ls target/fidcven-1.0-SNAPSHOT.jar

# 4. Las dependencias quedarán en:
ls target/lib/
```

> 💡 **Para el Junior:** El flag `-s settings_artifactory` le dice a Maven que use un archivo `settings.xml` específico que contiene las credenciales para descargar dependencias del Artifactory interno de Banco azul. Sin este archivo configurado, la compilación fallará porque `fiduBase` y `archivos` no existen en Maven Central.

### Pasos para ejecutar localmente

```bash
java -jar target/fidcven-1.0-SNAPSHOT.jar
```

### Pipeline CI/CD (Jenkinsfile)

El proyecto tiene un `Jenkinsfile` que define el pipeline de integración continua usando la librería compartida `jenkins-workflow`:

```groovy
spring {
    verbosity = 2        // Nivel de detalle del log del pipeline (2 = debug)
    country = 'mx'       // País: México
    group = 'fiduciariomx'
    uuaa = 'MFID'        // Identificador único del componente en Banco azul
    build = [
        maven_settings: 'file:settings_artifactory',
        maven_args: ' '
    ]
    java = 'JDK8'
    maven = 'Maven3.6.3'
}
```

El pipeline se encarga automáticamente de compilar, ejecutar pruebas, generar reportes de cobertura y publicar el artefacto en Artifactory.

---

## 9. Estructura del proyecto

```
fidcven/
│
├── Jenkinsfile                              # Pipeline de CI/CD para Jenkins
├── pom.xml                                  # Configuración del proyecto Maven
├── README.md                                # Este archivo
│
└── src/
    ├── main/
    │   ├── java/
    │   │   │
    │   │   ├── cargamasiva/                 # Paquete principal del negocio
    │   │   │   │
    │   │   │   ├── dao/                     # Capa de acceso a datos
    │   │   │   │   ├── ContratosDAO.java    # Interfaz: define qué métodos hay
    │   │   │   │   └── impl/
    │   │   │   │       └── ContratosDAOImpl.java  # Implementación: cómo funcionan
    │   │   │   │
    │   │   │   ├── dto/                     # Objetos de transferencia de datos
    │   │   │   │   ├── FideicomisarioDTO.java     # Datos del beneficiario
    │   │   │   │   └── FideicomitenteDTO.java     # Datos del fideicomitente
    │   │   │   │
    │   │   │   ├── service/                 # Capa de lógica de negocio
    │   │   │   │   ├── CatalogoContratosService.java  # Interfaz del servicio principal
    │   │   │   │   ├── ContratosService.java          # Interfaz de creación de archivo
    │   │   │   │   └── impl/
    │   │   │   │       ├── CatalogoContratosServiceImpl.java  # Orquestador
    │   │   │   │       └── ContratosServiceImpl.java          # Lógica de archivo
    │   │   │   │
    │   │   │   └── util/                    # Clases de utilidad
    │   │   │       ├── ConstantesBatch.java # Constantes del proyecto
    │   │   │       └── NoActiveProcess.java # Proceso cuando no hay actividad
    │   │   │
    │   │   └── com/banco azul/arqspring/
    │   │       └── App.java                 # ⭐ Punto de entrada (método main)
    │   │
    │   └── resources/
    │       ├── log4j.properties             # Configuración de logs
    │       └── properties/
    │           └── datos.properties         # Rutas de entrada y salida
    │
    └── test/
        └── java/
            └── com/banco azul/mfid/fidcven/batch/
                └── AppTest.java             # Prueba unitaria básica (placeholder)
```

### ¿Por qué hay dos clases `App.java`?

Notarás que existen:
- `com.banco azul.arqspring.App` → Es el **punto de entrada real** del proceso. Tiene toda la lógica.
- `com.banco azul.mfid.fidcven.batch.App` → Es una clase **placeholder de ejemplo** generada al crear el proyecto con el arquetipo Maven de Banco azul. Solo imprime "Hello World!" y debe eliminarse o ignorarse.

---

## 10. Notas Técnicas y Advertencias

### ⚠️ Bug #1: Nivel de log incorrecto en `validarCorreo()`

**Archivo:** `ContratosDAOImpl.java`

```java
// ❌ INCORRECTO - Se usa logger.error cuando el correo ES válido
if (mather.find()) {
    logger.error("Error al recuperar info: " + mather);
    return true;
}
```

**El problema:** Cuando un correo es válido y pasa la validación, el código registra un mensaje con nivel `ERROR`. Esto provoca que los archivos de log se llenen de falsos errores, dificultando la detección de errores reales.

**La corrección:**
```java
// ✅ CORRECTO
if (mather.find()) {
    logger.debug("Correo válido: " + email);
    return true;
}
```

---

### ⚠️ Bug #2: Asignación doble e innecesaria del correo

**Archivo:** `ContratosDAOImpl.java`, método `getCtoFideicomisario()`

```java
// Primera asignación (INNECESARIA, se sobreescribe en la siguiente línea)
ctoFideicomisario.setCorreo((String) query.get(constants));

// Segunda asignación (la que realmente importa)
formateado = String.format("%-300s", query.get("BEN_E_MAIL"));
ctoFideicomisario.setCorreo(formateado);
```

**El problema:** La primera línea asigna el correo sin formatear, pero inmediatamente la segunda línea lo sobreescribe con la versión formateada a 300 caracteres. La primera asignación no tiene ningún efecto.

**La corrección:**
```java
// ✅ CORRECTO - Solo una asignación, directamente formateada
formateado = String.format("%-300s", query.get(constants));
ctoFideicomisario.setCorreo(formateado);
```

---

### ⚠️ Bug #3: Posible `NullPointerException` en el `finally` de `createFile()`

**Archivo:** `ContratosServiceImpl.java`

```java
FileWriter escribir = null;
try {
    escribir = new FileWriter(file, false);  // Si esto lanza excepción...
    // ...
} catch (ExcepcionAplicacion e) {
    logg.info("Error al escribir el archivo");
} finally {
    escribir.close();  // ...aquí escribir sería null y lanzaría NullPointerException
}
```

**La corrección:**
```java
} finally {
    if (escribir != null) {
        escribir.close();
    }
}
```

O mejor aún, usando try-with-resources (Java 7+):
```java
try (FileWriter escribir = new FileWriter(file, false)) {
    // el close() se llama automáticamente al salir del bloque
}
```

---

### ⚠️ Advertencia #4: Posible inconsistencia en el filtro de `getCtoFideicomitente()`

**Archivo:** `ContratosDAOImpl.java`

El comentario en el código dice `'NOT ZONA RESTRINGIDA'`, pero el SQL filtra `CTO_NOM_ACTIVIDAD = 'ZONA RESTRINGIDA'` (igual que fideicomisarios). 

```java
// Comentario dice: 'NOT ZONA RESTRINGIDA'
sql.append("... AND CTO_NOM_ACTIVIDAD = 'ZONA RESTRINGIDA'");  // ← ¿Debería ser != ?
```

**Acción recomendada:** Revisar con el área funcional si fideicomitentes deben ser de zona restringida o de cualquier otra zona. Si el comentario es correcto, el filtro debería ser `<> 'ZONA RESTRINGIDA'`.

---

### ⚠️ Advertencia #5: Archivos de control en `NoActiveProcess` de otro proceso

**Archivo:** `NoActiveProcess.java`

Crea archivos `concilia_admitivos.txt`, `concilia_dividendos.txt`, `concilia_acciones.txt` que no tienen relación con contratos vencidos. Son probablemente residuos de otro proceso batch del que se tomó como base este código.

**Acción recomendada:** Revisar si estos archivos son requeridos por algún proceso externo que monitorea el scheduler. Si no, deben eliminarse o reemplazarse por el archivo de control correcto para este proceso.

---

### ℹ️ Nota: Limitaciones de la regex de validación de email

La expresión regular actual **no acepta** algunos formatos válidos de correo:
- Subdominios múltiples: `usuario@mail.empresa.com.mx` ✅ (sí acepta)
- TLDs modernos: `usuario@empresa.technology` ❌ (no acepta por el `{2,}` al final)
- Correos con `+`: `usuario+tag@gmail.com` ✅ (sí acepta, tiene `\\+`)
- Correos con dominio simple: `usuario@localhost` ❌ (no acepta)

Si en el futuro se detectan correos válidos rechazados, considera usar `org.apache.commons.validator.routines.EmailValidator`.

---

## 11. Preguntas frecuentes para un desarrollador nuevo

**¿Por qué el proyecto se llama `cargamasiva` en los paquetes pero `fidcven` en el artefacto?**

El paquete `cargamasiva` es el nombre lógico del módulo dentro del área Fiduciaria. El artefacto `fidcven` es la abreviación de "Fiduciario Contratos Vencidos" que identifica al proceso en el ecosistema de banco azul.

**¿Qué es `BaseDAO`?**

Es una clase del framework interno de banco azul (`fiduBase`) de la que heredan los DAOs. Proporciona automáticamente el `jdbcTemplate` (para ejecutar SQL) y el `logger` (para registrar eventos), entre otras utilidades. Por eso `ContratosDAOImpl` puede usar `jdbcTemplate` y `logger` sin declararlos explícitamente.

**¿Por qué se formatea el correo a 300 caracteres?**

El sistema receptor del archivo (probablemente un proceso mainframe o un sistema de envío masivo de correos) espera que el campo del correo ocupe exactamente 300 caracteres. Si el correo tiene menos, se rellena con espacios. Esto es un formato de campo de longitud fija, muy común en integraciones con sistemas heredados (legacy).

**¿Cómo sé si el proceso corrió bien?**

Revisar el archivo de log (la salida de consola redirigida). Al final debe aparecer:
```
FIN DE EJECUCION
```
Y el proceso debe haber terminado con código de salida 0. Si terminó con código 1, buscar en el log líneas con nivel `ERROR`.

**¿Puedo agregar pruebas unitarias?**

Sí. El archivo `AppTest.java` es solo un placeholder. Para agregar pruebas reales al DAO o servicio, usa Mockito/PowerMock para simular las respuestas del `jdbcTemplate` y del `FileWriter`, así puedes probar la lógica sin necesitar una base de datos real.

---

*Última actualización: generado a partir del análisis del código fuente del repositorio.*
