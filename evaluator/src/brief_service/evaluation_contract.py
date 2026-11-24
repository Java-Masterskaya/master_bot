from typing import Literal
from uuid import UUID

from pydantic import BaseModel, ConfigDict, Field

Recommendation = Literal["accept", "reject", "clarify", "simplify", "expertise"]


class ContractModel(BaseModel):
    model_config = ConfigDict(extra="forbid")


class EvaluationRequest(ContractModel):
    schemaVersion: Literal[1] = 1
    jobId: UUID
    applicationId: UUID
    text: str = Field(min_length=1)


class TaskAssessment(ContractModel):
    task: str
    category: Recommendation
    reason: str


class Usage(ContractModel):
    promptTokens: int = Field(ge=0)
    completionTokens: int = Field(ge=0)


class EvaluationResult(ContractModel):
    schemaVersion: Literal[1] = 1
    jobId: UUID
    status: Literal["DONE", "FAILED"]
    recommendation: Recommendation
    finalReasons: list[str]
    tasksAssessment: list[TaskAssessment]
    questions: list[str]
    responseDraft: str
    degraded: bool
    errors: list[str]
    usage: Usage
    model: str
    evaluatorVersion: str
    durationMs: int = Field(ge=0)
