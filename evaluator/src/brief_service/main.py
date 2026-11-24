from fastapi import FastAPI

from brief_service.evaluation_contract import EvaluationRequest, EvaluationResult
from brief_service.pipeline import evaluate

app = FastAPI()


@app.get("/healthz")
def health() -> dict[str, str]:
    return {"status": "ok"}


@app.post("/v1/evaluations")
def create_evaluation(request: EvaluationRequest) -> EvaluationResult:
    return evaluate(request)
