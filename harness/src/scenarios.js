'use strict';

const http = require('./http');

// Vaste id's uit de seed.
const VIEWER = {
  anne: '40000000-0000-0000-0000-000000000001',
  bea: '40000000-0000-0000-0000-000000000002',
  chris: '40000000-0000-0000-0000-000000000003',
  dirk: '40000000-0000-0000-0000-000000000004',
  eva: '40000000-0000-0000-0000-000000000005'
};

const EPISODE = {
  noorderlichtS01E01: '20000000-0000-0000-0000-000000000101',
  noorderlichtS01E03: '20000000-0000-0000-0000-000000000103',
  kanaalkoortsS01E01: '20000000-0000-0000-0000-000000000201',
  deltawachtS01E01: '20000000-0000-0000-0000-000000000301',
  nachtploegS01E01: '20000000-0000-0000-0000-000000000801'
};

/**
 * Elke test beschrijft wat een gebruiker probeert te doen en wat er zou moeten
 * gebeuren. Wat er werkelijk gebeurt, is de opdracht.
 */
const scenarios = [
  {
    id: 'S0',
    name: 'Basisstroom werkt',
    story: 'Anne bladert door het aanbod en start een aflevering.',
    tests: [
      {
        name: 'aanbod ophalen',
        run: () => http.get('/api/browse'),
        expect: { status: 200 }
      },
      {
        name: 'korte zoekopdracht',
        run: () => http.get('/api/search?query=no'),
        expect: { status: 200 }
      },
      {
        name: 'Anne start Noorderlicht S01E01',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.anne,
          episodeId: EPISODE.noorderlichtS01E01,
          deviceType: 'BROWSER'
        }),
        expect: { status: 201 }
      }
    ]
  },
  {
    id: 'S1',
    name: 'Kijker in Belgie kan niet starten',
    story: 'Bea (Belgie, actief abonnement) probeert Noorderlicht te kijken.',
    tests: [
      {
        name: 'Bea start Noorderlicht S01E01',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.bea,
          episodeId: EPISODE.noorderlichtS01E01,
          deviceType: 'SMART_TV'
        }),
        expect: { status: 201 }
      },
      {
        name: 'Bea start Kanaalkoorts S01E01 (controle)',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.bea,
          episodeId: EPISODE.kanaalkoortsS01E01,
          deviceType: 'SMART_TV'
        }),
        expect: { status: 201 }
      }
    ]
  },
  {
    id: 'S2',
    name: 'Zoeken loopt vast',
    story: 'Een bezoeker zoekt op een woord van drie letters of meer.',
    tests: [
      {
        name: 'zoeken op "noor"',
        run: () => http.get('/api/search?query=noor'),
        expect: { status: 200 }
      },
      {
        name: 'zoeken op "kanaal"',
        run: () => http.get('/api/search?query=kanaal'),
        expect: { status: 200 }
      }
    ]
  },
  {
    id: 'S3',
    name: 'Nieuwe aflevering speelt niet af',
    story: 'De derde aflevering van Noorderlicht staat in de app maar start niet.',
    tests: [
      {
        name: 'aflevering staat in de catalogus',
        run: () => http.get(`/api/episodes/${EPISODE.noorderlichtS01E03}`),
        expect: { status: 200 }
      },
      {
        name: 'Anne start Noorderlicht S01E03',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.anne,
          episodeId: EPISODE.noorderlichtS01E03,
          deviceType: 'BROWSER'
        }),
        expect: { status: 201 }
      }
    ]
  },
  {
    id: 'S4',
    name: 'Oude client crasht op heartbeat',
    story: 'Een oudere settopbox stuurt geen apparaattype mee.',
    tests: [
      {
        name: 'sessie starten zonder deviceType',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.anne,
          episodeId: EPISODE.kanaalkoortsS01E01
        }),
        expect: { status: 201 },
        capture: (result) => ({ sessionId: result.body && result.body.id })
      },
      {
        name: 'heartbeat sturen',
        run: (context) => http.post(`/api/sessions/${context.sessionId}/heartbeat`, { positionSeconds: 120 }),
        expect: { status: 200 },
        skipIf: (context) => !context.sessionId
      }
    ]
  },
  {
    id: 'S5',
    name: 'Tweede scherm wordt geweigerd',
    story: 'Chris kijkt nergens, maar krijgt toch de melding dat zijn limiet bereikt is.',
    tests: [
      {
        name: 'Chris heeft geen actieve sessies volgens de app',
        run: () => http.get(`/api/sessions?viewerId=${VIEWER.chris}`),
        expect: { status: 200 }
      },
      {
        name: 'Chris start Deltawacht S01E01',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.chris,
          episodeId: EPISODE.deltawachtS01E01,
          deviceType: 'BROWSER'
        }),
        expect: { status: 201 }
      }
    ]
  },
  {
    id: 'S6',
    name: 'Leeftijdsgrens en abonnement',
    story: 'Dirk (12 jaar profiel) en Eva (gepauzeerd abonnement) proberen te kijken.',
    tests: [
      {
        name: 'Dirk start Nachtploeg (18+)',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.dirk,
          episodeId: EPISODE.nachtploegS01E01,
          deviceType: 'BROWSER'
        }),
        expect: { status: 403 }
      },
      {
        name: 'Eva start Noorderlicht',
        run: () => http.post('/api/play', {
          viewerId: VIEWER.eva,
          episodeId: EPISODE.noorderlichtS01E01,
          deviceType: 'BROWSER'
        }),
        expect: { status: 403 }
      }
    ]
  }
];

module.exports = { scenarios, VIEWER, EPISODE };
