# haystaq-competence-proj2 - StreamForge

> Competence-dag, opdracht 2: **"Testresultaten analyseren kost veel tijd."**
> De opdracht staat in [MISSION.md](MISSION.md). Dit bestand beschrijft de applicatie.

StreamForge is een streamingplatform: een catalogus met series en afleveringen,
abonnementen met regio- en leeftijdsregels, en een afspeeldienst die sessies
bijhoudt. Het draait als vier losse services met elk een eigen database.

En het is stuk. Op zes plekken, om vijf verschillende redenen.

## Stack

| Onderdeel | Technologie |
| --- | --- |
| Services | Java 21, Spring Boot 3.4 (catalog, entitlement, playback, edge-gateway) |
| Databases | PostgreSQL 16, één database per service |
| Frontend | React 18 + TypeScript, Vite, nginx |
| Logging | JSON-logs met correlatie-id, naar stdout en naar `artifacts/logs/` |
| Testsuite | Node-harness die scenario's afspeelt en artefacten wegschrijft |
| Architectuur | Domain Driven Design per service |

Zie [docs/architecture.md](docs/architecture.md).

## Snel starten

Vereist: Docker Desktop (of Docker Engine + Compose v2).

```bash
docker compose up -d --build
```

De eerste build duurt een paar minuten. Daarna:

- UI: <http://localhost:3002>
- Gateway (BFF): <http://localhost:8090/api/browse>
- Catalog: <http://localhost:8091/api/catalog/titles>
- Entitlement: <http://localhost:8092/api/entitlements/viewers>
- Playback: <http://localhost:8093/api/playback/sessions>
- Postgres: `localhost:5434`, gebruiker `streamforge`, wachtwoord `streamforge`,
  databases `streamforge_catalog`, `streamforge_entitlement`, `streamforge_playback`

Poorten bezet? Maak een `.env` (zie [.env.example](.env.example)).

## De testsuite draaien

```bash
docker compose --profile tools run --rm harness
```

Dit speelt zeven scenario's af tegen de gateway en schrijft weg:

```
artifacts/
├── latest-report.json          laatste run, met per test request, response, requestId en duur
├── runs/<timestamp>/report.json
├── runs/<timestamp>/junit.xml
└── logs/<service>.log          JSON-logs van alle vier de services
```

Eén scenario draaien kan ook:

```bash
docker compose --profile tools run --rm harness S3
```

De suite eindigt met exitcode 1 zolang er tests falen. Dat is de bedoeling: op
dit moment falen er zes.

## Wat je ziet als je hem draait

```
PASS  S0  Anne start Noorderlicht S01E01
FAIL  S1  Bea start Noorderlicht S01E01        (verwacht 201, kreeg 403)
FAIL  S2  zoeken op "noor"                     (verwacht 200, kreeg 504)
FAIL  S3  Anne start Noorderlicht S01E03       (verwacht 201, kreeg 409)
FAIL  S4  heartbeat sturen                     (verwacht 200, kreeg 500)
FAIL  S5  Chris start Deltawacht S01E01        (verwacht 201, kreeg 409)
```

Vijf verschillende oorzaken, verdeeld over drie services, een database en een
stuk configuratie. De symptomen staan in [docs/scenarios.md](docs/scenarios.md).
De oorzaken staan daar **niet** in.

## Zelf rondkijken

Logs van één service, gefilterd op een correlatie-id:

```bash
docker compose logs playback | grep 4c24c5f9
```

Alle logs van alle services staan ook als bestand in `artifacts/logs/`. Elke
regel is JSON met `requestId`, `service`, `level`, `message` en bij fouten een
`stack_trace`.

Welke services doen het nog?

```bash
curl -s http://localhost:8090/api/status
```

Rechtstreeks de database in:

```bash
docker compose exec db psql -U streamforge -d streamforge_catalog -c "\dt"
```

Alles terugzetten naar de begintoestand:

```bash
curl -X POST http://localhost:8090/api/admin/reset
```

## Belangrijk om te weten

1. **De fouten zijn echt.** Niets wordt kunstmatig gefaald op basis van een
   vlaggetje. Elke storing komt voort uit data, code of configuratie die je kunt
   vinden en kunt repareren.
2. **De symptomen liggen niet bij de oorzaak.** De gateway meldt een timeout, de
   oorzaak zit in de catalogus. Playback meldt een conflict, de oorzaak zit in
   oude sessiedata.
3. **Het correlatie-id is je draad.** Elke response heeft `X-Request-Id`; elke
   logregel van elke service draagt hetzelfde id.
4. **Twee scenario's horen te falen.** S6 verwacht bewust een 403: dat is correct
   gedrag, geen storing. Niet elke rode test is een bug.

## Zonder Docker draaien (optioneel)

```bash
cd backend && mvn -q clean package -DskipTests
```

Start dan per service `java -jar <module>/target/<module>-1.0.0.jar` met de juiste
`DB_HOST`, `DB_NAME` en `CATALOG_URL`/`ENTITLEMENT_URL`/`PLAYBACK_URL`.
De frontend draai je met `cd frontend && npm install && npm run dev`.
