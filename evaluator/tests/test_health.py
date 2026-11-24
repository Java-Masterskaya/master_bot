from fastapi.testclient import TestClient

from brief_service.main import app


def test_health_endpoint_returns_ok():
    with TestClient(app) as client:
        response = client.get("/healthz")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}
