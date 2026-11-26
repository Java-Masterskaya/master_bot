package ru.yandex.masterbot.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class EvalContractsValidationTest {

  private static final Path CONTRACTS = Path.of("contracts", "eval");
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static JsonSchema load(String name) throws IOException {
    SchemaValidatorsConfig config =
        SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build();
    try (var in = Files.newInputStream(CONTRACTS.resolve(name))) {
      return JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012).getSchema(in, config);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"request.success.json"})
  void requestExamplesAreValid(String file) throws IOException {
    JsonSchema schema = load("eval.requests.v1.json");
    JsonNode node = MAPPER.readTree(CONTRACTS.resolve("examples").resolve(file).toFile());

    assertThat(schema.validate(node)).as("schema violations for " + file).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"result.success.json", "result.degraded.json", "result.failed.json"})
  void resultExamplesAreValid(String file) throws IOException {
    JsonSchema schema = load("eval.results.v1.json");
    JsonNode node = MAPPER.readTree(CONTRACTS.resolve("examples").resolve(file).toFile());

    assertThat(schema.validate(node)).as("schema violations for " + file).isEmpty();
  }

  @Test
  void requestRequiresJobIdAndApplicationIdAsUuid() throws IOException {
    JsonSchema schema = load("eval.requests.v1.json");
    JsonNode node =
        MAPPER.readTree(
            """
            {"schemaVersion":"1.0",
            "jobId":"not-a-uuid",
            "applicationId":"550e8400-e29b-41d4-a716-446655440000",
            "text":"x"}
            """);
    Set<ValidationMessage> errors = schema.validate(node);

    assertThat(errors).anyMatch(m -> m.getMessage().contains("jobId"));
  }

  @Test
  void failedResultForbidsRecommendation() throws IOException {
    JsonSchema schema = load("eval.results.v1.json");
    JsonNode node =
        MAPPER.readTree(
            """
            {"schemaVersion":"1.0",
            "jobId":"6f1c2b8a-1a2b-4c3d-9e0f-1a2b3c4d5e6f",
            "status":"FAILED",
            "recommendation":"accept",
            "finalReasons":[],
            "tasksAssessment":[],
            "questions":[],
            "responseDraft":null,
            "degraded":false,
            "errors":[{"code":"X","message":"Y"}],
            "usage":{"promptTokens":0,"completionTokens":0},
            "model":"m",
            "evaluatorVersion":"1",
            "durationMs":0}
            """);
    Set<ValidationMessage> errors = schema.validate(node);

    assertThat(errors).isNotEmpty();
  }
}
