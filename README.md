# Sistema de Compra de Planes de Salud en Línea — SPS

**Pontificia Universidad Javeriana · Arquitectura de Software** 

---

## 📋 Descripción del Sistema

SPS es una solución empresarial distribuida y de alta disponibilidad para la adquisición y gestión de planes de salud en línea. El sistema permite a los clientes autenticarse, explorar un catálogo de servicios médicos, consolidar un carrito de compras y procesar el flujo transaccional asincrónico que involucra la validación ante entidades gubernamentales externas (SNS) y plataformas de pago internas (.NET).

Una vez confirmado el pago, la arquitectura propaga la información mediante mensajería orientada a mensajes (MOM) hacia nodos independientes para la actualización de agendas médicas e historias clínicas.

---

## 🏛️ Arquitectura General y Despliegue

La arquitectura cumple con las siguientes restricciones obligatorias del proyecto:

* 
**Aislamiento de Perímetros:** La capa de presentación (`SPS-SPA`) se encuentra en una zona de red desmilitarizada (DMZ), completamente aislada de la lógica de negocio interna.


* 
**Desacoplamiento de Persistencia:** Cada sistema (`SPS`, `SaludPay`, `SAM`, `SHC`) cuenta con su propia base de datos relacional independiente; queda prohibida la conexión directa entre ellas.


* 
**Distribución Física de Nodos:** Despliegue distribuido utilizando la totalidad de las estaciones de trabajo del equipo de desarrollo.

*(Insertar captura del diagrama VPP corregido aquí)* 

---

## 📂 Estructura de Módulos del Proyecto

El repositorio está organizado en un enfoque de monorepo distribuido que separa componentes de presentación, lógica de negocio y transporte de mensajería:

| Carpeta | Tecnología | Componente / Rol | Descripción |
| --- | --- | --- | --- |
| `├── Balanceador` | Nginx | Infraestructura | Balanceador de carga inverso. Maneja la escalabilidad del backend de compras.

 |
| `├── Cola_Pago` | Docker / MOM | Mensajería | Broker/Cola dedicada para asincronía del flujo de facturación. |
| `├── Cola_SAM` | Docker / MOM | Mensajería | Broker dedicado para el envío de datos de servicios al SAM.

 |
| `├── Cola_SHC` | Docker / MOM | Mensajería | Broker dedicado para el envío de planes y usuarios al SHC.

 |
| `├── Email` | Spring Boot | Microservicio | Componente encargado del envío asincrónico de notificaciones por correo.

 |
| `├── MS-Auth-Catalogo` | Spring Boot | Microservicio | Gestión de seguridad (usuarios/contraseñas) y catálogo de servicios.

 |
| `├── MS-Compra` | Spring Boot | Microservicio | Core de negocio. Persiste compras y gestiona la comunicación asíncrona con la SNS.

 |
| `├── SaludPay-Back` | .NET 8 (C#) | Microservicio | Backend del sistema transaccional interno de pagos.

 |
| `├── SaludPay-SPA` | Angular | SPA (Frontend) | Interfaz de usuario para la autenticación por cédula y pago de deudas.

 |
| `├── SAM` | Spring Boot | Sistema Interno | Sistema de Agenda Médica. Consume de cola y almacena citas de doctores.

 |
| `├── SHC` | Spring Boot | Sistema Interno | Sistema de Historias Clínicas. Consume de cola y procesa datos clínicos.

 |
| `├── SPS-SPA` | Angular | SPA (Frontend) | Portal principal orientado al cliente para la selección y compra de planes.

 |

---

## 🛡️ Manejo de Secretos y Variables de Entorno

En la raíz de cada módulo que lo requiera, hay un archivo `.env`.

---

## 👥 Integrantes del Grupo y Topología de Red

Las IPs fijas asignadas para la distribución de los servicios del ecosistema SPS son:

| Nombre | Máquina / IP | Componentes Clave a Desplegar (Fuera de IDE) 

 |
| --- | --- | --- |
| **Daniel Castro** | `10.43.100.122` | `Balanceador`, `SNS`, `MS-Auth-Catalogo`, `Email` |
| **Mapa Rodríguez** | `10.43.101.18` | `SPS-SPA`, `SaludPay-Back`, `SaludPay-SPA` |
| **Katheryn Guasca** | `10.43.99.121` | `MS-Compra (Réplicas)`, `SAM`, `SHC` |
| **Juan Rozo** | `10.43.100.111` | `MS-Compra (Maestro)`, `Cola-SAM`, `Cola-SHC`, `Cola-Pago` |

---

## 🚀 Requisitos de Ejecución Mínimos

* 
**Entornos de Ejecución:** Java 17 LTS , .NET Core 8 SDK , Node.js 20+ y Angular CLI 17+.

* 
**Orquestación:** Docker Engine y Docker Compose instalados de manera nativa.