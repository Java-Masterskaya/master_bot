# Что сделать перед коммитом

**1. Отформатировать код**
```bash
./gradlew spotlessApply
```

**2. Проверить форматирование и стиль**
```bash
./gradlew spotlessCheck checkstyleMain
```

Если чекстайл не проходит из-за предупреждений WARNING - попробуйте исправить предупреждения в коде.  
Файл [suppressions.xml](/config/checkstyle/suppressions.xml) подавляет только WARNINGS для javaDoc.  
Если предупреждение убрать не получается - согласуйте увеличение значения maxWarnings в build.gradle.kts до 
необходимого вам числа.

**3. Полная проверка перед коммитом**
```bash
./gradlew check
```
