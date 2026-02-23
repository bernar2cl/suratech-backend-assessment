import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomSeed, uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

randomSeed(1234);

export const options = {
  vus: 20,
  duration: '2m',
  thresholds: {
    http_req_duration: ['p(95)<500'],   // 95% of requests < 500ms
    http_req_failed: ['rate<0.01']      // <1% errors
  }
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const TOKEN = __ENV.BEARER_TOKEN || 'dummy-token';

export default function () {
  const documentId = `DOC-${__VU}-${__ITER}`;
  const body = {
    documentId: documentId,
    customerId: `CUST-${__VU}`,
    currency: 'USD',
    amount: 100.0,
    effectiveFrom: new Date().toISOString()
  };

  const headers = {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`,
    'X-Idempotency-Key': uuidv4()
  };

  const res = http.post(`${BASE_URL}/api/v1/quotes`, JSON.stringify(body), { headers });

  check(res, {
    'status is 201 or 200 (idempotent)': (r) => r.status === 201 || r.status === 200,
  });

  sleep(0.2);
}