import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '10s', target: 1300 }, // 10초 동안 1300개의 동시 요청 유지
    ],
};

export function setup() {
    let successfulUserIds = [];
    for (let id = 1; id <= 1300; id++) {
        let url = `http://localhost:8080/api/tokens/issue?userId=${id}`;
        let payload = JSON.stringify({ userId: id });
        let params = { headers: { 'Content-Type': 'application/json' } };
        let res = http.post(url, payload, params);

        if (res.status === 200) {
            successfulUserIds.push(id);
        }
    }
    return successfulUserIds;
}

export default function(data) {
    let userId = data[Math.floor(Math.random() * data.length)];
    let url = `http://localhost:8080/api/tokens/get?userId=${userId}`;
    let res = http.get(url);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
