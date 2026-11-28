from typing import Annotated, Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

Recommendation = Literal["accept", "reject", "clarify", "simplify", "expertise"]
NonEmptyText = Annotated[str, Field(min_length=1)]


class ContractModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class EvaluationRequest(ContractModel):
    schemaVersion: str = Field(pattern=r"^1\.\d+$")
    jobId: UUID
    applicationId: UUID
    text: str = Field(min_length=1, max_length=100_000)


class TaskAssessment(ContractModel):
    task: NonEmptyText
    category: Recommendation
    reason: NonEmptyText


class EvaluationError(ContractModel):
    code: NonEmptyText
    message: NonEmptyText


class Usage(ContractModel):
    promptTokens: int = Field(ge=0)
    completionTokens: int = Field(ge=0)


class EvaluationResult(ContractModel):
    schemaVersion: Literal["1.0"] = "1.0"
    jobId: UUID
    status: Literal["DONE", "FAILED"]
    recommendation: Recommendation | None
    finalReasons: list[NonEmptyText]
    tasksAssessment: list[TaskAssessment]
    questions: list[NonEmptyText]
    responseDraft: str | None
    degraded: bool
    errors: list[EvaluationError]
    usage: Usage
    model: NonEmptyText
    evaluatorVersion: NonEmptyText
    durationMs: int = Field(ge=0)
