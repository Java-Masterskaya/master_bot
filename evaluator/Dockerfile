FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PYTHONPATH=/app/vendor/brief-evaluator
WORKDIR /app

COPY vendor/brief-evaluator/requirements.txt ./vendor/brief-evaluator/requirements.txt
RUN python -m pip install --no-cache-dir -r vendor/brief-evaluator/requirements.txt

COPY pyproject.toml ./
COPY src/ ./src/
RUN python -m pip install --no-cache-dir .

COPY vendor/brief-evaluator/ ./vendor/brief-evaluator/

HEALTHCHECK --interval=5s --timeout=3s --start-period=5s --retries=5 \
    CMD ["python", "-c", "from urllib.request import urlopen; urlopen('http://127.0.0.1:8000/healthz', timeout=2).close()"]

CMD ["python", "-m", "uvicorn", "brief_service.main:app", "--host", "0.0.0.0", "--port", "8000"]
