import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';
const RESERVATION_API = `${BASE_URL}/reservation/pessimistic`;

const users = [...Array(100).keys()].map(i => 100 + i);
const seatId = 1;

export const options = {
    stages: [
        { duration: '2m', target: 50 },  // 2분간 50명 유지
        { duration: '3m', target: 100 }, // 3분간 100명 유지
        { duration: '2m', target: 50 },  // 2분간 50명으로 감소
    ],
};

export default function () {
    const userId = users[Math.floor(Math.random() * users.length)];

    const payload = JSON.stringify({ userId, seatId });
    const headers = { 'Content-Type': 'application/json' };

    const res = http.post(RESERVATION_API, payload, { headers });

    check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'seat already reserved': (r) => r.status === 409,
    });

    sleep(1);
}
