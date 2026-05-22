# Sistema de Compra de Planes de Salud en Línea — SPS

**Pontificia Universidad Javeriana · Arquitectura de Software**

---

## Descripción del Sistema

SPS es una solución empresarial distribuida y de alta disponibilidad para la adquisición y gestión de planes de salud en línea. El sistema permite a los clientes autenticarse, explorar un catálogo de servicios médicos, consolidar un carrito de compras y procesar el flujo transaccional asincrónico que involucra la validación ante entidades gubernamentales externas (SNS) y plataformas de pago internas (.NET).

Una vez confirmado el pago, la arquitectura propaga la información mediante mensajería orientada a mensajes (MOM) hacia nodos independientes para la actualización de agendas médicas e historias clínicas.

---

## Arquitectura General y Despliegue

La arquitectura cumple con las siguientes restricciones obligatorias del proyecto:

- **Aislamiento de Perímetros:** La capa de presentación (`SPS-SPA`) se encuentra en una zona de red desmilitarizada (DMZ), completamente aislada de la lógica de negocio interna.
- **Desacoplamiento de Persistencia:** Cada sistema (`SPS`, `SaludPay`, `SAM`, `SHC`) cuenta con su propia base de datos independiente; queda prohibida la conexión directa entre ellas.
- **Distribución Física de Nodos:** Despliegue distribuido utilizando la totalidad de las estaciones de trabajo del equipo de desarrollo.

---

## Estructura de Módulos del Proyecto

El repositorio está organizado como monorepo distribuido que separa componentes de presentación, lógica de negocio y transporte de mensajería:

| Carpeta | Tecnología | Rol | Descripción |
| --- | --- | --- | --- |
| `Balanceador` | Spring Boot | Infraestructura | Balanceador de carga con Round-Robin y health-check hacia las réplicas de MS-Compra |
| `Cola_Pago` | ActiveMQ / Docker | Mensajería | Cola dedicada para el flujo asincrónico de facturación |
| `Cola_SAM` | ActiveMQ / Docker | Mensajería | Cola dedicada para el envío de datos de servicios al SAM |
| `Cola_SHC` | ActiveMQ / Docker | Mensajería | Cola dedicada para el envío de planes y usuarios al SHC |
| `Email` | Spring Boot | Microservicio | Envío de notificaciones por correo electrónico (aprobación SNS y confirmación de pago) |
| `MS-Auth-Catalogo` | Spring Boot | Microservicio | Gestión de seguridad (JWT) y catálogo de servicios médicos |
| `MS-Compra` | Spring Boot | Microservicio | Core de negocio: persiste compras y gestiona la comunicación asíncrona con la SNS |
| `SaludPay-Back` | .NET 8 (C#) | Microservicio | Backend del sistema transaccional interno de pagos |
| `SaludPay-SPA` | Angular | Frontend | Interfaz para autenticación por cédula y pago de deudas pendientes |
| `SAM` | Spring Boot | Sistema Interno | Sistema de Agenda Médica: consume de cola y almacena datos de citas |
| `SHC` | Spring Boot | Sistema Interno | Sistema de Historias Clínicas: consume de cola y procesa datos clínicos |
| `SNS` | Spring Boot | Servicio Externo | Superintendencia Nacional de Salud: valida planes ante el ente regulador |
| `SPS-SPA` | Angular | Frontend | Portal principal para la selección y compra de planes de salud |

---

## Integrantes y Topología de Red

| Nombre | IP | Componentes |
| --- | --- | --- |
| **Daniel Castro** | `10.43.100.122` | `Balanceador` · `SNS` · `MS-Auth-Catalogo` · `Email` |
| **Mapa Rodríguez** | `10.43.101.18` | `SPS-SPA` · `SaludPay-Back` · `SaludPay-SPA` |
| **Katheryn Guasca** | `10.43.99.121` | `MS-Compra (Réplicas)` · `SAM` · `SHC` |
| **Juan Rozo** | `10.43.100.111` | `MS-Compra (Maestro)` · `Cola-SAM` · `Cola-SHC` · `Cola-Pago` |

---

## Variables de Entorno

Existe un único `.env` en la raíz del repositorio con todos los secretos compartidos. Los módulos que los necesiten los leen desde allí vía Docker Compose o directamente en el `application.yml` con `${VAR}`.

```env
DB_USER=root
DB_PASS=root
SQLSERVER_PASS=SaludPay123!
JWT_SECRET=JWT123!
ACTIVEMQ_URL=tcp://10.43.100.111:61616
ACTIVEMQ_USER=admin
ACTIVEMQ_PASS=admin
MAIL_HOST=smtp.gmail.com
MAIL_USER=tucorreo@gmail.com
MAIL_PASS=tu-app-password
SALUD_PAY_URL=http://10.43.101.18:8080
SPS_URL_PAGO=http://10.43.101.18:4200/pago
SNS_URL=http://10.43.100.122:8090
SNS_CODIGO=ASEG001
```

> **Importante:** Antes de desplegar, reemplaza todos los valores marcados con valores reales y nunca subas el `.env` al repositorio.

---

## Requisitos de Ejecución

| Herramienta | Versión mínima |
| --- | --- |
| Java | 17 LTS |
| .NET SDK | 8 |
| Node.js | 20+ |
| Angular CLI | 17+ |
| Docker Engine | 24+ |
| Docker Compose | 2.x |

---

## Levantar un nodo

```bash
# Desde la raíz del repositorio, en la máquina correspondiente
docker compose up -d --build

# Ver logs en tiempo real
docker compose logs -f

# Verificar estado
docker compose ps
```

Cada nodo solo levanta los servicios que le corresponden según la tabla de topología.