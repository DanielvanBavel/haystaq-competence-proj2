'use strict';

const fs = require('fs');
const path = require('path');
const { scenarios } = require('./scenarios');
const http = require('./http');

const ARTIFACTS = process.env.ARTIFACTS_DIR || '/artifacts';

function selected() {
  const wanted = process.argv.slice(2).filter((arg) => !arg.startsWith('-'));
  if (wanted.length === 0) {
    return scenarios;
  }
  const ids = wanted.map((value) => value.toUpperCase());
  return scenarios.filter((scenario) => ids.includes(scenario.id));
}

async function waitForGateway() {
  for (let attempt = 1; attempt <= 60; attempt++) {
    const result = await http.get('/actuator/health');
    if (result.status === 200) {
      return true;
    }
    await new Promise((resolve) => setTimeout(resolve, 2000));
  }
  return false;
}

function junit(report) {
  const cases = report.scenarios.flatMap((scenario) =>
    scenario.tests.map((test) => {
      const name = `${scenario.id} ${test.name}`;
      if (test.outcome === 'passed') {
        return `    <testcase classname="${scenario.id}" name="${escapeXml(test.name)}" time="${test.durationMs / 1000}"/>`;
      }
      if (test.outcome === 'skipped') {
        return `    <testcase classname="${scenario.id}" name="${escapeXml(test.name)}"><skipped/></testcase>`;
      }
      const message = `expected status ${test.expectedStatus}, got ${test.actualStatus}`;
      return `    <testcase classname="${scenario.id}" name="${escapeXml(test.name)}" time="${test.durationMs / 1000}">\n` +
        `      <failure message="${escapeXml(message)}">${escapeXml(JSON.stringify(test.response, null, 2))}</failure>\n` +
        '    </testcase>';
    })
  );
  return '<?xml version="1.0" encoding="UTF-8"?>\n' +
    `<testsuite name="streamforge" tests="${report.summary.total}" failures="${report.summary.failed}" ` +
    `skipped="${report.summary.skipped}" time="${report.summary.durationMs / 1000}">\n` +
    cases.join('\n') + '\n</testsuite>\n';
}

function escapeXml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

async function main() {
  const startedAt = new Date();
  console.log(`StreamForge harness -> ${http.GATEWAY}`);

  if (!(await waitForGateway())) {
    console.error('gateway is niet bereikbaar');
    process.exit(2);
  }

  const report = {
    startedAt: startedAt.toISOString(),
    gateway: http.GATEWAY,
    scenarios: [],
    summary: { total: 0, passed: 0, failed: 0, skipped: 0, durationMs: 0 }
  };

  for (const scenario of selected()) {
    const context = {};
    const scenarioReport = { id: scenario.id, name: scenario.name, story: scenario.story, tests: [] };

    for (const test of scenario.tests) {
      if (test.skipIf && test.skipIf(context)) {
        scenarioReport.tests.push({
          name: test.name,
          outcome: 'skipped',
          reason: 'vorige stap leverde geen bruikbaar resultaat',
          expectedStatus: test.expect.status,
          actualStatus: null,
          durationMs: 0
        });
        report.summary.total++;
        report.summary.skipped++;
        continue;
      }

      const result = await test.run(context);
      if (test.capture) {
        Object.assign(context, test.capture(result));
      }
      const passed = result.status === test.expect.status;

      scenarioReport.tests.push({
        name: test.name,
        outcome: passed ? 'passed' : 'failed',
        expectedStatus: test.expect.status,
        actualStatus: result.status,
        requestId: result.requestId,
        durationMs: result.durationMs,
        request: { method: result.method, url: result.url, body: result.requestBody },
        response: result.body,
        networkError: result.networkError
      });

      report.summary.total++;
      report.summary.durationMs += result.durationMs;
      if (passed) {
        report.summary.passed++;
      } else {
        report.summary.failed++;
      }

      const mark = passed ? 'PASS' : 'FAIL';
      console.log(`${mark}  ${scenario.id}  ${test.name}  (verwacht ${test.expect.status}, kreeg ${result.status}` +
        `${result.requestId ? `, requestId ${result.requestId}` : ''})`);
    }

    report.scenarios.push(scenarioReport);
  }

  report.finishedAt = new Date().toISOString();

  const runDir = path.join(ARTIFACTS, 'runs', startedAt.toISOString().replace(/[:.]/g, '-'));
  fs.mkdirSync(runDir, { recursive: true });
  fs.writeFileSync(path.join(runDir, 'report.json'), JSON.stringify(report, null, 2));
  fs.writeFileSync(path.join(runDir, 'junit.xml'), junit(report));
  fs.writeFileSync(path.join(ARTIFACTS, 'latest-report.json'), JSON.stringify(report, null, 2));

  console.log(`\n${report.summary.passed} geslaagd, ${report.summary.failed} gefaald, ` +
    `${report.summary.skipped} overgeslagen`);
  console.log(`artefacten: ${runDir}`);

  process.exit(report.summary.failed > 0 ? 1 : 0);
}

main().catch((error) => {
  console.error(error);
  process.exit(2);
});
