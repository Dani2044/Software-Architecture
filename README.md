# Sistema de Compra de Planes de Salud en Línea — SPS

**Pontificia Universidad Javeriana · Arquitectura de Software · 2026-10**

---

## Descripción

SPS es una solución empresarial distribuida para la adquisición de planes de salud en línea. El cliente se autentica, explora un catálogo de planes (compuestos por servicios médicos), arma un carrito, y la compra se procesa de forma asíncrona: SPS valida cada plan contra la SNS, notifica al cliente por correo cuando es aprobada, recibe el pago a través de la plataforma transaccional `SaludPay` (.NET) y propaga la información a los sistemas internos `SAM` (Agenda Médica) y `SHC` (Historias Clínicas) vía mensajería MOM con ActiveMQ.

---

## Restricciones cumplidas

- **DMZ:** las SPA (`SPS-SPA`, `SaludPay-SPA`) están aisladas en el nodo de Mapa, separadas de la lógica de negocio.
- **BD por sistema:** MS-Auth, MS-Compra, SAM, SHC, SNS, Email y SaludPay tienen cada uno su propia BD (5 MySQL + 1 SQL Server). Nunca se conecta una BD desde otro sistema.
- **Despliegue distribuido:** los 4 integrantes levantan en paralelo desde la misma rama del repo, cada uno con su perfil Docker Compose.
- **Escalabilidad:** dos réplicas de `MS-Compra` (maestro en .122, réplica en .121) detrás del `Balanceador` Spring Boot.
- **MOM:** ActiveMQ con 3 colas (`cola.pago`, `cola.sam`, `cola.shc`). Todas las comunicaciones MS-Compra ⇄ SAM/SHC/SaludPay pasan por allí.

---

## Topología

| Integrante | IP | Motor | Componentes |
| --- | --- | --- | --- |
| **Daniel Castro** | `10.43.101.18` | Docker Engine | Balanceador · SNS · MS-Auth-Catalogo · Email |
| **Mapa Rodríguez** | `10.32.100.111` | Kestrel (.NET) + Docker | SPS-SPA · SaludPay-Back · SaludPay-SPA |
| **Katheryn Guasca** | `10.43.99.121` | Docker Engine | MS-Compra (réplica) · SAM · SHC |
| **Juan Rozo** | `10.43.100.122` | Docker Engine | MS-Compra (maestro) · ActiveMQ |

> **Dos nginx, roles distintos:**
> - `Balanceador.jar` (Spring Boot, en .101.18) → balanceador L7 entre las dos réplicas de MS-Compra.
> - `nginx` dentro de `SPS-SPA` y `SaludPay-SPA` → servidor de estáticos del bundle Angular (`ng build` → `dist/` en imagen `nginx:alpine`). No balancea nada.

---

## Estructura de módulos

| Carpeta | Tecnología | Rol |
| --- | --- | --- |
| `Balanceador` | Spring Boot 4 (WebFlux) | Load balancer round-robin con health-check hacia réplicas MS-Compra |
| `Email` | Spring Boot + JavaMailSender | Envío de notificaciones por correo (aprobación SNS + compra terminada) |
| `MS-Auth-Catalogo` | Spring Boot + JWT | Login centralizado para SPS-SPA y catálogo de planes/servicios |
| `MS-Compra` | Spring Boot + WebFlux + JMS | Core: persiste compras, llama SNS async, publica/consume colas |
| `SAM` | Spring Boot + JMS | Sistema Agenda Médica; `@JmsListener("cola.sam")` |
| `SHC` | Spring Boot + JMS | Sistema Historias Clínicas; `@JmsListener("cola.shc")` |
| `SNS` | Spring Boot | Simulador externo SuperIntendencia Nacional de Salud |
| `SaludPay-Back` | .NET 8 + EF Core + SQL Server | Pagos transaccionales; publica `cola.pago` con Apache.NMS.ActiveMQ |
| `SaludPay-SPA` | Angular 17 | Frontend de pagos (login por cédula + lista de compras) |
| `SPS-SPA` | Angular 17 | Frontend principal (login JWT + catálogo + carrito) |

---

## Variables de entorno

Copia `.env.example` a `.env` y completa:

```bash
cp .env.example .env
```

Todas las variables sensibles (passwords, JWT secret, credenciales SMTP) viven sólo en `.env`. El archivo está en `.gitignore`.

---

## Cómo levantar tu nodo

Antes de comenzar, asegúrate de tener Docker Engine 24+ y Docker Compose 2.x instalados.

```bash
# 1. Clona el repo
git clone <repo-url>
cd Software-Architecture

# 2. Crea tu .env
cp .env.example .env       # PowerShell: Copy-Item .env.example .env
nano .env                  # completa valores reales (especialmente MAIL_USER/MAIL_PASS)
```

Luego, **cada integrante levanta solo su perfil** con Docker Compose profiles:

### Juan (10.43.100.122) — broker + MS-Compra maestro
```bash
COMPOSE_PROFILES=juan docker compose up -d --build
# Consola ActiveMQ:  http://10.43.100.122:8161  (admin/admin)
# MS-Compra:         http://10.43.100.122:8081/api/compra/health
```

### Daniel (10.43.101.18) — Balanceador + SNS + Auth+Cat + Email
```bash
COMPOSE_PROFILES=daniel docker compose up -d --build
# Balanceador:  http://10.43.101.18:8080/actuator/health
# SNS:          http://10.43.101.18:8090/api/sns/health
# Auth+Cat:     http://10.43.101.18:8082/api/catalogo/health
# Email:        http://10.43.101.18:8084/api/email/health
```

