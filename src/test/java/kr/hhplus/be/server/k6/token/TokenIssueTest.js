import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 1300 }, // 10초 동안 1300개의 동시 요청 유지
    ],
};

export default function () {
    let userId = Math.floor(Math.random() * 100000);
    let url = `http://localhost:8080/api/tokens/issue?userId=${userId}`;  // DB 기반 API
    let res = http.post(url, JSON.stringify({ userId }), { headers: { 'Content-Type': 'application/json' } });

    console.log(`Response status: ${res.status}, Body: ${res.body}`);

    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(1);
}
