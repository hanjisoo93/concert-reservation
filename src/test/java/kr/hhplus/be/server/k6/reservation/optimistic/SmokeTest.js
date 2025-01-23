import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';
const RESERVATION_API = `${BASE_URL}/reservation/optimistic`;

export const options = {
    stages: [{ duration: '10s', target: 5 }],  // 10초 동안 5명의 VU 동시 실행
};

export default function () {
    const userId = Math.floor(Math.random() * 900) + 100;  // 100~999 사이의 userId 생성
    const seatId = 1;  // 같은 좌석에 대해 경쟁하는 상황을 테스트

    const payload = JSON.stringify({ userId, seatId });
    const headers = { 'Content-Type': 'application/json' };

    const res = http.post(RESERVATION_API, payload, { headers });

    console.log(`User ID: ${userId}, Response Status: ${res.status}, Body: ${res.body}`);

    check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'seat already reserved': (r) => r.status === 409,
    });

    sleep(1);
}