### Kath (10.43.99.121) — MS-Compra réplica + SAM + SHC
```bash
COMPOSE_PROFILES=kath docker compose up -d --build
# MS-Compra:  http://10.43.99.121:8081/api/compra/health
# SAM:        http://10.43.99.121:8086/api/sam/health
# SHC:        http://10.43.99.121:8087/api/shc/health
```

### Mapa (10.32.100.111) — SaludPay-Back + SPAs
```bash
COMPOSE_PROFILES=mapa docker compose up -d --build
# SaludPay-Back:  http://10.32.100.111:5000/api/saludpay/health
# SPS-SPA:        http://10.32.100.111:4200
# SaludPay-SPA:   http://10.32.100.111:4201
```

Si Mapa prefiere correr `SaludPay-Back` con Kestrel directamente (sin contenedor):
```bash
cd SaludPay-Back
dotnet run                 # escucha en http://0.0.0.0:5000
```

---

## Flujo end-to-end

```
[1] Cliente → SPS-SPA → Balanceador (.101.18) → MS-Compra (.122 o .121)
                                                  │ persiste (estado=CREADA), responde 202
                                                  ▼
                                              SNS (.122) ── WebClient async ──┐
                                                  │                            │
                                       APROBADO ◄─┘──► ENPROCESO → @Scheduled cada 15s
                                              │
                                              ▼
                              MS-Compra marca APROBADA
                                  ├─ POST → Email (correo + URL pago)
                                  └─ POST → SaludPay-Back (publica compra pendiente)

[2] Cliente → SaludPay-SPA → SaludPay-Back (Kestrel, .32.100.111)
                                  │ persiste pago en SQL Server
                                  │ PUBLISH cola.pago via Apache.NMS.ActiveMQ
                                  ▼
                          MS-Compra @JmsListener("cola.pago")
                                  │ marca PAGADA
                                  ├─► PUBLISH cola.sam ──► SAM (.121) → MySQL
                                  ├─► PUBLISH cola.shc ──► SHC (.121) → MySQL
                                  │ marca TERMINADA
                                  └─► POST → Email (correo "compra terminada")
```

---

## Diseño de colas ActiveMQ

| Cola | Productor | Consumidor | Payload |
| --- | --- | --- | --- |
| `cola.pago` | SaludPay-Back (.NET) | MS-Compra | `{ cedula, numeroCompra, valorPagado, fechaPago }` |
| `cola.sam`  | MS-Compra | SAM | `{ numeroCompra, cedulaCliente, servicios[] }` |
| `cola.shc`  | MS-Compra | SHC | `{ numeroCompra, persona{}, planes[] }` |

Las tres son **colas point-to-point** (no tópicos): cada mensaje lo procesa exactamente UN consumidor. Las propiedades clave:

- **Persistencia:** `delivery-mode: persistent` (sobreviven a reinicios del broker).
- **Idempotencia:** los consumidores SAM/SHC chequean `(numeroCompra, código)` antes de insertar; MS-Compra ignora pagos para compras ya `TERMINADA`.
- **DLQ:** ActiveMQ mueve a `ActiveMQ.DLQ` automáticamente tras 6 reintentos.
- **Conversor:** Jackson JSON con header `_type` para resolver el POJO destino (el cliente .NET publica con `_type=com.sps.compra.messaging.TransaccionPago`).

---

## Verificación

1. **Smoke tests** después de levantar cada perfil → consulta los `/health` listados arriba.
2. **Login en SPS-SPA** (`/login`) con `juan / juan123`.
3. **Agregar plan al carrito**, confirmar → estado debe quedar en `CREADA → EN_VALIDACION_SNS → APROBADA`.
4. **Verifica el correo** (revisa la BD `sps_email_db` o el inbox configurado).
5. **Abrir SaludPay-SPA** (`/login`) con `1000000001 / juan123`, pagar.
6. **Consola ActiveMQ** (`http://10.43.100.122:8161`): debes ver 1 mensaje en `cola.pago`, luego en `cola.sam` y `cola.shc`.
7. **Consulta SAM y SHC**: `GET http://10.43.99.121:8086/api/sam/agenda/1000000001` y `GET http://10.43.99.121:8087/api/shc/historia/1000000001` deben listar registros.
8. **Resiliencia SNS**: detén el contenedor `sns` → crear compra → debe quedar en `EN_VALIDACION_SNS`. Levanta `sns` → en menos de 15s la compra pasa a `APROBADA`.
9. **Balanceo**: detén el contenedor `ms-compra` en .122 → el balanceador debe enrutar todo a .121 (`GET http://10.43.101.18:8080/api/compra/registry`).

---

## Notas operacionales

- **Firewall Windows:** abrir 8080, 8081, 8082, 8084, 8086, 8087, 8090, 5000, 4200, 4201, 61616, 8161 entre las 4 VMs.
- **Reloj NTP:** sincronizar para no invalidar JWT entre máquinas.
- **No subir `.env`** al repo (está en `.gitignore`).

---

## Requisitos mínimos

| Herramienta | Versión |
| --- | --- |
| Java | 17 LTS |
| .NET SDK | 8 |
| Node.js | 20+ |
| Docker Engine | 24+ |
| Docker Compose | 2.x |
