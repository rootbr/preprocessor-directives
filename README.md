# Preprocessor directives

## Описание
Консольная утилита, которая частично эмулирует действия препроцессора.

Параметры утилиты:
- путь к папке, в которой содержатся файлы с исходным кодом на языке C#
- путь к тестовому файлу, содержащему набор определённых символов (defined symbols)
- путь к папке, в которую необходимо поместить обработанные файлы

Алгоритм работы утилиты

Необходимо для каждого файла с исходным кодом обработать в соответствии с [директивами препроцессора](https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/preprocessor-directives). Поддержка всех директив не требуется. Достаточно поддержать следующие директивы:
- \#if
- \#else
- \#elif
- \#endif

Символы, которые определяются в самих файлах исходного кода при помощи #define, можно игнорировать.

Для директивы #if достаточно поддержать оператор !, остальные операторы поддерживать не нужно. Скобочные выражения поддерживать не нужно.

Например, если утилита получает на вход файл с кодом

```csharp
#if FEATURE_MANAGED_ETW

// OK if we get this far without an exception, then we can at least write out error messages.
// Set m_provider, which allows this.

m_provider = provider;

#endif

#if !ES_BUILD_STANDALONE

// API available on OS >= Win 8 and patched Win 7.
// Disable only for FrameworkEventSource to avoid recursion inside exception handling.

var osVer = Environment.OSVersion.Version.Major * 10 + Environment.OSVersion.Version.Minor;

if (this.Name != "System.Diagnostics.Eventing.FrameworkEventSource" || osVer >= 62)

#endif
```

И список символов вида

```
FEATURE_MANAGED_ETW=true
ES_BUILD_STANDALONE=true
```

То на выходе должен быть сформирован файл с кодом

```csharp
// OK if we get this far without an exception, then we can at least write out error messages.
// Set m_provider, which allows this.

m_provider = provider;
```

Для тестов можно использовать проект

https://github.com/dotnet/roslyn

Утилита должна работать как на этом проекте, так и на любом другом проекте C#

## Сборка

Для сборки проекта необходимы JDK11:
* [JDK11](https://jdk.java.net/java-se-ri/11)

Gradle версии > 5:
* [Gradle](https://gradle.org/install/#manually)

```
java -version
gradle -version
```

Сборка

```
gradle clean fatJar
```

Запуск

```
java -jar -Xss10m ./build/libs/preprocessor-directives-all-1.0.jar --path-to-source /home/aleksei/RiderProjects/source/roslyn  --path-to-output /home/aleksei/RiderProjects/results/roslyn --path-to-defined-symbols ./tests/defined-symbols.properties
```

PS:
1. defined symbols должно определять и такие варианты как: true=true или True=true, в соответствии с поребностями
2. исходными файлами c# считаем файлы с расширением "cs". Например, файлы vb не обрабатываем.
3. в формате директивы многостроковый комметарий удалится вместе со строкой директивы

## License

[MIT License](LICENSE)

