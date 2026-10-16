# Sistema Automotivo — Gestão de Estoque de Veículos

Projeto acadêmico (UniFECAF · ADS · disciplina *Object Oriented Programming*) —
Repositório: https://github.com/PharmaBR/sistema-automotivo-unifecaf

Sistema CRUD orientado a objetos para gestão de estoque
de veículos, com **Java 21 + Spring Boot 3 + MySQL 8**, API REST documentada no Swagger e um
front-end simples em HTML/JS.

## Funcionalidades (requisitos funcionais)

| RF | Descrição | Onde |
|----|-----------|------|
| RF01 | Cadastro de veículos (modelo, marca, ano, cor, preço, km, status) | `POST /api/vehicles` |
| RF02 | Cadastro de marcas e modelos, vinculados aos veículos | `/api/brands`, `/api/models` |
| RF03 | Consulta e filtros por marca, modelo, tipo, preço, ano, status e cor, com paginação e ordenação | `GET /api/vehicles?...` |
| RF04 | Atualização de preço, quilometragem, status (e cor) | `PATCH /api/vehicles/{id}` |
| RF05 | Remoção de veículos vendidos ou descontinuados | `DELETE /api/vehicles/{id}` |

Regras de negócio implementadas na entidade `Vehicle`: preço > 0, quilometragem nunca diminui,
veículo vendido não volta ao estoque nem pode ser editado, remoção só de vendido/descontinuado.

## Como executar

### Opção 1 — Docker (recomendado: só precisa do Docker Desktop)

```bash
docker compose up --build
```

- Front-end: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- MySQL (Workbench/DBeaver): `localhost:3307`, usuário `automotivo`, senha `automotivo`

Na primeira execução o banco é populado com 6 marcas, 12 modelos e 14 veículos de exemplo.
Para zerar o banco: `docker compose down -v`.

### Opção 2 — Local (JDK 21 + Maven + MySQL instalados)

```bash
export DB_USERNAME=root DB_PASSWORD=suasenha   # o banco "automotivo" é criado sozinho
mvn spring-boot:run
```

### Testes

```bash
mvn test          # ou, sem Java local:
docker run --rm -v "$PWD":/app -w /app maven:3.9-eclipse-temurin-21 mvn -B test
```

- `VehicleTest` — testes de unidade das regras de negócio (Java puro, sem banco).
- `VehicleApiIntegrationTest` — testes de integração da API (Spring + MockMvc + H2 em modo MySQL).

## Arquitetura

Arquitetura em camadas, padrão **MVC** exposto como **API REST**:

```
src/main/java/br/edu/unifecaf/automotivo
├── domain/        Entidades e regras (Model): BaseEntity, Brand, VehicleModel, Vehicle, enums, exceções
├── repository/    Acesso a dados com Spring Data JPA + filtros dinâmicos (Specifications)
├── service/       Casos de uso e transações
├── web/           Controllers REST (Controller), DTOs e tratamento global de erros
└── config/        Swagger e carga de dados de exemplo
src/main/resources
├── db/schema.sql  Modelo físico MySQL (fonte da verdade do banco)
└── static/        Front-end (View): index.html, css, js
```

Fluxo de uma requisição: `Controller → Service → Entidade (regras) → Repository → MySQL`.

## Endpoints

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/brands` | Lista marcas |
| POST | `/api/brands` | Cria marca |
| PUT | `/api/brands/{id}` | Atualiza marca |
| DELETE | `/api/brands/{id}` | Remove marca (sem modelos) |
| GET | `/api/models?brandId=` | Lista modelos (opcionalmente por marca) |
| POST | `/api/models` | Cria modelo |
| PUT | `/api/models/{id}` | Atualiza modelo |
| DELETE | `/api/models/{id}` | Remove modelo (sem veículos) |
| GET | `/api/vehicles` | Consulta com filtros, paginação e ordenação |
| GET | `/api/vehicles/{id}` | Detalhe |
| GET | `/api/vehicles/summary` | Total por status |
| POST | `/api/vehicles` | Cadastra veículo |
| PATCH | `/api/vehicles/{id}` | Atualiza preço/km/status/cor |
| DELETE | `/api/vehicles/{id}` | Remove veículo vendido/descontinuado |

Exemplos prontos em [`docs/api.http`](docs/api.http).

Erros seguem o padrão *Problem Details* (RFC 9457): `400` dados inválidos, `404` não encontrado,
`422` regra de negócio violada, `409` conflito no banco.
