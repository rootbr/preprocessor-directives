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

Gradle:
* [Gradle](https://gradle.org/install/#manually)

```
java -version
gradle -version
```

Сборка

```
gradle build
```


## License

[MIT License](LICENSE)

