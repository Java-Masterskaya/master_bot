import json
from pathlib import Path
from types import SimpleNamespace

from fastapi.testclient import TestClient
from jsonschema import Draft202012Validator
from langchain_core.outputs import LLMResult
from src.models import (
    EvaluationResponse,
    GenerationResponse,
    QuestionGenerationResponse,
    StructuringResponse,
)

from brief_service import main
from brief_service.evaluation_contract import EvaluationRequest
from brief_service.pipeline import evaluate

REQUEST = EvaluationRequest(
    schemaVersion="1.0",
    jobId="550e8400-e29b-41d4-a716-446655440000",
    applicationId="550e8400-e29b-41d4-a716-446655440001",
    text="Build a reporting dashboard for a synthetic project.",
)
CONTRACTS = Path(__file__).resolve().parents[2] / "contracts" / "eval"


def validate_contract(name, value):
    with (CONTRACTS / name).open(encoding="utf-8") as schema_file:
        validator = Draft202012Validator(
            json.load(schema_file), format_checker=Draft202012Validator.FORMAT_CHECKER
        )
    validator.validate(value.model_dump(mode="json"))


class FakeLLM:
    def __init__(self, counter, failed_schema=None):
        self.counter = counter
        self.failed_schema = failed_schema

    def with_structured_output(self, schema):
        def invoke(messages):
            assert len(messages) == 2
            if schema is self.failed_schema:
                raise RuntimeError("private provider diagnostics")
            self.counter.on_llm_end(
                LLMResult(
                    generations=[],
                    llm_output={"token_usage": {"prompt_tokens": 3, "completion_tokens": 2}},
                )
            )
            return {
                StructuringResponse: {"tasks": ["Build a dashboard"]},
                EvaluationResponse: {
                    "assessment": [
                        {"task": "Build a dashboard", "category": "accept", "reason": "Fits"}
                    ],
                    "recommendation": "accept",
                    "final_reasons": ["Fits"],
                },
                QuestionGenerationResponse: {"questions": ["Which reports are required?"]},
                GenerationResponse: {"draft_email": "We can build this."},
            }[schema]

        return SimpleNamespace(invoke=invoke)


def test_success_counts_tokens_per_call():
    first = evaluate(REQUEST, FakeLLM)
    second = evaluate(REQUEST, FakeLLM)

    assert first.status == "DONE"
    assert first.jobId == REQUEST.jobId
    assert first.recommendation == "accept"
    assert first.tasksAssessment[0].category == "accept"
    assert first.questions == ["Which reports are required?"]
    assert first.responseDraft == "We can build this."
    assert first.degraded is False
    assert first.errors == []
    assert first.usage.model_dump() == {"promptTokens": 12, "completionTokens": 8}
    assert second.usage == first.usage
    validate_contract("eval.requests.v1.json", REQUEST)
    validate_contract("eval.results.v1.json", first)


def test_failed_evaluation_stage_is_degraded_without_leaking_provider_error():
    def create_fake(counter):
        return FakeLLM(counter, failed_schema=EvaluationResponse)

    result = evaluate(REQUEST, create_fake)

    assert result.status == "FAILED"
    assert result.recommendation is None
    assert result.degraded is True
    assert result.errors[0].model_dump() == {
        "code": "EvaluationResponse",
        "message": "RuntimeError",
    }
    assert result.usage.promptTokens == 9
    assert result.responseDraft is None
    validate_contract("eval.results.v1.json", result)


def test_failed_question_stage_preserves_recommendation():
    result = evaluate(REQUEST, lambda counter: FakeLLM(counter, QuestionGenerationResponse))

    assert result.status == "DONE"
    assert result.recommendation == "accept"
    assert result.degraded is True
    assert result.errors[0].code == "QuestionGenerationResponse"
    assert result.responseDraft == "We can build this."
    validate_contract("eval.results.v1.json", result)


def test_debug_endpoint_serializes_result(monkeypatch):
    monkeypatch.setattr(main, "evaluate", lambda request: evaluate(request, FakeLLM))

    with TestClient(main.app) as client:
        response = client.post("/v1/evaluations", json=REQUEST.model_dump(mode="json"))

    assert response.status_code == 200
    assert response.json()["jobId"] == str(REQUEST.jobId)
    assert response.json()["schemaVersion"] == "1.0"
    assert response.json()["degraded"] is False
