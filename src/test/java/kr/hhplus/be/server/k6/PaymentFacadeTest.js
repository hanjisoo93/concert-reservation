import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

export let options = {
    stages: [
        { duration: '30s', target: 100 },
        { duration: '1m', target: 500 },
        { duration: '2m', target: 1000 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],  // 95%의 요청 응답시간 3초 미만
        http_req_failed: ['rate<0.1'],      // 오류율 10% 미만 유지
    },
};

let errorCounter = new Counter('errors');
const PAYMENT_URL = 'http://localhost:8080/api/payment';

export function setup() {
    let paymentTestData = [];
    for (let i = 1; i <= 1000; i++) {
        let amount = Math.floor(Math.random() * 50000) + 10000;
        paymentTestData.push({
            userId: i,
            reservationId: i,
            amount: amount,
            token: "dummy_token"
        });
    }
    return paymentTestData;
}

export default function (data) {
    if (!data || data.length === 0) {
        console.error("Payment test data가 없어 결제 API 호출을 건너뜁니다.");
        sleep(1);
        return;
    }

    let d = data[Math.floor(Math.random() * data.length)];
    let payload = JSON.stringify({
        userId: d.userId,
        reservationId: d.reservationId,
        amount: d.amount
    });
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${d.token}`
        }
    };

    let res = http.post(PAYMENT_URL, payload, params);
    let success = check(res, {
        "결제 응답 200": (r) => r.status === 200,
        "또는 400": (r) => r.status === 400,
    });
    if (!success) {
        errorCounter.add(1);
        console.error(`결제 요청 실패 - userId: ${d.userId}, status: ${res.status}`);
    }
    sleep(1);
}