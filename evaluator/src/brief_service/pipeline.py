import logging
import os
from importlib.metadata import version
from time import perf_counter

from langchain_openai import ChatOpenAI
from src.models import (
    EvaluationResponse,
    GenerationResponse,
    ProjectContext,
    QuestionGenerationResponse,
    StructuringResponse,
    TokenCounterCallback,
)
from src.modules.evaluation import EvaluationModule
from src.modules.question_generation import QuestionGenerationModule
from src.modules.response_generation import ResponseGenerationModule
from src.modules.structuring import StructuringModule

from brief_service.evaluation_contract import (
    EvaluationError,
    EvaluationRequest,
    EvaluationResult,
)

logger = logging.getLogger(__name__)
MODEL = "openai/gpt-4o-mini"


def record_failure(errors, code, error):
    failure = EvaluationError(code=code, message=type(error).__name__)
    errors.append(failure)
    logger.warning("Evaluation failed in %s: %s", failure.code, failure.message)


class EvaluationTokenCounter(TokenCounterCallback):
    def on_llm_end(self, response, **kwargs):
        usage = (response.llm_output or {}).get("token_usage") or {}
        prompt = usage.get("prompt_tokens") or 0
        completion = usage.get("completion_tokens") or 0
        self.total_prompt_tokens += prompt
        self.total_completion_tokens += completion
        self.total_tokens += prompt + completion


def create_llm(counter):
    api_key = os.environ["KODIK_API_KEY"]
    if not api_key.strip():
        raise ValueError("KODIK_API_KEY is empty")
    return ChatOpenAI(
        api_key=api_key,
        base_url="https://api.kodikrouter.ru/v1",
        model=MODEL,
        temperature=0,
        max_tokens=2048,
        timeout=30,
        callbacks=[counter],
    )


class StructuredModelWithFallback:
    """Record invocation errors before DS modules can swallow or print them."""

    def __init__(self, llm, fallback, errors):
        self.llm = llm
        self.fallback = fallback
        self.errors = errors

    def with_structured_output(self, schema):
        structured = self.llm.with_structured_output(schema)
        errors = self.errors
        fallback = self.fallback

        class Invocation:
            def invoke(self, messages):
                try:
                    response = structured.invoke(messages)
                    if isinstance(response, schema):
                        return response
                    return schema.model_validate(response)
                except Exception as error:
                    record_failure(errors, schema.__name__, error)
                    return fallback

        return Invocation()


def evaluate(request: EvaluationRequest, llm_factory=create_llm) -> EvaluationResult:
    start = perf_counter()
    counter = EvaluationTokenCounter()
    errors = []
    context = ProjectContext(input_text=request.text)
    failed = False
    try:
        llm = llm_factory(counter)
    except Exception as error:
        record_failure(errors, "model_initialization", error)
        failed = True
    else:
        stages = (
            (StructuringModule, StructuringResponse()),
            (
                EvaluationModule,
                EvaluationResponse(
                    assessment=[], recommendation="expertise", final_reasons=[]
                ),
            ),
            (QuestionGenerationModule, QuestionGenerationResponse(questions=[])),
            (ResponseGenerationModule, GenerationResponse(draft_email="")),
        )
        for module, fallback in stages:
            stage_start = perf_counter()
            previous_error_count = len(errors)
            try:
                context = module(StructuredModelWithFallback(llm, fallback, errors)).process(
                    context
                )
            except Exception as error:
                record_failure(errors, module.__name__, error)
                if module is StructuringModule:
                    context.structure = fallback.model_dump()
            finally:
                logger.info(
                    "Evaluation stage %s completed in %.3fs",
                    module.__name__,
                    perf_counter() - stage_start,
                )
            if module in (StructuringModule, EvaluationModule):
                failed |= len(errors) > previous_error_count

    return EvaluationResult(
        jobId=request.jobId,
        status="FAILED" if failed else "DONE",
        recommendation=None if failed else context.recommendation or "expertise",
        finalReasons=[] if failed else [reason for reason in context.final_reasons if reason],
        tasksAssessment=[
            item for item in context.tasks_assessment or [] if item["task"] and item["reason"]
        ],
        questions=[question for question in context.questions if question],
        responseDraft=None if failed else context.response_draft or None,
        degraded=bool(errors),
        errors=errors,
        usage={
            "promptTokens": counter.total_prompt_tokens,
            "completionTokens": counter.total_completion_tokens,
        },
        model=MODEL,
        evaluatorVersion=version("master-bot-evaluator"),
        durationMs=int((perf_counter() - start) * 1000),
    )
