# Scenario's en meldingen

Dit is wat de servicedesk heeft doorgegeven. Geen oorzaken, alleen symptomen.

| Id | Melding | Verwacht | Werkelijk |
| --- | --- | --- | --- |
| S0 | - (controle dat de basis werkt) | 200/201 | 200/201 |
| S1 | "Ik zit in Belgie en kan die ene serie niet starten, andere series wel." | 201 | 403 |
| S2 | "Zoeken werkt niet meer. Korte woorden wel, langere niet." | 200 | 504 |
| S3 | "De nieuwste aflevering staat in de app maar start niet." | 201 | 409 |
| S4 | "Op de oude settopbox valt het beeld na een halve minuut uit." | 200 | 500 |
| S5 | "Ik kijk nergens, maar krijg te horen dat ik te veel schermen gebruik." | 201 | 409 |
| S6 | Twee gebruikers krijgen terecht geen toegang. | 403 | 403 |

## Details per melding

### S1 - Kijker in Belgie kan niet starten
Bea (`bea@example.com`) heeft een actief STANDARD-abonnement. Noorderlicht start
niet, Kanaalkoorts wel. Bij collega's in Nederland start Noorderlicht gewoon.

### S2 - Zoeken loopt vast
Zoeken op `no` werkt. Zoeken op `noor` of `kanaal` geeft na anderhalve seconde
een foutmelding. Rechtstreeks bij de catalogusservice komt er wel antwoord, maar
traag.

### S3 - Nieuwe aflevering speelt niet af
Noorderlicht S01E03 staat in de app met alle gegevens. Afspelen mislukt met een
conflict. De twee eerdere afleveringen werken.

### S4 - Oude client crasht op heartbeat
Een oudere settopbox start de sessie zonder problemen. Zodra de client de eerste
voortgangsmelding stuurt, krijgt hij een serverfout. Nieuwere clients hebben er
geen last van.

### S5 - Tweede scherm wordt geweigerd
Chris (`chris@example.com`) heeft STANDARD (twee gelijktijdige streams). Hij
kijkt naar eigen zeggen nergens. Elke poging om iets te starten wordt geweigerd.
In de app ziet hij zelf geen actieve sessies.

### S6 - Terecht geweigerd
Dirk heeft een profiel met leeftijdsgrens 12 en probeert een 18+-titel te
starten. Eva heeft een gepauzeerd abonnement. Beide keren is 403 het juiste
antwoord. Deze tests horen te slagen.

## Waar je kunt kijken

| Vraag | Ingang |
| --- | --- |
| Wat gebeurde er bij deze test? | `artifacts/latest-report.json`, veld `requestId` |
| Wat deden de services op dat moment? | `artifacts/logs/*.log`, zoek op hetzelfde `requestId` |
| Welke service was traag? | logregels `downstream ... in ...ms` in de gateway |
| Klopt de data? | `docker compose exec db psql -U streamforge -d streamforge_<service>` |
| Wat doet de code? | `backend/<service>/src/main/java` |
| Draaien alle services nog? | `curl http://localhost:8090/api/status` |
