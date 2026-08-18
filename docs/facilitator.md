# Facilitator - oplossingen

> **Spoilers.** Dit bestand is voor de begeleiding van de dag. Deelnemers die het
> lezen voordat ze zelf hebben gezocht, doen de opdracht niet.

Zes rode tests, **vijf** oorzaken. S2 telt voor twee tests met dezelfde oorzaak.

## S1 - Bea kan Noorderlicht niet starten (403 region_blocked)

**Oorzaak:** dataprobleem in de catalogus. `title_region` bevat voor Noorderlicht
alleen `NL`. Bea staat in de entitlement-database met `region = BE`. Playback
haalt de regio's op bij catalog en geeft ze door aan entitlement, dat de kijker
weigert.

**Bewijs:**
```sql
select region from title_region where title_id = '10000000-0000-0000-0000-000000000001';
```
Logregel in entitlement-service: `region check failed: viewer bea@example.com is in BE but title is licensed for [NL]`.

**Fix:** licentie toevoegen (`insert into title_region ... 'BE'`) of het product
laten bevestigen dat de licentie klopt. Dit is dus mogelijk *geen* bug, maar een
verkeerde testverwachting. Een sterke analyse benoemt dat.

**Valstrik:** de tweede test in S1 slaagt wel. Wie alleen naar de eerste kijkt,
denkt aan een storing in playback.

## S2 - Zoeken geeft 504 (twee tests)

**Oorzaak:** N+1-query in `CatalogService#similarTitleCounts`. Vanaf drie tekens
draait de "meer zoals dit"-verrijking mee. Die verrijking loopt niet over de
gevonden titels maar over de **hele catalogus** (8000 titels), met één count-query
per titel. Dat duurt ongeveer vijf seconden. De gateway heeft een leestimeout van
1500 ms (`CATALOG_TIMEOUT_MS`) en geeft een 504.

**Bewijs:**
- Gateway: `downstream catalog GET /api/catalog/titles?query=noor failed after 1500ms (status 504)`
- Catalog: `enrichment: 8008 titles scanned, 8009 queries, 5000ms`
- Rechtstreeks: `curl -w '%{time_total}' http://localhost:8091/api/catalog/titles?query=noor` geeft ~5s met status 200.

**Fix:** de verrijking alleen over de gevonden titels doen, of in één query
(`group by genre`). De timeout verhogen is symptoombestrijding.

**Valstrik:** de gateway meldt de fout, maar is niet de oorzaak. Een agent die op
de gateway blijft hangen, komt er niet.

## S3 - Noorderlicht S01E03 speelt niet af (409 manifest_unavailable)

**Oorzaak:** de aflevering staat op `asset_status = PENDING` en heeft geen
`manifest_url`. De transcodeerjob is zes dagen geleden gefaald en nooit opnieuw
gestart. De stacktrace staat in `ingest_job.error_message`
(`NoSuchFileException: /mnt/ingest/noorderlicht/s01e03.mxf`).

**Bewijs:**
```bash
curl -s "http://localhost:8090/api/ingest-jobs?episodeId=20000000-0000-0000-0000-000000000103"
```
Catalog logt: `episode ... is not playable: status=PENDING manifest=null`.

**Fix:** ingest opnieuw draaien; daarnaast bewaken dat afleveringen zonder
READY-asset niet in de app zichtbaar zijn.

## S4 - Heartbeat geeft 500

**Oorzaak:** `NullPointerException` in `PlaybackService#heartbeat`:
`session.deviceType().toUpperCase(...)` terwijl `device_type` nullable is. De
sessie is aangemaakt door een client die het veld niet meestuurt (de test doet
dat expres).

**Bewijs:** stacktrace in `artifacts/logs/playback-service.log`, veld
`stack_trace`, `java.lang.NullPointerException: Cannot invoke "String.toUpperCase()"`.

**Fix:** null-check of een default (`UNKNOWN`) bij het aanmaken van de sessie.

**Valstrik:** het starten van de sessie slaagt. De fout valt pas een stap later,
in een andere aanroep.

## S5 - Chris krijgt stream_limit_reached (409)

**Oorzaak:** twee sessies uit de seed staan al drie dagen op `ACTIVE` met een
`last_heartbeat_at` van drie dagen geleden. Playback telt alle `ACTIVE`-sessies en
geeft `activeStreams = 2` door aan entitlement; STANDARD staat twee streams toe,
dus de derde wordt geweigerd. Er is geen opruimjob die verweesde sessies sluit.

**Bewijs:**
```sql
select id, status, started_at, last_heartbeat_at
from playback_session
where viewer_id = '40000000-0000-0000-0000-000000000003';
```
Entitlement logt: `stream limit reached: viewer chris@example.com plan STANDARD allows 2 streams, playback reports 2 active`.

**Fix:** sessies zonder heartbeat binnen X minuten als beëindigd beschouwen, en
een opruimjob toevoegen.

**Valstrik:** de eerste test van S5 slaagt, want `GET /api/sessions?viewerId=...`
geeft de sessies wel terug - maar de UI toont ze alleen op de diagnosepagina.

## S6 - Terecht geweigerd

Geen bug. Dirk (leeftijdsgrens 12) mag een 18+-titel niet zien, Eva's abonnement
staat op PAUSED. Beide tests verwachten 403 en slagen. Een agent die dit als
storing meldt, heeft niet goed gekeken.

## Knoppen voor de begeleiding

| Wat | Hoe |
| --- | --- |
| Scenario 2 sneller of trager maken | `CATALOG_TIMEOUT_MS` in `.env` (lager = harder falen) |
| Alles terugzetten | `curl -X POST http://localhost:8090/api/admin/reset` |
| Eén scenario draaien | `docker compose --profile tools run --rm harness S3` |
| Extra logdetail | logniveau staat al op DEBUG voor `nl.haystaq.streamforge` |

## Suggestie voor de tijdsindeling

- Laat elk groepje eerst **één** storing met de hand oplossen (30 min). Zonder
  die referentie kunnen ze de agent niet beoordelen.
- Vraag halverwege welke van de zes tests dezelfde oorzaak delen. Dat is het
  moment waarop het over clusteren gaat in plaats van over samenvatten.
- Bewaar S1 en S6 voor het slot: allebei gaan ze over de vraag of een rode test
  wel een bug is.
