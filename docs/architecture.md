# Architectuur

StreamForge bestaat uit vier Spring Boot-services. Drie ervan hebben een eigen
database; de gateway heeft er geen.

```
              browser (React)
                    |
              edge-gateway  :8090      geen database
              /     |      \
   catalog :8091  entitlement :8092  playback :8093
        |               |                 |
 streamforge_catalog  ..._entitlement  ..._playback
```

Playback roept tijdens het starten van een sessie zowel catalog als entitlement
aan. Dat is de enige service-naar-service-aanroep buiten de gateway om, en
precies de plek waar meerdere storingen samenkomen.

## Verantwoordelijkheden

| Service | Domein | Aggregates |
| --- | --- | --- |
| catalog | Wat is er te zien, en is de video klaar? | `Title` (met `Episode`), `IngestJob` |
| entitlement | Mag deze kijker dit zien? | `Viewer`, `Subscription` |
| playback | Wie kijkt op dit moment waarnaar? | `PlaybackSession` |
| edge-gateway | Eén ingang voor de UI en de tests | geen |

Elke service is intern ingedeeld volgens DDD:

| Laag | Inhoud |
| --- | --- |
| `domain` | Aggregates en regels. Geen Spring Web, geen HTTP. |
| `application` | Use cases en leesmodellen. |
| `infrastructure` | Spring Data JPA-repositories. |
| `api` | REST-controllers. |

De module `common` bevat wat elke service nodig heeft: het correlatie-id-filter,
de foutafhandeling, de logconfiguratie en de client voor aanroepen naar andere
services.

## Correlatie

`CorrelationFilter` leest `X-Request-Id` of maakt er een aan, zet hem in de MDC
en geeft hem terug in de response. `DownstreamClient` stuurt hem mee naar de
volgende service. Elke logregel bevat daardoor hetzelfde id:

```json
{"@timestamp":"2026-08-18T12:45:14Z","message":"downstream catalog GET /api/catalog/episodes/... ok in 12ms",
 "logger_name":"nl.haystaq.streamforge.common.DownstreamClient","level":"INFO",
 "requestId":"be58ae21-dad4-47ea-b90d-5916b05efe8e","service":"playback-service","app":"playback-service"}
```

Zo volg je één gebruikersactie door vier processen heen.

## Foutmodel

| Situatie | Status | Body |
| --- | --- | --- |
| Domeinregel weigert | 400/403/404/409 | `{"code":"...","message":"...","service":"...","requestId":"..."}` |
| Onverwachte fout | 500 | `{"code":"internal_error","message":"something went wrong",...}` (stacktrace in de logs) |
| Andere service faalt | statuscode van die service | `{"code":"upstream_error","upstream":"catalog","upstreamStatus":504,...}` |

De gateway geeft de status van de achterliggende service door, maar niet de
oorzaak. Die staat in de logs van die service - dat is precies waar de opdracht
over gaat.

## Timeouts

| Van | Naar | Connect | Read |
| --- | --- | --- | --- |
| gateway | catalog | 2s | 1,5s (`CATALOG_TIMEOUT_MS`) |
| gateway | playback | 2s | 15s |
| gateway | entitlement | 2s | 5s |
| playback | catalog | 2s | 10s |
| playback | entitlement | 2s | 5s |

Dat de gateway strenger is dan playback, is met opzet: dezelfde trage service
levert daardoor op de ene route wel en op de andere geen fout op.

## Artefacten

Alle services schrijven hun logs naar `/artifacts/logs/<service>.log` in een
gedeeld volume (`./artifacts` op de host). De harness schrijft zijn rapporten
naar `./artifacts/runs/<timestamp>/`. Dat is bewust: een agent moet met bestanden
kunnen werken, niet alleen met `docker compose logs`.
