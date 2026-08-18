# Opdracht 2 - De agent die de oorzaak vindt

**Probleem uit de praktijk:** *"Testresultaten analyseren kost veel tijd."*
De suite is rood, je hebt logs van vier services, een testrapport, stacktraces en
tijden. Het duurt een half uur voordat je weet welke van de zes rode tests
eigenlijk één en dezelfde oorzaak hebben.

**AI-richting:** een agent die logs, testrapporten, API-responses en stacktraces
leest en daar een onderbouwde root-cause analyse van maakt.

---

## 1. De situatie

De nachtelijke run van StreamForge is rood (zie [README.md](README.md)). Zes
tests falen. Jij bent de tester die maandagochtend moet uitleggen wat er aan de
hand is - en welke tickets er nodig zijn.

Wat je hebt:

| Bron | Waar |
| --- | --- |
| Testrapport met per test request, response, status, duur en correlatie-id | `artifacts/latest-report.json` |
| JUnit-XML voor de CI | `artifacts/runs/<run>/junit.xml` |
| JSON-logs van vier services, inclusief stacktraces | `artifacts/logs/*.log` |
| De draaiende applicatie zelf | `http://localhost:8090` |
| De databases | `localhost:5434` |
| De broncode | `backend/` |

Wat je niet hebt: een lijst met oorzaken.

## 2. Wat je bouwt

Een **root-cause agent**. Minimale scope:

| Component | Wat het moet doen |
| --- | --- |
| **Inlezen** | Testrapport en logs inlezen, ook als ze groot zijn. Niet alles in de prompt gooien: filter eerst. |
| **Correleren** | Per gefaalde test het correlatie-id pakken en alle logregels van alle services daarbij zoeken. Zo bouw je per storing één tijdlijn. |
| **Clusteren** | Zes rode tests zijn geen zes problemen. Groepeer wat dezelfde oorzaak deelt. |
| **Analyseren** | Per cluster een hypothese met bewijs: welke service, welke regel code of welke data, en waarom past dat bij het symptoom. |
| **Rapporteren** | Een leesbaar rapport: symptoom, geraakte tests, oorzaak, bewijs, voorgestelde fix, en hoe zeker je bent. |

Voorstel voor MCP-tools:

```
list_failures(report_path)            -> gefaalde tests met status, duur en requestId
trace(request_id)                     -> alle logregels van alle services, chronologisch
service_logs(service, level?, since?) -> gefilterde logs
query_db(service, sql)                -> read-only query op de database van een service
http_probe(method, url, body?)        -> zelf een aanroep doen om een hypothese te toetsen
source_search(pattern)                -> zoeken in backend/
```

Die laatste twee zijn belangrijk: een goede analyse toetst zijn eigen hypothese.
Een agent die alleen logs samenvat, komt niet verder dan "er ging iets mis in
playback".

## 3. Aanpak in fases

Reken op ongeveer vier uur.

**Fase 0 - Zelf doen (30 min)**
Draai de suite. Kies één gefaalde test en zoek met de hand de oorzaak. Klok hoe
lang je erover doet. Dat is je nulmeting - en je referentie-antwoord.

**Fase 1 - Inlezen en correleren (45 min)**
Bouw de MCP-tools `list_failures` en `trace`. Laat de agent voor één gefaalde
test de complete tijdlijn over de vier services tonen. Als dat werkt, heb je het
saaiste deel van je werk geautomatiseerd.

**Fase 2 - Van tijdlijn naar hypothese (60 min)**
Laat de agent per storing een oorzaak benoemen met bewijs. Verplicht hem te
citeren: welke logregel, welk veld, welke query. Geen bewijs is geen conclusie.

**Fase 3 - Toetsen (45 min)**
Geef de agent `http_probe` en `query_db` en laat hem zijn eigen hypothese
bevestigen of onderuithalen. Bijvoorbeeld: als de hypothese "regio-licentie" is,
laat hem dan dezelfde aanroep doen namens een kijker in een andere regio.

**Fase 4 - Clusteren en rapporteren (45 min)**
Zes rode tests, hoeveel oorzaken? Laat de agent groeperen en een rapport
opleveren dat je maandagochtend echt zou versturen. Verpak het in een skill of
slash-command.

**Fase 5 - Demo (15 min)**
Draai de suite opnieuw en laat de agent er in één keer doorheen lopen.

## 4. Definition of done

- [ ] De agent draait op de echte artefacten, niet op een handmatig samengevatte versie.
- [ ] Per gefaalde test kun je de volledige keten zien: gateway -> service -> database.
- [ ] De agent groepeert de zes rode tests naar het juiste aantal oorzaken.
- [ ] Elke conclusie heeft bewijs: bestand, regel, of query met uitkomst.
- [ ] De agent geeft aan hoe zeker hij is, en zegt het als hij het niet weet.
- [ ] De agent merkt op dat scenario S6 correct gedrag test en dus geen bug is.
- [ ] Het rapport is leesbaar voor iemand die de applicatie niet kent.
- [ ] Alles staat in deze repo, in een branch met een pull request.

## 5. De lat: wat een goed antwoord bevat

Voor elke storing:

1. **Symptoom** - wat de gebruiker of de test ziet.
2. **Keten** - welke service gaf welke status, in welke volgorde.
3. **Oorzaak** - de precieze regel code, dataregel of instelling.
4. **Bewijs** - de logregel, de queryuitkomst of de responsbody.
5. **Fix** - wat je zou veranderen, en wat het risico daarvan is.
6. **Zekerheid** - hoog, midden of laag, met de reden.

Een antwoord als "playback-service geeft een 500" is geen root cause. "Playback
crasht op een NullPointerException in `PlaybackService.heartbeat` omdat de sessie
zonder `deviceType` is aangemaakt door een client die dat veld niet stuurt" wel.

## 6. Stretch goals

- Laat de agent zijn analyse omzetten in concept-tickets (titel, stappen om te
  reproduceren, prioriteit) en desgewenst als GitHub issue aanmaken.
- Laat de agent een fix voorstellen als diff en de suite opnieuw draaien om te
  bewijzen dat het werkt.
- Voeg trendanalyse toe: draai de suite meerdere keren en laat de agent zien
  welke storingen stabiel zijn en welke wisselen.
- Laat de agent zelf nieuwe scenario's schrijven voor gaten die hij in de
  dekking ziet.
- Meet het verschil: hoeveel tijd kost een analyse met en zonder agent?

## 7. Valkuilen

- **Hele logbestanden in de prompt.** Filter eerst op correlatie-id of niveau.
- **Het eerste antwoord geloven.** De eerste foutmelding in de keten is meestal
  een gevolg, niet de oorzaak.
- **Alles één probleem maken.** Of juist zes losse problemen. Beide zijn fout.
- **Geen tegenproef doen.** Een hypothese die je niet toetst, is een gok.
- **Vergeten dat een rode test correct kan zijn.** Zie S6.

## 8. Wat je oplevert

1. Werkende code in deze repo: MCP-server, agent of skill.
2. Het gegenereerde analyserapport over de laatste run.
3. Een demo van maximaal 10 minuten.
4. Eén alinea: hoe lang deed je er met de hand over, hoe lang met de agent, en
   waar ging de agent de mist in?
